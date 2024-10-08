package me.TreeOfSelf.PandaCommandWhitelist;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class CommandWhiteListConfig {
    private static final String CONFIG_FILE = "PandaCommandWhitelist.json";
    private static final List<String> DEFAULT_COMMANDS = Arrays.asList("tell *", "me *", "msg *", "w *");
    private static List<String> whitelistedCommands;

    public static void init() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), CONFIG_FILE);
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }
        loadConfig(configFile);
    }

    private static void createDefaultConfig(File configFile) {
        try {
            configFile.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(DEFAULT_COMMANDS, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(File configFile) {
        try {
            FileReader reader = new FileReader(configFile);
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            List<String> loadedCommands = new Gson().fromJson(reader, listType);
            reader.close();

            whitelistedCommands = new ArrayList<>();

            for (String cmd : loadedCommands) {
                if (cmd != null && !cmd.trim().isEmpty()) {
                    whitelistedCommands.add(cmd);
                    if (cmd.endsWith(" *")) {
                        String commandWithoutWildcard = cmd.substring(0, cmd.length() - 2).trim();
                        whitelistedCommands.add(commandWithoutWildcard);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            whitelistedCommands = new ArrayList<>(DEFAULT_COMMANDS);
        }
    }

    public static List<String> getWhitelistedCommands() {
        return whitelistedCommands != null ? whitelistedCommands : new ArrayList<>(DEFAULT_COMMANDS);
    }
}