package com.agent.sbwnpcaddon.mixin;

import net.minecraft.world.phys.Vec3;
import noppes.npcs.ai.EntityAIRangedAttack;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityAIRangedAttack.class, remap = false)
public abstract class MixinEntityAIRangedAttack {
    
    @Shadow
    private EntityNPCInterface npc;
    
    @Shadow
    private int rangedAttackTime;

    @Inject(method = {"tick()V", "m_8037_()V"}, at = @At("HEAD"), remap = false)
    private void sbw_onTick(CallbackInfo ci) {
        if (this.npc != null && this.npc.getPersistentData().getBoolean("SbwPhysicsEnabled")) {
            int type = this.npc.getPersistentData().getInt("SbwVehicleType");
            if (type == 0) { // Ground vehicle
                Vec3 vel = this.npc.getDeltaMovement();
                double horizSpeedSqr = vel.x * vel.x + vel.z * vel.z;
                // If moving faster than a tiny threshold, prevent the weapon from firing
                // by incrementing the timer to cancel out the -- that happens later in the tick.
                if (horizSpeedSqr > 0.0001) {
                    this.rangedAttackTime++;
                }
            }
        }
    }
}
