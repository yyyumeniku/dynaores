package net.smileycorp.dynaores.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.fml.common.Loader;
import net.smileycorp.dynaores.common.Constants;
import net.smileycorp.dynaores.common.DynaOresLogger;
import net.smileycorp.dynaores.common.GTCEuIntegration;
import net.smileycorp.dynaores.common.block.BlockRawOre;
import net.smileycorp.dynaores.common.data.OreEntry;
import net.smileycorp.dynaores.common.data.OreHandler;
import net.smileycorp.dynaores.common.item.IOreItem;
import net.smileycorp.dynaores.common.item.ItemBlockRawOre;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class OreModelLoader implements ICustomModelLoader, ISelectiveResourceReloadListener {
    
    public static final OreModelLoader INSTANCE = new OreModelLoader();
    
    protected final List<String> itemTextures = Lists.newArrayList();
    protected final List<String> blockTextures = Lists.newArrayList();
    private final java.util.Set<String> noItemTexture = com.google.common.collect.Sets.newHashSet();
    private final java.util.Set<String> noBlockTexture = com.google.common.collect.Sets.newHashSet();
    private final OreModelOverrides overrides = new OreModelOverrides();

    private static final List<String> SUPPORTED_ICON_SETS = Lists.newArrayList("metallic", "shiny", "diamond");
    private static final java.util.Map<String, Integer> KNOWN_MATERIAL_COLORS = new java.util.HashMap<>();
    static {
        KNOWN_MATERIAL_COLORS.put("iron", 0xFFB2B3B7);
        KNOWN_MATERIAL_COLORS.put("copper", 0xFFFFA040);
        KNOWN_MATERIAL_COLORS.put("tin", 0xFFD3D3D3);
        KNOWN_MATERIAL_COLORS.put("silver", 0xFFD5D5D5);
        KNOWN_MATERIAL_COLORS.put("gold", 0xFFFFCC33);
        KNOWN_MATERIAL_COLORS.put("lead", 0xFF7F7FAA);
        KNOWN_MATERIAL_COLORS.put("nickel", 0xFFD8D8B9);
        KNOWN_MATERIAL_COLORS.put("platinum", 0xFFE5E4E2);
        KNOWN_MATERIAL_COLORS.put("aluminum", 0xFFD8D8D8);
        KNOWN_MATERIAL_COLORS.put("zinc", 0xFFA5A5A5);
        KNOWN_MATERIAL_COLORS.put("tungsten", 0xFF555555);
        KNOWN_MATERIAL_COLORS.put("titanium", 0xFFB5B5B5);
        KNOWN_MATERIAL_COLORS.put("chrome", 0xFFD0D0DA);
        KNOWN_MATERIAL_COLORS.put("cobalt", 0xFF6060C0);
        KNOWN_MATERIAL_COLORS.put("manganese", 0xFFDADADA);
        KNOWN_MATERIAL_COLORS.put("antimony", 0xFFC8C8D0);
        KNOWN_MATERIAL_COLORS.put("magnesium", 0xFFCFCFCF);
        KNOWN_MATERIAL_COLORS.put("beryllium", 0xFFCDCDCD);
        KNOWN_MATERIAL_COLORS.put("lithium", 0xFFBFBFBF);
        KNOWN_MATERIAL_COLORS.put("uranium", 0xFFCDD085);
        KNOWN_MATERIAL_COLORS.put("thorium", 0xFF323232);
        KNOWN_MATERIAL_COLORS.put("osmium", 0xFFA0B0C8);
        KNOWN_MATERIAL_COLORS.put("iridium", 0xFFD2D2D8);
        KNOWN_MATERIAL_COLORS.put("steel", 0xFF8C8C8C);
        KNOWN_MATERIAL_COLORS.put("brass", 0xFFFFB540);
        KNOWN_MATERIAL_COLORS.put("bronze", 0xFFCD7F32);
        KNOWN_MATERIAL_COLORS.put("electrum", 0xFFD7CD70);
        KNOWN_MATERIAL_COLORS.put("invar", 0xFFB0B0B0);
        KNOWN_MATERIAL_COLORS.put("constantan", 0xFFB17F4F);
        KNOWN_MATERIAL_COLORS.put("signalum", 0xFFFFAA40);
        KNOWN_MATERIAL_COLORS.put("lumium", 0xFFFFF0A8);
        KNOWN_MATERIAL_COLORS.put("enderium", 0xFF4694B0);
        KNOWN_MATERIAL_COLORS.put("kanthal", 0xFFC8C896);
        KNOWN_MATERIAL_COLORS.put("mythril", 0xFF90A0C8);
        KNOWN_MATERIAL_COLORS.put("adamantium", 0xFFE0C0E0);
        KNOWN_MATERIAL_COLORS.put("ruby", 0xFFD71940);
        KNOWN_MATERIAL_COLORS.put("sapphire", 0xFF3072C4);
        KNOWN_MATERIAL_COLORS.put("emerald", 0xFF3FB74F);
        KNOWN_MATERIAL_COLORS.put("amethyst", 0xFF9966CC);
        KNOWN_MATERIAL_COLORS.put("diamond", 0xFF4FC3F7);
        KNOWN_MATERIAL_COLORS.put("peridot", 0xFF6FAB47);
        KNOWN_MATERIAL_COLORS.put("malachite", 0xFF4EAA70);
        KNOWN_MATERIAL_COLORS.put("tanzanite", 0xFF6B4F90);
        KNOWN_MATERIAL_COLORS.put("opal", 0xFFE0E0F0);
        KNOWN_MATERIAL_COLORS.put("jade", 0xFF37B574);
        KNOWN_MATERIAL_COLORS.put("lazuli", 0xFF3050C8);
        KNOWN_MATERIAL_COLORS.put("lapis", 0xFF2750B0);
        KNOWN_MATERIAL_COLORS.put("amber", 0xFFE0A040);
        KNOWN_MATERIAL_COLORS.put("coal", 0xFF222222);
        KNOWN_MATERIAL_COLORS.put("charcoal", 0xFF2A1F1A);
        KNOWN_MATERIAL_COLORS.put("sulfur", 0xFFDAD63A);
        KNOWN_MATERIAL_COLORS.put("saltpeter", 0xFFEEEEEE);
        KNOWN_MATERIAL_COLORS.put("redstone", 0xFFE82A2A);
        KNOWN_MATERIAL_COLORS.put("glowstone", 0xFFFBE89E);
        KNOWN_MATERIAL_COLORS.put("quartz", 0xFFE8E5DC);
        KNOWN_MATERIAL_COLORS.put("obsidian", 0xFF1B1729);
        KNOWN_MATERIAL_COLORS.put("fluorite", 0xFFA0B0C0);
        KNOWN_MATERIAL_COLORS.put("apatite", 0xFF7DC2C2);
        KNOWN_MATERIAL_COLORS.put("sodalite", 0xFF3044A4);
        KNOWN_MATERIAL_COLORS.put("pyrite", 0xFFB89437);
        KNOWN_MATERIAL_COLORS.put("galena", 0xFF50506A);
    }
    private static int lookupKnownColour(String name) {
        Integer c = KNOWN_MATERIAL_COLORS.get(name);
        return c != null ? c : -1;
    }

    public int getColourFor(ItemStack stack, OreEntry entry) {
        return getColourFor(stack).getRGB();
    }
    
    //find an appropriate colour to tint ores if no texture is provided
    public Color getColourFor(ItemStack stack) {
        if (stack.isEmpty()) return Color.WHITE;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            int itemTint = mc.getItemColors().colorMultiplier(stack, 0);
            if (itemTint != -1) {
                int opaqueTint = itemTint | 0xFF000000;
                DynaOresLogger.logInfo("Got IItemColor tint 0x" + Integer.toHexString(opaqueTint) + " for " + stack);
                return new Color(opaqueTint, true);
            }
            List<BakedQuad> quads;
            if (stack.getItem() instanceof ItemBlock) {
                IBlockState state = ((ItemBlock) stack.getItem()).getBlock().getStateForPlacement(mc.world,
                        new BlockPos(0,0,0), EnumFacing.UP, 0, 0, 0, stack.getMetadata(), mc.player);
                IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);
                quads = model.getQuads(state, null, 0);
                if (quads.isEmpty()) quads = model.getQuads(state, EnumFacing.NORTH, 0);
            } else {
                quads = mc.getRenderItem().getItemModelWithOverrides(stack, null, null).getQuads(null, null, 0);
            }
            if (quads.isEmpty()) {
                DynaOresLogger.logInfo("No baked quads to sample for " + stack + ", falling back to white");
                return Color.WHITE;
            }
            for (BakedQuad quad : quads) {
                TextureAtlasSprite sprite = quad.getSprite();
                List<Integer> colours = Lists.newArrayList();
                int[][] frameData = null;
                try {
                    frameData = sprite.getFrameTextureData(0);
                } catch (Exception ignored) {}
                if (frameData != null && frameData.length > 0) {
                    for (int[] rows : frameData) {
                        for (int colour : rows) if ((colour >>> 24 & 0xFF) > 0) colours.add(colour);
                    }
                }
                if (colours.isEmpty()) {
                    try {
                        ResourceLocation iconLoc = new ResourceLocation(sprite.getIconName());
                        ResourceLocation pngLoc = new ResourceLocation(iconLoc.getResourceDomain(),
                                "textures/" + iconLoc.getResourcePath() + ".png");
                        try (InputStream is = mc.getResourceManager().getResource(pngLoc).getInputStream()) {
                            BufferedImage img = javax.imageio.ImageIO.read(is);
                            if (img != null) for (int x = 0; x < img.getWidth(); x++) {
                                for (int y = 0; y < img.getHeight(); y++) {
                                    int colour = img.getRGB(x, y);
                                    if ((colour >>> 24 & 0xFF) > 0) colours.add(colour);
                                }
                            }
                            DynaOresLogger.logInfo("Used ImageIO PNG fallback for sprite " + iconLoc + " (" + stack + ")");
                        }
                    } catch (Exception ignored) {}
                }
                if (!colours.isEmpty()) {
                    long r = 0, g = 0, b = 0;
                    for (int colour : colours) {
                        r += (colour >> 16) & 0xFF;
                        g += (colour >> 8)  & 0xFF;
                        b += colour         & 0xFF;
                    }
                    Color c = new Color((int) r / colours.size(), (int) g / colours.size(), (int) b / colours.size(), 255);
                    DynaOresLogger.logInfo("Sampled colour " + c + " for " + stack);
                    return c;
                }
            }
            DynaOresLogger.logInfo("All sprite layers were transparent for " + stack + ", falling back to white");
            return Color.WHITE;
        } catch (Exception e) {
            DynaOresLogger.logError("Error getting colour for " + stack, e);
            return Color.WHITE;
        }
    }
    
    public void stitchTextures(TextureMap map) {
        itemTextures.clear();
        blockTextures.clear();
        Minecraft mc = Minecraft.getMinecraft();
        IResourceManager rm = mc.getResourceManager();
        if (Loader.isModLoaded("gregtech")) GTCEuIntegration.finalizeMaterials();
        //register template item textures
        map.registerSprite(Constants.loc("items/material_sets/dull/raw_ore"));
        map.registerSprite(Constants.loc("items/material_sets/dull/raw_ore_secondary"));
        map.registerSprite(Constants.loc("items/material_sets/metallic/raw_ore"));
        map.registerSprite(Constants.loc("items/material_sets/metallic/raw_ore_secondary"));
        map.registerSprite(Constants.loc("items/material_sets/shiny/raw_ore"));
        map.registerSprite(Constants.loc("items/material_sets/shiny/raw_ore_secondary"));
        map.registerSprite(Constants.loc("items/material_sets/diamond/raw_ore"));
        map.registerSprite(Constants.loc("items/material_sets/diamond/raw_ore_secondary"));
        map.registerSprite(Constants.loc("items/material_sets/none/raw_ore"));
        //register template block textures
        map.registerSprite(Constants.loc("blocks/material_sets/dull/raw_ore_block"));
        map.registerSprite(Constants.loc("blocks/material_sets/dull/raw_ore_block_secondary"));
        map.registerSprite(Constants.loc("blocks/material_sets/metallic/raw_ore_block"));
        map.registerSprite(Constants.loc("blocks/material_sets/metallic/raw_ore_block_secondary"));
        //register item textures
        for (String name : OreHandler.INSTANCE.getOreNames()) {
            name = name.toLowerCase(Locale.US);
            //use a try here to check if the texture exists
            try {
                rm.getAllResources(Constants.loc("textures/items/default/" + name + ".png"));
                //register the texture
                map.registerSprite(Constants.loc("items/default/" + name));
                itemTextures.add(name);
                DynaOresLogger.logInfo("Registered item texture for " + name);
            } catch (Exception e) {}
            //register block textures
            try {
                rm.getAllResources(Constants.loc("textures/blocks/default/" + name + ".png"));
                //register the texture
                map.registerSprite(Constants.loc("blocks/default/" + name));
                blockTextures.add(name);
                DynaOresLogger.logInfo("Registered block texture for " + name);
            } catch (Exception e) {}
        }
    }
    
    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        noItemTexture.clear();
        noBlockTexture.clear();
        for (OreEntry entry : OreHandler.INSTANCE.getOres()) entry.refresh();
    }
    
    @Override
    public boolean accepts(ResourceLocation loc) {
        return loc.getResourcePath().contains(".raw_ore");
    }
    
    @Override
    public IModel loadModel(ResourceLocation location) throws Exception {
        IResourceManager mngr = Minecraft.getMinecraft().getResourceManager();
        try {
            boolean block = location.getResourcePath().contains("block");
            String[] split = location.getResourcePath().split("\\.")[0].split("/");
            String name = split[split.length - 1].replace("_block", "");
            String nameLower = name.toLowerCase(Locale.US);

            String texName = block ? "blocks/default/" + name : "items/default/" + name;
            boolean hasCustomTexture = false;
            try {
                mngr.getAllResources(Constants.loc("textures/" + texName + ".png"));
                hasCustomTexture = true;
            } catch (Exception e) {}
            if (hasCustomTexture) {
                if (block) {
                    if (!blockTextures.contains(nameLower)) blockTextures.add(nameLower);
                } else {
                    if (!itemTextures.contains(nameLower)) itemTextures.add(nameLower);
                }
                DynaOresLogger.logInfo("Loading custom model for " + location);
                return block ? ModelLoaderRegistry.getModel(Constants.loc("block/raw_ore_block"))
                        .retexture(ImmutableMap.of("all", Constants.locStr(texName))) :
                        new ItemLayerModel(ImmutableList.of(Constants.loc(texName)), overrides);
            }

            MaterialTextureOverride override = MaterialTextureOverride.get(nameLower);
            String iconSet = "dull";
            if (Loader.isModLoaded("gregtech") && GTCEuIntegration.isGTCEuMaterial(nameLower)) {
                iconSet = GTCEuIntegration.getIconSetName(nameLower);
            }
            if (override != null && override.iconSet != null) iconSet = override.iconSet;
            boolean tintBaseLayer = (override != null && override.tintBaseLayer)
                    || "none".equals(iconSet);

            if (!hasCustomTexture) {
                if (block) {
                    String blockIconSet = iconSet;
                    if (blockIconSet.equals("shiny") || blockIconSet.equals("diamond")
                            || blockIconSet.equals("none")) {
                        blockIconSet = "metallic";
                    }
                    DynaOresLogger.logInfo("Loading block model for " + name + " using icon set " + iconSet);
                    return ModelLoaderRegistry.getModel(Constants.loc("block/raw_ore_block"))
                            .retexture(ImmutableMap.of("all",
                                    Constants.locStr("blocks/material_sets/" + blockIconSet + "/raw_ore_block")));
                } else {
                if (tintBaseLayer) {
                    if ("diamond".equals(iconSet)) {
                        DynaOresLogger.logInfo("Loading item model for " + name
                                + " using icon set " + iconSet + " (tint base layer, diamond folder textures)");
                        return ModelLoaderRegistry.getModel(Constants.loc("item/raw_ore_diamond"));
                    }
                    DynaOresLogger.logInfo("Loading item model for " + name
                            + " using icon set " + iconSet + " (tint base layer)");
                    return new ItemLayerModel(ImmutableList.of(
                            Constants.loc("items/material_sets/" + iconSet + "/raw_ore")), overrides);
                }
                    if (Loader.isModLoaded("gregtech") && GTCEuIntegration.isGTCEuMaterial(nameLower)) {
                        DynaOresLogger.logInfo("Loading GTCEu item model for " + name + " with icon set " + iconSet);
                        String modelVariant = "dull".equals(iconSet) ? "raw_ore" : "raw_ore_" + iconSet;
                        return ModelLoaderRegistry.getModel(Constants.loc("item/" + modelVariant));
                    }
                }
            }
            DynaOresLogger.logInfo("Loading default dull model for " + location);
            return block ? ModelLoaderRegistry.getModel(Constants.loc("block/raw_ore_block"))
                    .retexture(ImmutableMap.of("all", Constants.locStr("blocks/material_sets/dull/raw_ore_block"))) :
                    ModelLoaderRegistry.getModel(Constants.loc("item/raw_ore"));
        } catch (Exception e) {
            DynaOresLogger.logError("Failed loading model " + location, e);
            return ModelLoaderRegistry.getMissingModel();
        }
    }

    public int getColour(ItemStack stack, int index) {
        OreEntry entry = ((IOreItem) stack.getItem()).getEntry();
        String name = entry.getName().toLowerCase(Locale.US);
        boolean isBlock = stack.getItem() instanceof ItemBlockRawOre;
        if ((isBlock ? blockTextures : itemTextures).contains(name)) return 0xFFFFFFFF;
        if (isBlock ? noBlockTexture.contains(name) : noItemTexture.contains(name)) {
        } else {
            try {
                Minecraft.getMinecraft().getResourceManager().getAllResources(Constants.loc(
                        isBlock ? "textures/blocks/default/" + name + ".png" : "textures/items/default/" + name + ".png"));
                if (isBlock) blockTextures.add(name); else itemTextures.add(name);
                return 0xFFFFFFFF;
            } catch (Exception ignored) {
                if (isBlock) noBlockTexture.add(name); else noItemTexture.add(name);
            }
        }
        int knownColour = lookupKnownColour(name);
        if (knownColour != -1) {
            if (index == 0) return knownColour;
            return 0xFFFFFFFF;
        }
        if (Loader.isModLoaded("gregtech") && GTCEuIntegration.isGTCEuMaterial(name)) {
            MaterialTextureOverride override = MaterialTextureOverride.get(name);
            boolean tintBaseLayer = (override != null && override.tintBaseLayer)
                    || (override != null && "none".equals(override.iconSet));
            if (tintBaseLayer) {
                if (index == 0) return GTCEuIntegration.getMaterialRGB(name);
                return 0xFFFFFFFF;
            }
            if (index == 1 || isBlock) return GTCEuIntegration.getMaterialRGB(name);
            return 0xFFFFFFFF;
        }
        return entry.getColour();
    }

    public int getColour(IBlockState state, IBlockAccess world, BlockPos pos, int index) {
        OreEntry entry = ((BlockRawOre) state.getBlock()).getEntry();
        String name = entry.getName().toLowerCase(Locale.US);
        if (blockTextures.contains(name)) return 0xFFFFFFFF;
        if (!noBlockTexture.contains(name)) {
            try {
                Minecraft.getMinecraft().getResourceManager().getAllResources(Constants.loc(
                        "textures/blocks/default/" + name + ".png"));
                blockTextures.add(name);
                return 0xFFFFFFFF;
            } catch (Exception ignored) {
                noBlockTexture.add(name);
            }
        }
        int knownColour = lookupKnownColour(name);
        if (knownColour != -1) return knownColour;
        if (Loader.isModLoaded("gregtech") && GTCEuIntegration.isGTCEuMaterial(name)) {
            return GTCEuIntegration.getMaterialRGB(name);
        }
        return entry.getColour();
    }
    
}
