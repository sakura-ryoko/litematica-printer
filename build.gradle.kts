plugins {
    id("fabric-loom").version("1.14-SNAPSHOT")
    id("maven-publish")
}

val minecraft_version: String by project
val mappings_version: String by project
val fabric_loader_version: String by project
val fabric_api_version: String by project
val minecraft_version_out: String by project
val malilib_version: String by project
//val litematica_projectid: String by project
//val litematica_fileid: String by project
val litematica_version: String by project
val mod_menu_version: String by project

val archives_base_name: String by project
val mod_version: String by project

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://masa.dy.fi/maven/sakura-ryoko")
    maven("https://jitpack.io")
    //maven("https://www.cursemaven.com")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.fallenbreath.me/releases")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${mappings_version}:v2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_api_version}")
    //modImplementation("curse.maven:litematica-${litematica_projectid}:${litematica_fileid}")

    // Masa's Maven
    modImplementation("fi.dy.masa.malilib:malilib-fabric-${minecraft_version_out}:${malilib_version}")
    modImplementation("fi.dy.masa.litematica:litematica-fabric-${minecraft_version_out}:${litematica_version}")

    // Sakura's Jitpack
//    modImplementation("com.github.sakura-ryoko:malilib:${minecraft_version_out}-${malilib_version}")
//    modImplementation("com.github.sakura-ryoko:litematica:${minecraft_version_out}-${litematica_version}")

    // For Mod Menu display
    modCompileOnly("com.terraformersmc:modmenu:${mod_menu_version}")
//    modRuntimeOnly("me.fallenbreath:mixin-auditor:0.1.0")
}

loom {
    // HOW TO DO THIS WITH KTS ?
//    val commonVmArgs = listOf("-Dmixin.debug.export=true", "-Dmixin.debug.countInjections=true")
//    // [FEATURE] MIXIN_AUDITOR
//    val auditVmArgs = commonVmArgs + "-DmixinAuditor.audit=true"
//
//    fun clientMixinAudit() {
//        client()
//        vmArgs(auditVmArgs)
//        ideConfigGenerated = false
//    }
//
//    runs {
//        clientMixinAudit()
//    }
}

tasks.withType<ProcessResources> {
    inputs.property("version", mod_version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to mod_version))
    }
}

tasks.register("copyJar") {
    // Specify that this task runs after the 'build' task
    dependsOn("build")

    // Specify the task's action
    doLast {
        val destination = file("build/${archives_base_name}-${minecraft_version}-${mod_version}.jar")
        file("build/libs/litematica-printer.jar").copyTo(destination, true)
        println("Copied output to ${destination.absolutePath}")
    }
}

tasks.build {
    finalizedBy("copyJar")
}
