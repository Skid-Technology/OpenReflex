package me.darksidecode.kantanj.formatting;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import me.darksidecode.kantanj.types.Check;

public final class Formatting {
   private Formatting() {
   }

   public static String urlEncodeUtf8(String s) {
      try {
         return URLEncoder.encode(Check.notNull(s, "cannot encode null string"), "UTF-8");
      } catch (UnsupportedEncodingException ignored) {
         throw new RuntimeException("error in utf8 url encoding for string: " + s);
      }
   }

   public static String unixLines(String s) {
      return s.replace("\r\n", "\n").replace("\r", "\n");
   }
}
