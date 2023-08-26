package joserodpt.realmines.event;

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

import joserodpt.realmines.RealMines;
import joserodpt.realmines.config.Language;
import joserodpt.realmines.util.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    private final RealMines rm;
    public PlayerEvents(RealMines rm) {
        this.rm = rm;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if ((e.getPlayer().isOp() || e.getPlayer().hasPermission("realmines.update.notify") || e.getPlayer().hasPermission("realmines.admin")) && rm.hasNewUpdate()) {
            Text.send(e.getPlayer(), Language.file().getString("System.Update-Found") + " https://www.spigotmc.org/resources/realmines-1-14-to-1-20-1.73707/");
        }
    }
}