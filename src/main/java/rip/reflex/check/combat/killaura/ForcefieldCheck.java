package rip.reflex.check.combat.killaura;

import com.comphenix.protocol.events.PacketContainer;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.npc.HitListener;
import rip.reflex.util.protocol.npc.NPC;
import rip.reflex.util.protocol.npc.NPCCommons;
import rip.reflex.util.time.Timer;

public class ForcefieldCheck extends CombatCheck implements HitListener {
   public ForcefieldCheck() {
      super("KillAura", "ka.ffd", false, Cheat.valueOf("KillAura"));
      this.setSync(true);
      NPCCommons.getNpcCommonsInstance().registerListener(this);
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-51877295");
      if (!stats.hasNpcs()) {
         stats.spawnNpcs();
      }

      mp.addToTimings();
      return null;
   }

   @Override
   public void onHit(Player hitter, int entityId, PacketContainer packet) {
      MethodProfiler mp = new MethodProfiler("CT-5987129");
      if (hitter.getNearbyEntities(2.0, 2.0, 2.0).size() <= 0) {
         Stats stats = Stats.of(hitter);
         Random random = new Random();
         if (stats.isNpc(entityId)) {
            CheckResult result = new CheckResult();
            NPC npc = stats.getNpc(entityId);
            double dx = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
            double dz = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
            if (stats.isMainNpc(entityId)) {
               if (!stats.getStat("forcefieldTracker", Timer.class).hasPassed(800L)) {
                  result.setMod(5).addTag("forcefield");
                  Check.addVl(stats, this.cheat, result);
                  stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 2);
               }

               stats.getStat("forcefieldTracker", Timer.class).reset();
            }

            try {
               npc.setLocation(npc.getLocation().add(dx * 2.85, random.nextBoolean() ? 2.85 : -2.85, dz * 2.85));
            } catch (Exception ex) {
               ex.printStackTrace();
            }

            mp.addToTimings();
            Check.check(hitter, stats, null, this, result);
         }
      }
   }

   @Override
   public void onHit(Player hitter, int entityId) {
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }
}
