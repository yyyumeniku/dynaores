package net.smileycorp.dynaores.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.smileycorp.dynaores.common.Constants;
import net.smileycorp.dynaores.common.DynaOres;
import net.smileycorp.dynaores.common.data.OreEntry;

public class ItemRawOre extends Item implements IOreItem {
    
    private final OreEntry entry;
    
    public ItemRawOre(OreEntry entry) {
        this.entry = entry;
        String name = "Raw" + entry.getName();
        setRegistryName(Constants.loc(name));
        setUnlocalizedName(Constants.name(name));
        setCreativeTab(DynaOres.CREATIVE_TAB);
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = I18n.translateToLocal(entry.getLocalizedName())
                .replace("Ingot", "")
                .replace("Dust", "")
                .replace("Gem", "")
                .replace("Crushed", "")
                .replace("Purified", "")
                .replace("Centrifuged", "")
                .replace("Ore", "")
                .trim();
        return I18n.translateToLocalFormatted("items.dynaores.RawOre.name", name).trim();
    }
    
    @Override
    public OreEntry getEntry() {
        return entry;
    }
    
}
