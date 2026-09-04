package joserodpt.realmines.plugin.managers;

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

import joserodpt.realmines.api.RealMinesAPI;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import joserodpt.realmines.api.config.RMConfig;
import joserodpt.realmines.api.config.RMPrivateMinesConfig;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.event.RealMinesMineChangeEvent;
import joserodpt.realmines.api.managers.PrivateMinePlatform;
import joserodpt.realmines.api.managers.PrivateMineTemplate;
import joserodpt.realmines.api.managers.PrivateMinesManagerAPI;
import joserodpt.realmines.api.managers.PrivateMinesWorld;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.components.MineCuboid;
import joserodpt.realmines.api.mine.components.PrivateMineData;
import joserodpt.realmines.api.mine.components.RMFailedToLoadException;
import joserodpt.realmines.api.mine.components.RMineSettings;
import joserodpt.realmines.api.mine.types.BlockMine;
import joserodpt.realmines.api.mine.types.farm.FarmMine;
import joserodpt.realmines.api.utils.WorldEditUtils;
import joserodpt.realmines.plugin.RealMines;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PrivateMinesManager extends PrivateMinesManagerAPI {

    /**
     * Owner name every {@link #spawnDebugMine} mine is given, and what {@link #clearDebugMines} looks for.
     */
    public static final String DEBUG_OWNER = "Sharik";

    public static final String ADMIN_PERMISSION = "realmines.privatemines.admin";
    public static final String USE_PERMISSION = "realmines.privatemines";

    private final RealMines rm;
    private final Map<String, PrivateMineTemplate> templates = new LinkedHashMap<>();

    /**
     * How many grid slots to try before giving up, so a full or badly configured grid can't spin forever.
     */
    private static final int SLOT_SEARCH_LIMIT = 10000;

    public PrivateMinesManager(final RealMines rm) {
        this.rm = rm;
    }

    @Override
    public boolean isEnabled() {
        return RMPrivateMinesConfig.file() != null && RMPrivateMinesConfig.file().getBoolean("Private-Mines.Enabled", true);
    }

    @Override
    public int getMaxMinesPerPlayer() {
        return RMPrivateMinesConfig.file() == null ? 3
                : Math.max(1, RMPrivateMinesConfig.file().getInt("Private-Mines.Max-Mines-Per-Player", 3));
    }

    private double refundFraction() {
        return RMPrivateMinesConfig.file() == null ? 0D
                : Math.max(0D, Math.min(1D, RMPrivateMinesConfig.file().getDouble("Private-Mines.Refund-On-Release", 0D)));
    }

    private File templatesFolder() {
        return RMPrivateMinesConfig.getTemplatesFolder(this.rm.getPlugin());
    }

    private File ownerFolder(final UUID owner) {
        return RMPrivateMinesConfig.getOwnerFolder(this.rm.getPlugin(), owner);
    }

    // ------------------------------------------------------------------ templates

    @Override
    public void loadTemplates() {
        this.templates.clear();

        final File folder = this.templatesFolder();
        final File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (final File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".yml")) {
                continue;
            }

            final String id = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
            try {
                //copies are only ever built in RealMines' own world, so bring it up before anything
                //validates against it. Servers with no templates never get one created for them.
                if (this.isEnabled()) {
                    PrivateMinesWorld.get();
                }

                final PrivateMineTemplate template = new PrivateMineTemplate(id, file, YamlConfiguration.loadConfiguration(file));
                for (final String problem : template.validate()) {
                    this.rm.getLogger().warning("Private mine template '" + id + "': " + problem);
                }
                this.templates.put(id, template);
            } catch (final Exception e) {
                this.rm.getLogger().severe("Failed to load private mine template " + file.getName() + ": " + e.getMessage());
            }
        }

        if (!this.templates.isEmpty()) {
            this.rm.getLogger().info("Loaded " + this.templates.size() + " private mine template(s).");
        }
    }

    @Override
    public Collection<PrivateMineTemplate> getTemplates() {
        return this.templates.values();
    }

    @Override
    public PrivateMineTemplate getTemplate(final String id) {
        return id == null ? null : this.templates.get(id.toLowerCase());
    }

    @Override
    public PrivateMineTemplate snapshot(final RMine source, final String rawID) throws IllegalArgumentException {
        if (source == null) {
            throw new IllegalArgumentException("mine not found");
        }
        if (source.getType() == RMine.Type.SCHEMATIC) {
            throw new IllegalArgumentException("schematic mines can't be used as private mine templates");
        }
        if (source.isPrivate()) {
            throw new IllegalArgumentException("that is already a private mine");
        }
        if (source.getMineCuboid() == null) {
            throw new IllegalArgumentException("that mine has no region set");
        }

        final String id = rawID.toLowerCase().replaceAll("[^a-z0-9_-]", "");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("invalid template name");
        }

        //flush anything still only in memory so the snapshot matches what the admin sees in game
        source.saveConfig();

        final File sourceFile = new File(source.getConfigFolder(), source.getFileName() + ".yml");
        final YamlConfiguration snap = YamlConfiguration.loadConfiguration(sourceFile);

        //a copy must never point at the original's sign blocks, and reset commands would otherwise run
        //once per claimed instance
        snap.set("signs", new ArrayList<String>());
        snap.set("reset.commands", new ArrayList<String>());

        //not silent by default: a private mine's reset announcement goes to its owner and their trusted
        //players only, so it informs the people using it without touching global chat. An update keeps
        //whatever the admin set instead, further down.
        snap.set("reset.silent", false);
        snap.set(RMineSettings.BREAK_PERMISSION.getConfigKey(), false);
        snap.set("name", id);
        snap.set(PrivateMineData.ROOT, null);

        final PrivateMineTemplate existing = this.templates.get(id);

        snap.set(PrivateMineTemplate.ROOT + ".id", id);
        snap.set(PrivateMineTemplate.ROOT + ".source-mine", source.getName());
        snap.set(PrivateMineTemplate.ROOT + ".created-at", System.currentTimeMillis() / 1000L);

        //re-snapshotting keeps whatever the admin configured; a brand new template gets defaults
        if (existing == null) {
            snap.set(PrivateMineTemplate.ROOT + ".display-name", "&b%player%'s " + source.getDisplayName());
            snap.set(PrivateMineTemplate.ROOT + ".icon", source.getIcon().name());
            snap.set(PrivateMineTemplate.ROOT + ".description", new ArrayList<String>());
            snap.set(PrivateMineTemplate.ROOT + ".permission", "");
            snap.set(PrivateMineTemplate.ROOT + ".cost", 0D);
            snap.set(PrivateMineTemplate.ROOT + ".renew-cost", 0D);
            snap.set(PrivateMineTemplate.ROOT + ".lifecycle", PrivateMineData.Lifecycle.PERSISTENT.name());
            snap.set(PrivateMineTemplate.ROOT + ".duration", 3600L);
            snap.set(PrivateMineTemplate.ROOT + ".trusted-limit", 5);
            //no world here: every copy is built in the world RealMines makes for private mines
            snap.set(PrivateMineTemplate.ROOT + ".placement.origin", "0;64;0");
            snap.set(PrivateMineTemplate.ROOT + ".placement.spacing-x", Math.max(50, source.getMineCuboid().getSizeX() * 2));
            snap.set(PrivateMineTemplate.ROOT + ".placement.spacing-z", Math.max(50, source.getMineCuboid().getSizeZ() * 2));
            snap.set(PrivateMineTemplate.ROOT + ".placement.per-row", 20);
            snap.set(PrivateMineTemplate.ROOT + ".placement.platform-width", PrivateMinePlatform.DEFAULT_WIDTH);
            snap.set(PrivateMineTemplate.ROOT + ".placement.shell-schematic", "");
        } else {
            final YamlConfiguration old = existing.copySnapshot();

            //an update re-takes the blocks and geometry, not the admin's choices about how it is handed out
            snap.set("reset.silent", old.getBoolean("reset.silent", false));

            if (old.getConfigurationSection(PrivateMineTemplate.ROOT) != null) {
                for (final String key : old.getConfigurationSection(PrivateMineTemplate.ROOT).getKeys(true)) {
                    final String path = PrivateMineTemplate.ROOT + "." + key;
                    if (!key.equals("source-mine") && !key.equals("created-at") && !key.equals("id")) {
                        snap.set(path, old.get(path));
                    }
                }
            }
        }

        final File file = new File(this.templatesFolder(), id + ".yml");
        try {
            snap.save(file);
        } catch (final IOException e) {
            throw new IllegalArgumentException("couldn't write the template file: " + e.getMessage());
        }

        final PrivateMineTemplate template = new PrivateMineTemplate(id, file, YamlConfiguration.loadConfiguration(file));
        for (final String problem : template.validate()) {
            this.rm.getLogger().warning("Private mine template '" + id + "': " + problem);
        }
        this.templates.put(id, template);
        return template;
    }

    @Override
    public boolean deleteTemplate(final String id) {
        final PrivateMineTemplate template = this.getTemplate(id);
        if (template == null) {
            return false;
        }
        //only forget it once the file is really gone, otherwise it silently comes back on the next reload
        if (!template.getFile().delete() && template.getFile().exists()) {
            return false;
        }
        this.templates.remove(template.getID());
        return true;
    }

    // ------------------------------------------------------------------ instances

    @Override
    public void loadInstances() {
        if (!this.isEnabled()) {
            return;
        }

        final File root = RMPrivateMinesConfig.getFolder(this.rm.getPlugin());
        final File[] ownerFolders = root.listFiles(File::isDirectory);
        if (ownerFolders == null) {
            return;
        }

        int loaded = 0, purged = 0;
        for (final File ownerFolder : ownerFolders) {
            if (ownerFolder.getName().equals(RMPrivateMinesConfig.TEMPLATES_FOLDER)) {
                continue;
            }

            final UUID owner;
            try {
                owner = UUID.fromString(ownerFolder.getName());
            } catch (final IllegalArgumentException e) {
                continue;
            }

            //somebody owns a mine, so the world holding it has to be loaded before it is constructed.
            //Cheap to repeat: it is a lookup once the world is up.
            PrivateMinesWorld.get();

            final File[] files = ownerFolder.listFiles();
            if (files == null) {
                continue;
            }

            for (final File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".yml")) {
                    continue;
                }

                final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                final PrivateMineData data = PrivateMineData.load(config);
                if (data == null || !data.isOwner(owner)) {
                    this.rm.getLogger().warning("Skipping " + file.getPath() + ": it has no valid private mine owner.");
                    continue;
                }

                //deleting before constructing means a doomed mine is never built or filled in the first place.
                //A session mine only dies with its owner, so one whose owner is online right now is being
                //reloaded, not restarted, and has to survive.
                final boolean deadSession = data.getLifecycle() == PrivateMineData.Lifecycle.SESSION
                        && Bukkit.getPlayer(owner) == null;
                if (deadSession || data.hasExpired()) {
                    //wipe the blocks straight from the stored coordinates: building the mine only to
                    //delete it would fill the region first, and the slot is about to be handed out again
                    clearRegionOf(config, data);
                    if (file.delete()) {
                        purged++;
                    }
                    continue;
                }

                final String name = config.getString("name");
                if (name == null || this.rm.getMineManager().getMine(name) != null) {
                    this.rm.getLogger().warning("Skipping " + file.getPath() + ": a mine named '" + name + "' already exists.");
                    continue;
                }

                try {
                    final RMine mine = construct(name, config, ownerFolder, data);
                    this.rm.getMineManager().addMine(mine);
                    loaded++;
                } catch (final RMFailedToLoadException e) {
                    this.rm.getLogger().severe("Failed to load private mine " + file.getPath() + ": " + e.getMessage());
                }
            }

            deleteIfEmpty(ownerFolder);
        }

        if (loaded > 0 || purged > 0) {
            this.rm.getLogger().info("Loaded " + loaded + " private mine(s)" + (purged > 0 ? ", purged " + purged : "") + ".");
        }
    }

    private static RMine construct(final String name, final YamlConfiguration config, final File folder,
                                   final PrivateMineData data) throws RMFailedToLoadException {
        final RMine mine = "FARM".equals(config.getString("type"))
                ? new FarmMine(name, config, folder)
                : new BlockMine(name, config, folder);
        mine.setPrivateData(data);
        return mine;
    }

    /**
     * Empties the region a stored mine config describes, without constructing the mine. Used when a mine
     * is deleted before it is ever built, so the next tenant of that slot doesn't inherit its blocks.
     */
    private static void clearRegionOf(final YamlConfiguration config, final PrivateMineData data) {
        final World world = Bukkit.getWorld(String.valueOf(config.getString("world")));
        final int[] p1 = PrivateMineTemplate.parsePos(config.getString("pos1"));
        final int[] p2 = PrivateMineTemplate.parsePos(config.getString("pos2"));
        if (world == null || p1 == null || p2 == null) {
            return;
        }

        //the walkway and its fence sit outside the region, so they have to come down separately
        if (data != null && data.getPlatformWidth() > 0) {
            PrivateMinePlatform.remove(world, new int[]{
                    Math.min(p1[0], p2[0]), Math.min(p1[1], p2[1]), Math.min(p1[2], p2[2]),
                    Math.max(p1[0], p2[0]), Math.max(p1[1], p2[1]), Math.max(p1[2], p2[2])},
                    data.getPlatformWidth());
        }

        try {
            WorldEditUtils.setBlocks(
                    new CuboidRegion(BukkitAdapter.adapt(world),
                            BlockVector3.at(p1[0], p1[1], p1[2]), BlockVector3.at(p2[0], p2[1], p2[2])),
                    BukkitAdapter.adapt(Material.AIR.createBlockData()));
        } catch (final Exception e) {
            RealMinesAPI.getInstance().getLogger().warning("Couldn't clear a removed private mine's region: " + e.getMessage());
        }
    }

    private static void deleteIfEmpty(final File folder) {
        final String[] left = folder.list();
        if (left != null && left.length == 0) {
            folder.delete();
        }
    }

    @Override
    public List<RMine> getPrivateMines() {
        return this.rm.getMineManager().getMines().values().stream()
                .filter(RMine::isPrivate)
                .sorted(Comparator.comparing(RMine::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<RMine> getMinesOf(final UUID owner) {
        return this.rm.getMineManager().getMines().values().stream()
                .filter(m -> m.isPrivate() && m.getPrivateData().isOwner(owner))
                .sorted(Comparator.comparing(m -> m.getPrivateData().getTemplate()))
                .collect(Collectors.toList());
    }

    @Override
    public RMine getMineOf(final UUID owner, final String templateID) {
        return this.rm.getMineManager().getMines().values().stream()
                .filter(m -> m.isPrivate() && m.getPrivateData().isOwner(owner)
                        && m.getPrivateData().getTemplate().equalsIgnoreCase(templateID))
                .findFirst().orElse(null);
    }

    /**
     * How many private mines this player has on disk, which can be more than are loaded in memory.
     */
    private int countMinesOnDisk(final UUID owner) {
        final File[] files = this.ownerFolder(owner).listFiles((dir, fileName) -> fileName.endsWith(".yml"));
        return files == null ? 0 : files.length;
    }

    // ------------------------------------------------------------------ slots

    /**
     * Every region already taken by a private mine, as {world, minX, minY, minZ, maxX, maxY, maxZ}.
     * <p>
     * Read off disk rather than out of memory: a mine whose world failed to load never reaches the mine
     * map, but its blocks are still very much there.
     */
    private List<Object[]> occupiedRegions() {
        final List<Object[]> regions = new ArrayList<>();

        final File root = RMPrivateMinesConfig.getFolder(this.rm.getPlugin());
        final File[] ownerFolders = root.listFiles(File::isDirectory);
        if (ownerFolders == null) {
            return regions;
        }

        for (final File ownerFolder : ownerFolders) {
            if (ownerFolder.getName().equals(RMPrivateMinesConfig.TEMPLATES_FOLDER)) {
                continue;
            }
            final File[] files = ownerFolder.listFiles();
            if (files == null) {
                continue;
            }
            for (final File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".yml")) {
                    continue;
                }
                final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                final int[] p1 = PrivateMineTemplate.parsePos(config.getString("pos1"));
                final int[] p2 = PrivateMineTemplate.parsePos(config.getString("pos2"));
                final String world = config.getString("world");
                if (p1 == null || p2 == null || world == null) {
                    continue;
                }
                regions.add(new Object[]{world,
                        Math.min(p1[0], p2[0]), Math.min(p1[1], p2[1]), Math.min(p1[2], p2[2]),
                        Math.max(p1[0], p2[0]), Math.max(p1[1], p2[1]), Math.max(p1[2], p2[2])});
            }
        }
        return regions;
    }

    /**
     * The first grid slot of this template whose region touches nothing that already exists.
     * <p>
     * Checked as real regions rather than by slot number, because two templates can share a world and
     * their grids don't have to line up - comparing slot indexes alone would happily stack them.
     *
     * @return the slot, or -1 when the search limit was reached without finding a free one
     */
    @Override
    public int nextFreeSlot(final PrivateMineTemplate template) {
        final int[] bounds = template.getSnapshotBounds();
        final String world = template.getPlacement().getWorldName();
        if (bounds == null || world == null) {
            return -1;
        }

        final int sizeX = bounds[3] - bounds[0];
        final int sizeY = bounds[4] - bounds[1];
        final int sizeZ = bounds[5] - bounds[2];

        //a copy takes up its platform as well as its own region: a slot whose walkway would cut into an
        //existing mine is not free, however far apart the mines themselves would be
        final int margin = template.getPlacement().getPlatformMargin();
        final int below = margin > 0 ? PrivateMinePlatform.REACH_BELOW : 0;
        final int above = margin > 0 ? PrivateMinePlatform.reachAbove(sizeY + 1) : 0;

        final List<Object[]> occupied = occupiedRegions();

        for (int slot = 0; slot < SLOT_SEARCH_LIMIT; slot++) {
            final Location origin = template.getSlotOrigin(slot);
            if (origin == null) {
                return -1;
            }

            final int minX = origin.getBlockX() - margin, minY = origin.getBlockY() - below,
                    minZ = origin.getBlockZ() - margin;
            final int maxX = origin.getBlockX() + sizeX + margin, maxY = origin.getBlockY() + sizeY + above,
                    maxZ = origin.getBlockZ() + sizeZ + margin;

            boolean free = true;
            for (final Object[] r : occupied) {
                if (!world.equals(r[0])) {
                    continue;
                }
                if (minX <= (int) r[4] && maxX >= (int) r[1]
                        && minY <= (int) r[5] && maxY >= (int) r[2]
                        && minZ <= (int) r[6] && maxZ >= (int) r[3]) {
                    free = false;
                    break;
                }
            }

            if (free) {
                return slot;
            }
        }

        return -1;
    }

    // ------------------------------------------------------------------ claiming

    @Override
    public ClaimResult claim(final Player p, final PrivateMineTemplate template) {
        if (!this.isEnabled()) {
            return ClaimResult.DISABLED;
        }
        if (template == null) {
            return ClaimResult.ERROR;
        }

        final boolean admin = p.hasPermission(ADMIN_PERMISSION);

        //checked here too, not just on the command: the GUI reaches this same method
        if (!admin && !p.hasPermission(USE_PERMISSION)) {
            return ClaimResult.NO_PERMISSION;
        }
        if (!admin && template.hasPermission() && !p.hasPermission(template.getPermission())) {
            return ClaimResult.NO_PERMISSION;
        }
        if (template.getMineType() == RMine.Type.SCHEMATIC) {
            return ClaimResult.UNSUPPORTED_TYPE;
        }

        //count what is on disk, not just what loaded: an instance whose world is missing isn't in memory,
        //but re-claiming it would overwrite the file, charge twice and strand the old blocks
        if (this.getMineOf(p.getUniqueId(), template.getID()) != null
                || new File(this.ownerFolder(p.getUniqueId()), template.getID() + ".yml").isFile()) {
            return ClaimResult.ALREADY_OWNED;
        }
        if (this.countMinesOnDisk(p.getUniqueId()) >= this.getMaxMinesPerPlayer()) {
            return ClaimResult.LIMIT_REACHED;
        }

        final ClaimResult result = this.build(template, p.getUniqueId(), p.getName(), p);
        if (result != ClaimResult.OK) {
            return result;
        }

        //a fresh mine sits alone in an empty world, so put its owner on it rather than making them find
        //it. Silent: the claim message the caller sends next says the same thing better
        final RMine mine = this.getMineOf(p.getUniqueId(), template.getID());
        if (mine != null) {
            this.rm.getMineManager().teleport(p, mine, true, false);
        }
        return result;
    }

    /**
     * Debug helper: drops a copy of a template on the next free slot, owned by a made-up player, so the
     * layout - platform, fence, spacing, where the owner lands - can be walked around in game. Nothing is
     * charged, no permission or limit applies, and the mine is a real one in every other way.
     *
     * @return the mine, or null when it couldn't be placed; the reason is logged
     */
    public RMine spawnDebugMine(final PrivateMineTemplate template, final String ownerName) {
        if (template == null) {
            return null;
        }

        final UUID owner = UUID.randomUUID();
        final ClaimResult result = this.build(template, owner, ownerName, null);
        if (result != ClaimResult.OK) {
            this.rm.getLogger().warning("Couldn't spawn a debug private mine from template '"
                    + template.getID() + "': " + result);
            return null;
        }
        return this.getMineOf(owner, template.getID());
    }

    /**
     * Removes every mine {@link #spawnDebugMine} left behind, so a session of looking at layouts doesn't
     * silently keep slots taken.
     *
     * @return how many were removed
     */
    public int clearDebugMines() {
        final List<RMine> debug = this.getPrivateMines().stream()
                .filter(mine -> mine.getPrivateData().getOwnerName().startsWith(DEBUG_OWNER))
                .collect(Collectors.toList());
        debug.forEach(this::release);
        return debug.size();
    }

    /**
     * Builds a copy of a template on the next free slot and hands it to an owner.
     *
     * @param payer who is charged for it and whose permissions decide whether it is free, or null for the
     *              debug path, which charges nothing and asks nobody
     */
    private ClaimResult build(final PrivateMineTemplate template, final UUID owner, final String ownerName,
                              final Player payer) {
        //RealMines creates this world itself, so a null here means the server refused to make it
        final World world = template.getPlacement().getWorld();
        if (world == null) {
            this.rm.getLogger().severe("Refused a claim of private mine template '" + template.getID()
                    + "': the '" + PrivateMinesWorld.NAME + "' world couldn't be created.");
            return ClaimResult.WORLD_MISSING;
        }

        //a misconfigured template would build copies on top of each other, so refuse rather than
        //quietly corrupt the grid
        final List<String> problems = template.validate();
        if (!problems.isEmpty()) {
            this.rm.getLogger().warning("Refused a claim of private mine template '" + template.getID()
                    + "' because it is misconfigured:");
            problems.forEach(problem -> this.rm.getLogger().warning(" - " + problem));
            return ClaimResult.ERROR;
        }

        final int[] bounds = template.getSnapshotBounds();
        if (bounds == null) {
            return ClaimResult.ERROR;
        }

        final int slot = this.nextFreeSlot(template);
        if (slot < 0) {
            return ClaimResult.NO_FREE_SLOT;
        }
        final Location origin = template.getSlotOrigin(slot);
        if (origin == null) {
            return ClaimResult.NO_FREE_SLOT;
        }

        //the mine's name is the map key and is global, so it has to be unique
        final String name = "pm-" + template.getID() + "-" + owner;
        if (this.rm.getMineManager().getMine(name) != null) {
            return ClaimResult.ERROR;
        }

        //charge last, once everything else is known to be fine
        final double cost = payer == null || payer.hasPermission(ADMIN_PERMISSION) ? 0D : template.getCost();
        if (cost > 0D) {
            final Economy econ = RealMinesAPI.getInstance().getEconomy();
            if (econ == null) {
                this.rm.getLogger().warning("Private mine template '" + template.getID() + "' costs " + cost
                        + " but Vault/an economy plugin isn't available, so the claim was refused.");
                return ClaimResult.NO_ECONOMY;
            }
            if (!econ.has(payer, cost)) {
                return ClaimResult.INSUFFICIENT_FUNDS;
            }
            final EconomyResponse response = econ.withdrawPlayer(payer, cost);
            if (!response.transactionSuccess()) {
                return ClaimResult.ERROR;
            }
        }

        final File folder = this.ownerFolder(owner);
        if (!folder.exists() && !folder.mkdirs()) {
            refund(payer, cost);
            return ClaimResult.ERROR;
        }

        final long now = System.currentTimeMillis() / 1000L;
        final PrivateMineData data = new PrivateMineData(template.getID(), owner, ownerName, slot,
                template.getLifecycle(), now,
                template.getLifecycle() == PrivateMineData.Lifecycle.TIME_LIMITED
                        ? now + template.getDuration() : PrivateMineData.NEVER,
                cost);
        //written into the mine's file so releasing it later takes down the platform it actually got,
        //not whatever the template says by then
        data.setPlatformWidth(template.getPlacement().hasPlatform() ? template.getPlacement().getPlatformWidth() : 0);

        final YamlConfiguration cfg = buildInstanceConfig(template, data, name, ownerName, origin, bounds);

        RMine mine;
        try {
            mine = construct(name, cfg, folder, data);
        } catch (final RMFailedToLoadException | RuntimeException e) {
            this.rm.getLogger().severe("Failed to create a private mine for " + ownerName + ": " + e.getMessage());
            //the constructor creates the file before it can fail, so don't leave a broken one behind
            new File(folder, template.getID() + ".yml").delete();
            deleteIfEmpty(folder);
            refund(payer, cost);
            return ClaimResult.ERROR;
        }

        mine.saveConfig();
        this.rm.getMineManager().addMine(mine);

        //the world is empty, so without this the owner arrives beside a mine floating in the void.
        //Before the shell, so an admin's own decoration wins wherever the two overlap
        if (data.getPlatformWidth() > 0) {
            PrivateMinePlatform.build(world, instanceRegion(origin, bounds), data.getPlatformWidth());
        }

        //only paste the shell once the mine is definitely staying, so a failed claim can't leave
        //decoration behind on a slot that is reported free again
        if (template.getPlacement().hasShellSchematic()) {
            WorldEditUtils.pasteSchematic(template.getPlacement().getShellSchematic(), origin);
        }

        //fill last, so the mine's own blocks win wherever the shell overlaps them. Never reset(): that
        //would advance the block set, run the template's reset commands and announce the reset.
        mine.fillContent();

        Bukkit.getPluginManager().callEvent(new RealMinesMineChangeEvent(mine, RealMinesMineChangeEvent.ChangeOperation.ADDED));
        return ClaimResult.OK;
    }

    /**
     * Turns a template snapshot into a real mine config: same blocks and settings, moved onto the player's
     * grid slot and marked as theirs.
     */
    private static YamlConfiguration buildInstanceConfig(final PrivateMineTemplate template, final PrivateMineData data,
                                                         final String name, final String playerName,
                                                         final Location origin, final int[] bounds) {
        final YamlConfiguration cfg = template.copySnapshot();

        //an instance is a plain mine; the template metadata isn't part of it
        cfg.set(PrivateMineTemplate.ROOT, null);

        final int dx = origin.getBlockX() - bounds[0];
        final int dy = origin.getBlockY() - bounds[1];
        final int dz = origin.getBlockZ() - bounds[2];

        final int[] region = instanceRegion(origin, bounds);

        cfg.set("name", name);
        cfg.set("displayName", template.getDisplayNameFor(playerName));
        cfg.set("world", origin.getWorld().getName());
        cfg.set("pos1", region[0] + ";" + region[1] + ";" + region[2]);
        cfg.set("pos2", region[3] + ";" + region[4] + ";" + region[5]);

        //with a platform the owner lands on its corner, looking across their mine. Without one there is
        //nothing to stand on, so the template's own teleport is moved onto the slot instead
        final Location entrance = data.getPlatformWidth() > 0
                ? PrivateMinePlatform.entrance(origin.getWorld(), region, data.getPlatformWidth()) : null;
        cfg.set("teleport", entrance != null
                ? serializeTeleport(entrance) : shiftTeleport(cfg.getString("teleport"), dx, dy, dz, origin));

        //a copy must never inherit the template's sign blocks or its console commands
        cfg.set("signs", new ArrayList<String>());
        cfg.set("reset.commands", new ArrayList<String>());

        //nobody can hold "realmines.<generated name>.break", so leaving this on locks the owner out
        cfg.set(RMineSettings.BREAK_PERMISSION.getConfigKey(), false);

        //otherwise the mine inherits whatever was left of the template's countdown and may reset at once
        cfg.set("reset.time.countdown", cfg.getInt("reset.time.value", 120));

        data.save(cfg);
        return cfg;
    }

    /**
     * Where a copy of this template ends up when it is dropped on a slot, as
     * {minX, minY, minZ, maxX, maxY, maxZ}. Instances are moved so their lowest corner sits on the origin.
     */
    private static int[] instanceRegion(final Location origin, final int[] bounds) {
        return new int[]{
                origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(),
                origin.getBlockX() + (bounds[3] - bounds[0]),
                origin.getBlockY() + (bounds[4] - bounds[1]),
                origin.getBlockZ() + (bounds[5] - bounds[2])};
    }

    private static String serializeTeleport(final Location loc) {
        //block coordinates, because that is all a mine file keeps when it is saved again
        return loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ() + ";"
                + loc.getYaw() + ";" + loc.getPitch();
    }

    private static String shiftTeleport(final String teleport, final int dx, final int dy, final int dz, final Location origin) {
        if (teleport != null) {
            final String[] parts = teleport.split(";");
            if (parts.length == 5) {
                try {
                    return ((int) Math.floor(Double.parseDouble(parts[0])) + dx) + ";"
                            + ((int) Math.floor(Double.parseDouble(parts[1])) + dy) + ";"
                            + ((int) Math.floor(Double.parseDouble(parts[2])) + dz) + ";" + parts[3] + ";" + parts[4];
                } catch (final NumberFormatException ignored) {
                    //fall through to the slot origin below
                }
            }
        }
        return origin.getBlockX() + ";" + origin.getBlockY() + ";" + origin.getBlockZ() + ";0.0;0.0";
    }

    private static void refund(final Player p, final double amount) {
        if (p == null || amount <= 0D) {
            return;
        }
        final Economy econ = RealMinesAPI.getInstance().getEconomy();
        if (econ != null) {
            econ.depositPlayer(p, amount);
        }
    }

    // ------------------------------------------------------------------ release, extend, purge

    @Override
    public void release(final RMine instance) {
        this.teardown(instance, true, null);
    }

    /**
     * Removes a private mine for good: refunds if asked to, empties the region so the slot can be handed
     * out clean, and deletes the mine and its file.
     *
     * @param refund  whether the owner gets {@code Refund-On-Release} of what they paid back
     * @param notice  a message for the owner if they happen to be online, or null for none
     */
    private void teardown(final RMine instance, final boolean refund, final TranslatableLine notice) {
        if (instance == null || !instance.isPrivate()) {
            return;
        }

        final PrivateMineData data = instance.getPrivateData();
        final Player owner = Bukkit.getPlayer(data.getOwner());

        if (refund) {
            final double amount = data.getPaid() * this.refundFraction();
            if (amount > 0D) {
                final Economy econ = RealMinesAPI.getInstance().getEconomy();
                if (econ != null) {
                    econ.depositPlayer(Bukkit.getOfflinePlayer(data.getOwner()), amount);
                    if (owner != null) {
                        TranslatableLine.PRIVATE_MINE_REFUNDED
                                .setV1(TranslatableLine.ReplacableVar.VALUE.eq(String.valueOf(amount))).send(owner);
                    }
                }
            }
        }

        if (notice != null && owner != null) {
            notice.setV1(TranslatableLine.ReplacableVar.MINE.eq(instance.getDisplayName())).send(owner);
        }

        //before anything is taken down: the mine and its walkway are the only ground there is
        this.evacuate(instance, data.getPlatformWidth());

        //deleteMine only clears when a confusingly named option is on, so do it here: whoever gets this
        //slot next must not inherit the previous tenant's blocks
        try {
            if (instance.getMineCuboid() != null) {
                instance.clear();

                //the walkway and its fence are outside the region, so clearing the mine leaves them standing
                if (data.getPlatformWidth() > 0) {
                    final MineCuboid cuboid = instance.getMineCuboid();
                    PrivateMinePlatform.remove(cuboid.getWorld(), new int[]{
                            cuboid.getLowerX(), cuboid.getLowerY(), cuboid.getLowerZ(),
                            cuboid.getUpperX(), cuboid.getUpperY(), cuboid.getUpperZ()},
                            data.getPlatformWidth());
                }
            }
        } catch (final Exception e) {
            this.rm.getLogger().warning("Couldn't clear the region of private mine " + instance.getName() + ": " + e.getMessage());
        }

        final File folder = instance.getConfigFolder();
        this.rm.getMineManager().deleteMine(instance);
        deleteIfEmpty(folder);
    }

    /**
     * Moves anybody standing on a private mine or its platform to the server's default location. Both are
     * about to be taken away, and there is nothing under them in the private mines world.
     * <p>
     * Height is deliberately ignored: somebody flying over a slot that is about to be cleared has just as
     * little to land on as somebody walking it.
     */
    private void evacuate(final RMine instance, final int platformWidth) {
        final MineCuboid cuboid = instance.getMineCuboid();
        if (cuboid == null || cuboid.getWorld() == null) {
            return;
        }

        final Location safe = RMConfig.getDefaultLocation();
        if (safe == null) {
            return;
        }

        final int margin = PrivateMinePlatform.margin(platformWidth);
        for (final Player player : cuboid.getWorld().getPlayers()) {
            final Location at = player.getLocation();
            final boolean inside = at.getBlockX() >= cuboid.getLowerX() - margin
                    && at.getBlockX() <= cuboid.getUpperX() + margin
                    && at.getBlockZ() >= cuboid.getLowerZ() - margin
                    && at.getBlockZ() <= cuboid.getUpperZ() + margin;
            if (inside) {
                player.teleport(safe);
            }
        }
    }

    @Override
    public ClaimResult extend(final Player p, final RMine instance) {
        if (instance == null || !instance.isPrivate()) {
            return ClaimResult.ERROR;
        }

        final PrivateMineData data = instance.getPrivateData();
        if (data.getLifecycle() != PrivateMineData.Lifecycle.TIME_LIMITED) {
            return ClaimResult.ERROR;
        }

        final PrivateMineTemplate template = this.getTemplate(data.getTemplate());
        if (template == null) {
            return ClaimResult.ERROR;
        }

        final double cost = p.hasPermission(ADMIN_PERMISSION) ? 0D : template.getRenewCost();
        if (cost > 0D) {
            final Economy econ = RealMinesAPI.getInstance().getEconomy();
            if (econ == null) {
                return ClaimResult.NO_ECONOMY;
            }
            if (!econ.has(p, cost)) {
                return ClaimResult.INSUFFICIENT_FUNDS;
            }
            if (!econ.withdrawPlayer(p, cost).transactionSuccess()) {
                return ClaimResult.ERROR;
            }
        }

        final long from = Math.max(data.getExpiresAt(), System.currentTimeMillis() / 1000L);
        data.setExpiresAt(from + template.getDuration());
        data.setPaid(data.getPaid() + cost);
        instance.savePrivateData();
        return ClaimResult.OK;
    }

    @Override
    public boolean canUse(final Player p, final RMine instance) {
        if (instance == null || !instance.isPrivate()) {
            return true;
        }
        //explosions come through with no player: never let those chew up someone's mine
        if (p == null) {
            return false;
        }
        final PrivateMineData data = instance.getPrivateData();
        return data.isOwner(p) || data.isTrusted(p.getUniqueId()) || p.hasPermission(ADMIN_PERMISSION);
    }

    @Override
    public void purgeExpired() {
        if (!this.isEnabled()) {
            return;
        }
        for (final RMine mine : this.getPrivateMines()) {
            if (mine.getPrivateData().hasExpired()) {
                this.teardown(mine, false, TranslatableLine.PRIVATE_MINE_EXPIRED);
            }
        }
    }

    @Override
    public void purgeSessionMines(final UUID owner) {
        for (final RMine mine : this.getPrivateMines()) {
            final PrivateMineData data = mine.getPrivateData();
            if (data.getLifecycle() != PrivateMineData.Lifecycle.SESSION) {
                continue;
            }
            if (owner != null && !data.isOwner(owner)) {
                continue;
            }
            this.teardown(mine, false, null);
        }
    }
}
