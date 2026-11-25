package rip.reflex.util.protocol.npc;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import java.util.ArrayList;
import java.util.SplittableRandom;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;

public class NPCCommons {
   private static NPCCommons npcCommonsInstance;
   public final ArrayList<HitListener> listeners = new ArrayList<>();
   private final MCVersion mcVersion = VersionUtils.getServerVersion();
   public SplittableRandom rdm;

   public static ProtocolManager getProtocolManager() {
      return ProtocolLibrary.getProtocolManager();
   }

   public static MCVersion getMCVersion() {
      return getNpcCommonsInstance().mcVersion;
   }

   public static NPCCommons getNpcCommonsInstance() {
      return npcCommonsInstance;
   }

   public static ArrayList<HitListener> getListeners() {
      return getNpcCommonsInstance().listeners;
   }

   public static SplittableRandom getRdm() {
      return getNpcCommonsInstance().rdm;
   }

   public void init() {
      npcCommonsInstance = this;
      this.rdm = new SplittableRandom();
      new HitPacketListener();
   }

   public void registerListener(HitListener listener) {
      if (!this.listeners.contains(listener)) {
         this.listeners.add(listener);
      }
   }

   public void unregisterListener(HitListener listener) {
      this.listeners.remove(listener);
   }
}
