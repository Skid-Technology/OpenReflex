package me.darksidecode.easysockets.event.client;

import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventState;

public class ClientStartEvent extends Event {
   private final long time;

   public ClientStartEvent(EventState state, long time) {
      super(state);
      this.time = time;
   }

   public long getTime() {
      return this.time;
   }
}
