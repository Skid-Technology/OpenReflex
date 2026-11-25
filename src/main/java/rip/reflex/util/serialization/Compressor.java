package rip.reflex.util.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import rip.reflex.util.misc.Misc;

public class Compressor {
   public static byte[] compressSafely(byte[] data) {
      try {
         return compress(data);
      } catch (IOException ex) {
         throw new RuntimeException("Failed to compress " + data.length + " bytes", ex);
      }
   }

   public static byte[] compress(byte[] data) throws IOException {
      Deflater deflater = new Deflater(9);
      deflater.setInput(data);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
      deflater.finish();
      byte[] buffer = new byte[1024];

      while (!deflater.finished()) {
         outputStream.write(buffer, 0, deflater.deflate(buffer));
      }

      outputStream.close();
      return outputStream.toByteArray();
   }

   public static byte[] compressObjSafely(Object obj) {
      try {
         return compressObj(obj);
      } catch (IOException ex) {
         throw new RuntimeException("Failed to compress object: " + obj.toString(), ex);
      }
   }

   public static byte[] compressObj(Object obj) throws IOException {
      return compress(Misc.getBytes(obj));
   }

   public static byte[] decompressSafely(byte[] data) {
      try {
         return decompress(data);
      } catch (DataFormatException | IOException e) {
         throw new RuntimeException("Failed to decompress " + data.length + " bytes", e);
      }
   }

   public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
      Inflater inflater = new Inflater();
      inflater.setInput(data);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
      byte[] buffer = new byte[1024];

      while (!inflater.finished()) {
         outputStream.write(buffer, 0, inflater.inflate(buffer));
      }

      outputStream.close();
      return outputStream.toByteArray();
   }
}
