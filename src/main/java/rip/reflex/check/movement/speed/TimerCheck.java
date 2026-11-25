package rip.reflex.check.movement.speed;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.time.Timer;

public class TimerCheck extends PacketCheck {
   public TimerCheck() {
      super("Speed", "speed.tmr", Cheat.valueOf("Speed"));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (!Packets.isUpdatePacket(packet)) {
         return null;
      } else {
         CheckResult result = new CheckResult();
         if (!stats.getStat("joinTracker", Timer.class).hasPassed(3000L)) {
            this.reset(stats);
            return null;
         } else {
            stats.setStat("packetCounter", stats.getStat("packetCounter", int.class) + 1);
            long ticksSinceStart = stats.getStat("lifeStartTracker", Timer.class).getTimePassed() / 50L;
            result.addTag("t: " + ticksSinceStart);
            result.addTag("p: " + stats.getStat("packetCounter", int.class));
            if ((long)stats.getStat("packetCounter", int.class).intValue() > ticksSinceStart) {
               result.setMod(10);
               result.cancel(true);
               stats.setback();
               this.reset(stats);
            }

            return result;
         }
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onTeleport(PlayerTeleportEvent e) {
      Stats stats = Stats.of(e.getPlayer());
      if (!e.isCancelled() && stats.getStat("packetCounter", int.class) > 0) {
         stats.setStat("packetCounter", stats.getStat("packetCounter", int.class) - 1);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onJoin(PlayerJoinEvent e) {
      Bukkit.getScheduler().runTaskLaterAsynchronously(this.reflex, () -> this.reset(Stats.of(e.getPlayer())), 5L);
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onWorldChange(PlayerChangedWorldEvent e) {
      this.reset(Stats.of(e.getPlayer()));
   }

   private void reset(Stats stats) {
      stats.getStat("lifeStartTracker", Timer.class).setStartTime(System.currentTimeMillis() - 128L);
      stats.setStat("packetCounter", 0);
   }
}
