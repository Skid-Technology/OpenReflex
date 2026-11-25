package rip.reflex.networking;

import java.io.IOException;
import rip.reflex.Reflex;

public final class ReflexNetworking {
   private static final String TEST_FILE_PATH = "fa5f24a90d13d1d396ddae2cba49378b/data.xls";

   private ReflexNetworking() {
   }

   public static ReflexServer findSyncServer() {
      ReflexServer result = null;
      double highest = Double.MIN_VALUE;

      for (ReflexServer server : ReflexServer.values()) {
         if (server.isSyncActive()) {
            try {
               double speed = server.getSyncSpeedInMBs("fa5f24a90d13d1d396ddae2cba49378b/data.xls");
               if (speed > highest) {
                  result = server;
                  highest = speed;
               }
            } catch (IOException ex) {
               Reflex.getReflex().warn("Cannot establish connect to sync server with url " + server.getSyncUrl());
               ex.printStackTrace();
            }
         }
      }

      return result;
   }

   public static ReflexServer findCloudServer() {
      ReflexServer result = null;
      double lowest = Double.MAX_VALUE;

      for (ReflexServer server : ReflexServer.values()) {
         if (server.isCloudActive()) {
            try {
               double ping = (double)server.getCloudPing();
               if (ping < lowest) {
                  result = server;
                  lowest = ping;
               }
            } catch (IOException e) {
               e.printStackTrace();
               Reflex.getReflex().warn("Cannot establish connect to cloud server with host " + server.getCloudHost());
               e.printStackTrace();
            }
         }
      }

      return result;
   }
}
