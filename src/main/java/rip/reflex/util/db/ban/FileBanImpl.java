package rip.reflex.util.db.ban;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import rip.reflex.api.BanInfo;
import rip.reflex.api.db.BanImpl;
import rip.reflex.util.misc.Misc;

public class FileBanImpl implements BanImpl {
   private final File file;
   private FileBanMap map = new FileBanMap();

   @Override
   public boolean ban(UUID uuid, String message, long time, TimeUnit timeUnit) {
      if (this.map.getMap().containsKey(uuid)) {
         return false;
      } else {
         this.map
            .getMap()
            .put(
               uuid,
               new FileBanMap.InternalBanInfo(time == -1L ? -1L : System.currentTimeMillis() + timeUnit.toMillis(time), System.currentTimeMillis(), message)
            );
         return true;
      }
   }

   @Override
   public boolean unban(UUID uuid) {
      if (!this.map.getMap().containsKey(uuid)) {
         return false;
      } else {
         this.map.getMap().remove(uuid);
         return true;
      }
   }

   @Override
   public BanInfo getBanInfo(UUID uuid) {
      Objects.requireNonNull(this.map, "fbm");
      Objects.requireNonNull(this.map.getMap(), "fbm-m");
      FileBanMap.InternalBanInfo ib = this.map.getMap().get(uuid);
      if (ib == null) {
         return BanInfo.NOT_BANNED;
      } else if (ib.shallBeUnbanned()) {
         this.unban(uuid);
         return BanInfo.NOT_BANNED;
      } else {
         return new BanInfo(ib.getMessage(), ib.getEndOfBan() == -1L ? -1L : ib.getEndOfBan() - System.currentTimeMillis(), ib.getBanStart());
      }
   }

   @Override
   public int getBanCount() {
      return this.map.getMap().size();
   }

   @Override
   public int getBannedCountIn(TimeUnit unit, long value) {
      long timeStamp = System.currentTimeMillis() - unit.toMillis(value);
      return (int)this.map.getMap().entrySet().stream().filter(entry -> entry.getValue().getBanStart() > value).count();
   }

   @Override
   public void init() {
      if (this.file.exists()) {
         try {
            this.map = (FileBanMap)new Gson().fromJson(new String(Files.readAllBytes(this.file.toPath())), FileBanMap.class);
            Misc.lif(this.map == null, () -> this.map = new FileBanMap());
            Misc.lif(this.map.getMap() == null, () -> this.map.setMap());
            Objects.requireNonNull(this.map, "FileBanMap cannot be null");
         } catch (IOException ex) {
            ex.printStackTrace();
         }
      }
   }

   @Override
   public void off() {
      FileWriter w = null;

      try {
         Files.deleteIfExists(this.file.toPath());
         w = new FileWriter(this.file);
         new GsonBuilder().setPrettyPrinting().create().toJson(this.map, FileBanMap.class, w);
         w.flush();
      } catch (IOException e) {
         e.printStackTrace();

         try {
            w.close();
         } catch (IOException err) {
            err.printStackTrace();
         }
      }
   }

   public FileBanImpl(File file) {
      this.file = file;
   }
}
