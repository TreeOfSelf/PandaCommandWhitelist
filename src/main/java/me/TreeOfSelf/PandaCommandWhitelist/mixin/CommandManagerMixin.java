package me.TreeOfSelf.PandaCommandWhitelist.mixin;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.TreeOfSelf.PandaCommandWhitelist.CommandWhiteListConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(CommandManager.class)
public class CommandManagerMixin {

	@Redirect(method = "deepCopyNodes", at = @At(value="INVOKE", target="Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
	private static boolean canUseRedirection(CommandNode<ServerCommandSource> commandNode, Object objSource) {
		ServerCommandSource source = (ServerCommandSource) objSource;

		if(source.hasPermissionLevel(4)) return true;

		if (!(commandNode instanceof LiteralCommandNode<ServerCommandSource> node)) return commandNode.canUse(source);
		
		List<String> allowedCommands = CommandWhiteListConfig.getWhitelistedCommands()
			.stream()
			.map(cmd -> cmd.split(" ")[0])
			.collect(Collectors.toList());
		
		return allowedCommands.contains(node.getLiteral()) && commandNode.canUse(source);
	}
}