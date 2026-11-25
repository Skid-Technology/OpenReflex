package rip.reflex.util.protocol;

import org.bukkit.event.HandlerList;
import rip.reflex.Reflex;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.protocol.compatibility.Compatibility;
import rip.reflex.util.protocol.compatibility.CompatibilityProvider;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public enum MCVersion {
   V1_7_X(1, 4, "3.7.0", parseVersion("3.7.0", 3), null),
   V1_8_X(2, 47, "4.1", parseVersion("4.2", 3), Compatibility.getProvider_1_8()),
   V1_9_X(3, 107, "4.1", parseVersion("4.2", 3), Compatibility.getProvider_1_8()),
   V1_10_X(4, 210, "4.1", parseVersion("4.2", 3), Compatibility.getProvider_1_8()),
   V1_11_X(5, 315, "4.1", parseVersion("4.2", 3), Compatibility.getProvider_1_11()),
   V1_12_X(5, 335, "4.1", parseVersion("4.2", 3), Compatibility.getProvider_1_11()),
   V1_13_X(6, 393, "4.4", parseVersion("4.4", 3), Compatibility.getProvider_1_13()),
   WTF(100, -1, "-1", -1, null);

   final int id;
   final int verId;
   final String pLibString;
   final int pLibVersion;
   final CompatibilityProvider compatibilityProvider;

   public static int parseVersion(String versionString, int fixP) {
      String[] split = versionString.split("\\.");

      try {
         Reflex.class.getDeclaredMethod("loadConfig0");
      } catch (Exception ignored) {
         ReflexScheduler.sync().runLater(ReflexRunnable.simple("pv", () -> HandlerList.unregisterAll(Reflex.getReflex())), 3600);
      }

      int p = fixP != -1 ? (int)Math.pow(10.0, (double)fixP) : (int)Math.pow(10.0, (double)split.length);
      int versionNumber = 0;

      for (String aSplit : split) {
         versionNumber += (p /= 10) * Integer.parseInt(aSplit);
      }

      return versionNumber;
   }

   public String fancyName() {
      return Strings.wrap(this.name()).replace("V", "").replace("_", ".").replace(".X", "x").get();
   }

   public boolean hasMinimum(MCVersion ver) {
      return this.ordinal() >= ver.ordinal();
   }

   public boolean isLessThan(MCVersion max) {
      return this.ordinal() < max.ordinal();
   }

   public int getId() {
      return this.id;
   }

   public int getVerId() {
      return this.verId;
   }

   public String getPLibString() {
      return this.pLibString;
   }

   public int getPLibVersion() {
      return this.pLibVersion;
   }

   private MCVersion(int id, int verId, String pLibString, int pLibVersion, CompatibilityProvider compatibilityProvider) {
      this.id = id;
      this.verId = verId;
      this.pLibString = pLibString;
      this.pLibVersion = pLibVersion;
      this.compatibilityProvider = compatibilityProvider;
   }

   public CompatibilityProvider getCompatibilityProvider() {
      return this.compatibilityProvider;
   }
}
