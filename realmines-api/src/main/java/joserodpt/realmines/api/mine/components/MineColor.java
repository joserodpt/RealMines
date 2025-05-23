package joserodpt.realmines.api.mine.components;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.utils.Items;
import joserodpt.realmines.api.utils.SkullCreator;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public enum MineColor {
    //colors for use in the mines highlight
    YELLOW("&e", Color.YELLOW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTRjNDE0MWMxZWRmM2Y3ZTQxMjM2YmQ2NThjNWJjN2I1YWE3YWJmN2UyYTg1MmI2NDcyNTg4MThhY2Q3MGQ4In19fQ==", Material.YELLOW_CONCRETE),
    ORANGE("&6", Color.ORANGE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU5OTY5NTdiZmQ0Y2FmZTVkOTk5Njg0YzNkNTI5NGJkOWM1ZGZhNzg3ZGY0NjdiYjBhZmNmNTViYWFiZTgifX19", Material.ORANGE_CONCRETE),
    RED("&c", Color.RED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdjMWYxZWFkNGQ1MzFjYWE0YTViMGQ2OWVkYmNlMjlhZjc4OWEyNTUwZTVkZGJkMjM3NzViZTA1ZTJkZjJjNCJ9fX0=", Material.RED_CONCRETE),
    GREEN("&2", Color.GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZmNjlmN2I3NTM4YjQxZGMzNDM5ZjM2NThhYmJkNTlmYWNjYTM2NmYxOTBiY2YxZDZkMGEwMjZjOGY5NiJ9fX0=", Material.GREEN_CONCRETE),
    WHITE("&f", Color.WHITE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVhNzcwZTdlNDRiM2ExZTZjM2I4M2E5N2ZmNjk5N2IxZjViMjY1NTBlOWQ3YWE1ZDUwMjFhMGMyYjZlZSJ9fX0=", Material.WHITE_CONCRETE),
    GRAY("&7", Color.GRAY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUwOWRhOTg5NWJiZTc4YzFkY2NkMjZjYTY1MzY4ZDIxYWQ0N2VmYTk2ZTZiNjQxYjU4OTMzNjY0NTNhZWYifX19", Material.GRAY_CONCRETE),
    BLUE("&9", Color.fromRGB(51, 153, 255), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2IxOWRjNGQ0Njc4ODJkYmNhMWI1YzM3NDY1ZjBjZmM3MGZmMWY4MjllY2Y0YTg2NTc5NmI4ZTVjMjgwOWEifX19", Material.BLUE_CONCRETE),
    PURPLE("6d", Color.PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJhMGFkMzVjM2JhZTg3MTRmNWRhNWI5ZTc5OWM5ZmZkODY2M2YxYWJkNzEzNDNhMmZhMDE2ZDAyMmI0YmYifX19", Material.PURPLE_CONCRETE),
    BROWN("&4", Color.fromRGB(153, 102, 51), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU3NGE0Njc3MzcyOTViZDlkZGM4MjU0NWExYTRlMTQ2YTk0M2QwNWVjYzgyMWY5Y2M2YTU0M2ZmZTk5MzRhIn19fQ==", Material.BROWN_CONCRETE),
    ;

    private final Color color;
    private final String base64skin, colorPrefix;
    private final Material backupMaterial;

    MineColor(String colorPrefix, Color c, String base64skin, Material backupMaterial) {
        this.colorPrefix = colorPrefix;
        this.color = c;
        this.base64skin = base64skin;
        this.backupMaterial = backupMaterial;
    }

    public String getColorPrefix() {
        return this.colorPrefix + "●";
    }

    public Color getColor() {
        return this.color;
    }

    public ItemStack getItem(final String name, final List<String> desc) {
        try {
            return Items.changeItemStack(name, desc, SkullCreator.itemFromBase64(this.base64skin));
        } catch (Exception e) {
            return Items.createItem(this.backupMaterial, 1, name, desc);
        }
    }

    public static MineColor valueOf(Color c) {
        return Arrays.stream(MineColor.values())
                .filter(value -> value.getColor().equals(c))
                .findFirst()
                .orElse(null);
    }
}
