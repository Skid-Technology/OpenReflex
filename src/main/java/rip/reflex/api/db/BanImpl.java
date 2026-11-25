package rip.reflex.api.db;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import rip.reflex.api.BanInfo;

public interface BanImpl {
   default void init() {
   }

   default void off() {
   }

   boolean ban(UUID uuid, String message, long time, TimeUnit timeUnit);

   boolean unban(UUID uuid);

   BanInfo getBanInfo(UUID uuid);

   int getBanCount();

   int getBannedCountIn(TimeUnit timeUnit, long time);
}
