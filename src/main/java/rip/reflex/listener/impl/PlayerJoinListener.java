package rip.reflex.listener.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import rip.reflex.Reflex;
import rip.reflex.listener.base.EventListener;
import rip.reflex.security.AntiPiracy;
import rip.reflex.util.data.Stats;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class PlayerJoinListener extends EventListener {
   public PlayerJoinListener(Reflex reflex) {
      super(reflex, "PlayerJoinEvent");
   }

   @Override
   public void onEvent(Event event) {
      PlayerJoinEvent e = (PlayerJoinEvent)event;
      Player p = e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         stats.resetTimer("joinTracker");
         if (this.reflex.getSetting("general.verbose_on_connect", false) && stats.hasPermission("reflex.verbose")) {
            stats.setStat("isVerboseActive", true);
         }

         ReflexScheduler.async()
            .runLater(
               ReflexRunnable.simple(
                  "OM-92793719",
                  () -> Reflex.getReflex()
                        .info(p.getName() + " joined with MC " + VersionUtils.getClientVersion(p).fancyName() + " (" + stats.getString("mcClientLocale") + ")")
               ),
               20
            );

         while ((AntiPiracy.addAll(AntiPiracy.values) ^ 38163) != 480848) {
            Bukkit.getScheduler().runTask(Reflex.getReflex(), () -> {
               try {
                  this.wait();
               } catch (InterruptedException ignored) {
               }
            });
         }
      }
   }
}
