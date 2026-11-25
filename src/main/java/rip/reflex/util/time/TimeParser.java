package rip.reflex.util.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import rip.reflex.util.misc.Misc;

public class TimeParser {
   private static final HashMap<String, FullTimeUnit> typeMap = new HashMap<>();
   private long millis = 0L;

   public static void main(String[] args) {
      new TimeParser().parse("1w1d1h1m1s");
   }

   public TimeParser parse(String input) {
      StringBuilder current = new StringBuilder();
      List<String> list = new ArrayList<>();
      Boolean isLastChar = null;
      int lastSaved = -1;

      for (char c : input.toCharArray()) {
         if (!this.multiMatch(c, '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'M', 'm', 'y', 'Y', 'd', 'h', 's', 'w')) {
            throw new IllegalArgumentException("illegal char: " + c);
         }

         boolean isCurrentChar = this.multiMatch(c, 'M', 'm', 'y', 'd', 'h', 's', 'w');
         if (isLastChar == null) {
            isLastChar = isCurrentChar;
         }

         if (isCurrentChar == isLastChar) {
            current.append(c);
            isLastChar = isCurrentChar;
         } else {
            isLastChar = isCurrentChar;
            list.add(current.toString());
            current = new StringBuilder();
            current.append(c);
            if (lastSaved != -1) {
               int currentSave = isCurrentChar ? 1 : 0;
               if (lastSaved == currentSave) {
                  throw new IllegalArgumentException("Same save (impossible)");
               }

               lastSaved = currentSave;
            }
         }
      }

      list.add(current.toString());
      if (list.size() % 2 != 0) {
         throw new IllegalArgumentException("Could not find same amount of numbers and time chars");
      } else {
         List<Entry<String, String>> stringList = new ArrayList<>();
         String[] pair = new String[2];
         list.forEach(s -> {
            if (pair[0] == null) {
               pair[0] = s;
            } else if (pair[1] == null) {
               pair[1] = s;
            } else {
               stringList.add(new SimpleEntry<>(pair[0], pair[1]));
               pair[0] = s;
               pair[1] = null;
            }
         });
         stringList.add(new SimpleEntry<>(pair[0], pair[1]));
         List<Entry<Integer, FullTimeUnit>> timeList = new ArrayList<>();
         stringList.forEach(entry -> {
            if (Misc.isNumeric(entry.getKey()) && !Misc.isNumeric(entry.getValue())) {
               timeList.add(new SimpleEntry<>(Integer.valueOf(entry.getKey()), typeMap.get(entry.getValue())));
            } else {
               throw new IllegalArgumentException("Illegal char/digit sorting.");
            }
         });
         Collections.reverse(timeList);
         FullTimeUnit[] last = new FullTimeUnit[]{null};
         timeList.forEach(entry -> {
            this.millis = (long)((double)this.millis + entry.getValue().to((double)entry.getKey().intValue(), FullTimeUnit.MILLISECONDS));
            if (last[0] == null) {
               last[0] = entry.getValue();
            } else if (last[0].ordinal() > entry.getValue().ordinal()) {
               throw new IllegalArgumentException("Wrong order of time types");
            } else {
               last[0] = entry.getValue();
            }
         });
         return this;
      }
   }

   public long get() {
      return this.millis;
   }

   private boolean multiMatch(char c, char... chars) {
      for (char aChar : chars) {
         if (aChar == c) {
            return true;
         }
      }

      return false;
   }

   static {
      typeMap.put("s", FullTimeUnit.SECONDS);
      typeMap.put("m", FullTimeUnit.MINUTES);
      typeMap.put("h", FullTimeUnit.HOURS);
      typeMap.put("d", FullTimeUnit.DAYS);
      typeMap.put("w", FullTimeUnit.WEEKS);
      typeMap.put("M", FullTimeUnit.MONTHS);
      typeMap.put("y", FullTimeUnit.YEARS);
   }
}
