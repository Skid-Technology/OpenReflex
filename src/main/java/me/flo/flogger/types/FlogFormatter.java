package me.flo.flogger.types;

import java.text.SimpleDateFormat;
import java.util.Collections;

public interface FlogFormatter {
   FlogFormatter DEFAULT = new FlogFormatter.Default();

   String format(FlogRecord record);

   public static final class Default implements FlogFormatter {
      private final SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
      private FlogRecord cacheRecord;
      private String cacheFormatted;

      @Override
      public String format(FlogRecord record) {
         if (this.cacheRecord != null && this.cacheRecord.equals(record)) {
            return this.cacheFormatted;
         } else {
            this.cacheRecord = record;
            int i = 5 - record.getLevel().name().length();
            return this.cacheFormatted = '['
               + this.format.format(Long.valueOf(record.getTimestamp()))
               + " -> "
               + record.getLevel().name()
               + String.join("", Collections.nCopies(i, " "))
               + "]: "
               + record.getMessage();
         }
      }

      private Default() {
      }
   }
}
