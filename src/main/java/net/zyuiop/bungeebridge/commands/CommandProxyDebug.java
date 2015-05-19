package net.zyuiop.bungeebridge.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.zyuiop.bungeebridge.BungeeBridge;


/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CommandProxyDebug extends DefaultExecutor {

	private final BungeeBridge plugin;

	public CommandProxyDebug(BungeeBridge plugin) {
		super("proxydebug", "proxies.debug", "pdebug");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		plugin.getConnector().getCommandsSubscriber().toggle(commandSender);
		commandSender.sendMessage(new ComponentBuilder("Debug modifié.").color(ChatColor.GREEN).create());
	}


}
