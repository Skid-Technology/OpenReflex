package rip.reflex.check.player.invactions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.CustomCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;

public class ClosedInvClickCheck extends CustomCheck {
   public ClosedInvClickCheck() {
      super("InvActions", "invact.civ", Cheat.valueOf("InvActions"));
   }

   @EventHandler
   public void onInvClick(InventoryClickEvent e) {
      MethodProfiler mp = new MethodProfiler("PE-89271380");
      Player p = (Player)e.getWhoClicked();
      Stats stats = Stats.of(p);
      if (stats.canBeChecked(this)) {
         if (!stats.isInventoryOpened()
            && e.getInventory().getType() != InventoryType.CHEST
            && e.getSlotType() != SlotType.QUICKBAR
            && VersionUtils.getClientVersion(p).getVerId() < MCVersion.V1_12_X.getVerId()) {
            CheckResult result = new CheckResult().setMod(2).cancel(this.isNotSilent()).addTag("s: " + e.getRawSlot());
            Check.addVl(stats, this.getCheat(), result);
            if (this.isNotSilent()) {
               e.setResult(Result.DENY);
            }

            Check.check(p, stats, e, this, result);
         }

         mp.addToTimings();
      }
   }
}
