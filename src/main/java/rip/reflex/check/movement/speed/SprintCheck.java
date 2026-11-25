package rip.reflex.check.movement.speed;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.MovementCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;

public class SprintCheck extends MovementCheck {
   public SprintCheck() {
      super("Speed", "speed.srt", 0, Cheat.valueOf("Speed"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      if (!p.isSprinting()) {
         return null;
      } else {
         CheckResult result = new CheckResult();
         int mod = 0;
         if (p.isSneaking()) {
            result.addTag("sneak sprinting");
            mod += 5;
         }

         if (p.isBlocking()) {
            result.addTag("block sprinting");
            mod += 5;
         }

         if (LocationUtils.isBackwardsMove(from, to)) {
            result.addTag("omnidirectional sprinting");
            mod += 2;
         }

         if (p.hasPotionEffect(PotionEffectType.BLINDNESS) || p.hasPotionEffect(PotionEffectType.CONFUSION)) {
            result.addTag("b/c sprinting");
            mod += 5;
         }

         if (p.getFoodLevel() < 6) {
            result.addTag("low saturation sprinting");
            mod += 5;
         }

         result.setMod(mod);
         result.cancel(mod > 0 && Stats.of(p).getViolations(this.cheat) > 14);
         return result;
      }
   }
}
