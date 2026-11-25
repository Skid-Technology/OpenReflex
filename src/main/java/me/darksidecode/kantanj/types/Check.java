package me.darksidecode.kantanj.types;

import java.io.File;
import java.util.NoSuchElementException;

public final class Check {
   private static final String USERNAME_CHARSET = "qwertyuiopasdfghjklzxcvbnm1234567890_";

   private Check() {
   }

   public static <T> T notNull(T obj, String message, Object... format) {
      if (obj == null) {
         throw new NullPointerException(makeMessage(message, format));
      } else {
         return obj;
      }
   }

   public static <T> T[] notEmpty(T[] array, String message, Object... format) {
      if (((Object[])notNull((T)array, "array cannot be null")).length == 0) {
         throw new IllegalArgumentException(makeMessage(message, format));
      } else {
         return array;
      }
   }

   public static <T> T instanceOf(Object obj, Class<T> type, String message, Object... format) {
      notNull(obj, "object cannot be null");
      notNull(type, "type cannot be null");
      if (!type.isInstance(notNull(obj, message))) {
         throw new ClassCastException(makeMessage(message, format));
      } else {
         return (T)obj;
      }
   }

   public static String username(String username) {
      notNull(username, "username cannot be null");

      for (char c : username.toCharArray()) {
         if ("qwertyuiopasdfghjklzxcvbnm1234567890_".indexOf(c) == -1) {
            throw new IllegalArgumentException("illegal character in username: " + c);
         }
      }

      return username;
   }

   public static void state(boolean b, String message, Object... format) {
      if (b) {
         throw new IllegalStateException(makeMessage(message, format));
      }
   }

   public static File fileExists(File file, String message, Object... format) {
      if (!notNull(file, "file cannot be null").exists()) {
         throw new NoSuchElementException(makeMessage(message, format));
      } else {
         return file;
      }
   }

   private static String makeMessage(String message, Object... format) {
      return format != null && format.length > 0 ? String.format(message, format) : message;
   }
}
