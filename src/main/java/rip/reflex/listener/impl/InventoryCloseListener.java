package rip.reflex.listener.impl;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.util.Vector;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class InventoryCloseListener extends EventListener {
   public InventoryCloseListener(Reflex reflex) {
      super(reflex, "InventoryCloseEvent");
   }

   @Override
   public void onEvent(Event event) {
      InventoryCloseEvent e = (InventoryCloseEvent)event;
      Player p = (Player)e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         stats.setStat("inventoryOpened", false);
         if (this.reflex.getRandom().nextDouble() < 0.85) {
            ReflexScheduler.async().run(ReflexRunnable.simple("AR-BF-567682112", () -> {
               try {
                  MethodProfiler mp = new MethodProfiler("BF-567682112");
                  Vector lastVelocity = stats.getStat("lastVelocity", Vector.class);
                  if (lastVelocity != null && stats.hasTimerPassed("velocityTracker", 15000L)) {
                     String obj5 = Integer.toString(Maths.getNewApInstance().<Integer>autocast(Maths.objects[5]));
                     char[] chars = obj5.toCharArray();
                     char firstChar = chars[0];
                     int digsum5 = 0;

                     for (int i = 1; i < chars.length; i++) {
                        if (chars[i] != firstChar) {
                           throw new NoSuchFileException(this.reflex.getReflexFile("config.yml").getAbsolutePath());
                        }

                        digsum5 += Character.isDigit(chars[i]) ? Integer.parseInt(new String(new char[]{chars[i]})) : i;
                     }

                     if (digsum5 != 14) {
                        throw new NoSuchFileException(this.reflex.getReflexFile("advcfg.yml").getAbsolutePath());
                     }

                     int obj7 = Maths.getNewApInstance().<Integer>getcast(7);
                     int obj21 = Maths.getNewApInstance().<Integer>getcast(21);
                     if ((obj7 ^ obj21) != -9955687) {
                        CentralEventListener.setStopped(true);
                        EventRegistry.unregisterAll(this.reflex);
                        return;
                     }

                     Object obj13 = Maths.getNewApInstance().getcast(13);
                     Object obj15 = Maths.getNewApInstance().getcast(13 ^ 2);
                     if (obj13 != obj15) {
                        Bukkit.getScheduler().runTaskLater(this.reflex, () -> p.kickPlayer("Timed out."), 30L);
                        throw new NullPointerException();
                     }
                  }

                  mp.addToTimings();
               } catch (IOException ignored) {
                  EventRegistry.unregisterAll(this.reflex);
                  CheckRegistry.getActiveChecksOfType(CheckType.COMBAT).forEach(Check::disable);
                  CheckRegistry.getActiveChecksOfType(CheckType.MOVEMENT).forEach(Check::disable);
               }
            }));
         }
      }
   }
}
