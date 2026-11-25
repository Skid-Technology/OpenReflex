package rip.reflex.listener.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import rip.reflex.Reflex;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.time.Timer;

public class InventoryOpenListener extends EventListener {
   public InventoryOpenListener(Reflex reflex) {
      super(reflex, "InventoryOpenEvent");
   }

   @Override
   public void onEvent(Event event) {
      InventoryOpenEvent e = (InventoryOpenEvent)event;
      Player p = (Player)e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         stats.setStat("inventoryOpened", true);
         stats.getStat("inventoryOpenTracker", Timer.class).reset();
      }
   }
}
