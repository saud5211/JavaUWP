package banditvault.xboxcompat.mixin;

import banditvault.xboxcompat.BanditControllerCompat;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_437.class)
public abstract class BanditControllerScreenMixin {
    @Inject(method = "method_25394", at = @At("TAIL"))
    private void banditvault$renderControllerCursor(class_4587 matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BanditControllerCompat.renderCursor((class_437)(Object)this, matrices);
    }
}
