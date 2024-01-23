package joserodpt.realmines.api.managers;

import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineIcon;
import joserodpt.realmines.api.mine.components.MineSign;
import joserodpt.realmines.api.mine.components.items.MineItem;
import joserodpt.realmines.api.utils.converters.RMConverterBase;
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
    public abstract void teleport(Player target, RMine m, Boolean silent);

    public abstract RMine getMine(String name);

    public abstract MineItem findBlockUpdate(Player p, Cancellable e, Block b, boolean broken);

    public abstract List<MineSign> getSigns();

    public abstract void unloadMines();

    public abstract void setRegion(RMine m, Player p);

    public abstract void stopTasks();

    public abstract void startTasks();

    public abstract void deleteMine(RMine mine);

    public abstract void clearMemory();

    public abstract Map<String, RMine> getMines();

    public abstract void addMine(RMine mine);

    public abstract File getSchematicFolder();

    public abstract Map<String, RMConverterBase> getConverters();

    public abstract void renameMine(RMine m, String newName);

    public abstract void unregisterMine(RMine m);

    public abstract void registerMine(RMine m);
}
