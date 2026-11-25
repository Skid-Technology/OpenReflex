package me.flo.flogger.types;

public enum FlogLevel {
   OFF,
   ERROR,
   WARN,
   INFO,
   DEBUG,
   TRACE;

   public boolean isLoggable(FlogLevel logLevel) {
      return this.ordinal() <= logLevel.ordinal();
   }
}
