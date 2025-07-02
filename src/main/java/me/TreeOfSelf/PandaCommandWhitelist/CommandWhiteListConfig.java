package me.TreeOfSelf.PandaCommandWhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CommandWhiteListConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("panda-command-whitelist");
    private static final String CONFIG_FILE = "PandaCommandWhitelist.json";
    private static final List<String> DEFAULT_COMMANDS = Arrays.asList("tell *", "me *", "msg *", "w *");
    private static final String DEFAULT_BLOCKED_MESSAGE = "That command is blocked or doesn't exist.";
    
    private static List<String> whitelistedCommands;
    private static String blockedMessage;

    public static class Config {
        public List<String> commands;
        public String blockedMessage;
        
        public Config() {
            this.commands = new ArrayList<>(DEFAULT_COMMANDS);
            this.blockedMessage = DEFAULT_BLOCKED_MESSAGE;
        }
    }

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
            Config defaultConfig = new Config();
            new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(File configFile) {
        try {
            FileReader reader = new FileReader(configFile);
            Gson gson = new Gson();
            
            Object rawJson = gson.fromJson(reader, Object.class);
            reader.close();
            
            Config config;
            boolean needsConversion = false;
            
            if (rawJson instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> oldCommands = (List<String>) rawJson;
                config = new Config();
                config.commands = new ArrayList<>(oldCommands);
                config.blockedMessage = DEFAULT_BLOCKED_MESSAGE;
                needsConversion = true;
            } else {
                reader = new FileReader(configFile);
                config = gson.fromJson(reader, Config.class);
                reader.close();
                
                if (config == null) {
                    config = new Config();
                }
            }

            whitelistedCommands = new ArrayList<>();
            List<String> loadedCommands = config.commands != null ? config.commands : DEFAULT_COMMANDS;

            for (String cmd : loadedCommands) {
                if (cmd != null && !cmd.trim().isEmpty()) {
                    whitelistedCommands.add(cmd);
                    if (cmd.endsWith(" *")) {
                        String commandWithoutWildcard = cmd.substring(0, cmd.length() - 2).trim();
                        whitelistedCommands.add(commandWithoutWildcard);
                    }
                }
            }

            blockedMessage = config.blockedMessage != null ? config.blockedMessage : DEFAULT_BLOCKED_MESSAGE;

            if (needsConversion) {
                saveConfig(configFile, config);
            }

        } catch (IOException e) {
            e.printStackTrace();
            whitelistedCommands = new ArrayList<>(DEFAULT_COMMANDS);
            blockedMessage = DEFAULT_BLOCKED_MESSAGE;
        }
    }

    private static void saveConfig(File configFile, Config config) {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getWhitelistedCommands() {
        return whitelistedCommands != null ? whitelistedCommands : new ArrayList<>(DEFAULT_COMMANDS);
    }

    public static String getBlockedMessage() {
        return blockedMessage != null ? blockedMessage : DEFAULT_BLOCKED_MESSAGE;
    }

    public static void reload() {
        LOGGER.info("Reloading PandaCommandWhitelist configuration...");
        init();
        LOGGER.info("Configuration reloaded successfully. {} commands whitelisted.", whitelistedCommands.size());
    }
}