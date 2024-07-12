package me.sebastian420.PandaCommandWhitelist.mixin;

import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.sebastian420.PandaCommandWhitelist.CommandWhiteListConfig;

import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private final List<String> whitelistedCommands = CommandWhiteListConfig.getWhitelistedCommands();

    @Inject(method = "onCommandExecution", at = @At(value = "HEAD"), cancellable = true)
    private void onCommandExecution(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        String fullCommand = packet.command();
        ServerPlayerEntity player = ((ServerPlayNetworkHandler) (Object) this).player;

        if (player.hasPermissionLevel(4)) return;

        if (!isCommandAllowed(fullCommand)) {
            ci.cancel();
            player.sendMessage(Text.of("That command is blocked or doesn't exist."));
        }
    }

    @Unique
    private boolean isCommandAllowed(String fullCommand) {
        String[] commandParts = fullCommand.split(" ");

        for (String whitelistedCommand : whitelistedCommands) {
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