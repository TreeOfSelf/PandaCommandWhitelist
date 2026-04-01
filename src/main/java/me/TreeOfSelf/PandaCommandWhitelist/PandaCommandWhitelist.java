package me.TreeOfSelf.PandaCommandWhitelist;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

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
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			dispatcher.register(Commands.literal("pcw")
				.requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS)))
				.then(Commands.literal("reload")
					.executes(context -> {
						CommandWhiteListConfig.reload();
						context.getSource().sendSuccess(() -> Component.literal("PandaCommandWhitelist config reloaded!").withStyle(ChatFormatting.GREEN), true);
						return 1;
					})
				)
			);
		});
	}
}
