package rip.reflex.check.combat.killaura;

import com.comphenix.protocol.events.PacketContainer;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.mcaccess.BoundingBox;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class HitboxCheck extends PacketCheck {
   private double optPrecision;
   private double optLeniency;
   private double optMaxLowest;
   private int optThreshold;
   private int optSuspicionLifespan;
   private int optPeriodWeight;
   private int optInstantWeight;

   public HitboxCheck() {
      super("KillAura", "ka.hbx", Cheat.valueOf("KillAura"));
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optPrecision = this.getAdvancedLocalSetting("precision", Double.valueOf(0.005));
      this.optLeniency = this.getAdvancedLocalSetting("leniency", Double.valueOf(0.013));
      this.optMaxLowest = this.getAdvancedLocalSetting("max_lowest", Double.valueOf(1.679));
      this.optThreshold = this.getAdvancedLocalSetting("threshold", Integer.valueOf(7));
      this.optSuspicionLifespan = this.getAdvancedLocalSetting("suspicion_lifespan", Integer.valueOf(3000));
      this.optPeriodWeight = this.getAdvancedLocalSetting("period_weight", Integer.valueOf(20));
      this.optInstantWeight = this.getAdvancedLocalSetting("instant_weight", Integer.valueOf(15));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("CT-48721949");
         CheckResult result = new CheckResult();
         if (stats.getLagStatus() != LagStatus.EXTREME_LAG
            && !stats.hasNoStat("posCaches")
            && !stats.hasNoStat("currentTarget")
            && Packets.isAttackPacket(packet)) {
            Location l = p.getEyeLocation().clone();
            Vector v = l.getDirection().clone();
            LivingEntity target = stats.getCurrentTarget();
            Location tLoc = target.getEyeLocation().clone();
            HashMap posCaches = stats.getMap("posCaches");
            if (Packets.readInteger(packet, 0) != target.getEntityId()) {
               return null;
            } else {
               BoundingBox cur = BoundingBox.of((Entity)target).expand(0.3, 0.0, 0.3).expandUp(1.0).expandDown(2.0);
               BoundingBox old = Misc.castSafely(posCaches.get(target));
               if (old == null) {
                  return null;
               } else {
                  boolean intersects = false;
                  double lowestDist = 1.0E30;

                  for (double d = 0.0; d < (p.isSprinting() ? 0.192 : 0.175) + this.optLeniency; d += this.optPrecision) {
                     l.add(v.clone().multiply(d));
                     if (cur.isWithinBoundingBox(l) || old.isWithinBoundingBox(l)) {
                        intersects = true;
                        break;
                     }

                     lowestDist = Maths.min(lowestDist, cur.distance(l), old.distance(l));
                  }

                  if (!intersects) {
                     int u = stats.updateCreatedCounter("hitboxVl", (long)this.optSuspicionLifespan);
                     if (u > this.optThreshold && Maths.objects.length >= 16 && Maths.objects[13] == Integer.valueOf(6)) {
                        result.setMod(this.optPeriodWeight).addTag("hitbox (period)").addTag("recent suspicions: " + u).cancel(this.isNotSilent());
                     } else if (lowestDist > this.optMaxLowest) {
                        result.setMod(3).addTag("hitbox (instant)").addTag("lowest: " + lowestDist).cancel(this.isNotSilent());
                     }

                     if (result.shouldCancel()) {
                        stats.resetTimer("attackPenaltyTracker");
                     }
                  }

                  mp.addToTimings();
                  return result;
               }
            }
         } else {
            return null;
         }
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
