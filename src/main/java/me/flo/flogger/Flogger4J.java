package me.flo.flogger;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import me.flo.flogger.types.FlogFormatter;
import me.flo.flogger.types.FlogLevel;
import me.flo.flogger.types.FlogPublisher;
import me.flo.flogger.types.FlogRecord;

public class Flogger4J {
   private static Flogger4J global;
   private final String name;
   private FlogLevel level = FlogLevel.INFO;
   private FlogFormatter formatter = FlogFormatter.DEFAULT;
   private final List<FlogPublisher> publishers = new ArrayList<>();

   public void log(FlogLevel level, String message) {
      if (level == FlogLevel.OFF) {
         throw new IllegalArgumentException("Level can't be OFF");
      } else {
         FlogRecord record = new FlogRecord(this, level, message, System.currentTimeMillis());
         this.publishers.forEach(fp -> fp.handleIntern(record));
      }
   }

   public void error(String message) {
      this.log(FlogLevel.ERROR, message);
   }

   public void warning(String message) {
      this.log(FlogLevel.WARN, message);
   }

   public void info(String message) {
      this.log(FlogLevel.INFO, message);
   }

   public void debug(String message) {
      this.log(FlogLevel.DEBUG, message);
   }

   public void trace(String message) {
      this.log(FlogLevel.TRACE, message);
   }

   public void addPublisher(FlogPublisher publisher) {
      if (publisher.getLevel() == null) {
         publisher.level(this.level);
      }

      if (publisher.getFormatter() == null) {
         publisher.formatter(this.formatter);
      }

      this.publishers.add(publisher);
   }

   public static Flogger4J initializeGlobal(String name) {
      return global = new Flogger4J(name);
   }

   public static Flogger4J global() {
      return global;
   }

   @ConstructorProperties({"name"})
   public Flogger4J(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setLevel(FlogLevel level) {
      this.level = level;
   }

   public FlogLevel getLevel() {
      return this.level;
   }

   public void setFormatter(FlogFormatter formatter) {
      this.formatter = formatter;
   }

   public FlogFormatter getFormatter() {
      return this.formatter;
   }
}
