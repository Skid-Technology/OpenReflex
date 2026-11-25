package rip.reflex.check.combat.antiknockback;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.location.DirectionalHorizontalMove;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class AirborneDirectionCheck extends PacketCheck {
   private double optMaxAngle;
   private int optThreshold;
   private int optSuspicionLifespan;

   public AirborneDirectionCheck() {
      super("AntiKnockback", "vel.adr", Cheat.valueOf("AntiKnockback"));
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMaxAngle = this.getAdvancedLocalSetting("max_angle", Double.valueOf(35.0));
      this.optThreshold = this.getAdvancedLocalSetting("threshold", Integer.valueOf(3));
      this.optSuspicionLifespan = this.getAdvancedLocalSetting("suspicion_lifespan", Integer.valueOf(7500));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      MethodProfiler mp = new MethodProfiler("LF-4241098123");
      CheckResult result = null;
      if (packet.getType().equals(Packets.IN_POSITION) || packet.getType().equals(Packets.IN_POSLOOK)) {
         if (stats.isSneaking()
            || !stats.hasTimerPassed("sneakStartTracker", 1000L)
            || !stats.hasTimerPassed("velAdrLiquidTracker", 1500L)
            || stats.hasNoStat("recentYawRots")
            || !stats.hasTimerPassed("teleportTracker", 1500L)) {
            return null;
         }

         double newX = data.getX();
         double newZ = data.getZ();
         double oldX = stats.getDouble("velAdrOldX");
         double oldZ = stats.getDouble("velAdrOldZ");
         stats.setStat("velAdrOldX", newX);
         stats.setStat("velAdrOldZ", newZ);
         if (oldX == 0.0 && oldZ == 0.0) {
            return null;
         }

         double moveX = newX - oldX;
         double moveZ = newZ - oldZ;
         if (Math.abs(moveX) < 0.05 && Math.abs(moveZ) < 0.05) {
            return null;
         }

         if (p.isOnGround() || stats.getBoolean("lastTickOnGround")) {
            stats.removeStat("velAdrLastMove");
            stats.removeStat("lastPacketVelocity");
            return null;
         }

         DirectionalHorizontalMove move = new DirectionalHorizontalMove(oldX, oldZ, newX, newZ, moveX, moveZ);
         DirectionalHorizontalMove lastMove = stats.getStat("velAdrLastMove", DirectionalHorizontalMove.class);
         stats.setStat("velAdrLastMove", move);
         if (lastMove == null) {
            return null;
         }

         NumberStorage recentYawRots = stats.getNumberStorage("recentYawRots");
         double highestRecentYawRot = recentYawRots.max();
         if (highestRecentYawRot > 120.0) {
            return null;
         }

         Vector curVelocity = stats.getStat("lastPacketVelocity", Vector.class);
         if (curVelocity != null) {
            Vector lastVelocity = stats.getStat("velAdrLastVelocity", Vector.class);
            stats.setStat("velAdrLastVelocity", curVelocity);
            if (lastVelocity != null
               && (lastVelocity.getX() != 0.0 || lastVelocity.getZ() != 0.0)
               && !Maths.areHorizontallyCodirectional(curVelocity, lastVelocity)) {
               stats.setStat("velAdrTicksSinceVelocityChange", 0);
               return null;
            }
         }

         Location loc = p.getLocation().clone();
         Block blockInside = loc.getBlock();
         if (blockInside.isLiquid()) {
            stats.resetTimer("velAdrLiquidTracker");
            return null;
         }

         Block blockBelow = blockInside.getRelative(BlockFace.DOWN);
         if (blockBelow.isLiquid()) {
            stats.resetTimer("velAdrLiquidTracker");
            return null;
         }

         stats.addNumber("velAdrTicksSinceVelocityChange", 1);
         int curSignX = Double.compare(move.getMovedX(), 0.0);
         int curSignZ = Double.compare(move.getMovedZ(), 0.0);
         int lastSignX = Double.compare(lastMove.getMovedX(), 0.0);
         int lastSignZ = Double.compare(lastMove.getMovedZ(), 0.0);
         double angle = Math.min(180.0, Math.toDegrees(move.angle(lastMove)));
         int ticksSinceVelocityChange = stats.getInt("velAdrTicksSinceVelocityChange");
         if ((angle > this.optMaxAngle && stats.hasTimerPassed("generalDamageTracker", 2000L) || curSignX != lastSignX && curSignZ != lastSignZ)
            && (ticksSinceVelocityChange >= 60 || Maths.matchEx(ticksSinceVelocityChange, 1, 15))) {
            int u = stats.updateCreatedCounter("velAdrVlCounter", (long)this.optSuspicionLifespan);
            if (u > this.optThreshold) {
               result = new CheckResult()
                  .setMod(25)
                  .addTag("airborne direction change")
                  .addTag("recent suspicions: " + u + "/" + this.optThreshold)
                  .addTag("angle: " + angle + " degrees")
                  .addTag("last: " + lastMove.getMoveString())
                  .addTag("current: " + move.getMoveString())
                  .addTag("tsvc: " + ticksSinceVelocityChange)
                  .addTag("hryr: " + highestRecentYawRot);
            }
         }
      }

      mp.addToTimings();
      return result;
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
