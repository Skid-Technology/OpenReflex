package rip.reflex.util.protocol.npc;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import rip.reflex.Reflex;
import rip.reflex.util.protocol.packet.Packets;

public class HitPacketListener {
   public HitPacketListener() {
      NPCCommons.getProtocolManager().addPacketListener(new PacketAdapter(Reflex.getReflex(), Client.USE_ENTITY) {
         public void onPacketReceiving(PacketEvent event) {
            if (event != null) {
               try {
                  if (!Packets.isAttackPacket(event.getPacket())) {
                     return;
                  }

                  int id = Packets.readInteger(event.getPacket(), 0);

                  try {
                     for (HitListener hitListener : NPCCommons.getListeners()) {
                        hitListener.onHit(event.getPlayer(), id);
                     }

                     for (HitListener hitListener : NPCCommons.getListeners()) {
                        hitListener.onHit(event.getPlayer(), id, event.getPacket());
                     }
                  } catch (Exception ignored) {
                  }
               } catch (Exception ignoredEx) {
               }
            }
         }
      });
   }
}
