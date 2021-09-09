package josegamerpt.realmines.mines;

import josegamerpt.realmines.mines.components.MineBlock;
import josegamerpt.realmines.mines.components.MineCuboid;
import josegamerpt.realmines.mines.components.MineSign;
import josegamerpt.realmines.mines.gui.MineBlockIcon;
import josegamerpt.realmines.mines.tasks.MineTimer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RMine {

    String toString();

    String getColorIcon();

    Color getColor();

    void setColor(Color c);

    void setColor(String s);

    boolean hasFaceBlock(MineCuboid.CuboidDirection up);

    Location getPOS2();

    void setPOS(Location p1, Location p2);

    boolean hasTP();

    ArrayList<String> getSignList();

    String getDisplayName();

    void setDisplayName(String input);

    MineCuboid getMineCuboid();

    int getBlockCount();

    int getRemainingBlocks();

    int getRemainingBlocksPer();

    int getMinedBlocks();

    int getMinedBlocksPer();

    void fill();

    void register();

    void saveData(Data t);

    void saveAll();

    List<String> getBlockList();

    List<MineBlockIcon> getBlocks();

    void reset();

    void addSign(Block block, String modif);

    void updateSigns();

    void removeBlock(MineBlock mb);

    void addBlock(MineBlock mineBlock);

    void clear();

    void kickPlayers(String s);

    void broadcastMessage(String s, Boolean prefix);

    ArrayList<Player> getPlayersInMine();

    void removeDependencies();

    List<Location> getCube();

    void highlight();

    String getName();

    boolean isResetBy(Reset e);

    int getResetValue(Reset e);

    void setResetStatus(Reset e, boolean b);

    void setResetValue(Reset e, int d);

    Material getIcon();

    void setIcon(Material a);

    boolean isHighlighted();

    void setHighlight(boolean b);

    Location getTeleport();

    void setTeleport(Location location);

    ArrayList<MineSign> getSigns();

    MineTimer getTimer();

    Material getFaceBlock(MineCuboid.CuboidDirection up);

    void setFaceBlock(MineCuboid.CuboidDirection cd, Material a);

    void removeFaceblock(MineCuboid.CuboidDirection d);

    HashMap<MineCuboid.CuboidDirection, Material> getFaces();

    Type getType();

    Location getSchematicPlace();

    String getSchematicFilename();

    Location getPOS1();

    enum Reset {PERCENTAGE, TIME}

    enum Type {BLOCKS, SCHEMATIC}

    enum Data {BLOCKS, ICON, TELEPORT, SIGNS, PLACE, OPTIONS, NAME, FACES, COLOR, MINE_TYPE}

    enum Color {YELLOW, ORANGE, RED, GREEN, WHITE, GRAY, BLUE, PURPLE, BROWN}
}
