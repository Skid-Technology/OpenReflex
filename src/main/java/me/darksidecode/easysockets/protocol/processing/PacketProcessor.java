package me.darksidecode.easysockets.protocol.processing;

import me.darksidecode.easysockets.protocol.packet.Packet;

public interface PacketProcessor {
   boolean process(Packet packet);
}
