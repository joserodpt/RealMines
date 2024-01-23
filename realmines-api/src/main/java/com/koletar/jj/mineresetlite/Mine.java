package com.koletar.jj.mineresetlite;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionEffect;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author jjkoletar
 */
public class Mine implements ConfigurationSerializable {
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private World world;
    private Map<SerializableBlock, Double> composition;
    private Set<SerializableBlock> structure; // structure material defining the mine walls, radder, etc.
    private int resetDelay;
    private List<Integer> resetWarnings;
    private List<Integer> resetWarningsLastMinute;
    private String name;
    private SerializableBlock surface;
    private boolean fillMode;
    private int resetClock;
    private boolean isSilent;
    private int tpX = 0;
    private int tpY = -Integer.MAX_VALUE;
    private int tpZ = 0;
    private int tpYaw = 0;
    private int tpPitch = 0;

    // from MineResetLitePlus
    private double resetPercent = -1.0;

    private List<PotionEffect> potions = new ArrayList<>();

    int luckyBlocks = 0;
    List<Integer> luckyNum;
    List<String> luckyCommands;

    private boolean tpAtReset;

    public Mine(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String name, World world) {
        this.name = name;
        redefine(minX, minY, minZ, maxX, maxY, maxZ, world);
        composition = new HashMap<>();
        resetWarnings = new LinkedList<>();
        resetWarningsLastMinute = new LinkedList<>();
        structure = new HashSet<>();
        luckyNum = new ArrayList<>();
        luckyCommands = new ArrayList<>();
    }

    public Mine(Map<String, Object> me) {
        try {
            redefine((Integer) me.get("minX"), (Integer) me.get("minY"), (Integer) me.get("minZ"),
                    (Integer) me.get("maxX"), (Integer) me.get("maxY"), (Integer) me.get("maxZ"),
                    Bukkit.getServer().getWorld((String) me.get("world")));
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error deserializing coordinate pairs");
        }

        if (world == null) {
            Logger l = Bukkit.getLogger();
            l.severe("[MineResetLite] Unable to find a world! Please include these logger lines along with the stack trace when reporting this bug!");
            l.severe("[MineResetLite] Attempted to load world named: " + me.get("world"));
            l.severe("[MineResetLite] Worlds listed: " + Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()));
            throw new IllegalArgumentException("World was null!");
        }
        try {
            Map<String, Double> sComposition = (Map<String, Double>) me.get("composition");
            composition = new HashMap<>();
            for (Map.Entry<String, Double> entry : sComposition.entrySet()) {
                BigDecimal bd = new BigDecimal(entry.getValue());
                bd = bd.setScale(4, RoundingMode.HALF_UP);
                composition.put(new SerializableBlock(entry.getKey()), bd.doubleValue());
            }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error deserializing composition");
        }

        try {
            List<String> sStructure = (List<String>) me.get("structure");
            structure = new HashSet<>();
            for (String entry : sStructure) {
                structure.add(new SerializableBlock(entry));
            }
        } catch (Throwable t) {
            //throw new IllegalArgumentException("Error deserializing structure");
        }

        name = (String) me.get("name");
        resetDelay = (Integer) me.get("resetDelay");
        List<String> warnings = (List<String>) me.get("resetWarnings");
        resetWarnings = new LinkedList<>();
        resetWarningsLastMinute = new LinkedList<>();
        for (String warning : warnings) {
            try {
                if (warning.toLowerCase().endsWith("s")) {
                    warning = warning.toLowerCase().replace("s", "");
                    resetWarningsLastMinute.add(Integer.valueOf(warning));
                } else {
                    resetWarnings.add(Integer.valueOf(warning));
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Non-numeric reset warnings supplied");
            }
        }
        if (me.containsKey("surface")) {
            if (!me.get("surface").equals("")) {
                surface = new SerializableBlock((String) me.get("surface"));
            }
        }
        if (me.containsKey("fillMode")) {
            fillMode = (Boolean) me.get("fillMode");
        }
        if (me.containsKey("resetClock")) {
            resetClock = (Integer) me.get("resetClock");
        }
        //Compat for the clock
        if (resetDelay > 0 && resetClock == 0) {
            resetClock = resetDelay;
        }
        if (me.containsKey("isSilent")) {
            isSilent = (Boolean) me.get("isSilent");
        }
        if (me.containsKey("tpY")) { // Should contain all three if it contains this one
            tpX = (int) me.get("tpX");
            tpY = (int) me.get("tpY");
            tpZ = (int) me.get("tpZ");
        }

        if (me.containsKey("tpYaw")) {
            tpYaw = (int) me.get("tpYaw");
            tpPitch = (int) me.get("tpPitch");
        }

        if (me.containsKey("resetPercent")) {
            resetPercent = (double) me.get("resetPercent");
        }

        tpAtReset = !me.containsKey("tpAtReset") || (boolean) me.get("tpAtReset");
    }

