package josegamerpt.realmines.mines;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.MineResetTasks;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class MineResetTask {

    private String name;
    private int delay;
    private List<Mine> mines = new ArrayList<>();
    private BukkitTask task;

    public MineResetTask(String name, int delay, Boolean nova) {
        this.name = name;
        this.delay = delay;
        this.startTimer();
        if (nova) {
            this.save();
        }
    }

    private void save() {
        MineResetTasks.file().set(name + ".Delay", this.delay);

        ArrayList<String> tmp = new ArrayList<>();
        this.mines.forEach(mine -> tmp.add(mine.getName()));
        MineResetTasks.file().set(name + ".LinkedMines", tmp);

        MineResetTasks.save();
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
                mines.forEach(Mine::reset);
            }
        }.runTaskTimer(RealMines.getInstance(), 0L, this.delay * 20L);

    }

    public void addMine(Mine m) {
        this.mines.add(m);
        this.save();
    }

    public void removeMine(Mine m) {
        this.mines.remove(m);
        this.save();
    }

    public String getName() {
        return this.name;
    }

    public void clearLinks() {
        this.mines.clear();
    }

    public boolean hasMine(Mine mine) {
        return this.mines.contains(mine);
    }
}
