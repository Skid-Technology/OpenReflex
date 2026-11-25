package rip.reflex.util.protocol.protocolsupport;

import rip.reflex.Reflex;

public class ProtocolSupportHook {
   private static PSAPIAdapter adapter;
   private static Boolean present = null;

   public static boolean isPresent() {
      if (present != null) {
         return present;
      } else {
         try {
            Class.forName("protocolsupport.api.ProtocolSupportAPI");
            Reflex.getReflex().info("ProtocolSupport is present");
            Reflex.getReflex().warn("ProtocolSupport compatibility is EXPERIMENTAL!");
            return present = true;
         } catch (ClassNotFoundException ignored) {
            Reflex.getReflex().info("ProtocolSupport is not present");
            return present = false;
         }
      }
   }

   public static boolean isInitialized() {
      return adapter != null;
   }

   public static void init() {
      if (isPresent()) {
         setAdapter(new PSAPIAdapter());
      }
   }

   public static PSAPIAdapter getAdapter() {
      return adapter;
   }

   public static void setAdapter(PSAPIAdapter adapter) {
      ProtocolSupportHook.adapter = adapter;
   }
}
