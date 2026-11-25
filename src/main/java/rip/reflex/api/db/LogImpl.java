package rip.reflex.api.db;

import me.flo.flogger.types.FlogLevel;

public interface LogImpl {
   default void init() {
   }

   default void off() {
   }

   void log(FlogLevel level, String message, Object... args);

   String getExecutionID();

   long getStarttime();
}
