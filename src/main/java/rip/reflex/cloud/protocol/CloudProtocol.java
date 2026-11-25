package rip.reflex.cloud.protocol;

public class CloudProtocol {
   public static final String HOST = "ccd.reflex.rip";
   public static final int PORT = 8109;
   public static final int E2E_AES256_ITERS = 5;
   public static final int MAX_PACKET_SIZE = 33554432;
   public static final int SEND_RATE_LIMIT = 100;
   public static final int RECEIVE_RATE_LIMIT = 100;
   public static final int TIMEOUT = 60000;
   public static final byte[] AES_KEY = new byte[]{48, -21, 62, 98, 83, -100, -43, -87, 79, 47, 88, 8, -100, 61, 57, 53};
   public static final byte[] AES_IV = new byte[]{87, 61, -32, 27, 40, -116, 17, -66, 74, -41, 126, -26, -42, 98, -47, -104};
}
