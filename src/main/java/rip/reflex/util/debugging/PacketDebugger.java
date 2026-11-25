package rip.reflex.util.debugging;

import com.comphenix.protocol.events.PacketContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class PacketDebugger {
   private static final Map<Player, DebugContainer> debug = new ConcurrentHashMap<>();

   public static void addEntry(Player player, PacketContainer packet, boolean async) {
      if (isDebugging(player)) {
         debug.get(player).addEntry((async ? "ASYNC" : "SYNC") + ": " + packet.toString());
      }
   }

   public static String printAndReset(Player player) {
      if (!isDebugging(player)) {
         return null;
      } else {
         String s = debug.get(player).toString();
         disableDebug(player);
         return s;
      }
   }

   public static boolean isDebugging(Player p) {
      return debug.containsKey(p);
   }

   public static boolean toogleDebug(Player p) {
      if (debug.containsKey(p)) {
         debug.remove(p);
         return false;
      } else {
         debug.put(p, new DebugContainer());
         return true;
      }
   }

   public static void enableDebug(Player p) {
      if (!debug.containsKey(p)) {
         debug.put(p, new DebugContainer());
      }
   }

   public static void disableDebug(Player p) {
      if (debug.containsKey(p)) {
         debug.remove(p);
      }
   }

   public static void clearAll() {
      debug.clear();
   }

   public static void cleanup() {
      List<Player> toRm = new ArrayList<>();
      debug.keySet().stream().filter(p -> !p.isOnline()).forEach(toRm::add);
      toRm.forEach(debug::remove);
   }
}
