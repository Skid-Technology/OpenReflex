package me.darksidecode.easysockets.event.server.socket;

import java.net.Socket;
import me.darksidecode.easysockets.event.EventState;

public class SocketConnectEvent extends SocketEvent {
   public SocketConnectEvent(EventState state, Socket socket) {
      super(state, socket);
   }
}
