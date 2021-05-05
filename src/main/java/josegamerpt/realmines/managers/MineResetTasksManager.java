package josegamerpt.realmines.managers;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.MineResetTasks;
import josegamerpt.realmines.mines.Mine;
import josegamerpt.realmines.mines.MineResetTask;

import java.util.ArrayList;
import java.util.List;

public class MineResetTasksManager {

    private final RealMines rm;
    public ArrayList<MineResetTask> tasks = new ArrayList<>();

    public MineResetTasksManager(RealMines rm) {
        this.rm = rm;
    }

    public void addTask(String name, Integer i) {
        this.tasks.add(new MineResetTask(name, i, true));
    }

    public void loadTasks() {
        for (String s : MineResetTasks.file().getConfigurationSection("").getKeys(false)) {
            int interval = MineResetTasks.file().getInt(s + ".Delay");

            MineResetTask mrt = new MineResetTask(s, interval, false);

            for (String s1 : MineResetTasks.file().getStringList(s + ".LinkedMines")) {
                Mine m = rm.getMineManager().get(s1);
                if (m != null) {
                    mrt.addMine(m);
                }
            }

            this.tasks.add(mrt);
        }
    }

    public MineResetTask getTask(String name) {
        for (MineResetTask task : this.tasks) {
            if (task.getName().equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }

    public List<String> getRegisteredTasks() {
        MineResetTasks.reload();
        ArrayList<String> ret = new ArrayList<>();
        this.tasks.forEach(s -> ret.add(s.getName()));
        return ret;
    }

    public void removeTask(MineResetTask mrt) {
        String name = mrt.getName();
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
