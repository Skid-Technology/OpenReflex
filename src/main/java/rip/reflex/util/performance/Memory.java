package rip.reflex.util.performance;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.util.data.Stats;
import rip.reflex.util.machinelearning.Classifier;
import rip.reflex.util.machinelearning.Dataset;
import rip.reflex.util.math.UniqueRandom;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.protocol.packet.PacketCache;
import rip.reflex.util.protocol.packet.Packets;

public class Memory {
   private static final String LINE_SEPARATOR = "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
   private static final String MEMORY_DUMP_ASCII_ART = "   __  ___                           ___                \n  /  |/  /__ __ _  ___  ______ __   / _ \\__ ____ _  ___ \n / /|_/ / -_)  ' \\/ _ \\/ __/ // /  / // / // /  ' \\/ _ \\\n/_/  /_/\\__/_/_/_/\\___/_/  \\_, /  /____/\\_,_/_/_/_/ .__/\n                          /___/                  /_/    \n";
   private static final String THREAD_DUMP_ASCII_ART = " ________                   __   ___                \n/_  __/ /  _______ ___ ____/ /  / _ \\__ ____ _  ___ \n / / / _ \\/ __/ -_) _ `/ _  /  / // / // /  ' \\/ _ \\\n/_/ /_//_/_/  \\__/\\_,_/\\_,_/  /____/\\_,_/_/_/_/ .__/\n                                             /_/    \n";
   private static long memoryUsed = 0L;

