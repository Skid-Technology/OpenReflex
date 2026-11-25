package me.darksidecode.easysockets.event.server.socket;

import java.net.Socket;
import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventState;

public class SocketEvent extends Event {
   private final Socket socket;

   public SocketEvent(EventState state, Socket socket) {
      super(state);
      this.socket = socket;
   }

   public Socket getSocket() {
      return this.socket;
   }
}
