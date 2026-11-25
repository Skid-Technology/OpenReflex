package me.flo.flogger.types;

public class FlogException extends RuntimeException {
   public FlogException(String message, Throwable cause) {
      super(message, cause);
   }
}
