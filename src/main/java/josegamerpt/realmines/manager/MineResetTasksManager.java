package josegamerpt.realmines.manager;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.MineResetTasks;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.mine.task.MineResetTask;

import java.util.ArrayList;
import java.util.List;

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
        for (final MineResetTask task : this.tasks) {
            if (task.getName().equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }

    public List<String> getRegisteredTasks() {
        MineResetTasks.reload();
        final ArrayList<String> ret = new ArrayList<>();
        this.tasks.forEach(s -> ret.add(s.getName()));
        return ret;
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
