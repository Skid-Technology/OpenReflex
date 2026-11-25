package me.darksidecode.easysockets.protocol.packet;

public class PacketSignatureError extends RuntimeException {
   public PacketSignatureError() {
   }

   public PacketSignatureError(String message) {
      super(message);
   }

   public PacketSignatureError(String message, Throwable cause) {
      super(message, cause);
   }

   public PacketSignatureError(Throwable cause) {
      super(cause);
   }
}
