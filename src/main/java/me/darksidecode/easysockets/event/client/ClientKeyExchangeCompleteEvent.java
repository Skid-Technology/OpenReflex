package me.darksidecode.easysockets.event.client;

import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventState;

public class ClientKeyExchangeCompleteEvent extends Event {
   public ClientKeyExchangeCompleteEvent() {
      super(EventState.POST);
   }
}
