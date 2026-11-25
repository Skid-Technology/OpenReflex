package me.darksidecode.easysockets.event.server.socket;

import java.net.Socket;
import me.darksidecode.easysockets.event.EventState;

public class SocketDisconnectEvent extends SocketEvent {
   public SocketDisconnectEvent(EventState state, Socket socket) {
      super(state, socket);
   }
}
