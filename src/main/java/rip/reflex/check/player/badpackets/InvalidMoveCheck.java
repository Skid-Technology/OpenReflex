package rip.reflex.check.player.badpackets;

import com.comphenix.protocol.events.PacketContainer;
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
import rip.reflex.util.time.Timer;

public class InvalidMoveCheck extends PacketCheck {
   public InvalidMoveCheck() {
      super("BadPackets", "badpcks.inv", Cheat.valueOf("BadPackets"));
      this.setLagSensetive(false);
      LoopHelper.loopStatsEvery(stats -> {
         stats.setStat("accurateMoveVl", 0);
         stats.setStat("illegalPoslookVl", 0);
         stats.setStat("groundcheckVl", 0);
      }, 200);
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("QH-99821658");
         if (packet.getType() != Packets.IN_POSLOOK && packet.getType() != Packets.IN_POSITION) {
            return null;
         } else {
            CheckResult result = new CheckResult();
            if (!stats.hasTimerPassed("worldChangeTracker", 5000L)) {
               return null;
            } else {
               double x = data.getX();
               double y = data.getY();
               double z = data.getZ();
               double dX = Math.abs(x - stats.getStat("pkLastX", double.class));
               double dY = Math.abs(y - stats.getStat("pkLastY", double.class));
               double dZ = Math.abs(z - stats.getStat("pkLastZ", double.class));
               if (stats.getStat("teleportTracker", Timer.class).hasPassed(1000L)) {
                  if (dX != 0.0 && dX % 1.0 == 0.0 || dZ != 0.0 && dZ % 1.0 == 0.0) {
                     stats.setStat("accurateMoveVl", stats.getStat("accurateMoveVl", int.class) + 1);
                     if (stats.getStat("accurateMoveVl", int.class) > this.getLocalSetting("acc_move_threshold", Integer.valueOf(5))) {
                        result.setMod(2).addTag("type C").cancel(this.isNotSilent());
                        stats.setStat("accurateMoveVl", 0);
                     }
                  }

                  if (dX == 0.0
                     && dY == 0.0
                     && dZ == 0.0
                     && packet.getType().equals(Packets.IN_POSLOOK)
                     && LocationUtils.isPushable(p)
                     && data.getYaw() == stats.getStat("pkLastYaw", float.class)
                     && data.getPitch() == stats.getStat("pkLastPitch", float.class)) {
                     stats.setStat("illegalPoslookVl", stats.getStat("illegalPoslookVl", int.class) + 1);
                     if (stats.getStat("illegalPoslookVl", int.class) > this.getLocalSetting("illegal_poslook_threshold", Integer.valueOf(10))) {
                        result.setMod(3).addTag("type D").cancel(this.isNotSilent());
                        stats.setStat("illegalPoslookVl", 0);
                     }
                  }
               }

               stats.setStat("pkLastX", x);
               stats.setStat("pkLastY", y);
               stats.setStat("pkLastZ", z);
               stats.setStat("pkLastDX", dX);
               stats.setStat("pkLastDY", dY);
               stats.setStat("pkLastDZ", dZ);
               mp.addToTimings();
               return result;
            }
         }
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
