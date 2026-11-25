package me.darksidecode.kantanj.time;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import me.darksidecode.kantanj.types.Check;

public class ExpiringList {
   private final long entryLifespanMillis;
   private final List<Long> entries;
   private final Object lock = new Object();

   public ExpiringList(long singleEntryLifespan, TimeUnit lifespanUnit) {
      if (singleEntryLifespan < 1L) {
         throw new IllegalArgumentException("invalid singleEntryLifespan: " + singleEntryLifespan);
      } else {
         this.entryLifespanMillis = Check.notNull(lifespanUnit, "lifespanUnit cannot be null").toMillis(singleEntryLifespan);
         this.entries = new ArrayList<>();
      }
   }

   public int updateAndCount() {
      synchronized (this.lock) {
         List<Long> expired = new ArrayList<>();
         this.entries.stream().filter(e -> System.currentTimeMillis() > e + this.entryLifespanMillis).forEach(expired::add);
         expired.forEach(this.entries::remove);
         this.entries.add(System.currentTimeMillis());
         return this.entries.size();
      }
   }

   @Deprecated
   public int count() {
      synchronized (this.lock) {
         return this.entries.size();
      }
   }
}