    public Map<String, Object> serialize() {
        Map<String, Object> me = new HashMap<>();
        me.put("minX", minX);
        me.put("minY", minY);
        me.put("minZ", minZ);
        me.put("maxX", maxX);
        me.put("maxY", maxY);
        me.put("maxZ", maxZ);
        me.put("world", world.getName());
        //Make string form of composition
        Map<String, Double> sComposition = new HashMap<>();
        for (Map.Entry<SerializableBlock, Double> entry : composition.entrySet()) {
            sComposition.put(entry.getKey().toString(), entry.getValue());
        }
        me.put("composition", sComposition);
        List<String> sStructure = new ArrayList<>();
        for (SerializableBlock entry : structure) {
            sStructure.add(entry.toString());
        }
        me.put("structure", sStructure);
        me.put("name", name);
        me.put("resetDelay", resetDelay);
        List<String> warnings = new LinkedList<>();
        for (Integer warning : resetWarnings) {
            warnings.add(warning.toString());
        }
        for (Integer warning : resetWarningsLastMinute) {
            warnings.add(warning.toString() + 's');
        }

        me.put("resetWarnings", warnings);
        if (surface != null) {
            me.put("surface", surface.toString());
        } else {
            me.put("surface", "");
        }
        me.put("fillMode", fillMode);
        me.put("resetClock", resetClock);
        me.put("isSilent", isSilent);
        me.put("tpX", tpX);
        me.put("tpY", tpY);
        me.put("tpZ", tpZ);
        me.put("tpYaw", tpYaw);
        me.put("tpPitch", tpPitch);

        me.put("resetPercent", resetPercent);

        Map<String, Integer> potionpairs = new HashMap<>();
        for (PotionEffect pe : this.potions) {
            potionpairs.put(pe.getType().getName(), pe.getAmplifier());
        }
        me.put("potions", potionpairs);

        me.put("lucky_blocks", luckyBlocks);
        me.put("lucky_commands", luckyCommands);

        me.put("tpAtReset", tpAtReset);

        return me;
    }

    public World getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public Map<SerializableBlock, Double> getComposition() {
        return composition;
    }
    public Location getTpPos() {
        return new Location(this.getWorld(), this.tpX, this.tpY, this.tpZ);
    }

    public double getTpY() {
        return this.tpY;
    }

    public Location getMin() {
        return new Location(this.getWorld(), this.minX, this.minY, this.minZ);
    }

    public Location getMax() {
        return new Location(this.getWorld(), this.maxX, this.maxY, this.maxZ);
    }

    public double getResetPercent() {
        return this.resetPercent;
    }

    public void redefine(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, World world) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.world = world;

        computeCenterOfGravity(this.world, this.minX, this.maxX, this.minY, this.maxY, this.minZ, this.maxZ);
    }

    private Location centerOfGravity;
    private Location computeCenterOfGravity(World w, int min_x, int max_x, int min_y, int max_y, int min_z, int max_z) {
        Location max = new Location(w, Math.max(max_x, min_x), max_y, Math.max(max_z, min_z));
        Location min = new Location(w, Math.min(max_x, min_x), min_y, Math.min(max_z, min_z));

        centerOfGravity = max.add(min).multiply(0.5);
        return centerOfGravity;
    }
}