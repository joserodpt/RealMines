package joserodpt.realmines.plugin.events;

/*
 *  ______           ____  ____
 *  | ___ \         | |  \/  (_)
 *  | |_/ /___  __ _| | .  . |_ _ __   ___  ___
 *  |    // _ \/ _` | | |\/| | | '_ \ / _ \/ __|
 *  | |\ \  __/ (_| | | |  | | | | | |  __/\__ \
 *  \_| \_\___|\__,_|_\_|  |_/_|_| |_|\___||___/
 *
 * Licensed under the MIT License
 * @author José Rodrigues © 2019-2025
 * @link https://github.com/joserodpt/RealMines
 */

import joserodpt.realmines.api.RealMinesAPI;
import joserodpt.realmines.api.config.TranslatableLine;
import joserodpt.realmines.api.utils.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    private final RealMinesAPI rm;

    public PlayerEvents(RealMinesAPI rm) {
        this.rm = rm;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if ((e.getPlayer().isOp() || e.getPlayer().hasPermission("realmines.update.notify") || e.getPlayer().hasPermission("realmines.admin")) && rm.hasNewUpdate()) {
            Text.send(e.getPlayer(), TranslatableLine.SYSTEM_UPDATE_FOUND.get() + " https://www.spigotmc.org/resources/73707/");
        }
    }
}