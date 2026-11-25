package rip.reflex.cloud.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventListener;
import me.darksidecode.easysockets.event.client.ClientPacketReceiveEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import rip.reflex.Reflex;
import rip.reflex.check.base.CheckLoader;
import rip.reflex.cloud.CloudComputer;
import rip.reflex.cloud.protocol.server.AuthResponsePacket;
import rip.reflex.util.machinelearning.Classifier;

public class AuthResponseListener implements EventListener {
   @Override
   public void onEvent(Event event) {
      if (event.isPost() && event instanceof ClientPacketReceiveEvent) {
         ClientPacketReceiveEvent e = (ClientPacketReceiveEvent)event;
         Packet p = e.getPacket();
         if (p.getType().equals("RC_AUTH_RES")) {
            AuthResponsePacket response = new AuthResponsePacket(p);
            String errorMessage = null;
            switch (response.getResponseCode()) {
               case 0:
                  Reflex.getReflex().info("Cloud connection has been established successfully");
                  Reflex.getReflex().getCloudService().setCloudComputer(new CloudComputer());
                  Reflex.getReflex().getCloudService().setReconnections(0);
                  long expireTime = response.getExpireDate();
                  long left = expireTime - System.currentTimeMillis();
                  String expireStr = Reflex.getReflex().getDateFormat().format(new Date(expireTime));
                  long daysLeft = TimeUnit.MILLISECONDS.toDays(left);
                  Reflex.getReflex().info("Your Cloud key expires " + expireStr);
                  if (left <= TimeUnit.DAYS.toMillis(3L)) {
                     Reflex.getReflex().warn("-----------------------------------------------");
                     Reflex.getReflex().warn("  WARNING:");
                     Reflex.getReflex().warn("  Your Cloud Computings key expires " + (daysLeft > 0L ? "in " + daysLeft + " day(s)" : "TODAY") + "!");
                     Reflex.getReflex().warn("  Don't forget to renew it if you wish to continue using this feature!");
                     Reflex.getReflex().warn("-----------------------------------------------");
                  }
                  break;
               case 1:
                  errorMessage = "invalid request";
                  break;
               case 2:
                  errorMessage = "invalid user";
                  break;
               case 3:
                  errorMessage = "unauthorized user (key banned)";
                  break;
               case 4:
                  errorMessage = "internal error, please report this";
                  break;
               case 5:
                  errorMessage = "the key is invalid or expired";
                  break;
               default:
                  errorMessage = "unknown error " + response.getResponseCode();
            }

            CheckLoader.loadAll(Reflex.getReflex());
            if (errorMessage == null) {
               Classifier.getNetlist().forEach(ArrayList::clear);
               Classifier.getNetlist().clear();
            } else {
               Reflex.getReflex().warn("Failed to connect to Cloud: " + errorMessage);
            }
         }
      }
   }
}
