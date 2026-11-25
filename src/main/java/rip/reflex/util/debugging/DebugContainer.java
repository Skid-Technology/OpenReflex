package rip.reflex.util.debugging;

import java.util.Date;
import rip.reflex.Reflex;
import rip.reflex.util.data.LimitedList;

public class DebugContainer {
   private final LimitedList<String> entries = new LimitedList<>(100);

   public DebugContainer addEntry(String entry) {
      this.entries.add("[" + Reflex.getReflex().getTimeFormat().format(new Date(System.currentTimeMillis())) + "]: " + entry);
      return this;
   }

   @Override
   public String toString() {
      StringBuilder res = new StringBuilder("Last 100 packets sent by Reflex:\n");
      this.entries.forEach(e -> {
         res.append(e);
         res.append('\n');
      });
      return res.deleteCharAt(res.length() - 1).toString();
   }

   public LimitedList<String> getEntries() {
      return this.entries;
   }
}
