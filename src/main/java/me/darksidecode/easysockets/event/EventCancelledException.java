package me.darksidecode.easysockets.event;

public class EventCancelledException extends RuntimeException {
   public EventCancelledException() {
   }

   public EventCancelledException(String message) {
      super(message);
   }

   public EventCancelledException(String message, Throwable cause) {
      super(message, cause);
   }

   public EventCancelledException(Throwable cause) {
      super(cause);
   }
}
