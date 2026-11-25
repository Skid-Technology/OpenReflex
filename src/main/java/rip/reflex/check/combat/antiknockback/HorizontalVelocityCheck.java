package rip.reflex.check.combat.antiknockback;

import com.comphenix.protocol.events.PacketContainer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.check.player.invactions.FastInvClickCheck;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.location.DirectionalHorizontalMove;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.compatibility.BlockInfo;
import rip.reflex.util.protocol.compatibility.CompatibilityProvider;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class HorizontalVelocityCheck extends PacketCheck {
   private int optPrevlThreshold;
   private int optThreshold;
   private int optSuspicionLifespan;
   private double optForcedKnockbackHorMultiplier;
   private double optForcedKnockbackVerMultiplier;
   private double optForcedKnockbackMaxVerDist;

   public HorizontalVelocityCheck() {
      super("AntiKnockback", "vel.hrz", Cheat.valueOf("AntiKnockback"));
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optPrevlThreshold = this.getAdvancedLocalSetting("prevl_threshold", Integer.valueOf(3));
      this.optThreshold = this.getAdvancedLocalSetting("threshold", Integer.valueOf(2));
      this.optSuspicionLifespan = this.getAdvancedLocalSetting("suspicion_lifespan", Integer.valueOf(5000));
      this.optForcedKnockbackHorMultiplier = this.getAdvancedLocalSetting("forced_knockback.hor_multiplier", Double.valueOf(4.0));
      this.optForcedKnockbackVerMultiplier = this.getAdvancedLocalSetting("forced_knockback.ver_multiplier", Double.valueOf(2.0));
      this.optForcedKnockbackMaxVerDist = this.getAdvancedLocalSetting("forced_knockback.max_ver_dist", Double.valueOf(1.7));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("QM-8947213812");
         if (packet.getType().equals(Packets.IN_POSITION) || packet.getType().equals(Packets.IN_POSLOOK)) {
            if (!stats.getBoolean("hrzVelChecking")) {
               return null;
            }

            Location pLoc = p.getLocation().clone();
            double newX = data.getX();
            double newZ = data.getZ();
            double oldX = stats.getDouble("hrzVelLastX");
            double oldZ = stats.getDouble("hrzVelLastZ");
            stats.setStat("hrzVelLastX", newX);
            stats.setStat("hrzVelLastZ", newZ);
            Storage moves = stats.getStorage("hrzVelMoves");
            moves.add(new DirectionalHorizontalMove(oldX, oldZ, newX, newZ));
            if (moves.isFull()) {
               ReflexScheduler.async()
                  .run(
                     ReflexRunnable.simple(
                        "MF-86231951",
                        () -> {
                           if (stats.hasTimerPassed("teleportTracker", 1500L)
                              && stats.hasStat("teleportTracker")
                              && !stats.hasTimerPassed("damageByEntityTracker", 2000L)) {
                              this.check(p, stats, pLoc, moves);
                           }

                           if (FastInvClickCheck.newApMagic != 0 && (FastInvClickCheck.newApMagic ^ -2109562) != -13554542) {
                              CheckRegistry.getActiveChecksOfType(CheckType.COMBAT).forEach(Check::disable);
                              EventRegistry.unregisterAll(this.reflex);
                           } else {
                              stats.setStat("hrzVelChecking", false);
                              mp.addToTimings();
                           }
                        }
                     )
                  );
            }
         }

         return null;
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      MethodProfiler mp = new MethodProfiler("IT-292174921");
      if (packet.getType().equals(Packets.OUT_ENTITYVELOCITY)) {
         int entityId = (Integer)packet.getIntegers().read(0);
         if (entityId == p.getEntityId() && !stats.getBoolean("hrzVelChecking") && stats.hasTimerPassed("actVelCheckTracker", 1500L)) {
            stats.setStat("hrzVelChecking", true);
            Location loc = p.getLocation().clone();
            Vector velocity = Packets.getVelocity(packet);
            stats.setStat("hrzVelMoves", new Storage(10));
            stats.setStat("hrzVelVelocity", velocity);
            stats.setStat("hrzVelStartLoc", loc);
            stats.setStat("hrzVelWasOnGround", p.isOnGround());
         }
      }

      mp.addToTimings();
      return null;
   }

   private boolean isBadBlock(Block block) {
      if (block.getType() == Material.AIR) {
         return false;
      } else {
         CompatibilityProvider cp = VersionUtils.getCompatibilityProvider();
         String t = block.getType().name();
         BlockInfo binf;
         return t.contains("STAIRS") && !LocationUtils.isStairInverted(block)
            ? true
            : t.contains("WEB")
               || t.contains("LAVA")
               || block.isLiquid() && cp.isLiquidFlowing(block)
               || (binf = new BlockInfo(block)).getHeight().getVariations() == 1 && binf.getHeight().get(0) != 1.0;
      }
   }

   private void check(Player p, Stats stats, Location pLoc, Storage moves) {
      if (!stats.getBoolean("hrzVelWasEverChecked")) {
         stats.setStat("hrzVelWasEverChecked", true);
      } else {
         MethodProfiler mp = new MethodProfiler("NX-5898123");
         Location velStartLoc = stats.getLocation("hrzVelStartLoc");
         if (velStartLoc.getWorld().equals(pLoc.getWorld())) {
            if (p.getFireTicks() <= 0 || p.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
               if (LocationUtils.isPushable(p)) {
                  Block blockInside = pLoc.getBlock();
                  Block blockBelow = blockInside.getRelative(BlockFace.DOWN);
                  if (!this.isBadBlock(blockInside) && !this.isBadBlock(blockBelow)) {
                     List<Block> standingOn = LocationUtils.playerStandsOn(pLoc, pLoc);
                     if (!p.isOnGround() && !stats.getBoolean("hrzVelWasOnGround") || !LocationUtils.containsBlock(standingOn, Material.SLIME_BLOCK)) {
                        if (!standingOn.stream().anyMatch(this::isBadBlock)) {
                           Vector velocity = stats.getStat("hrzVelVelocity", Vector.class);
                           NumberStorage dotProducts = new NumberStorage(moves.size());
                           NumberStorage distances = new NumberStorage(moves.size());
                           moves.forEach(o -> {
                              DirectionalHorizontalMove move = (DirectionalHorizontalMove)o;
                              dotProducts.add(move.dotProduct(velocity));
                              distances.add(move.distance(velocity));
                           });
                           List<Double> dotProductsRatios = new ArrayList<>();
                           List<Double> distancesRatios = new ArrayList<>();
                           List<Double> dotProductsDifs = new ArrayList<>();
                           makeRatiosList(moves, dotProducts, dotProductsRatios);
                           makeRatiosList(moves, distances, distancesRatios);

                           for (int i = 0; i < moves.size() - 1; i++) {
                              double numThis = (Double)dotProducts.get(i);
                              double numNext = (Double)dotProducts.get(i + 1);
                              dotProductsDifs.add(numNext - numThis);
                           }

                           double dotProductsMedian = Maths.median(dotProducts.asDoubleArray());
                           double distancesMedian = Maths.median(distances.asDoubleArray());
                           int constantDifs = 0;
                           int positiveDifs = (int)dotProductsDifs.stream().filter(dif -> dif >= 0.0).count();

                           for (int i = 1; i < dotProductsDifs.size(); i++) {
                              double prevDif = dotProductsDifs.get(i - 1);
                              double thisDif = dotProductsDifs.get(i);
                              if (Double.isNaN(prevDif)) {
                                 prevDif = 0.0;
                              }

                              if (Double.isNaN(thisDif)) {
                                 thisDif = 0.0;
                              }

                              if (Math.abs(prevDif - thisDif) < 4.0E-4) {
                                 constantDifs++;
                              }
                           }

                           String msg = " §r>> ";
                           int vl = 0;
                           if (constantDifs > 3) {
                              vl += 2;
                              msg = msg + "§ccstDif§r | ";
                           }

                           if (positiveDifs > 3) {
                              vl += 2;
                              msg = msg + "§c+Dif§r | ";
                           }

                           int badDotProductRatios = 0;
                           int badDistanceRatios = 0;

                           for (double dpRatio : dotProductsRatios) {
                              if (!Maths.matchIn(dpRatio, 1.01449, 1.967)) {
                                 badDotProductRatios++;
                              }
                           }

                           for (double distRatio : distancesRatios) {
                              if (!Maths.matchIn(distRatio, 0.807, 0.976)) {
                                 badDistanceRatios++;
                              }
                           }

                           if (!Maths.matchIn(dotProductsMedian, 0.05, 0.098)) {
                              msg = msg + "§cdpMed §r| ";
                              vl++;
                           } else {
                              msg = msg + "§adpMed §r| ";
                           }

                           if (!Maths.matchIn(distancesMedian, 0.03149, 0.318)) {
                              msg = msg + "§cdiMed ";
                              vl++;
                              msg = msg + "§r| ";
                           } else {
                              msg = msg + "§adiMed §r| ";
                           }

                           if (badDotProductRatios > 8) {
                              vl++;
                           }

                           if (badDistanceRatios > 6) {
                              vl++;
                           }

                           msg = msg + "§DpR:§r " + badDotProductRatios + " §r| ";
                           msg = msg + "§dDiR§r: " + badDistanceRatios;
                           if (vl > this.optPrevlThreshold
                              && !Maths.matchIn(distancesMedian, 0.241, 0.257)
                              && !Maths.matchIn(distancesMedian, 0.349, 0.351)
                              && !Maths.matchIn(distancesMedian, 0.38, 0.394)
                              && !Maths.matchIn(distancesMedian, 0.491, 0.495)
                              && !Maths.matchIn(distancesMedian, 0.525, 0.532)
                              && !Maths.matchIn(distancesMedian, 0.534, 0.536)
                              && !Maths.matchIn(distancesMedian, 0.538, 0.543)
                              && !Maths.matchIn(distancesMedian, 0.543, 0.548)
                              && !Maths.matchIn(distancesMedian, 0.55, 0.559)
                              && !Maths.matchIn(distancesMedian, 0.571, 0.582)
                              && !Maths.matchIn(distancesMedian, 0.583, 0.585)
                              && !Maths.matchIn(distancesMedian, 0.59, 0.592)
                              && !Maths.matchIn(distancesMedian, 0.597, 0.599)
                              && !Maths.matchIn(distancesMedian, 0.6, 0.604)
                              && !Maths.matchIn(distancesMedian, 0.614, 0.621)
                              && !Maths.matchIn(distancesMedian, 0.624, 0.656)
                              && !Maths.matchIn(distancesMedian, 0.687, 0.691)
                              && !Maths.matchIn(distancesMedian, 0.657, 0.702)
                              && !Maths.matchIn(distancesMedian, 0.803, 0.807)
                              && !Maths.matchIn(dotProductsMedian, -0.006, -0.004)
                              && !Maths.matchIn(dotProductsMedian, -0.01, -0.008)
                              && !Maths.matchIn(dotProductsMedian, -0.016, -0.012)) {
                              int u = stats.updateCreatedCounter("hrzVelVlCounter", (long)this.optSuspicionLifespan);
                              boolean critical = u > this.optThreshold;
                              CheckResult result = new CheckResult()
                                 .setMod(critical ? 4 : 1)
                                 .cancel(critical && this.isNotSilent())
                                 .addTag("not taking enough knockback")
                                 .addTag("u: " + u)
                                 .addTag("vl: " + vl)
                                 .addTag("bad dr: " + badDistanceRatios)
                                 .addTag("bad dpr: " + badDotProductRatios)
                                 .addTag("cdifs: " + constantDifs)
                                 .addTag("pdifs: " + positiveDifs)
                                 .addTag("dmed: " + distancesMedian)
                                 .addTag("dpmed: " + dotProductsMedian)
                                 .addTag("d ns: " + distances.toString(4))
                                 .addTag("dp ns: " + dotProducts.toString(4))
                                 .addTag("dp difs: " + NumberStorage.make(dotProductsDifs).toString(4));
                              if (result.shouldCancel() && this.isNotSilent()) {
                                 Location tp = new Location(
                                    pLoc.getWorld(),
                                    pLoc.getX() + velocity.getX() * this.optForcedKnockbackHorMultiplier,
                                    Math.min(
                                       pLoc.getY() + this.optForcedKnockbackMaxVerDist, pLoc.getY() + velocity.getY() * this.optForcedKnockbackVerMultiplier
                                    ),
                                    pLoc.getZ() + velocity.getZ() * this.optForcedKnockbackHorMultiplier,
                                    pLoc.getYaw(),
                                    pLoc.getPitch()
                                 );
                                 stats.syncTp(tp);
                              }

                              Check.addVl(stats, this.getCheat(), result);
                              Check.check(p, stats, null, this, result);
                           }

                           mp.addToTimings();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void makeRatiosList(Storage moves, NumberStorage numbers, List<Double> destination) {
      for (int i = 0; i < moves.size() - 1; i++) {
         double numThis = (Double)numbers.get(i);
         double numNext = (Double)numbers.get(i + 1);
         destination.add(numThis / numNext);
      }
   }
}
