package rip.reflex.util.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unzipper implements Closeable {
   private final byte[] data;
   private final BiConsumer<ZipEntry, byte[]> consumer;

   public void exec() {
      try {
         this.exec0();
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   private void exec0() throws IOException {
      ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
      ZipInputStream zipIn = new ZipInputStream(byteIn);
      byte[] buffer = new byte[1024];

      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
         ByteArrayOutputStream bout = new ByteArrayOutputStream();

         int i;
         while ((i = zipIn.read(buffer)) > 0) {
            bout.write(buffer, 0, i);
         }

         this.consumer.accept(entry, bout.toByteArray());
         bout.close();
      }

      this.close();
   }

   @Override
   public void close() throws IOException {
   }

   public Unzipper(byte[] data, BiConsumer<ZipEntry, byte[]> consumer) {
      this.data = data;
      this.consumer = consumer;
   }
}
