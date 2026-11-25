package rip.reflex.check.movement;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.compatibility.CompatibilityProvider;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class VehicleMoveCheck extends PacketCheck {
   private static final double MAX_NON_AIR_Y = 0.055;
   private static final double FIRST_BOAT_GRAVITY = -0.15;
   private static final double SECOND_BOAT_GRAVITY = -0.5;
   private static final double THIRD_BOAT_GRAVITY = -1.35;
   private static final double FOURTH_BOAT_GRAVITY = -2.95;
   private double optMaxNormalSpeed;
   private double optMaxFlowingSpeed;
   private double optMaxGroundSpeed;
   private double optMaxIceSpeed;
   private double optMaxAcceleration;

   public VehicleMoveCheck() {
      super("VehicleMove", "vehmv.boat", Cheat.valueOf("VehicleMove"));
      this.setupVlSubtractionThread(5, 1);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMaxNormalSpeed = this.getAdvancedLocalSetting("max_normal_speed", Double.valueOf(0.52));
      this.optMaxFlowingSpeed = this.getAdvancedLocalSetting("max_flowing_speed", Double.valueOf(0.58));
      this.optMaxGroundSpeed = this.getAdvancedLocalSetting("max_ground_speed", Double.valueOf(0.26));
      this.optMaxIceSpeed = this.getAdvancedLocalSetting("max_ice_speed", Double.valueOf(2.08));
      this.optMaxAcceleration = this.getAdvancedLocalSetting("max_acceleration", Double.valueOf(0.06));
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void vehicleExited(VehicleExitEvent e) {
      if (!e.isCancelled() && e.getExited() instanceof Player) {
         Player p = (Player)e.getExited();
         Stats stats = Stats.of(p);
         stats.removeStat("vehmvLastX");
         stats.removeStat("vehmvLastY");
         stats.removeStat("vehmvLastZ");
         stats.removeStat("vehmvLastSpeed");
         stats.removeStat("vehmvLastAirborne");
         stats.removeStat("vehmvAirborneTicks");
         stats.resetTimer("vehmvIceMoveTracker");
      }
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (!data.isVehicleMove()) {
         return null;
      } else {
         Entity vehicle = p.getVehicle();
         if (vehicle != null && vehicle.getType().name().toUpperCase().contains("BOAT")) {
            CheckResult result = new CheckResult();
            MethodProfiler mp = new MethodProfiler("RB-8095182");
            double newX = data.getX();
            double newY = data.getY();
            double newZ = data.getZ();
            double lastX = stats.getDouble("vehmvLastX");
            double lastY = stats.getDouble("vehmvLastY");
            double lastZ = stats.getDouble("vehmvLastZ");
            stats.setStat("vehmvLastX", newX);
            stats.setStat("vehmvLastY", newY);
            stats.setStat("vehmvLastZ", newZ);
            if (lastX == 0.0 && lastY == 0.0 && lastZ == 0.0) {
               return null;
            } else {
               double movedX = newX - lastX;
               double movedY = newY - lastY;
               double movedZ = newZ - lastZ;
               if (movedX == 0.0 && movedY == 0.0 && movedZ == 0.0) {
                  return null;
               } else {
                  double speed = Math.sqrt(Maths.square(movedX) + Maths.square(movedZ));
                  double lastSpeed = stats.getDouble("vehmvLastSpeed");
                  double acceleration = (speed - lastSpeed) / 1.0;
                  stats.setStat("vehmvLastSpeed", speed);
                  Block blockInside = p.getLocation().clone().getBlock();
                  Block blockBelow = blockInside.getRelative(BlockFace.DOWN);
                  CompatibilityProvider cp = VersionUtils.getCompatibilityProvider();
                  boolean isAirborne = blockInside.getType() == Material.AIR && blockBelow.getType() == Material.AIR;
                  boolean isOnFlowingLiquid = !isAirborne
                     && (blockInside.isLiquid() && cp.isLiquidFlowing(blockInside) || blockBelow.isLiquid() && cp.isLiquidFlowing(blockBelow));
                  boolean isOnGround = !isAirborne && !isOnFlowingLiquid && !blockInside.isLiquid() && !blockBelow.isLiquid();
                  boolean isOnIce = isOnGround && (blockInside.getType().name().contains("ICE") || blockBelow.getType().name().contains("ICE"))
                     || !stats.hasTimerPassed("vehmvIceMoveTracker", 4000L);
                  boolean lastAirborne = stats.getBoolean("vehmvLastAirborne");
                  if (isOnIce) {
                     stats.resetTimer("vehmvIceMoveTracker");
                  }

                  stats.setStat("vehmvLastAirborne", isAirborne);
                  if (isAirborne) {
                     if (lastAirborne) {
                        stats.addNumber("vehmvAirborneTicks", 1);
                     } else {
                        stats.setStat("vehmvAirborneTicks", 0);
                     }
                  } else if (lastAirborne) {
                     stats.setStat("vehmvAirborneTicks", 0);
                  }

                  double maxSpeed = isOnIce
                     ? this.optMaxIceSpeed
                     : (isOnGround ? this.optMaxGroundSpeed : (isOnFlowingLiquid ? this.optMaxFlowingSpeed : this.optMaxNormalSpeed));
                  int vl = 0;
                  if (speed > maxSpeed) {
                     result.addTag("moved too quickly on boat").addTag("speed: " + speed).addTag("max: " + maxSpeed);
                     vl++;
                  }

                  if (isOnGround && acceleration > this.optMaxAcceleration) {
                     result.addTag("moving too quickly on boat").addTag("acceleration: " + acceleration);
                     vl++;
                  }

                  int ticksInMidair = stats.getInt("vehmvAirborneTicks");
                  double maxY = this.getGravity(ticksInMidair);
                  if (movedY > maxY) {
                     result.addTag("unexpected gravity on boat: " + movedY).addTag("max: " + maxY).addTag("ticks in midair: " + ticksInMidair);
                     vl++;
                  }

                  mp.addToTimings();
                  if (vl > 0) {
                     result.setMod(vl);
                     if (this.shouldCancel(stats)) {
                        ReflexScheduler.sync().run(ReflexRunnable.simple("OM-2891784", vehicle::eject));
                        result.cancel();
                     }

                     result.addTagIf("flowing", isOnFlowingLiquid).addTagIf("ground", isOnGround).addTagIf("on ice", isOnIce).addTagIf("airborne", isAirborne);
                  }

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

   private double getGravity(int ticksInMidair) {
      if (ticksInMidair >= 80) {
         return -2.95;
      } else if (ticksInMidair >= 40) {
         return -1.35;
      } else if (ticksInMidair >= 20) {
         return -0.5;
      } else {
         return ticksInMidair >= 10 ? -0.15 : 0.055;
      }
   }
}
