package me.TreeOfSelf.PandaCommandWhitelist;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaCommandWhitelist implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("panda-command-whitelist");

	@Override
	public void onInitialize() {
		CommandWhiteListConfig.init();
		registerCommands();
		LOGGER.info("PandaCommandWhitelist Started!");
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("pcw")
				.requires(source -> source.hasPermissionLevel(4))
				.then(CommandManager.literal("reload")
					.executes(context -> {
						CommandWhiteListConfig.reload();
						context.getSource().sendFeedback(() -> Text.of("§aPandaCommandWhitelist config reloaded!"), true);
						return 1;
					})
				)
			);
		});
	}
}