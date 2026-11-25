package rip.reflex.listener.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import rip.reflex.Reflex;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;

public class PlayerDeathListener extends EventListener {
   public PlayerDeathListener(Reflex reflex) {
      super(reflex, "PlayerDeathEvent");
   }

   @Override
   public void onEvent(Event event) {
      PlayerDeathEvent e = (PlayerDeathEvent)event;
      Player p = e.getEntity();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         stats.despawnNpcs();
         stats.despawnFrontEntities();
         stats.resetTimer("deathTracker");
      }
   }
}
