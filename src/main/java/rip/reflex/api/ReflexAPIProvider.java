package rip.reflex.api;

import org.bukkit.Bukkit;
import rip.reflex.Reflex;
import rip.reflex.ReflexAPIWrapper;
import rip.reflex.api.event.ReflexLoadEvent;

public class ReflexAPIProvider {
   private static ReflexAPI api = null;
   private static boolean init = false;

   public static ReflexAPI getAPI() {
      if (init && api != null) {
         return api;
      } else {
         throw new IllegalStateException("Reflex API has not been initialized yet!");
      }
   }

   public static void init(Reflex reflex) {
      if (init) {
         throw new IllegalStateException("Cannot initialize Reflex API twice!");
      } else {
         Reflex.setApiWrapper(new ReflexAPIWrapper(reflex, api = new ReflexAPI(reflex)));
         init = true;
         Bukkit.getPluginManager().callEvent(new ReflexLoadEvent(Reflex.getReflex().getDescription().getVersion(), 6));
         Reflex.getReflex().info("Reflex API v6 has been loaded.");
      }
   }
}
