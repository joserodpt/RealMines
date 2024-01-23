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
import joserodpt.realmines.api.config.Config;
import joserodpt.realmines.api.config.Language;
import joserodpt.realmines.api.mine.RMine;
import joserodpt.realmines.api.utils.Countdown;

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
        this.count = new Countdown(RealMinesAPI.getInstance().getPlugin(), s, () -> {
            //
        }, () -> {
            this.m.reset();
            this.startTask(this.m.getResetValue(RMine.Reset.TIME));
        }, (t) -> {
            if (Config.file().getStringList("RealMines.announceTimes") != null && Config.file().getStringList("RealMines.announceTimes").contains(String.valueOf(count.getSecondsLeft()))) {
                if (this.m.isSilent()) return;
                this.m.broadcastMessage(Language.file().getString("Mines.Reset.Warning").replaceAll("%mine%", this.m.getDisplayName()).replaceAll("%time%", String.valueOf(count.getSecondsLeft())));
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
