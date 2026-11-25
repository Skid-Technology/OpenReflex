package rip.reflex.moonlight;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;

public class MoonlightKeyReader {
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/YYYY");
   private final String origin;
   private final String protocolEncryptionKey;
   private final long creationTimestamp;

   public MoonlightKeyReader(String string) {
      this.origin = string;

      try {
         String[] parts = Objects.requireNonNull(string, "input string cannot be null").split(Pattern.quote("-"));
         if (parts.length != 3) {
            throw new IllegalArgumentException("illegal key length: " + parts.length);
         } else {
            String b64ProtocolEncKey = parts[0];
            String b64KeyEnc = parts[1];
            String b64Timestamp = parts[2];
            byte[] protocolEncKey = Base64.decodeBase64(b64ProtocolEncKey);
            byte[] keyEnc = Base64.decodeBase64(b64KeyEnc);
            byte[] timestampBytes = Base64.decodeBase64(b64Timestamp);

            for (int i = 0; i < protocolEncKey.length; i++) {
               for (byte b : keyEnc) {
                  protocolEncKey[i] ^= b;
               }
            }

            this.protocolEncryptionKey = new String(protocolEncKey, StandardCharsets.UTF_8);

            for (int i = 0; i < timestampBytes.length; i++) {
               for (byte b : keyEnc) {
                  timestampBytes[i] ^= b;
               }
            }

            ByteBuffer buf = ByteBuffer.wrap(timestampBytes);
            this.creationTimestamp = buf.getLong();
         }
      } catch (Exception ignored) {
         throw new RuntimeException("invalid key");
      }
   }

   public String getCreationDateString() {
      return DATE_FORMAT.format(this.creationTimestamp);
   }

   public String getOrigin() {
      return this.origin;
   }

   public String getProtocolEncryptionKey() {
      return this.protocolEncryptionKey;
   }

   public long getCreationTimestamp() {
      return this.creationTimestamp;
   }
}
