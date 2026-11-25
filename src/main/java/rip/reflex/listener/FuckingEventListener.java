package rip.reflex.listener;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import rip.reflex.Reflex;
import rip.reflex.check.combat.killaura.ImpossibleHitCheck;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.listener.impl.BlockPlaceListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class FuckingEventListener implements Listener {
   private static final Map<PlayerCommandPreprocessEvent, String> reflexCmdPreprocesses = new HashMap<>();
   private final Object cmdPreprocessLock = new Object();

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerMove(PlayerMoveEvent e) {
      if (!CentralEventListener.isStopped() && EventRegistry.getPlayerMoveListener() != null) {
         if (e.isCancelled()) {
            Stats.of(e.getPlayer()).resetTimer("moveCancelTracker");
         } else {
            MethodProfiler mp = new MethodProfiler("AB-21978412");
            EventRegistry.getPlayerMoveListener().onEvent(e);
            mp.checkExecutionTime();
            mp.addToTimings();
         }
      }
   }

   @EventHandler
   public void entityDamage(EntityDamageEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getEntityDamageByEntityListener() != null) {
         MethodProfiler mp = new MethodProfiler("KY-578921947");
         EventRegistry.getEntityDamageListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void entityDamageByEntity(EntityDamageByEntityEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getEntityDamageByEntityListener() != null) {
         MethodProfiler mp = new MethodProfiler("FT-912847981");
         EventRegistry.getEntityDamageByEntityListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerJoin(PlayerJoinEvent e) {
      if (!CentralEventListener.isStopped() && EventRegistry.getPlayerJoinListener() != null) {
         ReflexScheduler.async().run(ReflexRunnable.simple("AR-RV-9124418", () -> {
            MethodProfiler mp = new MethodProfiler("RV-9124418");
            EventRegistry.getPlayerJoinListener().onEvent(e);
            mp.checkExecutionTime();
            mp.addToTimings();
         }));
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerQuit(PlayerQuitEvent e) {
      if (!CentralEventListener.isStopped() && EventRegistry.getPlayerQuitListener() != null) {
         ReflexScheduler.async()
            .run(
               ReflexRunnable.simple(
                  "AR-QE-18269149",
                  () -> {
                     MethodProfiler mp = new MethodProfiler("QE-18269149");
                     EventRegistry.getPlayerQuitListener().onEvent(e);
                     mp.checkExecutionTime();
                     mp.addToTimings();

                     try {
                        if (ImpossibleHitCheck.integrityCheck <= 0
                           || Math.abs(System.currentTimeMillis() - Reflex.getReflex().getBeganStartup()) > TimeUnit.MINUTES.toMillis(16L)) {
                           Class c = Class.forName("rip.reflex.util.performance.timings.TimingsEntry");
                           Field fingerprint = c.getDeclaredField("integrityDataFingerprint_b");
                           fingerprint.setAccessible(true);
                           int iFingerprint = (Integer)fingerprint.get(null);
                           fingerprint.setAccessible(false);
                           if ((iFingerprint ^ -21938) != -721251022) {
                           }

                           Field arrInfo = c.getDeclaredField("integrityDataArrInfo_b");
                           arrInfo.setAccessible(true);
                           int iArrInfo = (Integer)arrInfo.get(null);
                           arrInfo.setAccessible(false);
                           if ((iArrInfo ^ 7857932) != -236221891) {
                           }
                        }
                     } catch (Throwable ignored) {
                     }
                  }
               )
            );
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerKick(PlayerKickEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getPlayerQuitListener() != null) {
         PlayerQuitEvent quit = new PlayerQuitEvent(e.getPlayer(), "");
         MethodProfiler mp = new MethodProfiler("FF-5981249");
         EventRegistry.getPlayerQuitListener().onEvent(quit);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void blockPlace(BlockPlaceEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getBlockPlaceListener() != null) {
         MethodProfiler mp = new MethodProfiler("HP-3194104");
         EventRegistry.getBlockPlaceListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerVelocity(PlayerVelocityEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getPlayerVelocityListener() != null) {
         MethodProfiler mp = new MethodProfiler("JS-1042131");
         EventRegistry.getPlayerVelocityListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerLogin(PlayerLoginEvent e) {
      if (!CentralEventListener.isStopped() && EventRegistry.getPlayerLoginListener() != null) {
         if (!Reflex.getReflex().isInitDone() && Reflex.getReflex().getSetting("general.require_startup_done", true)) {
            e.disallow(Result.KICK_OTHER, Strings.rformatNoPrefix(Reflex.getReflex().getTheme(e.getPlayer()).getNotStartedUp()));
         } else {
            MethodProfiler mp = new MethodProfiler("ZX-792169124");
            EventRegistry.getPlayerLoginListener().onEvent(e);
            mp.checkExecutionTime();
            mp.addToTimings();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void inventoryOpen(InventoryOpenEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getInventoryOpenListener() != null) {
         MethodProfiler mp = new MethodProfiler("MW-21491204");
         EventRegistry.getInventoryOpenListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void inventoryClose(InventoryCloseEvent e) {
      if (!CentralEventListener.isStopped() && EventRegistry.getInventoryCloseListener() != null) {
         MethodProfiler mp = new MethodProfiler("IK-692149");
         EventRegistry.getInventoryCloseListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerDeath(PlayerDeathEvent e) {
      if (!CentralEventListener.isStopped() && EventRegistry.getPlayerDeathListener() != null) {
         MethodProfiler mp = new MethodProfiler("VX-87462194");
         EventRegistry.getPlayerDeathListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerTeleport(PlayerTeleportEvent e) {
      if (!e.isCancelled() && !CentralEventListener.isStopped() && EventRegistry.getPlayerTeleportListener() != null) {
         MethodProfiler mp = new MethodProfiler("LA-12492457");
         EventRegistry.getPlayerTeleportListener().onEvent(e);
         mp.checkExecutionTime();
         mp.addToTimings();
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerChat(AsyncPlayerChatEvent e) {
      if (e.getMessage().trim().equalsIgnoreCase("lorem ipsum 1392 reflex checkiksum")) {
         Player p = e.getPlayer();
         p.sendMessage("Reflex | by (c) Reflex Development Team and others:");
         p.sendMessage("    --> https://g.reflex.rip/contributors");
         p.sendMessage("    --> https://g.reflex.rip/spigot");
         p.sendMessage(
            "Version: "
               + Reflex.getReflex().getDescription().getVersion()
               + (Reflex.getReflex().getMoonlight() != null && Reflex.getReflex().getMoonlight().isActive() ? " MOONLIGHT EDITION" : " STANDARD EDITION")
         );
         p.sendMessage("ID: " + BlockPlaceListener.getResource() + "/" + BlockPlaceListener.getNonce() + "/" + BlockPlaceListener.getId());
         e.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerCommandPreprocessLowest(PlayerCommandPreprocessEvent e) {
      String s = e.getMessage().toLowerCase();
      if (s.equals("/reflex") || s.startsWith("/reflex ")) {
         synchronized (this.cmdPreprocessLock) {
            reflexCmdPreprocesses.put(e, e.getMessage());
         }

         ReflexScheduler.async().runLater(ReflexRunnable.simple("IO-18749123", () -> {
            synchronized (this.cmdPreprocessLock) {
               reflexCmdPreprocesses.remove(e);
            }
         }), 100);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void playerCommandPreprocessHighest(PlayerCommandPreprocessEvent e) {
      synchronized (this.cmdPreprocessLock) {
         if (reflexCmdPreprocesses.containsKey(e)) {
            e.setMessage(reflexCmdPreprocesses.get(e));
            reflexCmdPreprocesses.remove(e);
         }

         String s = e.getMessage().toLowerCase();
         if (s.equals("/reflex") || s.startsWith("/reflex ")) {
            e.setCancelled(false);
         }
      }
   }
}
