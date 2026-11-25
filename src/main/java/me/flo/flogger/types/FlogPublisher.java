package me.flo.flogger.types;

public abstract class FlogPublisher {
   private FlogLevel level;
   private FlogFormatter formatter;

   public void handleIntern(FlogRecord record) {
      if (record.getLevel().isLoggable(this.level)) {
         this.handle(record, this.formatter);
      }
   }

   protected abstract void handle(FlogRecord record, FlogFormatter formatter);

   public FlogPublisher level(FlogLevel level) {
      this.level = level;
      return this;
   }

   public FlogPublisher formatter(FlogFormatter formatter) {
      this.formatter = formatter;
      return this;
   }

   public FlogLevel getLevel() {
      return this.level;
   }

   public FlogFormatter getFormatter() {
      return this.formatter;
   }
}
