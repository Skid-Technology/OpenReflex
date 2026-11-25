package rip.reflex.util.protocol;

import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.security.AntiPiracy;
import rip.reflex.util.protocol.compatibility.CompatibilityProvider;
import rip.reflex.util.protocol.protocolsupport.ProtocolSupportHook;
import rip.reflex.util.protocol.viaversion.ViaVersionHook;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

public class VersionUtils {
   private static final Object cacheLock = new Object();
   public static final ConcurrentHashMap<Player, MCVersion> clientVersions = new ConcurrentHashMap<>();
   static int i = 0;
   private static MCVersion cached;
   private static String vstr;
   private static Boolean forceLegacyMechanics;

   public static MCVersion getServerVersion() {
      String version = Bukkit.getVersion();
      if (i++ == 0) {
         AntiPiracy.f(new String[]{"--vagdedes", "--is", "--not", "--cool"});
      } else {
         i = 1;
      }

      if (cached != null) {
         return cached;
      } else if (version.contains("(MC: 1.7")) {
         return cached = MCVersion.V1_7_X;
      } else if (version.contains("(MC: 1.8")) {
         return cached = MCVersion.V1_8_X;
      } else if (version.contains("(MC: 1.9")) {
         return cached = MCVersion.V1_9_X;
      } else if (version.contains("(MC: 1.10")) {
         return cached = MCVersion.V1_10_X;
      } else if (version.contains("(MC: 1.11")) {
         return cached = MCVersion.V1_11_X;
      } else if (version.contains("(MC: 1.12")) {
         return cached = MCVersion.V1_12_X;
      } else {
         return version.contains("(MC: 1.13") ? (cached = MCVersion.V1_13_X) : (cached = MCVersion.WTF);
      }
   }

   public static CompatibilityProvider getCompatibilityProvider() {
      return getServerVersion().getCompatibilityProvider();
   }

   public static MCVersion getClientVersion(Player player) {
      synchronized (cacheLock) {
         if (clientVersions.containsKey(player)) {
            return clientVersions.get(player);
         } else if (ViaVersionHook.isPresent() && ViaVersionHook.isInitialized()) {
            if (ViaVersionHook.getAdapter().hasMinimum(player, ProtocolVersion.v1_13)) {
               return setClientVersion(player, MCVersion.V1_13_X);
            } else if (ViaVersionHook.getAdapter().hasMinimum(player, ProtocolVersion.v1_12)) {
               return setClientVersion(player, MCVersion.V1_12_X);
            } else if (ViaVersionHook.getAdapter().hasMinimum(player, ProtocolVersion.v1_11)) {
               return setClientVersion(player, MCVersion.V1_11_X);
            } else if (ViaVersionHook.getAdapter().hasMinimum(player, ProtocolVersion.v1_10)) {
               return setClientVersion(player, MCVersion.V1_10_X);
            } else if (ViaVersionHook.getAdapter().hasMinimum(player, ProtocolVersion.v1_9)) {
               return setClientVersion(player, MCVersion.V1_9_X);
            } else {
               return ViaVersionHook.getAdapter().hasMinimum(player, ProtocolVersion.v1_8)
                  ? setClientVersion(player, MCVersion.V1_8_X)
                  : setClientVersion(player, MCVersion.V1_7_X);
            }
         } else if (ProtocolSupportHook.isPresent() && ProtocolSupportHook.isInitialized()) {
            int pv = ProtocolSupportHook.getAdapter().getProtocolVersion(player);
            if (pv >= MCVersion.V1_13_X.verId) {
               return setClientVersion(player, MCVersion.V1_13_X);
            } else if (pv >= MCVersion.V1_12_X.verId) {
               return setClientVersion(player, MCVersion.V1_12_X);
            } else if (pv >= MCVersion.V1_11_X.verId) {
               return setClientVersion(player, MCVersion.V1_11_X);
            } else if (pv >= MCVersion.V1_10_X.verId) {
               return setClientVersion(player, MCVersion.V1_10_X);
            } else if (pv >= MCVersion.V1_9_X.verId) {
               return setClientVersion(player, MCVersion.V1_9_X);
            } else {
               return pv >= MCVersion.V1_8_X.verId ? setClientVersion(player, MCVersion.V1_8_X) : setClientVersion(player, MCVersion.V1_7_X);
            }
         } else {
            return setClientVersion(player, getServerVersion());
         }
      }
   }

   public static void uncache(Player p) {
      synchronized (cacheLock) {
         clientVersions.remove(p);
      }
   }

   private static MCVersion setClientVersion(Player p, MCVersion v) {
      MCVersion serv = getServerVersion();
      if (v.getId() < serv.getId()) {
         clientVersions.put(p, serv);
         return serv;
      } else {
         clientVersions.put(p, v);
         return v;
      }
   }

   public static String getVersionString() {
      return vstr == null ? (vstr = getServerVersion().name().trim().replace('_', '.').replaceFirst("V", "")) : vstr;
   }

   public static boolean isVersion(String s) {
      return getVersionString().contains(s);
   }

   public static boolean isOn19mechanics() {
      return !forceLegacyMechanics() && getServerVersion().getId() > MCVersion.V1_8_X.getId();
   }

   public static Boolean forceLegacyMechanics() {
      return forceLegacyMechanics == null
         ? (forceLegacyMechanics = Reflex.getReflex().getSetting("general.force_legacy_mechanics", false))
         : forceLegacyMechanics;
   }

   public static ConcurrentHashMap<Player, MCVersion> getClientVersions() {
      return clientVersions;
   }
}
