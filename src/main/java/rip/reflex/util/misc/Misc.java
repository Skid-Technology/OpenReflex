package rip.reflex.util.misc;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.InflaterInputStream;
import rip.reflex.Reflex;

public class Misc {
   public static int getSize(Object object) {
      try {
         if (object == null) {
            return 1;
         } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            byte[] byteArray = baos.toByteArray();
            return byteArray == null ? 1 : byteArray.length;
         }
      } catch (NotSerializableException ignored) {
         return 1;
      } catch (Exception ex) {
         throw new RuntimeException("Error getting object size (" + object.toString() + ")", ex);
      }
   }

   public static byte[] getBytes(Object obj) {
      if (obj == null) {
         return new byte[0];
      } else {
         try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            byte[] bytes = baos.toByteArray();
            baos.close();
            return bytes;
         } catch (Exception e) {
            throw new RuntimeException("Failed to get bytes of " + obj.toString(), e);
         }
      }
   }

   public static String postPaste(String content, String url, String userAgent) throws IOException {
      HttpURLConnection con = (HttpURLConnection)Reflex.getReflex().getURL(url + "/documents").openConnection();
      con.setRequestMethod("POST");
      con.setRequestProperty("User-Agent", userAgent);
      con.setDoOutput(true);
      OutputStream out = con.getOutputStream();
      out.write(content.getBytes("UTF-8"));
      String result = readFully(con.getInputStream());
      return url + "/" + result.substring(8, result.length() - 2);
   }

   public static void sortDescending(List list) {
      Collections.sort(list);
      Collections.reverse(list);
   }

   public static void sortDescending(List list, Comparator c) {
      Collections.sort(list, c);
      Collections.reverse(list);
   }

   public static void lif(boolean b, Runnable r) {
      if (b) {
         r.run();
      }
   }

   public static void sleep(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public static <T> T castSafely(Object o) {
      return (T)(o == null ? null : o);
   }

   public static <K, V> Map<K, V> copyMap(Map<K, V> map, boolean concurrent) {
      Map<K, V> result = (Map<K, V>)(concurrent ? new ConcurrentHashMap<>() : new HashMap<>());

      for (K key : map.keySet()) {
         result.put(key, map.get(key));
      }

      return result;
   }

   public static <T> T sneaky(Throwable t) {
      throw new RuntimeException(t);
   }

   public static <A, B> Object reverseGet(Class<A> a, Class<B> b, B obj, HashMap<A, B> map) {
      return map.entrySet().stream().filter(entry -> entry.getValue().equals(obj)).findFirst().map(Entry::getKey).orElse(null);
   }

   public static void close(Closeable... closeables) {
      for (Closeable closeable : closeables) {
         try {
            closeable.close();
         } catch (IOException ex) {
            ex.printStackTrace();
         }
      }
   }

   public static boolean isShort(String s) {
      return isNumeric(s) && Long.valueOf(s) <= 32767L && Long.valueOf(s) >= -32768L;
   }

   public static <T> void forEachMatch(List<T> list, Predicate<T> pred, Consumer<T> consumer) {
      for (T t : list) {
         if (pred.test(t)) {
            consumer.accept(t);
         }
      }
   }

   public static boolean isNumeric(String s) {
      for (char c : s.toCharArray()) {
         if (!Character.isDigit(c)) {
            return false;
         }
      }

      return true;
   }

   public static byte[] getBytesAndClose(ByteArrayOutputStream bout, OutputStream... s) throws Exception {
      byte[] data = bout.toByteArray();
      bout.close();

      for (OutputStream outputStream : s) {
         outputStream.close();
      }

      return data;
   }

   public static String readFully(InputStream in) throws IOException {
      StringBuilder builder = new StringBuilder();
      byte[] buf = new byte[1024];

      int i;
      while ((i = in.read(buf)) != -1) {
         builder.append(new String(buf, 0, i, "UTF-8"));
      }

      return builder.toString();
   }

   public static String readCompressed(InputStream in) throws IOException {
      InflaterInputStream infin = new InflaterInputStream(in);
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];

      int i;
      while ((i = infin.read(buf)) != -1) {
         bout.write(buf, 0, i);
      }

      byte[] data = bout.toByteArray();
      bout.close();
      infin.close();
      return new String(data, "UTF-8");
   }

   public static long parseTime(String s) {
      return Period.parse(s).get(ChronoUnit.DAYS);
   }

   public static boolean isNumber(String s) {
      for (char c : s.toCharArray()) {
         if (!Character.isDigit(c)) {
            return false;
         }
      }

      return true;
   }

   public static boolean isBoolean(String s) {
      return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
   }

   public static boolean isUUID(String s) {
      return s.length() == 36 && s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
   }
}
