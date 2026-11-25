package rip.reflex.util.protocol.compatibility;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rip.reflex.util.protocol.packet.Packets;

public class CompatibilityProvider_1_9 extends CompatibilityProvider_1_8 {
   @Override
   public boolean isItemInHand(Player p, Material itemType) {
      return p.getInventory().getItemInMainHand().getType() == itemType || p.getInventory().getItemInOffHand().getType() == itemType;
   }

   @Override
   public boolean isBlockInHand(Player p) {
      Material mainItem = p.getInventory().getItemInMainHand().getType();
      if (mainItem != Material.AIR && mainItem.isBlock()) {
         return true;
      } else {
         Material offItem = p.getInventory().getItemInOffHand().getType();
         return offItem != Material.AIR && offItem.isBlock();
      }
   }

   @Override
   public boolean isBlockPlacePacket(PacketContainer packet) {
      return packet.getType().equals(Packets.IN_USEITEM);
   }
}
