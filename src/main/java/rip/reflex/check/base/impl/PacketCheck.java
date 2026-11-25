package rip.reflex.check.base.impl;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckType;
import rip.reflex.util.data.Stats;
import rip.reflex.util.protocol.packet.SharedPacketData;

public abstract class PacketCheck extends Check {
   public PacketCheck(String name, String comp, Cheat cheat) {
      super(name, comp, CheckType.PACKET, cheat);
   }

   public abstract CheckResult checkIn(Player player, Stats stats, PacketContainer packet, SharedPacketData data);

   public abstract CheckResult checkOut(Player player, Stats stats, PacketContainer packet);
}
