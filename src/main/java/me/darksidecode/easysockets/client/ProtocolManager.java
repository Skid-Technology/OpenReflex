package me.darksidecode.easysockets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import me.darksidecode.easysockets.client.config.SocketClientConfig;
import me.darksidecode.easysockets.event.EventManager;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.event.client.ClientPacketReceiveEvent;
import me.darksidecode.easysockets.event.client.ClientPacketSendEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.util.LimitedList;
import me.darksidecode.easysockets.util.Timer;

public class ProtocolManager {
   private final Object sendLock = new Object();
   private final Object receiveLock = new Object();
   private final List<Packet> recentSent = new LimitedList<>(100);
   private final List<Packet> recentReceived = new LimitedList<>(100);
   private final int protocolVersion;
   private final Socket socket;
   private final DataInputStream in;
   private final DataOutputStream out;
   private final Timer sendTracker = new Timer(0L);
   private final Timer receiveTracker = new Timer(0L);
   private final EventManager eventManager;
   private final SocketClientConfig config;

   public void sendPacket(Packet packet) {
      synchronized (this.sendLock) {
         ClientPacketSendEvent e = new ClientPacketSendEvent(EventState.PRE, packet);
         if (!this.eventManager.callEvent(e)) {
            packet = e.getPacket();

            try {
               packet.writeAll();
               byte[] bytesToSend = packet.send();
               this.out.writeInt(bytesToSend.length);
               this.out.write(bytesToSend);
               this.out.flush();
               this.recentSent.add(packet);
               this.sendTracker.reset();
               this.eventManager.callEvent(new ClientPacketSendEvent(EventState.POST, packet));
            } catch (Exception ex) {
               throw new RuntimeException("Failed to send packet", ex);
            }
         }
      }
   }

   public void receivePacket(Packet packet) {
      ClientPacketReceiveEvent e = new ClientPacketReceiveEvent(EventState.PRE, packet);
      if (!this.eventManager.callEvent(e)) {
         (packet = e.getPacket()).process();
         this.recentReceived.add(packet);
         this.eventManager.callEvent(new ClientPacketReceiveEvent(EventState.POST, packet));
      }
   }

   public ProtocolManager(int protocolVersion, Socket socket, DataInputStream in, DataOutputStream out, EventManager eventManager, SocketClientConfig config) {
      this.protocolVersion = protocolVersion;
      this.socket = socket;
      this.in = in;
      this.out = out;
      this.eventManager = eventManager;
      this.config = config;
   }

   Object getSendLock() {
      return this.sendLock;
   }

   Object getReceiveLock() {
      return this.receiveLock;
   }

   public List<Packet> getRecentSent() {
      return this.recentSent;
   }

   public List<Packet> getRecentReceived() {
      return this.recentReceived;
   }

   Timer getSendTracker() {
      return this.sendTracker;
   }

   Timer getReceiveTracker() {
      return this.receiveTracker;
   }
}
