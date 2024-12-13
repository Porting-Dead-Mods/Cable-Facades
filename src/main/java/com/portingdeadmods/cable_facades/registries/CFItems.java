package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class CFItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CFMain.MODID);

    public static final DeferredItem<FacadeItem> FACADE = ITEMS.register("facade", () -> new FacadeItem(new Item.Properties()
            .component(CFDataComponents.FACADE_BLOCK, Optional.empty())
            .component(CFDataComponents.HAS_FACADE_REMAINDER, false)));

    public static final DeferredItem<Item> WRENCH = ITEMS.register("facade_wrench", () -> new Item(new Item.Properties().stacksTo(1)));
}
