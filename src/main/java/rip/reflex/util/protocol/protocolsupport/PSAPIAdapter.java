package rip.reflex.util.protocol.protocolsupport;

import java.lang.reflect.Method;
import org.bukkit.entity.Player;

public class PSAPIAdapter {
   private final Method getpvm;
   private final Method getIdMethod;

   public PSAPIAdapter() {
      try {
         this.getpvm = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class);
         this.getIdMethod = Class.forName("protocolsupport.api.ProtocolVersion").getMethod("getId");
      } catch (Exception ex) {
         throw new ExceptionInInitializerError(ex);
      }
   }

   public Integer getProtocolVersion(Player p) {
      try {
         return (Integer)this.getIdMethod.invoke(this.getpvm.invoke(null, p));
      } catch (Exception e) {
         throw new RuntimeException("Failed to retreive protocol version of " + p.getName() + "'s client using ProtocolSupport", e);
      }
   }
}
