package rip.reflex.util.protocol.npc;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public interface HitListener {
   void onHit(Player player, int entityId);

   default void onHit(Player hitter, int entityId, PacketContainer packet) {
   }
}
