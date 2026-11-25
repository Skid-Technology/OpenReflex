package rip.reflex.check.combat.antiknockback;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.CustomCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class ActiveVelocityCheck extends CustomCheck {
   private double optMaxOffset;
   private int optThreshold;
   private int optSuspicionLifespan;
   private int optFlagDelay;

   public ActiveVelocityCheck() {
      super("AntiKnockback", "vel.act", Cheat.valueOf("AntiKnockback"));
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMaxOffset = this.getAdvancedLocalSetting("max_offset", Double.valueOf(0.024));
      this.optThreshold = this.getAdvancedLocalSetting("threshold", Integer.valueOf(2));
      this.optSuspicionLifespan = this.getAdvancedLocalSetting("suspicion_lifespan", Integer.valueOf(30000));
      this.optFlagDelay = this.getAdvancedLocalSetting("flag_delay", Integer.valueOf(100));
   }

   public void check(Player p, Stats stats, Inventory inv) {
      MethodProfiler mp = new MethodProfiler("OP-18057912");
      if (!this.isDisabled()
         && !this.isCompDisabled()
         && !this.isDisabledInWorld(p.getLocation().getWorld())
         && this.isEnabledInConfig()
         && stats.canBeChecked(this)
         && stats.hasTimerPassed("actVelCheckTracker", 1000L)) {
         Vector currentVelocity = p.getVelocity().clone();
         if (p.isOnGround() && (LocationUtils.isZero(currentVelocity) || LocationUtils.isNaturalGravity(currentVelocity))) {
            Location oldLoc = p.getLocation().clone();
            List<Block> standingOn = LocationUtils.playerStandsOn(oldLoc, oldLoc);
            if (!standingOn.isEmpty()) {
               for (Block block : standingOn) {
                  if (block.getType().toString().toLowerCase().contains("ice") || !LocationUtils.isNormalBlock(block)) {
                     return;
                  }
               }

               if (!LocationUtils.isNearWalls(p)) {
                  boolean reverse = stats.getBoolean("actVelReverse");
                  stats.setStat("actVelReverse", !reverse);
                  double vx = 0.0175 + Math.random() * 0.008;
                  double vz = 0.0175 + Math.random() * 0.008;
                  if (reverse) {
                     vx = -vx;
                     vz = -vz;
                  }

                  Vector velocity = new Vector(vx, 0.0, vz);
                  stats.setStat("actVelVelocity", velocity);
                  stats.setStat("actVelStartLoc", oldLoc);
                  stats.resetTimer("actVelCheckTracker");
                  stats.setStat("actVelCheckInv", inv);
                  p.setVelocity(velocity);
                  mp.addToTimings();
                  ReflexScheduler.async()
                     .runLater(
                        ReflexRunnable.simple(
                           "OA-9561251941",
                           () -> {
                              MethodProfiler mp1 = new MethodProfiler("CL-782814015");
                              Inventory invNow = stats.getStat("actVelCheckInv", Inventory.class);
                              stats.removeStat("actVelCheckInv");
                              if (stats.hasTimerPassed("velocityTracker", 300L)
                                 && stats.isAnyInvOpen()
                                 && invNow == inv
                                 && stats.hasTimerPassed("teleportTracker", 2000L)) {
                                 Location newLoc = p.getLocation().clone();
                                 if (newLoc.getWorld().equals(oldLoc.getWorld())) {
                                    Vector moveVector = newLoc.toVector().subtract(oldLoc.toVector());
                                    double offsetX = Math.abs(moveVector.getX() - velocity.getX());
                                    double offsetZ = Math.abs(moveVector.getZ() - velocity.getZ());
                                    CheckResult result = new CheckResult();
                                    int vl = 0;
                                    if (!Maths.areHorizontallyCodirectional(moveVector, velocity)) {
                                       result.addTag("unexpected move direction");
                                       vl += 2;
                                    }

                                    if (offsetX > this.optMaxOffset) {
                                       result.addTag("unexpected knockback position").addTag("offset x: " + offsetX);
                                       vl++;
                                    }

                                    if (offsetZ > this.optMaxOffset) {
                                       result.addTag("unexpected knockback position").addTag("offset z: " + offsetZ);
                                       vl++;
                                    }

                                    boolean damnFalsePos = offsetX > 0.25 || offsetZ > 0.25;
                                    if (vl > 1 && !damnFalsePos) {
                                       int recentVls = stats.updateCreatedCounter("actVelVlCounter", (long)this.optSuspicionLifespan);
                                       result.addTag("ignoring forced velocity")
                                          .addTag("last inventory: " + inv.getTitle() + " (" + inv.getType().name() + ")")
                                          .addTag("recent suspicions: " + recentVls + "/" + this.optThreshold)
                                          .addTag("y: " + oldLoc.getYaw())
                                          .addTag("p: " + oldLoc.getPitch())
                                          .addTagIf("rev", reverse);
                                       boolean fatal = recentVls > this.optThreshold;
                                       if (fatal) {
                                          result.addTag("fatal").setMod(25);
                                       } else {
                                          result.setMod(1);
                                       }

                                       if (fatal) {
                                          ReflexScheduler.async().runLater(ReflexRunnable.simple("PR-94589721", () -> {
                                             Check.addVl(stats, this.getCheat(), result);
                                             Check.check(p, stats, null, this, result);
                                          }), this.optFlagDelay);
                                       } else {
                                          Check.addVl(stats, this.getCheat(), result);
                                          Check.check(p, stats, null, this, result);
                                       }
                                    }

                                    mp1.addToTimings();
                                 }
                              }
                           }
                        ),
                        15
                     );
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onInventoryOpen(InventoryOpenEvent e) {
      if (!e.isCancelled()) {
         Player p = (Player)e.getPlayer();
         this.check(p, Stats.of(p), e.getInventory());
      }
   }
}
