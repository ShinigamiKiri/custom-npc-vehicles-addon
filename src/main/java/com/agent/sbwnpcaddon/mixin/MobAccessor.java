package com.agent.sbwnpcaddon.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor("moveControl")
    void setMoveControl(MoveControl moveControl);

    @Accessor("lookControl")
    void setLookControl(LookControl lookControl);
}
