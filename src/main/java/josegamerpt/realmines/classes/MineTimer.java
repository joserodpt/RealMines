package josegamerpt.realmines.classes;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.utils.Countdown;

public class MineTimer {

    private Countdown count;
    private Mine m;

    public MineTimer(Mine mi) {
        this.m = mi;
    }

    public void start() {
        startTask(m.getResetValue(Mine.Reset.TIME));
    }

    private void startTask(int s) {
        count = new Countdown(RealMines.getPlugin(RealMines.class), s, () -> {
            //
        }, () -> {
            m.reset();
            startTask(this.m.getResetValue(Mine.Reset.TIME));
        }, (t) -> {
            if (Config.file().getStringList("RealMines.announceTimes") != null && Config.file().getStringList("RealMines.announceTimes").contains(count.getSecondsLeft() + "")) {
                m.broadcastMessage(Language.file().getString("Mines.Reset.Warning").replaceAll("%mine%", m.getDisplayName()).replaceAll("%time%", count.getSecondsLeft() + ""), false);
            }
        });

        count.scheduleTimer();
    }

    public void kill() {
        if (count != null)
            count.killTask();
    }

    public void restart() {
        kill();
        start();
    }
}
