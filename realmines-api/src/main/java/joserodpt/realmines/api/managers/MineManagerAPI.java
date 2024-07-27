package joserodpt.realmines.api.managers;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineIcon;
import joserodpt.realmines.api.mine.components.MineSign;
import joserodpt.realmines.api.mine.components.items.MineItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class MineManagerAPI {
    protected abstract Map<Material, MineItem> getBlocks(String mineName, RMine.Type type);

    public abstract List<String> getRegisteredMines();

    public abstract void loadMines();

    public abstract void createMine(Player p, String name);

    public abstract void createCropsMine(Player p, String name);

    public abstract void createSchematicMine(Player p, String name);

    public abstract void saveAllMineData(RMine mine);

    public abstract void saveMine(RMine mine, RMine.Data t);

    public abstract List<MineIcon> getMineList();

    //permission for teleport: realmines.tp.<name>
    public abstract void teleport(Player target, RMine m, Boolean silent, Boolean checkForPermission);

    public abstract RMine getMine(String name);

    public abstract MineItem findBlockUpdate(Player p, Cancellable e, Block b, boolean broken);

    public abstract List<MineSign> getSigns();

    public abstract void unloadMines();

    public abstract void setBounds(RMine m, Player p);

    public abstract void stopTasks();

    public abstract void startTasks();

    public abstract void deleteMine(RMine mine);

    public abstract void clearMemory();

    public abstract Map<String, RMine> getMines();

    public abstract void addMine(RMine mine);

    public abstract File getSchematicFolder();

    public abstract void renameMine(RMine m, String newName);

    public abstract void unregisterMine(RMine m);

    public abstract void registerMine(RMine m);
}
