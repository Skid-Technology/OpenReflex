package rip.reflex.check.combat.killaura;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.npc.HitListener;
import rip.reflex.util.protocol.npc.NPCCommons;
import rip.reflex.util.time.Timer;

public class FrontEntityCheck extends CombatCheck implements HitListener {
   private int optWeight;

   public FrontEntityCheck() {
      super("KillAura", "ka.fe", false, Cheat.valueOf("KillAura"));
      this.setSync(true);
      NPCCommons.getNpcCommonsInstance().registerListener(this);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optWeight = this.getAdvancedLocalSetting("weight", Integer.valueOf(7));
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-1495981395");
      CheckResult result = new CheckResult();
      if (p.getNearbyEntities(4.5, 4.5, 4.5).size() <= (VersionUtils.isOn19mechanics() ? 2 : 3) && stats.hasStat("currentTarget")) {
         LivingEntity lastTarget = stats.getStat("currentTarget", LivingEntity.class);
         if (!stats.getStat("targetSwitchTracker", Timer.class).hasPassed(1800L)) {
            return null;
         } else {
            if (stats.hasFrontEntities()
               && Maths.distanceExact(p, target) > 2.1
               && target.getEntityId() == lastTarget.getEntityId()
               && Maths.distance(stats.getFrontEntities().get(0).getLocation().clone(), p.getLocation().clone()) < 10.0) {
               result.addTag("entity raytrace");
               result.setMod(this.optWeight);
               if (this.shouldCancel(stats, 10)) {
                  stats.resetTimer("attackPenaltyTracker");
               }

               result.cancel(this.shouldCancel(stats, 10));
            } else if (stats.getStat("frontEntitiesDespawnTracker", Timer.class).hasPassed(350L)
               && Maths.objects.length >= 20
               && ((Integer)Maths.objects[7] ^ ~(Integer)Maths.objects[19]) == 5798579) {
               stats.spawnFrontEntities();
               return null;
            }

            if (result.isCheckFailed()) {
               stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + result.getViolationsMod());
            }

            mp.addToTimings();
            return result;
         }
      } else {
         return null;
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }

   @Override
   public void onHit(Player hitter, int entityId, PacketContainer packet) {
      Stats stats = Stats.of(hitter);
      LivingEntity target = stats.getStat("currentTarget", LivingEntity.class);
      if (stats.hasFrontEntities() && target != null && stats.isFrontEntity(entityId)) {
         if (Maths.distanceExact(hitter, target) < 3.75 && Maths.getDistFromMouse(hitter, target) < 17.5) {
            packet.getIntegers().write(0, target.getEntityId());
         }

         stats.despawnFrontEntities();
      }
   }

   @Override
   public void onHit(Player hitter, int entityId) {
   }

   @EventHandler
   public void targetDead(EntityDeathEvent e) {
      LivingEntity entity = e.getEntity();
      Player killer = entity.getKiller();
      if (killer != null && killer.isOnline()) {
         Stats stats = Stats.of(killer);
         stats.despawnFrontEntities();
      }
   }
}
