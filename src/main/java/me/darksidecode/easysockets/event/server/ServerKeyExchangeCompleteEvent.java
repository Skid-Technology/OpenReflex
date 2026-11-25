package me.darksidecode.easysockets.event.server;

import java.net.Socket;
import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventState;

public class ServerKeyExchangeCompleteEvent extends Event {
   private final Socket client;

   public ServerKeyExchangeCompleteEvent(Socket client) {
      super(EventState.POST);
      this.client = client;
   }

   public Socket getClient() {
      return this.client;
   }
}
