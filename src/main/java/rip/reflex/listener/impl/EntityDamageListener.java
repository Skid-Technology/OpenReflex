package rip.reflex.listener.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import rip.reflex.Reflex;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;

public class EntityDamageListener extends EventListener {
   public EntityDamageListener(Reflex reflex) {
      super(reflex, "EntityDamageEvent");
   }

   @Override
   public void onEvent(Event event) {
      EntityDamageEvent e = (EntityDamageEvent)event;
      if (e.getEntity() instanceof Player) {
         Player p = (Player)e.getEntity();
         Stats stats = Stats.of(p);
         stats.resetTimer("generalDamageTracker");
      }
   }
}
