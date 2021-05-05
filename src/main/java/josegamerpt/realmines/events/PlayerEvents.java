package josegamerpt.realmines.events;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.utils.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && RealMines.getInstance().newUpdate) {
            Text.send(e.getPlayer(), "&6&lWARNING! &fThere is a new version of RealMines!");
        }
    }

}