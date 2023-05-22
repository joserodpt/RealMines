package josegamerpt.realmines.mine.tasks;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Config;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.mine.RMine;
import josegamerpt.realmines.util.Countdown;

public class MineTimer {

    private Countdown count;
    private final RMine m;

    public MineTimer(final RMine mi) {
        this.m = mi;
    }

    public void start() {
        this.startTask(this.m.getResetValue(RMine.Reset.TIME));
    }

    private void startTask(final int s) {
        this.count = new Countdown(RealMines.getPlugin(RealMines.class), s, () -> {
            //
        }, () -> {
            this.m.reset();
            this.startTask(this.m.getResetValue(RMine.Reset.TIME));
        }, (t) -> {
            if (Config.file().getStringList("RealMines.announceTimes") != null && Config.file().getStringList("RealMines.announceTimes").contains(String.valueOf(count.getSecondsLeft()))) {
                this.m.broadcastMessage(Language.file().getString("Mines.Reset.Warning").replaceAll("%mine%", this.m.getDisplayName()).replaceAll("%time%", String.valueOf(count.getSecondsLeft())), false);
            }
        });

        this.count.scheduleTimer();
    }

    public void kill() {
        if (this.count != null)
            this.count.killTask();
    }

    public void restart() {
        this.kill();
        this.start();
    }

    public Countdown getCountdown() {
        return this.count;
    }
}
