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
 * @author José Rodrigues © 2019-2024
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.config.RPMineResetTasksConfig;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.mine.task.MineResetTask;
import joserodpt.realmines.api.managers.MineResetTasksManagerAPI;
import joserodpt.realmines.plugin.RealMines;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineResetTasksManager extends MineResetTasksManagerAPI {

    private final RealMines rm;
    public List<MineResetTask> tasks = new ArrayList<>();

    public MineResetTasksManager(final RealMines rm) {
        this.rm = rm;
    }

    @Override
    public void addTask(final String name, final Integer i) {
        this.tasks.add(new MineResetTask(rm, name, i, true));
    }

    @Override
    public void loadTasks() {
        if (RPMineResetTasksConfig.file().isSection("")) {
            for (final String s : RPMineResetTasksConfig.file().getSection("").getRoutesAsStrings(false)) {
                final int interval = RPMineResetTasksConfig.file().getInt(s + ".Delay");

                final MineResetTask mrt = new MineResetTask(rm, s, interval, false);

                for (final String s1 : RPMineResetTasksConfig.file().getStringList(s + ".LinkedMines")) {
                    final RMine m = this.rm.getMineManager().getMine(s1);
                    if (m != null) {
                        mrt.addMine(m);
                    }
                }

                this.tasks.add(mrt);
            }
        }
    }

    @Override
    public MineResetTask getTask(final String name) {
        return this.tasks.stream()
                .filter(task -> task.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getRegisteredTasks() {
        RPMineResetTasksConfig.reload();
        return this.tasks.stream()
                .map(MineResetTask::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void removeTask(final MineResetTask mrt) {
        final String name = mrt.getName();
        mrt.stopTimer();
        mrt.clearLinks();
        this.tasks.remove(mrt);
        RPMineResetTasksConfig.file().set(name, null);
        RPMineResetTasksConfig.save();
    }

    @Override
    public List<MineResetTask> getTasks() {
        return this.tasks;
    }
}
