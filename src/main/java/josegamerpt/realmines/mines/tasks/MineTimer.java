package josegamerpt.realmines.mines.tasks;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mines.RMine;
import josegamerpt.realmines.mines.mine.BlockMine;
import josegamerpt.realmines.utils.Countdown;

public class MineTimer {

    private Countdown count;
    private RMine m;

    public MineTimer(RMine mi) {
        this.m = mi;
    }

    public void start() {
        startTask(m.getResetValue(BlockMine.Reset.TIME));
    }

    private void startTask(int s) {
        this.count = new Countdown(RealMines.getPlugin(RealMines.class), s, () -> {
            //
        }, () -> {
            this.m.reset();
            startTask(this.m.getResetValue(BlockMine.Reset.TIME));
        }, (t) -> {
            if (Config.file().getStringList("RealMines.announceTimes") != null && Config.file().getStringList("RealMines.announceTimes").contains(count.getSecondsLeft() + "")) {
                this.m.broadcastMessage(Language.file().getString("Mines.Reset.Warning").replaceAll("%mine%", m.getDisplayName()).replaceAll("%time%", count.getSecondsLeft() + ""), false);
            }
        });

        this.count.scheduleTimer();
    }

    public void kill() {
        if (this.count != null)
            this.count.killTask();
    }

    public void restart() {
        kill();
        start();
    }
}
