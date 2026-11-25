package rip.reflex.check.base.impl;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckType;
import rip.reflex.util.data.Stats;

public abstract class CombatCheck extends Check {
   private final boolean heuristic;

   public CombatCheck(String name, String comp, boolean heuristic, Cheat cheat) {
      super(name, comp, CheckType.COMBAT, cheat);
      this.heuristic = heuristic;
   }

   public abstract CheckResult check(Player player, Stats stats, LivingEntity target);

   public abstract CheckResult check(Player player, Stats stats, Location from, Location to);

   public boolean isHeuristic() {
      return this.heuristic;
   }
}
