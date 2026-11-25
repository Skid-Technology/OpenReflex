package me.darksidecode.easysockets.util;

public class Validate {
   public static <T> T notNull(T object, String message) {
      if (object == null) {
         throw new NullPointerException(message);
      } else {
         return object;
      }
   }

   public static char[] notEmpty(char[] array, String message) {
      if (notNull(array, message).length == 0) {
         throw new ArrayIndexOutOfBoundsException(message);
      } else {
         return array;
      }
   }
}
