package josegamerpt.realmines.manager;

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

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.MineResetTasks;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.mine.task.MineResetTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MineResetTasksManager {

    private final RealMines rm;
    public ArrayList<MineResetTask> tasks = new ArrayList<>();

    public MineResetTasksManager(final RealMines rm) {
        this.rm = rm;
    }

    public void addTask(final String name, final Integer i) {
        this.tasks.add(new MineResetTask(name, i, true));
    }

    public void loadTasks() {
        for (final String s : MineResetTasks.file().getConfigurationSection("").getKeys(false)) {
            final int interval = MineResetTasks.file().getInt(s + ".Delay");

            final MineResetTask mrt = new MineResetTask(s, interval, false);

            for (final String s1 : MineResetTasks.file().getStringList(s + ".LinkedMines")) {
                final RMine m = this.rm.getMineManager().get(s1);
                if (m != null) {
                    mrt.addMine(m);
                }
            }

            this.tasks.add(mrt);
        }
    }

    public MineResetTask getTask(final String name) {
        return this.tasks.stream()
                .filter(task -> task.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<String> getRegisteredTasks() {
        MineResetTasks.reload();
        return this.tasks.stream()
                .map(MineResetTask::getName)
                .collect(Collectors.toList());
    }

    public void removeTask(final MineResetTask mrt) {
        final String name = mrt.getName();
        mrt.stopTimer();
        mrt.clearLinks();
        this.tasks.remove(mrt);
        MineResetTasks.file().set(name, null);
        MineResetTasks.save();
    }

    public ArrayList<MineResetTask> getTasks() {
        return this.tasks;
    }
}
