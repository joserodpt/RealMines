package josegamerpt.realmines.classes;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.utils.Countdown;

public class MineTimer {

    public Countdown count;
    public Mine m;

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
            if (count.getSecondsLeft() == 30) {
                m.broadcastMessage("&7[&6Warning&7] &r" + m.getDisplayName() + " &fwill reset in &9" + count.getSecondsLeft()
                        + " seconds.");
            }
            if (count.getSecondsLeft() == 20) {
                m.broadcastMessage("&7[&6Warning&7] &r" + m.getDisplayName() + " &fwill reset in &9" + count.getSecondsLeft()
                        + " seconds.");
            }
            if (count.getSecondsLeft() == 10) {
                m.broadcastMessage("&7[&6Warning&7] &r" + m.getDisplayName() + " &fwill reset in &9" + count.getSecondsLeft()
                        + " seconds.");
            }
            if (count.getSecondsLeft() <= 5) {
                m.broadcastMessage("&7[&6Warning&7] &r" + m.getDisplayName() + " &fwill reset in &9" + count.getSecondsLeft()
                        + " seconds.");
            }
        });

        count.scheduleTimer();
    }

    public void kill() {
        if (count != null) {
            count.killTask();
        }
    }

    public void restart() {
        kill();
        start();
    }
}
