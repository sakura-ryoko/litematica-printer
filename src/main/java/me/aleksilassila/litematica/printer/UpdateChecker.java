package me.aleksilassila.litematica.printer;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

public class UpdateChecker {
    public static final String version = FabricLoader.getInstance().getModContainer("litematica-printer").get()
            .getMetadata().getVersion()
            .getFriendlyString();


    @SuppressWarnings("deprecation")
    public static String getPrinterVersion() {
        try (InputStream inputStream = new URL("https://api.github.com/repos/aleksilassila/litematica-printer/tags")
                .openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                JsonArray tags = new JsonParser().parse(scanner.next()).getAsJsonArray();
                return ((JsonObject) tags.get(0)).get("name").getAsString();
            }
        } catch (Exception exception) {
            System.out.println("Cannot look for updates: " + exception.getMessage());
        }

        return "";
    }
}
