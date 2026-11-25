package rip.reflex.util.protocol.viaversion;

import rip.reflex.Reflex;

public class ViaVersionHook {
   private static VVAPIAdapter adapter;
   private static Boolean present = null;

   public static boolean isPresent() {
      if (present != null) {
         return present;
      } else {
         try {
            Class.forName("us.myles.ViaVersion.api.ViaVersion");
            Class.forName("us.myles.ViaVersion.api.ViaAPI");
            Reflex.getReflex().info("ViaVersion is present");
            return present = true;
         } catch (ClassNotFoundException ignored) {
            Reflex.getReflex().info("ViaVersion is not present");
            return present = false;
         }
      }
   }

   public static boolean isInitialized() {
      return adapter != null;
   }

   public static void init() {
      if (isPresent()) {
         setAdapter(new VVAPIAdapter());
      }
   }

   public static VVAPIAdapter getAdapter() {
      return adapter;
   }

   public static void setAdapter(VVAPIAdapter adapter) {
      ViaVersionHook.adapter = adapter;
   }
}
