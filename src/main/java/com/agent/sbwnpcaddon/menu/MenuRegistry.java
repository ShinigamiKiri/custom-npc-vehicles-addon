package com.agent.sbwnpcaddon.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "sbw_npc_addon");

    public static final RegistryObject<MenuType<NpcTradingSetupMenu>> NPC_TRADING_SETUP_MENU = MENUS.register("npc_trading_setup_menu",
            () -> IForgeMenuType.create(NpcTradingSetupMenu::new));
            
    public static final RegistryObject<MenuType<NpcTradingMenu>> NPC_TRADING_MENU = MENUS.register("npc_trading_menu",
            () -> IForgeMenuType.create(NpcTradingMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
