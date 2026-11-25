package rip.reflex.networking;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import rip.reflex.Reflex;

public enum ReflexServer {
   EU("EU/EU", "http://sync.reflex.rip/", "ccd.reflex.rip", true, true),
   USA("USA/China", "http://sync-yuki.reflex.rip/", "yuki.reflex.rip", true, true);

   private final String title;
   private final String syncUrl;
   private final String cloudHost;
   private final boolean syncActive;
   private final boolean cloudActive;

   public double getSyncSpeedInMBs(String path) throws IOException {
      URLConnection downloadConnection = new URL(this.syncUrl + path).openConnection();
      downloadConnection.setRequestProperty("User-Agent", "Reflex/" + Reflex.getReflex().getDescription().getVersion());
      int totalRead = 0;

      try (InputStream inputStream = downloadConnection.getInputStream()) {
         byte[] buffer = new byte[1024];
         long startTime = System.currentTimeMillis();

         int len;
         while ((len = inputStream.read(buffer)) != -1 && System.currentTimeMillis() - startTime < 10000L) {
            totalRead += len;
         }

         long stopTime = System.currentTimeMillis();
         double seconds = ((double)(stopTime - startTime) + 1.0) / 1000.0;
         double readMB = (double)totalRead / 1024.0 / 1024.0;
         return readMB / seconds;
      }
   }

   public long getCloudPing() throws IOException {
      long start = System.currentTimeMillis();
      if (InetAddress.getByName(this.cloudHost).isReachable(5000)) {
         long finish = System.currentTimeMillis();
         return finish - start;
      } else {
         return 2147483647L;
      }
   }

   public String getTitle() {
      return this.title;
   }

   public String getSyncUrl() {
      return this.syncUrl;
   }

   public String getCloudHost() {
      return this.cloudHost;
   }

   public boolean isSyncActive() {
      return this.syncActive;
   }

   public boolean isCloudActive() {
      return this.cloudActive;
   }

   private ReflexServer(String title, String syncUrl, String cloudHost, boolean syncActive, boolean cloudActive) {
      this.title = title;
      this.syncUrl = syncUrl;
      this.cloudHost = cloudHost;
      this.syncActive = syncActive;
      this.cloudActive = cloudActive;
   }
}