   public static String dump() {
      String dir = Reflex.getReflex().getWorkingDir();
      File f = new File((dir.endsWith("/") ? dir : dir + "/") + "dump-" + System.currentTimeMillis() + ".txt");
      if (f.exists()) {
         f.delete();
      }

      String path = f.getAbsolutePath();

      try {
         FileWriter fw = new FileWriter(f);
         fw.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         );
         fw.append('\n');
         fw.append(
            "   __  ___                           ___                \n  /  |/  /__ __ _  ___  ______ __   / _ \\__ ____ _  ___ \n / /|_/ / -_)  ' \\/ _ \\/ __/ // /  / // / // /  ' \\/ _ \\\n/_/  /_/\\__/_/_/_/\\___/_/  \\_, /  /____/\\_,_/_/_/_/ .__/\n                          /___/                  /_/    \n"
         );
         fw.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         );
         fw.append('\n');
         fw.append(dump0());
         fw.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         );
         fw.append('\n');
         fw.append(
            " ________                   __   ___                \n/_  __/ /  _______ ___ ____/ /  / _ \\__ ____ _  ___ \n / / / _ \\/ __/ -_) _ `/ _  /  / // / // /  ' \\/ _ \\\n/_/ /_//_/_/  \\__/\\_,_/\\_,_/  /____/\\_,_/_/_/_/ .__/\n                                             /_/    \n"
         );
         fw.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         );
         fw.append('\n');
         fw.append(Strings.tab("Name", 50)).append("Stacktrace");
         fw.append(" \n \n");
         int count = 0;

         for (Thread t : Thread.getAllStackTraces().keySet()) {
            StackTraceElement[] stackTrace = (StackTraceElement[])t.getStackTrace().clone();
            if (stackTrace.length != 0) {
               boolean any = false;

               for (StackTraceElement e : stackTrace) {
                  if (e.toString().contains("rip.reflex")) {
                     any = true;
                     break;
                  }
               }

               if (any) {
                  count++;
                  fw.append(Strings.tab(t.getName(), 50)).append(stackTrace[0].toString()).append('\n');
                  if (stackTrace.length > 1) {
                     for (int i = 1; i < Math.min(75, stackTrace.length); i++) {
                        fw.append(Strings.tab("", 50)).append(stackTrace[i].toString()).append('\n');
                     }
                  }

                  fw.append('\n');
               }
            }
         }

         fw.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         );
         fw.append('\n');
         fw.append(String.valueOf(count)).append(" threads in total.\n");
         fw.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         );
         fw.flush();
         fw.close();
         return path;
      } catch (Exception ex) {
         throw new RuntimeException("Memory dump failed with an error", ex);
      }
   }

   public static String dump0() {
      StringBuilder s = new StringBuilder();
      int configCaches = 0;
      long bConfigCaches = 0L;

      for (Check c : CheckRegistry.loadedChecksList()) {
         configCaches += c.getConfigCache().size();
         bConfigCaches += (long)getSize(c.getConfigCache());
      }

      configCaches += Reflex.getReflex().getConfigCache().size();
      bConfigCaches += (long)getSize(Reflex.getReflex().getConfigCache());
      int packetCaches = Packets.getCaches().size();
      long bPacketCaches = (long)getSize(Packets.getCaches());

      for (PacketCache pc : Packets.getCaches().values()) {
         packetCaches += pc.getMap().size();
         bPacketCaches += (long)getSize(pc.getMap());
      }

      int violations = 0;
      int heuristicsData = 0;
      int permissionCaches = 0;
      int stats = 0;
      long bViolations = 0L;
      long bHeuristicsData = 0L;
      long bPermissionCaches = 0L;
      long bStats = 0L;

      for (Player p : Bukkit.getOnlinePlayers()) {
         Stats st = Stats.of(p);
         violations += st.getViolations().size();
         bViolations += (long)getSize(st.getViolations());
         heuristicsData += st.getHeuristicsData().size();
         bHeuristicsData += (long)getSize(st.getHeuristicsData());
         permissionCaches += st.getPermissionCache().size();
         bPermissionCaches += (long)getSize(st.getPermissionCache());
         stats += st.getStats().size();
         bStats += (long)getSize(st.getStats());
      }

      int statlist = Stats.getStatlist().size();
      long bStatlist = (long)getSize(Stats.getStatlist());
      int checkStatus = CheckRegistry.getStatus().size();
      long bCheckStatus = (long)getSize(CheckRegistry.getStatus());
      int uniqueRandomCaches = 0;
      long bUniqueRandomCaches = 0L;

      for (UniqueRandom ur : UniqueRandom.getList()) {
         uniqueRandomCaches += ur.getCache().size();
         bUniqueRandomCaches += (long)getSize(ur.getCache());
      }

      int neuralNetworkData = 0;
      long bNeuralNetworkData = 0L;

      for (List<Dataset> n : Classifier.getNetlist()) {
         for (Dataset ds : n) {
            neuralNetworkData += ds.getData().size();
            bNeuralNetworkData += (long)getSize(ds.getData());
         }
      }

      int total = configCaches
         + packetCaches
         + violations
         + heuristicsData
         + permissionCaches
         + stats
         + checkStatus
         + uniqueRandomCaches
         + neuralNetworkData
         + statlist;
      long bTotal = bConfigCaches
         + bPacketCaches
         + bViolations
         + bHeuristicsData
         + bPermissionCaches
         + bStats
         + bCheckStatus
         + bUniqueRandomCaches
         + bNeuralNetworkData
         + bStatlist;
      long[] memory = getMemory();
      s.append(Strings.tab("Entry", 50)).append(Strings.tab("Objects", 50)).append("Bytes estimate\n");
      s.append(" \n");
      s.append(Strings.tab("Config caches", 50)).append(Strings.tab(String.valueOf(configCaches), 50)).append(String.valueOf(bConfigCaches)).append("\n");
      s.append(Strings.tab("Packet caches", 50)).append(Strings.tab(String.valueOf(packetCaches), 50)).append(String.valueOf(bPacketCaches)).append("\n");
      s.append(Strings.tab("Violations", 50)).append(Strings.tab(String.valueOf(violations), 50)).append(String.valueOf(bViolations)).append("\n");
      s.append(Strings.tab("Heuristics data", 50)).append(Strings.tab(String.valueOf(heuristicsData), 50)).append(String.valueOf(bHeuristicsData)).append("\n");
      s.append(Strings.tab("Permission caches", 50))
         .append(Strings.tab(String.valueOf(permissionCaches), 50))
         .append(String.valueOf(bPermissionCaches))
         .append("\n");
      s.append(Strings.tab("Stats", 50)).append(Strings.tab(String.valueOf(stats), 50)).append(String.valueOf(bStats)).append("\n");
      s.append(Strings.tab("Statlist", 50)).append(Strings.tab(String.valueOf(statlist), 50)).append(String.valueOf(bStatlist)).append("\n");
      s.append(Strings.tab("Check status", 50)).append(Strings.tab(String.valueOf(checkStatus), 50)).append(String.valueOf(bCheckStatus)).append("\n");
      s.append(Strings.tab("UniqueRandom caches", 50))
         .append(Strings.tab(String.valueOf(uniqueRandomCaches), 50))
         .append(String.valueOf(bUniqueRandomCaches))
         .append("\n");
      s.append(Strings.tab("Neural network data", 50))
         .append(Strings.tab(String.valueOf(neuralNetworkData), 50))
         .append(String.valueOf(bNeuralNetworkData))
         .append("\n");
      s.append(Strings.tab("Total", 50)).append(Strings.tab(String.valueOf(total), 50)).append(String.valueOf(bTotal)).append("\n");
      s.append(
            "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
         )
         .append("\n");
      s.append(Strings.tab("Max memory", 50)).append(String.valueOf(memory[0])).append("\n");
      s.append(Strings.tab("Allocated memory", 50)).append(String.valueOf(memory[1])).append("\n");
      s.append(Strings.tab("Free memory", 50)).append(String.valueOf(memory[2])).append("\n");
      s.append(Strings.tab("Delta", 50)).append(String.valueOf(Math.abs(Math.abs(memory[0] - memory[2]) - updateMemoryUsed(bTotal)))).append("\n");
      return s.toString();
   }

   public static long updateMemoryUsed(long l) {
      memoryUsed = l;
      return l;
   }

   public static int getSize(Object object) {
      return Misc.getSize(object);
   }

   public static long[] getMemory() {
      Runtime rt = Runtime.getRuntime();
      return new long[]{rt.maxMemory(), rt.totalMemory(), rt.freeMemory()};
   }

   public static long getMemoryUsed() {
      return memoryUsed;
   }
}
