package net.smileycorp.dynaores.common;

import com.google.common.collect.Maps;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.RecipeBuilder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.smileycorp.dynaores.common.data.OreEntry;
import net.smileycorp.dynaores.common.data.OreHandler;

import java.util.Map;

public class GTCEuIntegration {

    private static final Map<String, Material> GTCEU_MATERIALS = Maps.newHashMap();
    private static boolean materialsFinalized = false;

    public static Map<String, Material> getGTCEuMaterials() {
        return GTCEU_MATERIALS;
    }

    public static void tryAddMaterial(ItemStack oreStack, String oreName) {
        if (!Loader.isModLoaded("gregtech")) return;
        String materialName = oreName.startsWith("ore") ? oreName.substring(3) : oreName;
        if (materialName.isEmpty()) return;
        String key = materialName.toLowerCase();
        if (GTCEU_MATERIALS.containsKey(key)) return;

        try {
            Material mat = GregTechAPI.materialManager.getMaterial(materialName);
            if (mat == null) mat = GregTechAPI.materialManager.getMaterial(key);
            if (mat != null) {
                GTCEU_MATERIALS.put(key, mat);
                DynaOresLogger.logInfo("Mapped GTCEu material " + materialName);
            }
        } catch (Exception ignored) {}
    }

    public static void finalizeMaterials() {
        if (!Loader.isModLoaded("gregtech") || materialsFinalized) return;
        materialsFinalized = true;
        try {
            for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
                String matName = material.toString();
                if (ConfigHandler.isBlacklisted(matName)) continue;
                String key = matName.toLowerCase();
                GTCEU_MATERIALS.putIfAbsent(key, material);
            }
            DynaOresLogger.logInfo("Finalized " + GTCEU_MATERIALS.size() + " GTCEu materials");
        } catch (Exception e) {
            DynaOresLogger.logError("Failed to finalize GTCEu materials", e);
        }
    }

    private static OreEntry findExistingEntry(String materialName) {
        OreEntry entry = OreHandler.INSTANCE.getEntry("ore" + materialName);
        if (entry != null) return entry;

        for (OreEntry e : OreHandler.INSTANCE.getOres()) {
            if (e.getName().equalsIgnoreCase(materialName)) return e;
        }
        return null;
    }

    public static void registerRecipes() {
        if (!Loader.isModLoaded("gregtech")) return;

        for (Map.Entry<String, Material> entry : GTCEU_MATERIALS.entrySet()) {
            Material material = entry.getValue();

            OreEntry oreEntry = findExistingEntry(material.toString());
            if (oreEntry == null) continue;

            ItemStack rawOreStack = new ItemStack(oreEntry.getItem());
            ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, material);
            if (crushedStack.isEmpty()) continue;

            OreProperty oreProp = material.getProperty(PropertyKey.ORE);
            int oreMultiplier = oreProp != null ? oreProp.getOreMultiplier() : 1;

            Material byproductMaterial = oreProp != null ? getFirstByproduct(oreProp) : null;
            ItemStack byproductStack = ItemStack.EMPTY;
            if (byproductMaterial != null) {
                byproductStack = OreDictUnifier.get(OrePrefix.gem, byproductMaterial);
                if (byproductStack.isEmpty()) {
                    byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
                }
            }

            try {
                RecipeBuilder<?> maceratorBuilder = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(rawOreStack)
                    .outputs(OreDictUnifier.get(OrePrefix.crushed, material, 2 * oreMultiplier))
                    .duration(200)
                    .EUt(12);
                if (!byproductStack.isEmpty()) {
                    maceratorBuilder.chancedOutput(byproductStack.copy(), 1400, 850);
                }
                maceratorBuilder.buildAndRegister();

                RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputs(rawOreStack)
                    .outputs(OreDictUnifier.get(OrePrefix.crushed, material, oreMultiplier))
                    .duration(10)
                    .EUt(16)
                    .buildAndRegister();

                DynaOresLogger.logInfo("Registered GT recipes for " + material);
            } catch (Exception e) {
                DynaOresLogger.logError("Failed to register GT recipe for " + material, e);
            }
        }
    }

    public static int getMaterialRGB(String name) {
        if (!Loader.isModLoaded("gregtech")) return -1;
        Material mat = GTCEU_MATERIALS.get(name.toLowerCase());
        if (mat == null) return -1;
        return 0xFF000000 | mat.getMaterialRGB();
    }

    public static String getIconSetName(String name) {
        if (!Loader.isModLoaded("gregtech")) return "dull";
        if (!materialsFinalized) finalizeMaterials();
        Material mat = GTCEU_MATERIALS.get(name.toLowerCase());
        if (mat == null) return "dull";
        String iconSet = mat.getMaterialIconSet().getName().toLowerCase();
        if (iconSet.equals("metallic") || iconSet.equals("shiny") || iconSet.equals("diamond")) return iconSet;
        return "dull";
    }

    public static boolean isGTCEuMaterial(String name) {
        if (!Loader.isModLoaded("gregtech")) return false;
        if (!materialsFinalized) finalizeMaterials();
        return GTCEU_MATERIALS.containsKey(name.toLowerCase());
    }

    private static Material getFirstByproduct(OreProperty oreProp) {
        if (oreProp.getOreByProducts().isEmpty()) return null;
        return oreProp.getOreByProducts().get(0);
    }

    public static ItemStack getMaterialStack(Material material) {
        if (material == null) return ItemStack.EMPTY;
        for (OrePrefix prefix : new OrePrefix[] {
                OrePrefix.crushed, OrePrefix.crushedPurified, OrePrefix.crushedCentrifuged,
                OrePrefix.dust, OrePrefix.dustSmall, OrePrefix.gem, OrePrefix.ingot }) {
            ItemStack stack = OreDictUnifier.get(prefix, material);
            if (!stack.isEmpty()) return stack;
        }
        ItemStack smelted = getSmeltResult(material);
        if (!smelted.isEmpty()) return smelted;
        return ItemStack.EMPTY;
    }

    public static ItemStack getSmeltResult(Material material) {
        OreProperty oreProp = material.getProperty(PropertyKey.ORE);
        Material directSmelt = oreProp != null ? oreProp.getDirectSmeltResult() : null;
        if (directSmelt == null) directSmelt = material;

        if (directSmelt.hasProperty(PropertyKey.INGOT)) {
            return OreDictUnifier.get(OrePrefix.ingot, directSmelt);
        }
        if (directSmelt.hasProperty(PropertyKey.GEM)) {
            return OreDictUnifier.get(OrePrefix.gem, directSmelt);
        }
        if (directSmelt.hasProperty(PropertyKey.DUST)) {
            return OreDictUnifier.get(OrePrefix.dust, directSmelt);
        }
        return ItemStack.EMPTY;
    }

}
