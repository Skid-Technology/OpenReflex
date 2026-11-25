package rip.reflex.util.time.date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import rip.reflex.Reflex;

public class ReflexFormatter {
   private static final ConcurrentHashMap<String, SimpleDateFormat> formats = new ConcurrentHashMap<>();

   public static String formatTime(String format, Date d) {
      return getFormat(format).format(d);
   }

   public static String formatTimeDefault(Date d) {
      return getFormat("dd.MM.YYYY HH:mm:ss").format(d);
   }

   public static SimpleDateFormat getFormat(String format) {
      SimpleDateFormat f;
      if (!formats.containsKey(format)) {
         formats.put(format, f = new SimpleDateFormat(format));
      } else {
         f = formats.get(format);
      }

      return f;
   }

   public static String formatLog(Level level, String message, Object... o) {
      message = Pattern.quote(message);
      return "[Reflex "
         + Reflex.getReflex().getDescription().getVersion()
         + "/"
         + Reflex.getReflex().getDataVersion()
         + "] "
         + formatTime("dd.MM.YYYY HH:mm:ss", new Date(System.currentTimeMillis()))
         + ' '
         + level.getName()
         + ": "
         + String.format(message, o);
   }

   static {
      formats.put("dd.MM.YYYY HH:mm:ss", new SimpleDateFormat("dd.MM.YYYY HH:mm:ss"));
   }
}
