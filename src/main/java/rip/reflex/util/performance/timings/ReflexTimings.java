package rip.reflex.util.performance.timings;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rip.reflex.Reflex;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.protocol.VersionUtils;

public class ReflexTimings {
   private static final Object lock = new Object();
   private static final List<TimingsEntry> entries = new ArrayList<>();
   private static boolean enabled;
   private static boolean canAdd = true;
   private static Integer reqTimingMillis = null;

   public static void addEntry(TimingsEntry entry) {
      if (enabled && canAdd && entry.getExecutionTime() >= (long)reqTimingMillis()) {
         synchronized (lock) {
            if (!entries.contains(entry)) {
               entries.add(entry);
            }
         }
      }
   }

   private static int reqTimingMillis() {
      return reqTimingMillis == null ? (reqTimingMillis = Reflex.getReflex().getSetting("general.required_timing_millis", 5)) : reqTimingMillis;
   }

   public static void saveAndReset() {
      synchronized (lock) {
         canAdd = false;
         File dir = new File(Reflex.getReflex().getWorkingDir() + "/timings/");
         File file = new File(
            dir.getAbsolutePath()
               + "/timings-"
               + Reflex.getReflex().getDate()
               + "_"
               + Strings.wrap(Reflex.getReflex().getTime()).replace(":", "-").toString()
               + ".txt"
         );
         if (!dir.exists()) {
            dir.mkdirs();
         }

         entries.sort(Comparator.comparingLong(TimingsEntry::getExecutionTime));
         Collections.reverse(entries);

         try {
            FileWriter write = new FileWriter(file);
            write.append(" ------------------------------------------------------------------------\n");
            write.append("   METHOD PROFILING\n");
            write.append(" ------------------------------------------------------------------------\n");

            for (TimingsEntry entry : entries) {
               write.append("      ").append(Strings.tab(entry.getName(), 50)).append(String.valueOf(entry.getExecutionTime())).append("\n");
            }

            entries.clear();
            write.append(" \n \n");
            write.append(" ------------------------------------------------------------------------\n");
            write.append("   GENERAL INFO\n");
            write.append(" ------------------------------------------------------------------------\n");
            write.append("      OS: ").append(System.getProperty("os.name")).append("\n");
            write.append("      Java: ").append(System.getProperty("java.version")).append("\n");
            write.append("      Server: ").append(VersionUtils.getServerVersion().toString()).append("\n");
            write.append("      Lowest TPS: ").append(String.valueOf(Reflex.getReflex().getTpsMeter().getLowestTps())).append("\n");
            write.append("      Reflex: ").append(Reflex.getReflex().getDescription().getVersion()).append("\n");
            write.append("      Network: ").append(String.valueOf(Reflex.getReflex().getDataVersion())).append("\n");
            write.append("      Using Cloud: ").append(String.valueOf(Reflex.getReflex().getCloudService().isConnected())).append("\n");
            write.append(" \n \n");
            write.append(" ").append(Reflex.getReflex().getDate()).append(" at ").append(Reflex.getReflex().getTime());
            write.flush();
            write.close();
            setEnabled(false);
         } catch (Exception ex) {
            ex.printStackTrace();
         }

         canAdd = true;
         setEnabled(false);
         Reflex.getReflex().getTpsMeter().setLowestTps(20.0);
      }
   }

   public static void setEnabled(boolean flag) {
      enabled = flag;
      if (flag) {
         synchronized (lock) {
            entries.clear();
         }
      }
   }

   public static boolean isEnabled() {
      return enabled;
   }
}
