package rip.reflex.check.movement.speed;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class SneakCheck extends PacketCheck {
   public SneakCheck() {
      super("Speed", "speed.snk", Cheat.valueOf("Speed"));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      CheckResult result = new CheckResult();
      if (Packets.isSneakPacket(packet)) {
         stats.setStat("sneakTogglesPerSecond", stats.getStat("sneakTogglesPerSecond", int.class) + 1);
      } else if (Packets.isUpdatePacket(packet) && stats.getStat("hasSecondTicked", boolean.class)) {
         if (stats.getStat("sneakTogglesPerSecond", int.class) >= 40) {
            if (!this.getLocalSetting("silent", Boolean.valueOf(false))) {
               stats.setback();
            }

            result.setMod(19);
         }

         result.addTag("sneak toggle rate: " + stats.getStat("sneakTogglesPerSecond", int.class));
         stats.setStat("sneakTogglesPerSecond", 0);
      }

      return result;
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
