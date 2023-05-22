package josegamerpt.realmines.event;

import josegamerpt.realmines.RealMines;
import josegamerpt.realmines.config.Language;
import josegamerpt.realmines.util.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && RealMines.getInstance().newUpdate) {
            Text.send(e.getPlayer(), Language.file().getString("System.Update-Found"));
        }
    }
}