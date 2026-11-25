package me.darksidecode.easysockets.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.darksidecode.easysockets.event.EventManager;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.event.server.socket.SocketDisconnectEvent;
import me.darksidecode.easysockets.event.server.socket.SocketPacketReceiveEvent;
import me.darksidecode.easysockets.event.server.socket.SocketPacketSendEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.server.config.SocketServerConfig;
import me.darksidecode.easysockets.util.LimitedList;
import me.darksidecode.easysockets.util.Timer;
import me.darksidecode.easysockets.util.io.Compressor;

public class ProtocolManager {
   private final BasicSocketServer server;
   private final Map<Socket, Object> sendLocks = new ConcurrentHashMap<>();
   private final Map<Socket, Object> receiveLocks = new ConcurrentHashMap<>();
   private final Map<Socket, DataOutputStream> dosMap = new ConcurrentHashMap<>();
   private final List<Packet> recentSent = new LimitedList<>(100);
   private final List<Packet> recentReceived = new LimitedList<>(100);
   private final int protocolVersion;
   private final ServerSocket socket;
   private final ServerStats stats;
   private final Map<Socket, Timer> sendTrackers = new ConcurrentHashMap<>();
   private final Map<Socket, Timer> receiveTrackers = new ConcurrentHashMap<>();
   private final EventManager eventManager;
   private final SocketServerConfig config;

   public void sendPacketSynchronouslyLazily(Socket target, Packet packet) {
      if (target.isClosed()) {
         this.socketDisconnected(target);
      } else {
         SocketListener listenerThread = this.server.getSocketListeners().get(target);
         if (listenerThread != null) {
            listenerThread.addToSyncLazySendQueue(packet);
         } else {
            System.err
               .println(
                  "Unable to send packet "
                     + packet.toString()
                     + " synchronously lazily to "
                     + target.getInetAddress().getHostAddress()
                     + ": no listener thread bound"
               );
         }
      }
   }

   public void sendPacket(Socket target, Packet packet) {
      if (target.isClosed()) {
         this.socketDisconnected(target);
      } else {
         Timer sendTracker = this.getSendTracker(target);
         SocketPacketSendEvent e = new SocketPacketSendEvent(EventState.PRE, target, packet);
         if (!this.eventManager.callEvent(e)) {
            packet = e.getPacket();
            long debugId = System.nanoTime() % 1000L;
            long beginTime = System.currentTimeMillis();

            try {
               packet.writeAll();
               long serBegin = System.currentTimeMillis();
               byte[] bytesToSend = packet.send();
               long serEnd = System.currentTimeMillis();

               try {
                  long dosBegin = System.currentTimeMillis();
                  DataOutputStream dataOut = this.getDataOutputStream(target);
                  long dosEnd = System.currentTimeMillis();
                  if (dataOut == null) {
                     return;
                  }

                  long wintBegin = System.currentTimeMillis();
                  dataOut.writeInt(bytesToSend.length);
                  long wintEnd = System.currentTimeMillis();
                  long wbytesBegin = System.currentTimeMillis();
                  dataOut.write(bytesToSend);
                  long wbytesEnd = System.currentTimeMillis();
                  dataOut.flush();
                  this.stats.bytesSent += (long)bytesToSend.length;
                  long endTime = System.currentTimeMillis();
                  long elapsed = endTime - beginTime;
               } catch (SocketException ignored) {
                  this.socketDisconnected(target);
                  return;
               }

               this.recentSent.add(packet);
               sendTracker.reset();
               this.eventManager.callEvent(new SocketPacketSendEvent(EventState.POST, target, packet));
            } catch (Exception ex) {
               System.err.println("### pain in the ass ###");
               System.err.println("socket: " + target.getInetAddress().getHostAddress() + ", packet: " + packet.toString() + ", stacktrace:");
               ex.printStackTrace();
            }
         }
      }
   }

   public void receivePacket(Socket sender, Packet packet) {
      SocketPacketReceiveEvent e = new SocketPacketReceiveEvent(EventState.PRE, sender, packet);
      if (!this.eventManager.callEvent(e)) {
         (packet = e.getPacket()).process();
         this.recentReceived.add(packet);
         this.eventManager.callEvent(new SocketPacketReceiveEvent(EventState.POST, sender, packet));
      }
   }

   public void socketDisconnected(Socket s) {
      this.receiveTrackers.remove(s);
      this.receiveLocks.remove(s);
      this.sendTrackers.remove(s);
      this.sendLocks.remove(s);
      this.dosMap.remove(s);
      this.eventManager.callEvent(new SocketDisconnectEvent(EventState.POST, s));
   }

   private DataOutputStream getDataOutputStream(Socket s) {
      return this.dosMap.computeIfAbsent(s, k -> {
         try {
            return new DataOutputStream(Compressor.newDeflateOutput(s.getOutputStream()));
         } catch (SocketException ignored) {
            this.socketDisconnected(s);
            return null;
         } catch (IOException ex) {
            throw new RuntimeException("Failed to obtain DOS of " + s.getInetAddress().getHostAddress(), ex);
         }
      });
   }

   private Timer getSendTracker(Socket s) {
      return this.sendTrackers.computeIfAbsent(s, k -> new Timer());
   }

   Timer getReceiveTracker(Socket s) {
      return this.receiveTrackers.computeIfAbsent(s, k -> new Timer(0L));
   }

   public ProtocolManager(
      BasicSocketServer server, int protocolVersion, ServerSocket socket, ServerStats stats, EventManager eventManager, SocketServerConfig config
   ) {
      this.server = server;
      this.protocolVersion = protocolVersion;
      this.socket = socket;
      this.stats = stats;
      this.eventManager = eventManager;
      this.config = config;
   }

   Map<Socket, Object> getSendLocks() {
      return this.sendLocks;
   }

   Map<Socket, Object> getReceiveLocks() {
      return this.receiveLocks;
   }

   public Map<Socket, DataOutputStream> getDosMap() {
      return this.dosMap;
   }

   public List<Packet> getRecentSent() {
      return this.recentSent;
   }

   public List<Packet> getRecentReceived() {
      return this.recentReceived;
   }

   Map<Socket, Timer> getSendTrackers() {
      return this.sendTrackers;
   }

   Map<Socket, Timer> getReceiveTrackers() {
      return this.receiveTrackers;
   }
}
