package rip.reflex.check.player.badpackets;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.time.Timer;

public class RepetitiveRotationCheck extends PacketCheck {
   public RepetitiveRotationCheck() {
      super("BadPackets", "badpcks.rrn", Cheat.valueOf("BadPackets"));
      this.setLagSensetive(false);
      LoopHelper.loopStatsEvery(stats -> {
         stats.setStat("repRotVl", 0);
      }, 200);
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("TA-508129358");
         if ((packet.getType().equals(Packets.IN_POSLOOK) || packet.getType().equals(Packets.IN_LOOK)) && !p.isInsideVehicle()) {
            CheckResult result = new CheckResult();
            if (!stats.hasTimerTicked("teleportTracker", 20L)
               || !stats.hasTimerTicked("moveCancelTracker", 20L)
               || !stats.hasTimerPassed("worldChangeTracker", 5000L)) {
               return null;
            } else if (stats.getLagStatus() == LagStatus.EXTREME_LAG) {
               return null;
            } else {
               float yaw = data.getYaw();
               float pitch = data.getPitch();
               float lastYaw = stats.getStat("pkLastYaw", float.class);
               float lastPitch = stats.getStat("pkLastPitch", float.class);
               if (yaw == lastYaw && pitch == lastPitch && stats.getStat("teleportTracker", Timer.class).hasPassed(1000L)) {
                  stats.setStat("repRotVl", stats.getStat("repRotVl", int.class) + 1);
                  if (stats.getStat("repRotVl", int.class) > this.getLocalSetting("rep_rot_threshold", Integer.valueOf(10))) {
                     result.setMod(5).addTag("type B");
                     stats.setStat("repRotVl", 0);
                  }
               }

               stats.setStat("pkLastYaw", yaw);
               stats.setStat("pkLastPitch", pitch);
               mp.addToTimings();
               return result;
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
