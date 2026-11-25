package rip.reflex.cloud.protocol;

public class PacketType {
   public static final String PING = "RC_PING";

   public static class Client {
      public static final String AUTH_REQUEST = "RC_AUTH_REQ";
      public static final String COMPUTE_REQUEST = "RC_COMP_REQ";
   }

   public static class Server {
      public static final String AUTH_RESPONSE = "RC_AUTH_RES";
      public static final String COMPUTE_RESPONSE = "RC_COMP_RES";
   }
}
