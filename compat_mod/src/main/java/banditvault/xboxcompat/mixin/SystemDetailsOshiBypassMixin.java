package banditvault.xboxcompat.mixin;

import banditvault.xboxcompat.XboxCompatLog;
import net.minecraft.class_6396;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import oshi.SystemInfo;

@Mixin(class_6396.class)
public abstract class SystemDetailsOshiBypassMixin {
    @Inject(method = "method_37128", at = @At("HEAD"), cancellable = true, require = 0)
    private void banditvault$skipLegacyOshiHardwareProbe(SystemInfo systemInfo, CallbackInfo ci) {
        XboxCompatLog.log("Skipping legacy OSHI hardware probe in Xbox sandbox");
        ci.cancel();
    }

    @Inject(method = "method_68673", at = @At("HEAD"), cancellable = true, require = 0)
    private void banditvault$skipModernOshiHardwareProbe(CallbackInfo ci) {
        XboxCompatLog.log("Skipping modern OSHI hardware probe in Xbox sandbox");
        ci.cancel();
    }
}
