package banditvault.forgecontroller.mixin;

import banditvault.forgecontroller.ForgeControllerCompat;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = AbstractContainerScreen.class, remap = false)
public abstract class ForgeControllerHandledScreenMixin {
    @ModifyVariable(method = "m_88315_", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private int banditvault$useControllerMouseX(int mouseX) {
        return ForgeControllerCompat.screenMouseX(mouseX);
    }

    @ModifyVariable(method = "m_88315_", at = @At("HEAD"), ordinal = 1, argsOnly = true, remap = false)
    private int banditvault$useControllerMouseY(int mouseY) {
        return ForgeControllerCompat.screenMouseY(mouseY);
    }
}
