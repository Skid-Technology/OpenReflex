package me.darksidecode.easysockets.protocol.packet;

import me.darksidecode.easysockets.protocol.processing.PacketProcessing;
import me.darksidecode.easysockets.util.io.IO;

public class Packet {
   private PacketContents contents;
   private final String type;

   public Packet(String type) {
      this(type, new PacketContents());
   }

   public Packet(String type, PacketContents contents) {
      this.type = type;
      this.contents = contents;
   }

   public boolean process() {
      return PacketProcessing.process(this);
   }

   public Packet setContents(PacketContents contents) {
      this.contents = contents;
      return this;
   }

   public byte[] send() {
      return sendPacket(this);
   }

   public Packet receive(byte[] in) {
      RawPacket raw = receiveRaw(in);
      if (!raw.getType().equals(this.type)) {
         throw new PacketSignatureError("Packet type mismatch: " + raw.getType() + ", expected " + this.type);
      } else {
         return this.setContents(new PacketContents(raw.getContents()));
      }
   }

   public Packet writeAll() {
      return this;
   }

   public Packet readAll() {
      return this;
   }

   public static byte[] sendPacket(Packet p) {
      return IO.ser(new RawPacket(p.getType(), p.getContents().getRaw()));
   }

   @Override
   public String toString() {
      return this.type + this.contents.toString();
   }

   public static Packet receivePacket(byte[] in) {
      RawPacket raw = receiveRaw(in);
      return new Packet(raw.getType(), new PacketContents(raw.getContents()));
   }

   public static RawPacket receiveRaw(byte[] in) {
      Object obj = IO.deser(in);
      if (obj != null && obj instanceof RawPacket) {
         return (RawPacket)obj;
      } else {
         throw new PacketSignatureError("Invalid packet received");
      }
   }

   public PacketContents getContents() {
      return this.contents;
   }

   public String getType() {
      return this.type;
   }
}
