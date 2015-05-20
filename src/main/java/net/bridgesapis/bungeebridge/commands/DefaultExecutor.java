package net.bridgesapis.bungeebridge.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.bridgesapis.bungeebridge.BungeeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DefaultExecutor extends Command implements TabExecutor {
	public DefaultExecutor(String name) {
		super(name);
	}

	public DefaultExecutor(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}

	@Override
	public java.lang.Iterable<java.lang.String> onTabComplete(CommandSender sender, String[] args) {
		List<String> result = new ArrayList<>();

		if(args.length < 1) {
			return result;
		}

		result.addAll(BungeeBridge.getInstance().getNetworkBridge().getOnlinePlayersNames().stream().filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList()));

		return result;
	}
}
