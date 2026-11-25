package rip.reflex.check.combat.killaura;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.MovementCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.location.MoveDirection;
import rip.reflex.util.location.Movement;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.time.Timer;

public class PacketRotsCheck extends MovementCheck {
   public PacketRotsCheck() {
      super("KillAura", "ka.pts", 1, Cheat.valueOf("KillAura"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      MethodProfiler mp = new MethodProfiler("CT-718659");
      CheckResult result = new CheckResult();
      if (stats.getStat("attackCounter", int.class) >= 2
         && !stats.getStat("attackTracker0", Timer.class).hasPassed(500L)
         && !stats.hasSpeedEffect()
         && !stats.hasJumpEffect()) {
         Movement movement = LocationUtils.getMovement(p, from, to);
         MoveDirection direction = movement.getDirection();
         double moveSpeed = Maths.horizontalDistance(from, to);
         double motion = direction == MoveDirection.BACKWARD ? movement.getBackwardFactor() : movement.getStrafingFactor();
         double ratio = movement.getForwardFactor() / motion;
         boolean knockingBack = LocationUtils.isKnockingBack(p, stats)
            || !stats.getStat("damageByEntityTracker", Timer.class).hasPassed(700L)
               && !LocationUtils.isZero(p.getVelocity().clone())
               && !LocationUtils.isGroundExact(p);
         if (direction != MoveDirection.FORWARD
            && Maths.matchEx(Math.abs(movement.getForwardFactor() - motion), 0.19, 0.6)
            && Maths.matchEx(motion, 0.63, 0.8)
            && !Maths.matchIn(motion, 0.68, 0.6999)
            && !Maths.anyRange(ratio, 1.0, 1.2, 1.3, 1.365)
            && ratio < 2.0
            && !knockingBack
            && (moveSpeed > 0.267 || p.isSprinting())) {
            stats.setStat("packetRotsViolations", stats.getStat("packetRotsViolations", int.class) + 1);
            stats.getStat("packetRotsViolateTracker", Timer.class).reset();
            if (stats.getStat("packetRotsViolations", int.class) > this.getLocalSetting("m_threshold", Integer.valueOf(3))) {
               result.setMod(100)
                  .addTag("moves suspiciously")
                  .addTag(direction.shorten() + ": " + motion)
                  .addTag("r: " + ratio)
                  .addTag("m: " + moveSpeed)
                  .addTag("sprinting: " + p.isSprinting())
                  .addTag(LocationUtils.isGroundExact(p) ? "grounded" : "midair");
               stats.setStat("packetRotsViolations", 0);
            }
         } else if (stats.hasTimerPassed("packetRotsViolateTracker", 5000L)) {
            stats.setStat("packetRotsViolations", 0);
         }

         if (result.isCheckFailed()) {
            stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 5);
         }

         mp.addToTimings();
         return result;
      } else {
         return null;
      }
   }
}
