package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.content.items.WrenchItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CFItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CFMain.MODID);

    public static final RegistryObject<FacadeItem> FACADE = ITEMS.register("facade", () -> new FacadeItem(new Item.Properties()));

    public static final RegistryObject<WrenchItem> WRENCH = ITEMS.register("facade_wrench", () -> new WrenchItem(new Item.Properties()));
}
