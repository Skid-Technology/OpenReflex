package rip.reflex.util.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import rip.reflex.Reflex;

public class Files {
   public static void write(String path, String data) {
      if (Reflex.getReflex().getExecutor() == null) {
         new Thread(() -> write0(path, data), "Reflex File Write Thread").start();
      } else {
         Reflex.getReflex().getExecutor().execute(() -> write0(path, data));
      }
   }

   private static void write0(String path, String data) {
      try (FileWriter fw = new FileWriter(new File(path), true)) {
         fw.write(data + "\n");
         fw.flush();
         fw.close();
      } catch (Exception ex) {
         throw new RuntimeException("Failed to write to file: " + path, ex);
      }
   }

   public static void writeFromStream(InputStream in, File file) {
      try {
         OutputStream out = new FileOutputStream(file);
         byte[] buf = new byte[1024];

         int len;
         while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
         }

         out.close();
         in.close();
      } catch (Exception e) {
         throw new RuntimeException("Failed to write to file " + file.getAbsolutePath() + " from InputStream", e);
      }
   }

   public static String read(String path) {
      if (path.startsWith("null")) {
         return "";
      } else {
         File file = new File(path);

         try (FileReader fr = new FileReader(file)) {
            char[] buf = new char[(int)file.length()];
            fr.read(buf);
            fr.close();
            return new String(buf);
         } catch (Exception ex) {
            throw new RuntimeException("Failed to read from file: " + path, ex);
         }
      }
   }

   public static void closeWriterSneaky(FileWriter fw) {
      try {
         fw.close();
      } catch (Exception err) {
         throw new RuntimeException("Failed to close a file writer", err);
      }
   }

   public static void closeReaderSneaky(FileReader fr) {
      try {
         fr.close();
      } catch (Exception e) {
         throw new RuntimeException("Failed to close a file reader", e);
      }
   }

   public static void flushSneaky(FileWriter fw) {
      try {
         fw.flush();
      } catch (Exception ex) {
         throw new RuntimeException("Failed to flush a file writer", ex);
      }
   }

   public static void ensureExists(File f) {
      if (f.getName().contains(".")) {
         f.getParentFile().mkdirs();
      } else {
         f.mkdirs();
      }
   }

   public static File getFile(String path) {
      File f = new File(path);
      ensureExists(f);
      return f;
   }

   public static File getFile(File parent, String child) {
      File f = new File(parent, child);
      ensureExists(f);
      return f;
   }

   public static File getFile(String parent, String child) {
      File f = new File(parent, child);
      ensureExists(f);
      return f;
   }
}
