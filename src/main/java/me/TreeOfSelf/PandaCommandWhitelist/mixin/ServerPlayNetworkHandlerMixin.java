package me.TreeOfSelf.PandaCommandWhitelist.mixin;

import me.TreeOfSelf.PandaCommandWhitelist.CommandWhiteListConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {

	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
	private void onChatCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
		if (player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
			return;
		}
		if (!pcw$isCommandAllowed(packet.command())) {
			ci.cancel();
			player.sendSystemMessage(Component.literal(CommandWhiteListConfig.getBlockedMessage()));
		}
	}

	@Inject(
		method = "handleSignedChatCommand",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tryHandleChat(Ljava/lang/String;ZLjava/lang/Runnable;)V", shift = At.Shift.BEFORE),
		cancellable = true
	)
	private void onSignedChatCommand(ServerboundChatCommandSignedPacket packet, CallbackInfo ci) {
		if (player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ADMINS))) {
			return;
		}
		if (!pcw$isCommandAllowed(packet.command())) {
			ci.cancel();
			player.sendSystemMessage(Component.literal(CommandWhiteListConfig.getBlockedMessage()));
		}
	}

	@Unique
	private boolean pcw$isCommandAllowed(String fullCommand) {
		if (fullCommand == null || fullCommand.trim().isEmpty()) {
			return false;
		}

		String[] commandParts = fullCommand.split(" ");
		List<String> whitelistedCommands = CommandWhiteListConfig.getWhitelistedCommands();

		for (String whitelistedCommand : whitelistedCommands) {
			if (whitelistedCommand == null || whitelistedCommand.trim().isEmpty()) {
				continue;
			}

			String[] whitelistedParts = whitelistedCommand.split(" ");

			if (whitelistedParts.length > commandParts.length) continue;

			boolean match = true;
			for (int i = 0; i < whitelistedParts.length; i++) {
				if (whitelistedParts[i].equals("*")) continue;
				if (!whitelistedParts[i].equals(commandParts[i])) {
					match = false;
					break;
				}
			}

			if (match) {
				if (whitelistedParts.length == commandParts.length ||
						(whitelistedParts.length > 0 && whitelistedParts[whitelistedParts.length - 1].equals("*"))) {
					return true;
				}
			}
		}

		return false;
	}
}
