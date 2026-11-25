package rip.reflex.listener.impl;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Giant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class EntityDamageByEntityListener extends EventListener {
   public EntityDamageByEntityListener(Reflex reflex) {
      super(reflex, "EntityDamageByEntityEvent");
   }

   @Override
   public void onEvent(Event event) {
      final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
      if (e.getEntity() instanceof LivingEntity
         && e.getCause().equals(DamageCause.ENTITY_ATTACK)
         && !(e.getDamager() instanceof Projectile)
         && !(e.getEntity() instanceof ArmorStand)
         && !(e.getEntity() instanceof Giant)
         && !(e.getEntity() instanceof TNTPrimed)
         && !e.getEntity().getType().name().toLowerCase().contains("minecart")) {
         if (e.getEntity() instanceof Player) {
            Stats stats0 = Stats.of((Player)e.getEntity());
            stats0.getStat("damageByEntityTracker", Timer.class).reset();
            stats0.setStat("damagerLocationOnAttack", e.getDamager().getLocation().clone());
            stats0.setStat("hurttimeLocation", e.getEntity().getLocation().clone());
            if (e.getDamager() instanceof LivingEntity) {
               stats0.setStat("lastAttackedBy", e.getDamager());
            }
         }

         if (e.getDamager() instanceof Player) {
            MethodProfiler mp = new MethodProfiler("PI-59827193");
            final Player p = (Player)e.getDamager();
            final Stats stats = Stats.of(p);
            if (!stats.isAdmin()) {
               if (stats.getStat("teleportTracker", Timer.class).hasPassed(2500L) && stats.getStat("deathTracker", Timer.class).hasPassed(2500L)) {
                  if (!stats.getStat("attackPenaltyTimer", Timer.class).hasPassed((long)this.reflex.getSetting("killaura.penalty_time", 1500).intValue())
                     && stats.getStat("attackCounter", int.class) > 1) {
                     e.setCancelled(true);
                  } else {
                     final LivingEntity target = (LivingEntity)e.getEntity();
                     if (stats.hasStat("currentTarget") && stats.getStat("currentTarget", LivingEntity.class).getEntityId() != target.getEntityId()) {
                        stats.getStat("targetSwitchTracker", Timer.class).reset();
                     }

                     List<Check> activeChecks = CheckRegistry.getActiveChecksOfType(CheckType.COMBAT);
                     final List<Check> failedChecks = new ArrayList<>();
                     double dist = Maths.distance(p.getLocation(), target.getLocation());
                     if (!(dist < 1.25)) {
                        if (stats.getStat("attackTracker", Timer.class).hasPassed(900L)) {
                           stats.getStat("attackTracker", Timer.class).reset();
                        }

                        World w = p.getWorld();

                        for (final Check check : activeChecks) {
                           if (!check.isDisabledInWorld(w) && stats.canBeChecked(check) && !((CombatCheck)check).isHeuristic()) {
                              if (check.isSync()) {
                                 MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "(sync)");
                                 CheckResult result = ((CombatCheck)check).check(p, stats, target);
                                 if (result != null) {
                                    if (result.isCheckFailed()) {
                                       failedChecks.add(check);
                                    }

                                    Check.addVl(stats, check.getCheat(), result);
                                    Check.check(p, stats, e, check, result);
                                 }

                                 mp0.checkExecutionTime();
                                 mp0.addToTimings();
                              } else {
                                 ReflexScheduler.async().run(new ReflexRunnable("async-" + check.getName() + ":" + check.comp() + "-thread") {
                                    @Override
                                    public void execute() throws Exception {
                                       MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "(async)");
                                       CheckResult result = ((CombatCheck)check).check(p, stats, target);
                                       if (result != null) {
                                          if (result.isCheckFailed()) {
                                             failedChecks.add(check);
                                          }

                                          Check.addVl(stats, check.getCheat(), result);
                                          Check.check(p, stats, e, check, result);
                                       }

                                       mp0.checkExecutionTime();
                                       mp0.addToTimings();
                                    }
                                 });
                              }
                           }
                        }

                        if (failedChecks.isEmpty()) {
                           stats.setViolations(
                              Cheat.valueOf("KillAura"), stats.getViolations(Cheat.valueOf("KillAura")) - this.reflex.getSetting("killaura.reduce_factor", 2)
                           );
                        }

                        stats.getStat("attackTracker0", Timer.class).reset();
                        stats.setStat("attackCounter", stats.getStat("attackCounter", int.class) + 1);
                        if (stats.getStat("attackCounter", int.class) > 100) {
                           stats.setStat("attackCounter", 0);
                        }

                        stats.setStat("currentTarget", target);
                        mp.checkExecutionTime();
                        mp.addToTimings();
                     }
                  }
               }
            }
         }
      }
   }
}
