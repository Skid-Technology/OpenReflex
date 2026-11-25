package rip.reflex.util.protocol.viaversion;

import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

public class VVAPIAdapter {
   public int getProtocolVersion(Player player) {
      return Via.getAPI().getPlayerVersion(player.getUniqueId());
   }

   public boolean hasMinimum(Player player, ProtocolVersion version) {
      return this.getProtocolVersion(player) >= version.getId();
   }

   public ViaAPI getAPI() {
      return Via.getAPI();
   }
}
