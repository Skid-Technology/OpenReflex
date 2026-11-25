package rip.reflex.cloud.listener;

import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventListener;
import me.darksidecode.easysockets.event.client.ClientPacketReceiveEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import rip.reflex.Reflex;
import rip.reflex.cloud.CloudService;
import rip.reflex.cloud.protocol.PingPacket;
import rip.reflex.util.math.Maths;

public class PingListener implements EventListener {
   @Override
   public void onEvent(Event event) {
      if (event.isPost() && event instanceof ClientPacketReceiveEvent) {
         ClientPacketReceiveEvent e = (ClientPacketReceiveEvent)event;
         Packet p = e.getPacket();
         if (p.getType().equals("RC_PING")) {
            CloudService service = Reflex.getReflex().getCloudService();
            PingPacket ping = new PingPacket(p);
            service.setPing(Math.max(1, Maths.floor((double)ping.getId() / 2.0)));
            service.getClient().getProtocolManager().sendPacket(ping);
            service.getPingTracker().reset();
         }
      }
   }
}
