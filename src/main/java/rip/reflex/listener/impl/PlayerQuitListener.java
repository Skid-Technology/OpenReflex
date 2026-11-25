package rip.reflex.listener.impl;

import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.combat.killaura.ImpossibleHitCheck;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.reflex.SubCommandPacketdebug;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.util.appearance.ThemeManager;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class PlayerQuitListener extends EventListener {
   public PlayerQuitListener(Reflex reflex) {
      super(reflex, "PlayerQuitEvent");
   }

   @Override
   public void onEvent(Event event) {
      PlayerQuitEvent e = (PlayerQuitEvent)event;
      Player p = e.getPlayer();
      Stats.reset(p);
      ReflexScheduler.async()
         .run(
            ReflexRunnable.simple(
               "AsyncQuit/" + p.getName(),
               () -> {
                  MethodProfiler mp = new MethodProfiler("WB-69871927");
                  if ((
                        ImpossibleHitCheck.integrityCheck <= 0
                           || Math.abs(System.currentTimeMillis() - this.reflex.getBeganStartup()) > TimeUnit.MINUTES.toMillis(16L)
                     )
                     && (CommandConditions.intcheck() ^ 67214878) != -101192492) {
                  }

                  SubCommandPacketdebug.dumpTo(SubCommandPacketdebug.getDebugger(p), p);
                  VersionUtils.uncache(p);
                  if (Maths.objects != null && Maths.objects.length >= 1) {
                     try {
                        if ((Maths.objects[0] != null || Maths.objects[3] instanceof Maths || Maths.objects.length > 22 && Maths.objects[22] != null)
                           && (
                              !((PlayerMoveListener)EventRegistry.getPlayerMoveListener()).isNewApEverCalled()
                                 || !((PlayerVelocityListener)EventRegistry.getPlayerVelocityListener()).isNewAPEverRan()
                           )) {
                           CentralEventListener.setStopped(true);
                           CheckRegistry.getActiveChecksOfType(CheckType.PACKET).forEach(Check::disable);
                           Maths.objects[0] = "Reflex";
                        }
                     } catch (Throwable ignored) {
                        Bukkit.getPluginManager().disablePlugin(this.reflex);
                     }
                  } else {
                     EventRegistry.unregisterAll(Reflex.getReflex());
                     this.reflex.offlineMode = true;
                  }

                  ThemeManager.getPersonalThemes().remove(p);
                  ThemeManager.getPersonalRawThemes().remove(p);
                  mp.addToTimings();
               }
            )
         );
   }
}
