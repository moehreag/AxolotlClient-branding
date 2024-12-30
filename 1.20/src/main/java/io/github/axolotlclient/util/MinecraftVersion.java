package io.github.axolotlclient.util;

import net.minecraft.util.Identifier;

public class MinecraftVersion {
	public static Identifier id(String namespace, String path) {
		return new Identifier(namespace, path);
	}

	public static Identifier id(String path) {
		return id("axolotlclient", path);
	}
}
