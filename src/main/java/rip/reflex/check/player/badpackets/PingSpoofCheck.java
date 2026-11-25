package rip.reflex.check.player.badpackets;

import com.comphenix.protocol.events.PacketContainer;
import java.util.SplittableRandom;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.SafeLong;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class PingSpoofCheck extends PacketCheck {
   private final SplittableRandom random = new SplittableRandom();

   public PingSpoofCheck() {
      super("BadPackets", "badpcks.spf", Cheat.valueOf("BadPackets"));
      this.setLagSensetive(false);
      this.setupVlResetThread(45);
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("LV-291874187");
         CheckResult result = new CheckResult();
         if (!stats.getStat("teleportTracker", Timer.class).hasPassed(20000L)) {
            stats.setStat("pingspoofViolations", 0);
            stats.setViolations(this.cheat, 0);
            return null;
         } else if (packet.getType().equals(Packets.IN_KEEPALIVE)) {
            stats.setStat("ps_nextId", new SafeLong(-1L));
            return null;
         } else {
            if (packet.getType().equals(Packets.IN_TRANSACTION) && stats.getSafeLong("ps_nextId").get() != -1L) {
               stats.setStat("pingspoofViolations", stats.getStat("pingspoofViolations", int.class) + 3);
               if (stats.getStat("pingspoofViolations", int.class) > this.getLocalSetting("latency_threshold", Integer.valueOf(11))) {
                  stats.setStat("pingspoofViolations", 0);
                  result.setMod(8).addTag("type A");
               }
            } else if (packet.getType().equals(Packets.IN_TRANSACTION) && stats.getStat("pingspoofViolations", int.class) > 0) {
               stats.setStat("pingspoofViolations", stats.getStat("pingspoofViolations", int.class) - 1);
            }

            mp.addToTimings();
            return result;
         }
      }
   }

   @Override
   public CheckResult checkOut(final Player p, final Stats stats, PacketContainer packet) {
      MethodProfiler mp = new MethodProfiler("JO-67198912");
      if (!stats.getStat("teleportTracker", Timer.class).hasPassed(20000L)) {
         stats.setStat("pingspoofViolations", 0);
         stats.setViolations(this.cheat, 0);
         return null;
      } else {
         if (stats.getStat("ps_next", boolean.class)) {
            stats.setStat("ps_next", false);
            final PacketContainer transPacket = new PacketContainer(Packets.OUT_TRANSACTION);
            short tid = (short)((short)this.random.nextInt(32767) * (this.random.nextBoolean() ? 1 : -1));
            transPacket.getBytes().writeSafely(0, (byte)0);
            transPacket.getShorts().writeSafely(0, tid);
            transPacket.getBooleans().writeSafely(0, false);
            ReflexScheduler.sync().run(new ReflexRunnable("JO-219372189") {
               @Override
               public void execute() throws Exception {
                  Packets.sendPacketAsync(p, transPacket);
                  stats.getStat("ps_transactionRequestTracker", Timer.class).reset();
               }
            });
         }

         if (packet.getType().equals(Packets.OUT_KEEPALIVE)) {
            stats.setStat("ps_next", true);
            stats.setStat("ps_nextId", this.readKeepAliveID(packet));
            stats.getStat("ps_keepAliveRequestTracker", Timer.class).reset();
         }

         mp.addToTimings();
         return null;
      }
   }

   private SafeLong readKeepAliveID(PacketContainer p) {
      return p.getIntegers().size() == 0 ? new SafeLong(Packets.readLong(p, 0)) : new SafeLong(Packets.readInteger(p, 0));
   }
}
