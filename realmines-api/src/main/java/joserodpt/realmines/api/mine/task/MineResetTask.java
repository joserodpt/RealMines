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
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.RPMineResetTasksConfig;
import joserodpt.realmines.api.mine.RMine;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineResetTask {

    private final RealMinesAPI rm;
    private final String name;
    private final int delay;
    private final List<RMine> mines = new ArrayList<>();
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
        RPMineResetTasksConfig.file().set(this.name + ".LinkedMines", this.mines.stream().map(RMine::getName).collect(Collectors.toList()));

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
                MineResetTask.this.mines.forEach(RMine::reset);
            }
        }.runTaskTimer(rm.getPlugin(), 0L, this.delay * 20L);

    }

    public void addMine(final RMine m) {
        this.mines.add(m);
        this.save();
    }

    public void removeMine(final RMine m) {
        this.mines.remove(m);
        this.save();
    }

    public String getName() {
        return this.name;
    }

    public void clearLinks() {
        this.mines.clear();
    }

    public boolean hasMine(final RMine mine) {
        return this.mines.contains(mine);
    }
}
