package rip.reflex.cloud.listener;

import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventListener;
import me.darksidecode.easysockets.event.client.ClientPacketReceiveEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.cloud.CloudComputer;
import rip.reflex.cloud.ComputeEntry;
import rip.reflex.cloud.protocol.server.ComputeResponsePacket;
import rip.reflex.util.crypto.CloudEncoder;

public class ComputeResponseListener implements EventListener {
   @Override
   public void onEvent(Event event) {
      if (event.isPost() && event instanceof ClientPacketReceiveEvent) {
         ClientPacketReceiveEvent e = (ClientPacketReceiveEvent)event;
         Packet packet = e.getPacket();
         if (packet.getType().equals("RC_COMP_RES")) {
            ComputeResponsePacket response = new ComputeResponsePacket(packet);
            CloudComputer cc = Reflex.getReflex().getCloudService().getCloudComputer();
            long id = response.getRequestID();
            int checkID = response.getCheckID();
            ComputeEntry entry = cc.getAwaitingResponse().get(id);
            if (entry != null) {
               Player p = entry.getPlayer();
               if (p != null) {
                  cc.getAwaitingResponse().remove(id);
                  cc.process(p, checkID, CloudEncoder.decodeInner(response.getResult()), entry.getDebugML(), entry.getHeurSnapshot());
               }
            }
         }
      }
   }
}
