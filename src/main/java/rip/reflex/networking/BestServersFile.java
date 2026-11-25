package rip.reflex.networking;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import me.darksidecode.kantanj.types.Check;
import rip.reflex.Reflex;

public class BestServersFile {
   private static final String FILE_NAME = "best_servers_cache.dat";
   private static final long CACHE_RESET_TIME = TimeUnit.DAYS.toMillis(5L);
   private ReflexServer syncServer;
   private ReflexServer cloudServer;

   public BestServersFile(Reflex reflex) {
      File file = reflex.getReflexFile("best_servers_cache.dat");
      if (file.exists()) {
         try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String contents = new String(bytes, StandardCharsets.UTF_8).trim().toUpperCase();
            if (!contents.contains(":")) {
               throw new IllegalArgumentException("invalid file format: unsplittable");
            }

            String[] data = contents.split(":");
            if (data.length != 3) {
               throw new IllegalArgumentException("invalid file format: invalid data length " + data.length);
            }

            long created = Long.parseLong(data[0]);
            if (System.currentTimeMillis() < created + CACHE_RESET_TIME) {
               this.syncServer = ReflexServer.valueOf(data[1]);
               this.cloudServer = ReflexServer.valueOf(data[2]);
               reflex.info("Loaded best servers from cache: " + this.syncServer + "/" + this.cloudServer);
            } else {
               file.delete();
               reflex.info("Cache best_servers_cache.dat is out of date. Will have to find the best servers again! (might take some time)");
            }
         } catch (Exception ex) {
            file.delete();
            this.syncServer = this.cloudServer = null;
            this.error(reflex, ex);
         }
      } else {
         this.syncServer = this.cloudServer = null;
      }
   }

   public boolean isValid() {
      return this.syncServer != null && this.cloudServer != null;
   }

   private void error(Reflex reflex, Exception ex) {
      reflex.warn(
         "Failed to read best_servers_cache.dat. Will have to find the best servers again! (might take some time). The error that occurred was: "
            + ex.toString()
      );
   }

   public static void create(Reflex reflex) {
      ReflexServer syncServer = Check.notNull(reflex.getSyncServer(), "sync server is not determined");
      ReflexServer cloudServer = Check.notNull(reflex.getCloudServer(), "cloud server is not determined");
      String s = System.currentTimeMillis() + ":" + syncServer.name() + ":" + cloudServer.name();
      File file = reflex.getReflexFile("best_servers_cache.dat");
      if (file.exists()) {
         file.delete();
      }

      reflex.info("Caching best servers data: " + syncServer + "/" + cloudServer + " (5 days valid)");

      try {
         byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
         Files.write(file.toPath(), bytes);
      } catch (Exception e) {
         reflex.warn("Failed to save best_servers_cache.dat");
         e.printStackTrace();
      }
   }

   public ReflexServer getSyncServer() {
      return this.syncServer;
   }

   public ReflexServer getCloudServer() {
      return this.cloudServer;
   }
}
