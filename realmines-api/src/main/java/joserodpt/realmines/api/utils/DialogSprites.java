package joserodpt.realmines.api.utils;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2026
 * @link https://github.com/joserodpt/RealMines
 */

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The texture drawn for a material inside a dialog's text.
 *
 * <p>The server has no textures to look in, so this is worked out from the name: a block's is
 * usually {@code block/<name>}, an item's {@code item/<name>}. The blocks and crops a mine is likely
 * to hold whose texture is named otherwise - one per face, or one per growth stage - are listed
 * below; anything else wrongly guessed shows as the missing-texture square, and belongs here.</p>
 */
final class DialogSprites {

    /**
     * Private-use characters, which nothing RealMines shows would contain. Here rather than in
     * {@link PaperText}, which the Spigot side must never load.
     */
    static final char START = '\uE000';
    static final char SEPARATOR = '\uE001';
    static final char END = '\uE002';

    private static final Map<Material, String> OVERRIDES = new HashMap<>();

    static {
        //blocks whose texture is not named after them: one per face shows its side, as in the inventory
        put("GRASS_BLOCK", "block/grass_block_side");
        put("PODZOL", "block/podzol_side");
        put("MYCELIUM", "block/mycelium_side");
        put("DIRT_PATH", "block/dirt_path_side");
        put("FARMLAND", "block/farmland");
        put("SANDSTONE", "block/sandstone");
        put("RED_SANDSTONE", "block/red_sandstone");
        put("QUARTZ_BLOCK", "block/quartz_block_side");
        put("SNOW_BLOCK", "block/snow");
        put("MELON", "block/melon_side");
        put("PUMPKIN", "block/pumpkin_side");
        put("CARVED_PUMPKIN", "block/carved_pumpkin");
        put("HAY_BLOCK", "block/hay_block_side");
        put("TNT", "block/tnt_side");
        put("BONE_BLOCK", "block/bone_block_side");
        put("BASALT", "block/basalt_side");
        put("POLISHED_BASALT", "block/polished_basalt_side");
        put("ANCIENT_DEBRIS", "block/ancient_debris_side");
        put("MAGMA_BLOCK", "block/magma");
        put("CACTUS", "block/cactus_side");
        put("CRIMSON_NYLIUM", "block/crimson_nylium_side");
        put("WARPED_NYLIUM", "block/warped_nylium_side");
        put("PURPUR_PILLAR", "block/purpur_pillar");
        put("DEEPSLATE", "block/deepslate");
        put("BAMBOO_BLOCK", "block/bamboo_block");

        //crops, planted as blocks named after the plant: their item, as a player would recognise it
        put("WHEAT", "item/wheat");
        put("CARROTS", "item/carrot");
        put("POTATOES", "item/potato");
        put("BEETROOTS", "item/beetroot");
        put("NETHER_WART", "item/nether_wart");
        put("SUGAR_CANE", "item/sugar_cane");
        put("SWEET_BERRY_BUSH", "item/sweet_berries");
        put("COCOA", "item/cocoa_beans");
        put("BAMBOO", "item/bamboo");
        put("KELP", "item/kelp");
        put("MELON_STEM", "item/melon_seeds");
        put("PUMPKIN_STEM", "item/pumpkin_seeds");
    }

    private DialogSprites() {
    }

    /** By name, so a material this server's version doesn't have is simply skipped. */
    private static void put(final String material, final String texture) {
        final Material m = Material.getMaterial(material);
        if (m != null) {
            OVERRIDES.put(m, texture);
        }
    }

    /** The text {@link PaperText} draws as the sprite {@code texture} from {@code atlas}. */
    static String marker(final String atlas, final String texture) {
        return START + atlas + SEPARATOR + texture + END;
    }

    /** The texture path, such as {@code block/diamond_ore}. */
    static String texture(final Material material) {
        final String override = OVERRIDES.get(material);
        if (override != null) {
            return override;
        }
        final String name = material.name().toLowerCase(Locale.ROOT);
        //logs and wood share the log's bark; the rest of a block's name is its texture
        if (material.isBlock()) {
            return "block/" + (name.endsWith("_wood") ? name.substring(0, name.length() - "_wood".length()) + "_log" : name);
        }
        return "item/" + name;
    }

    /**
     * The atlas a texture is in. Items got their own in 1.21.11; before that everything was in the
     * blocks one.
     */
    static String atlas(final String texture, final boolean itemsAtlas) {
        return itemsAtlas && texture.startsWith("item/") ? "minecraft:items" : "minecraft:blocks";
    }
}
