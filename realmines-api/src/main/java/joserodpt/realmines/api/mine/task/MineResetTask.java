package joserodpt.realmines.api.mine.task;

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
import joserodpt.realmines.api.config.RPMineResetTasksConfig;
import joserodpt.realmines.api.mine.RMine;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MineResetTask {

    private final RealMinesAPI rm;
    private final String name;
    private final int delay;
    //names, looked up on every run: /rm reload replaces every RMine, and holding the objects kept resetting
    //the discarded ones (and, after a delete, the deleted mine's region) until a restart
    private final List<String> mines = new ArrayList<>();
    private BukkitTask task;

    public MineResetTask(final RealMinesAPI rm, final String name, final int delay, final Boolean nova) {
        this.rm = rm;
        this.name = name;
        this.delay = delay;
        this.startTimer();
        if (nova) {
            this.save();
        }
    }

    private void save() {
        RPMineResetTasksConfig.file().set(this.name + ".Delay", this.delay);
        RPMineResetTasksConfig.file().set(this.name + ".LinkedMines", new ArrayList<>(this.mines));

        RPMineResetTasksConfig.save();
    }

    public void stopTimer() {
        if (!this.task.isCancelled()) {
            this.task.cancel();
        }
    }

    public void startTimer() {
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                MineResetTask.this.mines.stream()
                        .map(mineName -> rm.getMineManager().getMine(mineName))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                        .forEach(RMine::reset);
            }
        }.runTaskTimer(rm.getPlugin(), 0L, Math.max(1, this.delay) * 20L);

    }

    public void addMine(final RMine m) {
        if (!this.mines.contains(m.getName())) {
            this.mines.add(m.getName());
        }
        this.save();
    }

    public void removeMine(final RMine m) {
        this.mines.remove(m.getName());
        this.save();
    }

    public String getName() {
        return this.name;
    }

    public void clearLinks() {
        this.mines.clear();
    }

    public boolean hasMine(final RMine mine) {
        return this.mines.contains(mine.getName());
    }
}
