package rip.reflex.util.crypto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rip.reflex.util.misc.Validate;

public class CloudEncoder {
   private static final Map<Integer, String> dictionary = new ConcurrentHashMap<>();

   public static String decode(int i) {
      return Validate.notNull(dictionary.get(i), "No strings are encoded as " + i + " found");
   }

   public static String decodeInner(String s) {
      for (int code : dictionary.keySet()) {
         s = s.replace("^" + code, dictionary.get(code));
      }

      return s;
   }

   public static String encodeInner(String s) {
      for (int code : dictionary.keySet()) {
         s = s.replace(dictionary.get(code), "^" + code);
      }

      return s;
   }

   public static int encode(String s) {
      return dictionary.keySet()
         .stream()
         .filter(i -> dictionary.get(i).equalsIgnoreCase(s.trim()))
         .findFirst()
         .orElseThrow(() -> new NullPointerException("String \"" + s + "\" cannot be encoded"));
   }

   static {
      dictionary.put(10, "MACHINE_LEARNING");
      dictionary.put(11, "SMALL_YAW_STATISTIC");
      dictionary.put(12, "MEDIUM_YAW_STATISTIC");
      dictionary.put(13, "BIG_YAW_STATISTIC");
      dictionary.put(14, "REACH");
      dictionary.put(20, "LEGIT");
      dictionary.put(21, "HACKS");
      dictionary.put(22, "ERROR");
      dictionary.put(23, "NoLogon");
      dictionary.put(24, "Reloading");
   }
}
