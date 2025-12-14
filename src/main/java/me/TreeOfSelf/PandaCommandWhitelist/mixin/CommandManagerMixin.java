package me.TreeOfSelf.PandaCommandWhitelist.mixin;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.TreeOfSelf.PandaCommandWhitelist.CommandWhiteListConfig;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(CommandManager.class)
public class CommandManagerMixin {

	@Unique
	private static final ThreadLocal<Deque<String>> COMMAND_PATH = ThreadLocal.withInitial(ArrayDeque::new);

	@Inject(method = "deepCopyNodes", at = @At("HEAD"))
	private static <S> void beforeDeepCopy(CommandNode<S> root, CommandNode<S> newRoot,
										   S source, Map<CommandNode<S>, CommandNode<S>> nodes,
										   CallbackInfo ci) {

		if (newRoot instanceof LiteralCommandNode) {
			COMMAND_PATH.get().addLast(((LiteralCommandNode<?>) newRoot).getLiteral());
		}
	}

	@Inject(method = "deepCopyNodes", at = @At("RETURN"))
	private static <S> void afterDeepCopy(CommandNode<S> root, CommandNode<S> newRoot,
										  S source, Map<CommandNode<S>, CommandNode<S>> nodes,
										  CallbackInfo ci) {

		if (newRoot instanceof LiteralCommandNode && !COMMAND_PATH.get().isEmpty()) {
			COMMAND_PATH.get().removeLast();
		}
	}

	@Redirect(method = "deepCopyNodes", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/tree/CommandNode;canUse(Ljava/lang/Object;)Z"))
	private static <S> boolean checkCanUse(CommandNode<S> commandNode, Object source) {
		if (!commandNode.canUse((S) source)) {
			return false;
		}

		if (!(source instanceof ServerCommandSource)) {
			return true;
		}

		ServerCommandSource serverSource = (ServerCommandSource) source;

		if (serverSource.getPermissions().hasPermission(new Permission.Level(PermissionLevel.ADMINS))) {
			return true;
		}

		if (!(commandNode instanceof LiteralCommandNode)) {
			return true;
		}

		List<String> pathParts = new ArrayList<>(COMMAND_PATH.get());
		pathParts.add(((LiteralCommandNode<?>) commandNode).getLiteral());

		if (pathParts.isEmpty()) {
			return true;
		}

		String fullPath = String.join(" ", pathParts);
		List<String> allowedCommands = CommandWhiteListConfig.getWhitelistedCommands();

		for (String whitelisted : allowedCommands) {
			if (whitelisted.equals(fullPath)) {
				return true;
			}

			if (whitelisted.endsWith(" *")) {
				String baseCommand = whitelisted.substring(0, whitelisted.length() - 2);
				if (fullPath.equals(baseCommand) || fullPath.startsWith(baseCommand + " ")) {
					return true;
				}
			}
		}

		return false;
	}
}