package com.agent.sbwnpcaddon.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "sbw_npc_addon");

    public static final RegistryObject<SoundEvent> RIFLE_SHOT_01 = registerSoundEvent("rifle_shot_01");
    public static final RegistryObject<SoundEvent> CANNON_FIRE_01 = registerSoundEvent("cannon_fire_01");
    public static final RegistryObject<SoundEvent> BULLET_FLYBY_01 = registerSoundEvent("bullet_flyby_01");
    public static final RegistryObject<SoundEvent> BULLET_IMPACT_01 = registerSoundEvent("bullet_impact_01");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("sbw_npc_addon", name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
