package net.bridgesapis.bungeebridge.moderation.commands;

import net.bridgesapis.bungeebridge.commands.DefaultExecutor;
import net.bridgesapis.bungeebridge.BungeeBridge;

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
