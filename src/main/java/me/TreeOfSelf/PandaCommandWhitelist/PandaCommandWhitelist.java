package me.TreeOfSelf.PandaCommandWhitelist;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaCommandWhitelist implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("panda-command-whitelist");

	@Override
	public void onInitialize() {
		CommandWhiteListConfig.init();
		LOGGER.info("PandaCommandWhitelist Started!");
	}
}