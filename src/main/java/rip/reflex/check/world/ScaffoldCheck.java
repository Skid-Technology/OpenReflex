package rip.reflex.check.world;

import com.comphenix.protocol.wrappers.BlockPosition;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.WorldCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.compatibility.Compatibility;
import rip.reflex.util.time.Timer;

public class ScaffoldCheck extends WorldCheck {
   private double optMinHor;
   private double optMinDia;

   public ScaffoldCheck() {
      super("BlockActions", "blckact.sfd", Cheat.valueOf("BlockActions"));
      this.setSync(true);
      LoopHelper.loopStatsEvery(stats -> {
         stats.setStat("scaffoldBuildspeedVl", 0);
         stats.setStat("constBuildspeedVl", 0);
      }, 200);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMinHor = this.getAdvancedLocalSetting("min_hor", Double.valueOf(5.0));
      this.optMinDia = this.getAdvancedLocalSetting("min_dia", Double.valueOf(5.0));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Block placed, Block against, ItemStack heldItem) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("WG-9479182738");
         if (p.getLocation().getBlockY() - placed.getLocation().getBlockY() == 1
            && placed.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)
            && !(Maths.distance(placed.getLocation().clone(), p.getLocation().clone()) > 6.0)
            && LocationUtils.isNormalBlock(placed)
            && LocationUtils.isBridging(p, stats)
            && (p.getLocation().getBlockX() == placed.getX() || p.getLocation().getBlockZ() == placed.getZ())
            && p.getLocation().getBlockY() != placed.getY()
            && LocationUtils.isNormalBlock(against)
            && !Compatibility.isAny(heldItem, "hoe", "banner", "brewing")
            && !Compatibility.isNearby(p, 4, "vine", "ladder", "banner", "wall", "fence")
            && LocationUtils.getDirectNeighbors(placed).size() <= 1) {
            CheckResult result = new CheckResult();
            Block looking = p.getTargetBlock((Set)null, 120);
            double dist = Maths.distance(against.getLocation(), looking.getLocation());
            double yaw = (double)p.getLocation().getYaw();
            double pitch = (double)p.getLocation().getPitch();
            boolean samexz = placed.getLocation().getBlockX() == p.getLocation().getBlockX() && placed.getLocation().getBlockZ() == p.getLocation().getBlockZ();
            boolean pushable = LocationUtils.isPushable(p.getEyeLocation().clone());
            int mod = 0;
            Location eye = p.getEyeLocation().clone();
            Location lplaced = placed.getLocation().clone();
            Location lagainst = against.getLocation().clone();
            lplaced.setX((double)Math.round(lplaced.getX()) + 0.5);
            lplaced.setY((double)Math.round(lplaced.getY()) + 0.5);
            lplaced.setZ((double)Math.round(lplaced.getZ()) + 0.5);
            lagainst.setX((double)Math.round(lagainst.getX()) + 0.5);
            lagainst.setY((double)Math.round(lagainst.getY()) + 0.5);
            lagainst.setZ((double)Math.round(lagainst.getZ()) + 0.5);
            double dplaced = Maths.distance(eye, lplaced);
            double dagainst = Maths.distance(eye, lagainst);
            NumberStorage delays = stats.getOrCreateNumberStorage("scaffoldBuildSpeed", 5);
            int buildspeed = Maths.floor(delays.average());
            double d = dplaced - dagainst;
            boolean fast = delays.isFull() && buildspeed < this.getLocalSetting("fast_buildspeed_ms", Integer.valueOf(500));
            if (!looking.equals(placed) && fast) {
               mod += 3;
               result.addTag("type A");
            }

            if (dist > 2.0 && pushable) {
               mod += 7;
               result.addTag("dist: " + dist);
            }

            if (against.getType().equals(Material.AIR) || against.isLiquid()) {
               mod += 9;
               result.addTag("type B");
            }

            if (d > -0.03 && fast) {
               mod += 6;
               result.addTag("d: " + d);
            }

            if (!samexz && pushable) {
               mod += 2;
               result.addTag("type C");
            }

            if (yaw % 90.0 == 0.0 || yaw % 1.0 == 0.0) {
               Location tp = p.getLocation().clone();
               tp.setYaw(tp.getYaw() + (float)Math.random() * 2.0F);
               tp.setPitch(tp.getPitch() + (float)Math.random() * 2.0F);
               mod += 9;
               result.addTag("type D");
               p.teleport(tp);
            }

            if (pitch % 1.0 == 0.0 && Maths.matchEx(pitch, 0, 90)) {
               Location tp = p.getLocation().clone();
               tp.setYaw(tp.getYaw() + (float)Math.random() * 2.0F);
               tp.setPitch(tp.getPitch() + (float)Math.random() * 2.0F);
               mod += 4;
               result.addTag("type E");
               p.teleport(tp);
            }

            Block lastPlaced = stats.getStat("lastBlockPlaced", Block.class);
            if (lastPlaced != null
               && lastPlaced.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR)
               && lastPlaced.getY() == placed.getY()
               && (
                  lastPlaced.getX() != placed.getX() && lastPlaced.getZ() == placed.getZ()
                     || lastPlaced.getX() == placed.getX() && lastPlaced.getZ() != placed.getZ()
               )) {
               int buildSpeed = (int)stats.getStat("bridgeTracker", Timer.class).getTimePassed();
               stats.addInCreatedNumberStorage("scaffoldBuildSpeed", 5, buildSpeed);
               if (pushable) {
                  if (buildSpeed < 400) {
                     stats.setStat("lastBuildspeed", buildSpeed);
                  }

                  stats.getStat("bridgeTracker", Timer.class).reset();
               }
            }

            if (this.shouldCancel(stats, 10)) {
               for (Object o : stats.getStat("recentlyPlaced", Storage.class).values()) {
                  ((Block)o).setType(Material.AIR);
               }

               Location setback = ((Block)stats.getStat("recentlyPlaced", Storage.class).first()).getLocation().clone();
               setback.setYaw(p.getLocation().clone().getYaw() - 180.0F);
               setback.setPitch(-90.0F);
               stats.resetTimer("buildPenaltyTimer");
               stats.setback(setback);
               result.cancel();
            }

            result.setMod(mod > 2 ? mod : 0);
            mp.addToTimings();
            return result;
         } else {
            return null;
         }
      }
   }

   @Override
   public CheckResult packetBlockPlace(
      Player p,
      Stats stats,
      Location pLoc,
      BlockPosition pos,
      ItemStack heldItem,
      Material type,
      int ticksSincePlace,
      int missesSincePlace,
      Storage recentPlaced
   ) {
      if (this.isServerLagging()) {
         return null;
      } else if (!LocationUtils.isPushable(p.getEyeLocation().clone())) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("WG-19218935");
         ScaffoldCheck.BridgeState state = LocationUtils.getBridgeState(recentPlaced);
         if (state == ScaffoldCheck.BridgeState.NOT_BRIDGING) {
            return null;
         } else {
            CheckResult result = new CheckResult();
            if (state == ScaffoldCheck.BridgeState.BRIDGING_HORIZONTALLY) {
               NumberStorage delays = stats.getOrCreateNumberStorage("scfHorDelays", 5);
               if (delays.addChecking(ticksSincePlace)) {
                  double avg = delays.average();
                  if (avg < this.optMinHor) {
                     result.setMod(12).addTag("type M").addTag("h: " + avg).cancel(this.isNotSilent());
                  }

                  delays.clear();
               }

               int minMisses = 0;
               if (ticksSincePlace < 5) {
                  minMisses = 1;
               }

               if (ticksSincePlace < 4) {
                  minMisses = 2;
               }

               if (missesSincePlace < minMisses) {
                  if (stats.addNumber("scfHorAccuracyVl", 3) > 7) {
                     result.setMod(12)
                        .addTag("type I")
                        .addTag("h: " + ticksSincePlace)
                        .addTag("m: " + missesSincePlace + "/" + minMisses)
                        .cancel(this.isNotSilent());
                     stats.setStat("scfHorAccuracyVl", 0);
                  }
               } else {
                  stats.subtractNumberTilPositive("scfHorAccuracyVl", 1);
               }
            } else if (state == ScaffoldCheck.BridgeState.BRIDGING_DIAGONALLY) {
               NumberStorage delaysx = stats.getOrCreateNumberStorage("scfDiaDelays", 5);
               NumberStorage misses = stats.getOrCreateNumberStorage("scfDiaMisses", 5);
               misses.add(missesSincePlace);
               if (delaysx.addChecking(ticksSincePlace)) {
                  double avgDelay = delaysx.average();
                  double avgMisses = misses.average();
                  if (avgDelay < this.optMinDia) {
                     result.setMod(12).addTag("type J").addTag("d: " + avgDelay).addTag("m: " + avgMisses).cancel(this.isNotSilent());
                  }

                  if (avgMisses >= 7.0) {
                     result.setMod(12).addTag("type K").addTag("d: " + avgDelay).addTag("m: " + avgMisses).cancel(this.isNotSilent());
                  }

                  delaysx.clear();
                  misses.clear();
               }
            }

            mp.addToTimings();
            return result;
         }
      }
   }

   public static enum BridgeState {
      NOT_BRIDGING,
      BRIDGING_HORIZONTALLY,
      BRIDGING_DIAGONALLY;
   }
}
