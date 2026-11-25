package me.darksidecode.easysockets.protocol.processing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.darksidecode.easysockets.protocol.packet.Packet;

public class PacketProcessing {
   private static final Map<String, PacketProcessor> processors = new ConcurrentHashMap<>();

   public static boolean process(Packet packet) {
      PacketProcessor pp = processors.get(packet.getType());
      return pp == null ? false : pp.process(packet);
   }

   public static void setProcessor(String type, PacketProcessor pp) {
      processors.put(type, pp);
   }
}
