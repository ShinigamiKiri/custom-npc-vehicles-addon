package com.agent.sbwnpcaddon.mixin;

import noppes.npcs.ai.EntityAIFollow;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityAIFollow.class, remap = false)
public abstract class MixinEntityAIFollow {

    @Shadow
    private EntityNPCInterface npc;

    @Inject(method = {"m_8036_()Z", "m_8036_", "canUse", "canUse()Z"}, at = @At("HEAD"), remap = false, cancellable = true)
    private void sbw_canUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.npc != null && this.npc.getPersistentData().getBoolean("SbwCommandActive")) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = {"m_8045_()Z", "m_8045_", "canContinueToUse", "canContinueToUse()Z"}, at = @At("HEAD"), remap = false, cancellable = true)
    private void sbw_canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.npc != null && this.npc.getPersistentData().getBoolean("SbwCommandActive")) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = {"m_8037_()V", "m_8037_", "tick", "tick()V"}, at = @At("HEAD"), remap = false, cancellable = true)
    private void sbw_tick(CallbackInfo ci) {
        if (this.npc != null && this.npc.getPersistentData().getBoolean("SbwCommandActive")) {
            ci.cancel();
        }
    }

    @Inject(method = {"m_8056_()V", "m_8056_", "start", "start()V"}, at = @At("HEAD"), remap = false, cancellable = true)
    private void sbw_start(CallbackInfo ci) {
        if (this.npc != null && this.npc.getPersistentData().getBoolean("SbwCommandActive")) {
            ci.cancel();
        }
    }
}
