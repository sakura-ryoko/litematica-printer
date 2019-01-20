package fi.dy.masa.litematica.render;

import javax.annotation.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.render.schematic.RenderGlobalSchematic;
import fi.dy.masa.litematica.render.shader.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

public class LitematicaRenderer
{
    private static final LitematicaRenderer INSTANCE = new LitematicaRenderer();

    private static final ShaderProgram SHADER_ALPHA = new ShaderProgram("litematica", null, "shaders/alpha.frag");
    private Minecraft mc;
    private RenderGlobalSchematic worldRenderer;
    private int frameCount;
    private long finishTimeNano;

    private Entity entity;
    private ICamera camera;
    private boolean renderPiecewise;
    private boolean renderPiecewiseBlocks;
    private boolean renderPiecewisePrepared;
    private boolean translucentSchematic;

    public static LitematicaRenderer getInstance()
    {
        return INSTANCE;
    }

    public RenderGlobalSchematic getWorldRenderer()
    {
        if (this.worldRenderer == null)
        {
            this.mc = Minecraft.getMinecraft();
            this.worldRenderer = new RenderGlobalSchematic(this.mc);
        }

        return this.worldRenderer;
    }

    public void loadRenderers()
    {
        this.getWorldRenderer().loadRenderers();
    }

    public void onSchematicWorldChanged(@Nullable WorldClient worldClient)
    {
        this.getWorldRenderer().setWorldAndLoadRenderers(worldClient);
    }

    private void calculateFinishTime()
    {
        long fpsLimit = this.mc.gameSettings.limitFramerate;
        long fpsMin = Math.min(Minecraft.getDebugFPS(), fpsLimit);
        fpsMin = Math.max(fpsMin, 60L);

        if (Configs.Generic.RENDER_THREAD_NO_TIMEOUT.getBooleanValue())
        {
            this.finishTimeNano = Long.MAX_VALUE;
        }
        else
        {
            this.finishTimeNano = System.nanoTime() + Math.max(1000000000L / fpsMin / 2L, 0L);
        }
    }

    public void renderSchematicWorld(float partialTicks)
    {
        if (this.mc.skipRenderWorld == false)
        {
            this.mc.profiler.startSection("litematica_schematic_world_render");
            this.mc.profiler.startSection("litematica_level");

            if (this.mc.getRenderViewEntity() == null)
            {
                this.mc.setRenderViewEntity(this.mc.player);
            }

            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();

            this.calculateFinishTime();
            this.renderWorld(partialTicks, this.finishTimeNano);
            this.cleanup();

            GlStateManager.popMatrix();

            this.mc.profiler.endSection();
            this.mc.profiler.endSection();
        }
    }

    private void renderWorld(float partialTicks, long finishTimeNano)
    {
        this.mc.profiler.endStartSection("litematica_culling");
        Entity entity = this.mc.getRenderViewEntity();
        ICamera icamera = this.createCamera(entity, partialTicks);

        this.startShaderIfEnabled();

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        this.mc.profiler.endStartSection("litematica_prepare_terrain");
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        RenderGlobalSchematic renderGlobal = this.getWorldRenderer();

        this.mc.profiler.endStartSection("litematica_terrain_setup");
        renderGlobal.setupTerrain(entity, partialTicks, icamera, this.frameCount++, this.mc.player.isSpectator());

        this.mc.profiler.endStartSection("litematica_update_chunks");
        renderGlobal.updateChunks(finishTimeNano);

        this.mc.profiler.endStartSection("litematica_terrain");
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();

        if (Configs.Visuals.SCHEMATIC_BLOCKS_ENABLED.getBooleanValue())
        {
            GlStateManager.pushMatrix();

            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(-0.2f, -0.4f);
            }

            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            renderGlobal.renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, entity);

            renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, partialTicks, entity);

            this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            renderGlobal.renderBlockLayer(BlockRenderLayer.CUTOUT, partialTicks, entity);
            this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.doPolygonOffset(0f, 0f);
                GlStateManager.disablePolygonOffset();
            }

            GlStateManager.disableBlend();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01F);

            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();

            this.mc.profiler.endStartSection("litematica_entities");

            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            renderGlobal.renderEntities(entity, icamera, partialTicks);

            GlStateManager.disableBlend();
            RenderHelper.disableStandardItemLighting();

            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();

            GlStateManager.enableCull();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            this.mc.profiler.endStartSection("litematica_translucent");
            GlStateManager.depthMask(false);

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            renderGlobal.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, partialTicks, entity);

            GlStateManager.popMatrix();

            this.disableShader();
        }

        this.mc.profiler.endStartSection("litematica_overlay");
        this.renderSchematicOverlay();

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();
    }

    public void renderSchematicOverlay()
    {
        if (Configs.Visuals.SCHEMATIC_OVERLAY_ENABLED.getBooleanValue())
        {
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-0.4f, -0.8f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth((float) Configs.Visuals.SCHEMATIC_OVERLAY_OUTLINE_WIDTH.getDoubleValue());
            GlStateManager.color(1f, 1f, 1f, 1f);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            if (Configs.Visuals.SCHEMATIC_OVERLAY_RENDER_THROUGH.getBooleanValue() ||
                Hotkeys.RENDER_OVERLAY_THROUGH_BLOCKS.getKeybind().isKeybindHeld())
            {
                GlStateManager.disableDepth();
            }

            this.getWorldRenderer().renderBlockOverlays();

            GlStateManager.enableDepth();
            GlStateManager.doPolygonOffset(0f, 0f);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }

    public void startShaderIfEnabled()
    {
        this.translucentSchematic = Configs.Visuals.RENDER_BLOCKS_AS_TRANSLUCENT.getBooleanValue() && OpenGlHelper.shadersSupported;

        if (this.translucentSchematic)
        {
            float alpha = (float) Configs.Visuals.GHOST_BLOCK_ALPHA.getDoubleValue();
            GL20.glUseProgram(SHADER_ALPHA.getProgram());
            GL20.glUniform1f(GL20.glGetUniformLocation(SHADER_ALPHA.getProgram(), "alpha_multiplier"), alpha);
        }
    }

    public void disableShader()
    {
        if (this.translucentSchematic)
        {
            GL20.glUseProgram(0);
        }
    }

    public void piecewisePrepareAndUpdate(float partialTicks)
    {
        this.renderPiecewise = Configs.Generic.BETTER_RENDER_ORDER.getBooleanValue() && Configs.Visuals.ENABLE_RENDERING.getBooleanValue();

        if (this.renderPiecewise)
        {
            boolean invert = Hotkeys.INVERT_GHOST_BLOCK_RENDER_STATE.getKeybind().isKeybindHeld();
            this.renderPiecewiseBlocks = Configs.Visuals.ENABLE_SCHEMATIC_RENDERING.getBooleanValue() != invert && Configs.Generic.BETTER_RENDER_ORDER.getBooleanValue();

            this.mc.profiler.startSection("litematica_culling");

            if (this.mc.getRenderViewEntity() == null)
            {
                this.mc.setRenderViewEntity(this.mc.player);
            }

            Entity entity = this.mc.getRenderViewEntity();
            ICamera icamera = this.createCamera(entity, partialTicks);

            this.startShaderIfEnabled();

            this.calculateFinishTime();
            RenderGlobalSchematic renderGlobal = this.getWorldRenderer();

            this.mc.profiler.endStartSection("litematica_terrain_setup");
            renderGlobal.setupTerrain(entity, partialTicks, icamera, this.frameCount++, this.mc.player.isSpectator());

            this.mc.profiler.endStartSection("litematica_update_chunks");
            renderGlobal.updateChunks(this.finishTimeNano);

            this.mc.profiler.endSection();

            this.renderPiecewisePrepared = true;
        }
    }

    public void piecewiseRenderSolid(float partialTicks)
    {
        if (this.renderPiecewise && this.renderPiecewisePrepared && this.renderPiecewiseBlocks)
        {
            this.mc.profiler.endStartSection("litematica_blocks_solid");

            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(-0.3f, -0.6f);
            }

            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            this.startShaderIfEnabled();

            this.getWorldRenderer().renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, this.entity);

            this.disableShader();

            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.doPolygonOffset(0f, 0f);
                GlStateManager.disablePolygonOffset();
            }
        }
    }

    public void piecewiseRenderCutoutMipped(float partialTicks)
    {
        if (this.renderPiecewise && this.renderPiecewisePrepared && this.renderPiecewiseBlocks)
        {
            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(-0.3f, -0.6f);
            }

            this.mc.profiler.endStartSection("litematica_blocks_cutout_mipped");
            this.startShaderIfEnabled();

            this.getWorldRenderer().renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, partialTicks, this.entity);

            this.disableShader();

            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.doPolygonOffset(0f, 0f);
                GlStateManager.disablePolygonOffset();
            }
        }
    }

    public void piecewiseRenderCutout(float partialTicks)
    {
        if (this.renderPiecewise && this.renderPiecewisePrepared && this.renderPiecewiseBlocks)
        {
            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.enablePolygonOffset();
                GlStateManager.doPolygonOffset(-0.3f, -0.6f);
            }

            this.mc.profiler.endStartSection("litematica_blocks_cutout");
            this.startShaderIfEnabled();

            this.getWorldRenderer().renderBlockLayer(BlockRenderLayer.CUTOUT, partialTicks, this.entity);

            this.disableShader();

            if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
            {
                GlStateManager.doPolygonOffset(0f, 0f);
                GlStateManager.disablePolygonOffset();
            }

            //GlStateManager.disableBlend();
        }
    }

    public void piecewiseRenderTranslucent(float partialTicks)
    {
        if (this.renderPiecewise && this.renderPiecewisePrepared)
        {
            if (this.renderPiecewiseBlocks)
            {
                if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
                {
                    GlStateManager.enablePolygonOffset();
                    GlStateManager.doPolygonOffset(-0.3f, -0.6f);
                }

                this.mc.profiler.endStartSection("litematica_translucent");
                this.startShaderIfEnabled();

                this.getWorldRenderer().renderBlockLayer(BlockRenderLayer.TRANSLUCENT, partialTicks, this.entity);

                this.disableShader();

                if (Configs.Visuals.RENDER_COLLIDING_SCHEMATIC_BLOCKS.getBooleanValue())
                {
                    GlStateManager.doPolygonOffset(0f, 0f);
                    GlStateManager.disablePolygonOffset();
                }

                this.mc.profiler.endStartSection("litematica_overlay");

                this.renderSchematicOverlay();
            }

            this.cleanup();
        }
    }

    public void piecewiseRenderEntities(float partialTicks)
    {
        if (this.renderPiecewise && this.renderPiecewisePrepared && this.renderPiecewiseBlocks)
        {
            this.mc.profiler.endStartSection("litematica_entities");
            this.startShaderIfEnabled();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            this.getWorldRenderer().renderEntities(this.entity, this.camera, partialTicks);

            GlStateManager.disableBlend();

            this.disableShader();
        }
    }

    private ICamera createCamera(Entity entity, float partialTicks)
    {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

        this.entity = entity;
        this.camera = new Frustum();
        this.camera.setPosition(x, y, z);

        return this.camera;
    }

    private void cleanup()
    {
        this.entity = null;
        this.camera = null;
        this.renderPiecewisePrepared = false;
    }
}
