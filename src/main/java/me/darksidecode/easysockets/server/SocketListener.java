package me.darksidecode.easysockets.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import me.darksidecode.easysockets.event.EventCancelledException;
import me.darksidecode.easysockets.event.EventManager;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.event.server.socket.SocketConnectEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.server.config.SocketServerConfig;
import me.darksidecode.easysockets.util.io.Compressor;

class SocketListener extends Thread {
   private final BasicSocketServer server;
   private final Socket socket;
   private final EventManager eventManager;
   private final ProtocolManager protocolManager;
   private final ServerStats stats;
   private final SocketServerConfig config;
   private boolean closed;
   private boolean error;
   private final List<Packet> syncLazySendQueue = new ArrayList<>();
   private final Object syncLazySendQueueLock = new Object();

   SocketListener(
      BasicSocketServer server, Socket socket, ProtocolManager protocolManager, ServerStats stats, EventManager eventManager, SocketServerConfig config
   ) throws IOException {
      this.setName(socket.getInetAddress().getHostName() + " Socket Listener Thread #" + System.nanoTime());
      this.server = server;
      this.socket = socket;
      this.config = config;
      this.eventManager = eventManager;
      this.protocolManager = protocolManager;
      this.stats = stats;
      if (eventManager.callEvent(new SocketConnectEvent(EventState.PRE, socket))) {
         if (!socket.isClosed()) {
            socket.close();
         }

         throw new EventCancelledException();
      }
   }

   @Override
   public void run() {
      this.establishConnection();

      try {
         DataInputStream in = new DataInputStream(Compressor.newInflateInput(this.socket.getInputStream()));

         while (this.socket.isConnected() && !this.socket.isClosed() && !this.closed) {
            int len = in.readInt();
            if (len > 0 && len <= this.config.getMaxPacketSize()) {
               byte[] bytes = new byte[len];
               in.readFully(bytes, 0, len);
               this.stats.bytesReceived += (long)bytes.length;
               Packet packet = Packet.receivePacket(bytes);
               this.protocolManager.receivePacket(this.socket, packet);
            }

            synchronized (this.syncLazySendQueueLock) {
               int i = 0;

               for (Packet packet : this.syncLazySendQueue) {
                  try {
                     this.protocolManager.sendPacket(this.socket, packet);
                  } catch (Exception ex) {
                     System.err
                        .println(
                           "Failed to send packet "
                              + packet.toString()
                              + " (index "
                              + i
                              + ") synchronously to "
                              + this.socket.getInetAddress().getHostAddress()
                              + " (through socket listener thread: "
                              + this.getName()
                              + "). Stacktrace:"
                        );
                     ex.printStackTrace();
                  } finally {
                     i++;
                  }
               }

               this.syncLazySendQueue.clear();
            }
         }

         if (!this.error && !this.closed) {
            this.end(null);
         }
      } catch (IOException e) {
         this.end(new RuntimeException("Exception in " + this.getName(), e));
      }
   }

   void addToSyncLazySendQueue(Packet packet) {
      synchronized (this.syncLazySendQueueLock) {
         this.syncLazySendQueue.add(packet);
      }
   }

   private void establishConnection() {
      try {
         this.socket.setSoTimeout(this.config.getTimeout());
         this.stats.connectionsRegistered++;
         this.protocolManager.getReceiveLocks().put(this.socket, new Object());
         this.protocolManager.getSendLocks().put(this.socket, new Object());
         this.socket.setTcpNoDelay(true);
         this.socket.setKeepAlive(true);
      } catch (Exception ex) {
         throw new RuntimeException("Failed to setup a socket listener", ex);
      }

      this.eventManager.callEvent(new SocketConnectEvent(EventState.POST, this.socket));
   }

   void end(Throwable problem) {
      if (!this.closed || problem != null) {
         this.error = problem != null;
         this.closed = true;
         if (this.error) {
            problem.printStackTrace();
         }

         if (!this.socket.isClosed()) {
            try {
               this.socket.close();
            } catch (Exception ignored) {
            }
         }

         this.protocolManager.socketDisconnected(this.socket);
         this.server.addToKickQueue(this.socket);
      }
   }

   void endQuietly() {
      this.closed = true;
      if (!this.socket.isClosed()) {
         try {
            this.socket.close();
         } catch (Exception ignoredEx) {
         }
      }

      this.protocolManager.socketDisconnected(this.socket);
   }

   public boolean isClosed() {
      return this.closed;
   }

   public boolean isError() {
      return this.error;
   }

   public void setClosed(boolean closed) {
      this.closed = closed;
   }

   public void setError(boolean error) {
      this.error = error;
   }
}
