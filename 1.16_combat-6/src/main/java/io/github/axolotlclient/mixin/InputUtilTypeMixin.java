package io.github.axolotlclient.mixin;

import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputUtil.Type.class)
public class InputUtilTypeMixin {

	@Inject(method = "method_27450", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetKeyName(II)Ljava/lang/String;"), cancellable = true)
	private static void fixScancodeError(Integer i, String string, CallbackInfoReturnable<Text> cir) {
		if (i == -1) {
			cir.setReturnValue(new TranslatableText(string));
		}
	}
}
