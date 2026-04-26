package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.content.items.WrenchItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class CFItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CFMain.MODID);

    public static final DeferredItem<FacadeItem> FACADE = ITEMS.registerItem("facade", FacadeItem::new, () -> new Item.Properties()
            .component(CFDataComponents.FACADE_BLOCK, Optional.empty())
            .component(CFDataComponents.HAS_FACADE_REMAINDER, false));

    public static final DeferredItem<DirectionalFacadeItem> DIRECTIONAL_FACADE = ITEMS.registerItem("directional_facade", DirectionalFacadeItem::new, () -> new Item.Properties()
            .component(CFDataComponents.FACADE_BLOCK, Optional.empty())
            .component(CFDataComponents.HAS_FACADE_REMAINDER, false));

    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.registerItem("facade_wrench", WrenchItem::new, () -> new Item.Properties().stacksTo(1));
}
