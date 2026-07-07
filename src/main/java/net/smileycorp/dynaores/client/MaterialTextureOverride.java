package net.smileycorp.dynaores.client;

import java.util.HashMap;
import java.util.Map;

public class MaterialTextureOverride {

    public final String iconSet;
    public final boolean tintBaseLayer;

    public MaterialTextureOverride(String iconSet, boolean tintBaseLayer) {
        this.iconSet = iconSet;
        this.tintBaseLayer = tintBaseLayer;
    }

    private static final Map<String, MaterialTextureOverride> OVERRIDES = new HashMap<>();

    static {
        registerNone("kyanite");
        registerNone("coal");
        registerNone("redstone");
        registerNone("bentonite");
        registerNone("lepidolite");
        registerNone("mica");
        registerNone("oilsands");
        registerNone("salt");
        registerNone("bastnasite");
        registerNone("grossular");
        register("lazurite", "diamond", true);
        register("sodalite", "diamond", true);
        register("lapis",    "diamond", true);
        register("opal",     "diamond", true);
        register("olivine",  "diamond", true);
        register("monazite", "diamond", true);
        register("topaz",    "diamond", true);
        register("pyrope", "metallic", true);
        register("quartzite", "shiny", false);
        register("lithium", "dull", true);
        register("realgar", "dull", true);
    }

    private static void registerNone(String name) {
        OVERRIDES.put(name, new MaterialTextureOverride("none", true));
    }

    private static void register(String name, String iconSet, boolean tintBaseLayer) {
        OVERRIDES.put(name, new MaterialTextureOverride(iconSet, tintBaseLayer));
    }

    public static MaterialTextureOverride get(String name) {
        if (name == null) return null;
        return OVERRIDES.get(name.toLowerCase());
    }
}
