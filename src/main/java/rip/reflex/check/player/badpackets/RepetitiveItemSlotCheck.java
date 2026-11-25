package rip.reflex.check.player.badpackets;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class RepetitiveItemSlotCheck extends PacketCheck {
   public RepetitiveItemSlotCheck() {
      super("BadPackets", "badpcks.ris", Cheat.valueOf("BadPackets"));
      LoopHelper.loopStatsEvery(stats -> {
         stats.subtractNumberTilPositive("repetitiveItemSlotVl", 1);
      }, 300);
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         if (packet.getType().equals(Packets.IN_HELDITEMCHANGE)) {
            int newSlot = (Integer)packet.getIntegers().read(0);
            if (stats.hasStat("lastHeldItemSlot")) {
               int oldSlot = stats.getInt("lastHeldItemSlot");
               if (newSlot == oldSlot && stats.addNumber("repetitiveItemSlotVl", 1) > 2) {
                  return new CheckResult().setMod(10).addTag("type E");
               }
            }

            stats.setStat("lastHeldItemSlot", newSlot);
         }

         return null;
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
