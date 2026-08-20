package com.agent.sbwnpcaddon.mixin;

import com.atsuishio.superbwarfare.entity.goal.GunShootGoal;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GunShootGoal.class)
public class MixinGunShootGoal {
    @Shadow(remap = false)
    private Mob mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void sbw_onCanUse(CallbackInfoReturnable<Boolean> cir) {
        if (mob != null && mob.getPersistentData().getBoolean("SbwPhysicsEnabled")) {
            int type = mob.getPersistentData().getInt("SbwVehicleType");
            if (type == 0) { // Ground vehicle
                double speedSq = mob.getDeltaMovement().horizontalDistanceSqr();
                if (speedSq > 0.001) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void sbw_onCanContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (mob != null && mob.getPersistentData().getBoolean("SbwPhysicsEnabled")) {
            int type = mob.getPersistentData().getInt("SbwVehicleType");
            if (type == 0) {
                double speedSq = mob.getDeltaMovement().horizontalDistanceSqr();
                if (speedSq > 0.001) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
