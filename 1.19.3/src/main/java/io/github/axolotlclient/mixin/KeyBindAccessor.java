package io.github.axolotlclient.mixin;

import java.util.Map;

import net.minecraft.client.option.KeyBind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBind.class)
public interface KeyBindAccessor {

	@Accessor("ORDER_BY_CATEGORIES")
	static Map<String, Integer> getOrderByCategories(){
		throw new UnsupportedOperationException();
	}
}
