package rip.reflex.listener.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.reflex.Reflex;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;

public class PlayerTeleportListener extends EventListener {
   public PlayerTeleportListener(Reflex reflex) {
      super(reflex, "PlayerTeleportEvent");
   }

   @Override
   public void onEvent(Event event) {
      PlayerTeleportEvent e = (PlayerTeleportEvent)event;
      Player p = e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         if (stats.hasStat("lp_tickDelays")) {
            stats.getNumberStorage("lp_tickDelays").removeLast();
         }

         stats.resetTimer("teleportTracker");
      }
   }
}
