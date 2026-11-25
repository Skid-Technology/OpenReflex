package rip.reflex.check.movement;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class InvMoveCheck extends PacketCheck {
   private int optOpenWait;

   public InvMoveCheck() {
      super("InvActions", "invact.mov", Cheat.valueOf("InvActions"));
      LoopHelper.loopStatsEvery(stats -> {
         stats.setStat("invmoveVl", 0);
      }, 200);
      this.setupVlResetThread(15);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optOpenWait = this.getAdvancedLocalSetting("open_wait", Integer.valueOf(500));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      MethodProfiler mp = new MethodProfiler("LD-2872156744");
      CheckResult result = new CheckResult();
      if ((!stats.hasJumpEffect() || p.isOnGround()) && stats.hasTimerPassed("iceMoveTracker", 2200L)) {
         boolean walked = false;
         boolean rotated = false;
         if (packet.getType().equals(Packets.IN_POSLOOK)) {
            double x = data.getX();
            double z = data.getZ();
            float yaw = data.getYaw();
            float pitch = data.getPitch();
            walked = x != stats.getStat("ia_lastX", double.class) || z != stats.getStat("ia_lastZ", double.class);
            rotated = yaw != stats.getStat("ia_lastYaw", float.class) || pitch != stats.getStat("ia_lastPitch", float.class);
            stats.setStat("ia_lastX", x);
            stats.setStat("ia_lastZ", z);
            stats.setStat("ia_lastYaw", yaw);
            stats.setStat("ia_lastPitch", pitch);
         } else if (packet.getType().equals(Packets.IN_POSITION)) {
            double x = data.getX();
            double z = data.getZ();
            walked = x != stats.getStat("ia_lastX", double.class) || z != stats.getStat("ia_lastZ", double.class);
            stats.setStat("ia_lastX", x);
            stats.setStat("ia_lastZ", z);
         } else if (packet.getType().equals(Packets.IN_LOOK)) {
            float yaw = data.getYaw();
            float pitch = data.getPitch();
            rotated = yaw != stats.getStat("ia_lastYaw", float.class) || pitch != stats.getStat("ia_lastPitch", float.class);
            stats.setStat("ia_lastYaw", yaw);
            stats.setStat("ia_lastPitch", pitch);
         }

         if (stats.isInventoryOpened()
            && stats.hasTimerPassed("velocityTracker", 2000L)
            && !p.isInsideVehicle()
            && stats.hasTimerPassed("inventoryOpenTracker", (long)this.optOpenWait)
            && this.getLocalSetting("block_movement", Boolean.valueOf(true))) {
            Location l = p.getLocation().clone();
            Material block = l.getBlock().getType();
            double v = LocationUtils.getAbsoluteVelocityXZ(p);
            double f = (double)p.getFallDistance();
            if (stats.getStat("invmoveVl", int.class) > 10) {
               stats.setback(stats.getStat("setbackPos", Location.class));
               stats.setStat("invmoveVl", 0);
               if (walked) {
                  result.addTag("walking").addTag("v: " + v).addTag("f: " + f);
               }

               if (rotated) {
                  result.addTag("rotating");
               }

               result.setMod(5).cancel(this.isNotSilent());
               if (this.isNotSilent()) {
                  stats.setStat("inventoryOpened", false);
                  ReflexScheduler.sync().run(ReflexRunnable.simple("SyncInvClose", p::closeInventory));
               }
            }

            stats.addNumberIf(
               "invmoveVl",
               1,
               rotated || walked && v < 0.24 && f < 5.0 && stats.hasTimerPassed("damageByEntityTracker", 1250L) && !block.toString().contains("STATIONARY")
            );
         }

         mp.addToTimings();
         return result;
      } else {
         return null;
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
