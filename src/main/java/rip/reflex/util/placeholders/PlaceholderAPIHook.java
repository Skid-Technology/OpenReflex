package rip.reflex.util.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook {
   private static Boolean present = null;

   public static boolean isPresent() {
      if (present == null) {
         try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            present = true;
         } catch (ClassNotFoundException ignored) {
            present = false;
         }
      }

      return present;
   }

   public static String replace(String orig, Player p) {
      return !isPresent() ? orig : PlaceholderAPI.setPlaceholders(p, orig);
   }
}
