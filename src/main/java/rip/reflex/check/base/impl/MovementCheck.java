package rip.reflex.check.base.impl;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckType;
import rip.reflex.util.data.Stats;

public abstract class MovementCheck extends Check {
   private final int checkTimeTicks;

   public MovementCheck(String name, String comp, int checkTimeTicks, Cheat cheat) {
      super(name, comp, CheckType.MOVEMENT, cheat);
      this.checkTimeTicks = checkTimeTicks;
   }

   public abstract CheckResult check(Player player, Stats stats, Location from, Location to);

   public int getCheckTimeTicks() {
      return this.checkTimeTicks;
   }
}
