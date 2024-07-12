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
        String command = packet.command().split(" ")[0]; // Get the first word of the command
        ServerPlayerEntity player = ((ServerPlayNetworkHandler) (Object) this).player;
        if (player.hasPermissionLevel(4)) {
            return;
        }

        if (whitelistedCommands.contains(command)) {
            // Command is whitelisted, allow it to proceed
        } else {
            ci.cancel();
            player.sendMessage(Text.of("That command is blocked or doesn't exist."));
        }
    }
}