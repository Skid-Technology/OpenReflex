package rip.reflex.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;

public class ReflexCheckEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Cheat cheat;
   private final String violationId;
   private final CheckResult result;
   private boolean cancelled;

   public ReflexCheckEvent(Player player, Cheat cheat, CheckResult result, String violationId) {
      this.player = player;
      this.cheat = cheat;
      this.result = result;
      this.violationId = violationId;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Cheat getCheat() {
      return this.cheat;
   }

   public CheckResult getResult() {
      return this.result;
   }

   public String getViolationId() {
      return this.violationId;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }
}
