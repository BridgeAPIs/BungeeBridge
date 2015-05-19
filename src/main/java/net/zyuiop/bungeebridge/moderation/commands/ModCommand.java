package net.zyuiop.bungeebridge.moderation.commands;

import net.zyuiop.bungeebridge.BungeeBridge;
import net.zyuiop.bungeebridge.commands.DefaultExecutor;

public abstract class ModCommand extends DefaultExecutor {

	protected final BungeeBridge bridge;

	public ModCommand(String name, BungeeBridge bridge) {
		super(name);
		this.bridge = bridge;
	}

	public ModCommand(String name, String permission, BungeeBridge bridge) {
		super(name, permission);
		this.bridge = bridge;
	}

	public ModCommand(String name, String permission, BungeeBridge bridge, String... aliases) {
		super(name, permission, aliases);
		this.bridge = bridge;
	}
}
