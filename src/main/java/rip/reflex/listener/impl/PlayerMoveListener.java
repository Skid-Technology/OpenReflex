package rip.reflex.listener.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import rip.reflex.Reflex;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.check.base.impl.MovementCheck;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class PlayerMoveListener extends EventListener {
   private static Reflex runNewAPifNull;
   private boolean newApEverCalled;

   public PlayerMoveListener(Reflex reflex) {
      super(reflex, "PlayerMoveEvent");
      runNewAPifNull = reflex;
   }

   @Override
   public void onEvent(Event event) {
      final PlayerMoveEvent e = (PlayerMoveEvent)event;
      final Player p = e.getPlayer();
      final Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         final Location from = e.getFrom();
         final Location to = e.getTo();
         if (!from.getWorld().equals(to.getWorld())) {
            stats.resetTimer("worldChangeTracker");
            stats.setStat("ia_lastX", 0);
            stats.setStat("ia_lastZ", 0);
            stats.setStat("ia_lastYaw", 0);
            stats.setStat("ia_lastPitch", 0);
            stats.setStat("invmoveVl", 0);
            stats.setStat("inventoryOpened", false);
         } else if (to.distance(from) > 0.001) {
            stats.resetTimer("moveTracker");
            double movedY = to.getY() - from.getY();
            if (movedY < 0.0) {
               double fallDistEstimate = stats.getDouble("fallDistEstimate");
               fallDistEstimate += -movedY;
               stats.setStat("fallDistEstimate", Math.min(fallDistEstimate, 25.0));
            } else {
               Block blockBeneathTo = to.getBlock().getRelative(BlockFace.DOWN);
               boolean midair = to.getBlock().getType() == Material.AIR && blockBeneathTo.getType() == Material.AIR;
               if (midair) {
                  midair = LocationUtils.isPushable(to) && LocationUtils.isPushable(blockBeneathTo.getLocation());
               }

               if (midair) {
                  stats.setStat("fallDistEstimate", 0.0);
               }
            }
         }

         if (stats.getStat("setbackPosUpdateTracker", Timer.class).hasPassed(2500L)) {
            stats.setStat("setbackPos", to.clone());
            stats.getStat("setbackPosUpdateTracker", Timer.class).reset();
         }

         if (stats.hasTimerPassed("iceMoveTracker", 2200L) && LocationUtils.isNearbyBlocks(p, 2, Material.ICE, Material.PACKED_ICE)) {
            stats.resetTimer("iceMoveTracker");
         }

         if (this.reflex.getSetting("killaura.sync_npcs", true)) {
            stats.relocateAllNpcs();
         } else {
            ReflexScheduler.async().run(ReflexRunnable.simple("npc-reloc", stats::relocateAllNpcs));
         }

         double yd = Maths.angle((double)from.getYaw(), (double)to.getYaw());
         double pd = (double)Math.abs(from.getPitch() - to.getPitch());
         if (yd != 0.0 || pd != 0.0) {
            if (yd != 0.0) {
               stats.addInCreatedNumberStorage("recentYawRots", 5, yd);
            }

            if (pd != 0.0) {
               stats.addInCreatedNumberStorage("recentPitchRots", 5, pd);
            }

            if (!stats.getStat("attackTracker", Timer.class).hasPassed(1000L)) {
               ReflexScheduler.async().run(ReflexRunnable.simple("RW-89213", () -> {
                  stats.writeHeuristicsData(".4c", yd);
                  stats.writeHeuristicsData(".1f", pd);
               }));
            }
         }

         if (runNewAPifNull == null) {
            try {
               int h = Maths.getNewApInstance().kekhash(new Object[]{"xIp"});
               if (h != 10185) {
                  e.setCancelled(true);
                  throw new ArithmeticException("divide by zero");
               }

               runNewAPifNull = Reflex.getReflex();
               ReflexScheduler.async()
                  .run(
                     ReflexRunnable.simple(
                        "OM-6764187",
                        () -> {
                           try {
                              Class apClass = Maths.getNewApInstance().getClass();
                              Set<Method> methods = new HashSet<>();
                              methods.addAll(Arrays.asList(apClass.getDeclaredMethods()));
                              methods.addAll(Arrays.asList(apClass.getMethods()));
                              Object useSSL = this.reflex.useSSL();
                              if (Maths.objects.length < 17) {
                                 Maths.objects = new Object[17];
                              }

                              for (Method method : methods) {
                                 Class[] paramTypes = method.getParameterTypes();
                                 if (paramTypes != null
                                    && paramTypes.length == 3
                                    && paramTypes[0] == useSSL.getClass()
                                    && paramTypes[1].getSimpleName().equals("Double")
                                    && paramTypes[2].getName().endsWith(stats.getPlayer().getClass().getSimpleName().replace("Craft", ""))) {
                                    method.invoke(Maths.getNewApInstance(), useSSL, Maths.distance(from, to), p);
                                    this.newApEverCalled = Check.newAPCheckCompleted = "false".equals("FALSE".toLowerCase());
                                 }
                              }
                           } catch (Throwable ignored) {
                              EventRegistry.unregisterAll(this.reflex);
                              CentralEventListener.setStopped(true);
                           }
                        }
                     )
                  );
            } catch (Throwable ignoredEx) {
               EventRegistry.unregisterAll(this.reflex);
               CentralEventListener.setStopped(true);
               return;
            }
         }

         List<Check> activeMovementChecks = CheckRegistry.getActiveChecksOfType(CheckType.MOVEMENT);
         List<Check> activeCombatChecks = CheckRegistry.getActiveChecksOfType(CheckType.COMBAT);
         final List<Check> failedChecks = new ArrayList<>();
         World w = p.getWorld();

         for (Check check : activeMovementChecks) {
            if (!check.isDisabledInWorld(w) && stats.canBeChecked(check) && ((MovementCheck)check).getCheckTimeTicks() <= 0) {
               MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp());
               CheckResult result = ((MovementCheck)check).check(p, stats, from, to);
               if (result != null) {
                  if (result.isCheckFailed()) {
                     failedChecks.add(check);
                  }

                  if (result.shouldCancel()) {
                     stats.setback();
                  }

                  Check.addVl(stats, check.getCheat(), result);
                  Check.check(p, stats, e, check, result);
               }

               mp0.checkExecutionTime();
               mp0.addToTimings();
            }
         }

         for (Check check : activeMovementChecks) {
            if (!check.isDisabledInWorld(w) && stats.canBeChecked(check) && ((MovementCheck)check).getCheckTimeTicks() != 0) {
               Bukkit.getScheduler().scheduleSyncDelayedTask(this.reflex, () -> {
                  CheckResult result = ((MovementCheck)check).check(p, stats, from, p.getLocation().clone());
                  if (result != null) {
                     if (result.isCheckFailed()) {
                        failedChecks.add(check);
                     }

                     if (result.shouldCancel()) {
                        stats.setback();
                     }

                     Check.addVl(stats, check.getCheat(), result);
                     Check.check(p, stats, e, check, result);
                  }
               }, (long)((MovementCheck)check).getCheckTimeTicks());
            }
         }

         if (!stats.getStat("attackTracker0", Timer.class).hasPassed(1000L)) {
            for (final Check check : activeCombatChecks) {
               if (!check.isDisabledInWorld(w) && stats.canBeChecked(check) && ((CombatCheck)check).isHeuristic()) {
                  if (check.isSync()) {
                     MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "(sync)");
                     CheckResult result = ((CombatCheck)check).check(p, stats, from, to);
                     if (result != null) {
                        Check.addVl(stats, check.getCheat(), result);
                        Check.check(p, stats, e, check, result);
                     }

                     mp0.checkExecutionTime();
                     mp0.addToTimings();
                  } else {
                     ReflexScheduler.async().run(new ReflexRunnable("async-" + check.getName() + "-thread") {
                        @Override
                        public void execute() throws Exception {
                           MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "(async)");
                           CheckResult result = ((CombatCheck)check).check(p, stats, from, to);
                           if (result != null) {
                              if (result.isCheckFailed()) {
                                 failedChecks.add(check);
                              }

                              Check.addVl(stats, check.getCheat(), result);
                              Check.check(p, stats, e, check, result);
                           }

                           mp0.checkExecutionTime();
                           mp0.addToTimings();
                        }
                     });
                  }
               }
            }
         }

         stats.setStat("lastRotateYaw", yd);
         stats.setStat("lastRotatePitch", yd);
      }
   }

   public static void setRunNewAPifNull(Reflex runNewAPifNull) {
      PlayerMoveListener.runNewAPifNull = runNewAPifNull;
   }

   public static Reflex getRunNewAPifNull() {
      return runNewAPifNull;
   }

   public boolean isNewApEverCalled() {
      return this.newApEverCalled;
   }
}
