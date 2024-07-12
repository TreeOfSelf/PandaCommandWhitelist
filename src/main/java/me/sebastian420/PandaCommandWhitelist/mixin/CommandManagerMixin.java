package me.sebastian420.PandaCommandWhitelist.mixin;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import me.sebastian420.PandaCommandWhitelist.CommandWhiteListConfig;

import java.util.List;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
	@Unique
	private final List<String> allowedCommands = CommandWhiteListConfig.getWhitelistedCommands();

	@Redirect(method = "makeTreeForSource", at = @At(value="INVOKE", remap = false, target="Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
	private boolean canUseRedirection(CommandNode<ServerCommandSource> commandNode, Object objSource) {
		ServerCommandSource source = (ServerCommandSource) objSource;

		if(source.hasPermissionLevel(4)){
			return true;
		}

		if (!(commandNode instanceof LiteralCommandNode<ServerCommandSource> node)) return commandNode.canUse(source);
		return allowedCommands.contains(node.getLiteral().split(" ")[0]) && commandNode.canUse(source);
	}
}