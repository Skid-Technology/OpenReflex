package me.darksidecode.easysockets.event.server;

import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventState;

public class ServerStopEvent extends Event {
   private final long time;

   public ServerStopEvent(EventState state, long time) {
      super(state);
      this.time = time;
   }

   public long getTime() {
      return this.time;
   }
}
