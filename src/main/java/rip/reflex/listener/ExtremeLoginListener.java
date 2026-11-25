package rip.reflex.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class ExtremeLoginListener implements Listener {
   private boolean suspendAllLoginAttempts = true;

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerLogin(PlayerLoginEvent e) {
      if (this.suspendAllLoginAttempts) {
         e.disallow(Result.KICK_OTHER, "The server is still starting up. Please retry in a few seconds.");
      }
   }

   public void setSuspendAllLoginAttempts(boolean suspendAllLoginAttempts) {
      this.suspendAllLoginAttempts = suspendAllLoginAttempts;
   }
}
