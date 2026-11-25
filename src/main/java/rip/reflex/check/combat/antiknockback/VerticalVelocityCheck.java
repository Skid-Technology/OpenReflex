package rip.reflex.check.combat.antiknockback;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.time.Timer;

public class VerticalVelocityCheck extends PacketCheck {
   public VerticalVelocityCheck() {
      super("AntiKnockback", "vel.vtl", Cheat.valueOf("AntiKnockback"));
      if (this.isEnabledInConfig()) {
         LoopHelper.loopStatsEvery(
            stats -> stats.setViolationsIf(
                  this.cheat, () -> Maths.floor((double)stats.getViolations(this.cheat) * 0.8), () -> stats.hasTimerPassed("akbVlTracker", 4000L)
               ),
            100
         );
      }

      this.registerPacketHandler(
         new PacketAdapter(this.reflex, new PacketType[]{Packets.OUT_ENTITYVELOCITY}) {
            public void onPacketSending(final PacketEvent e) {
               if (!VerticalVelocityCheck.this.isDisabled() && !e.isCancelled()) {
                  if (!VerticalVelocityCheck.this.isServerLagging()) {
                     MethodProfiler mpPrepare = new MethodProfiler("FV-12037178");
                     final Player p = e.getPlayer();
                     final Stats stats = Stats.of(p);
                     World w = p.getWorld();
                     if (!VerticalVelocityCheck.this.isDisabledInWorld(w)
                        && stats.canBeChecked(VerticalVelocityCheck.this)
                        && p.getFireTicks() <= 0
                        && stats.getStat("deathTracker", Timer.class).hasPassed(3000L)
                        && stats.getStat("teleportTracker", Timer.class).hasPassed(1750L)) {
                        PacketContainer packet = e.getPacket();
                        int id = Packets.readInteger(packet, 0);
                        if (id == p.getEntityId()) {
                           if (stats.getStat("knockbackThread", BukkitRunnable.class) != null) {
                              stats.getStat("knockbackThread", BukkitRunnable.class).cancel();
                           }

                           int y = Packets.readInteger(packet, 2);
                           if (y >= 500 && !LocationUtils.isCollided(p)) {
                              final Location old = p.getLocation().clone();
                              final CheckResult result = new CheckResult();
                              stats.getStat("pVelocityTracker", Timer.class).reset();
                              mpPrepare.addToTimings();
                              BukkitRunnable knockbackThread = new BukkitRunnable() {
                                 double maxy = 0.0;
                                 int ticksRunning = 0;

                                 public void run() {
                                    MethodProfiler mpTick = new MethodProfiler("MW-589710923");
                                    if (this.ticksRunning < 9) {
                                       this.ticksRunning++;
                                       this.maxy = p.getLocation().getY() > this.maxy ? p.getLocation().getY() : this.maxy;
                                    } else {
                                       double k = this.maxy - old.getY();
                                       double lastk = stats.getStat("lastKnockback", double.class);
                                       double minKnockback = VerticalVelocityCheck.this.getLocalSetting("min_knockback", Double.valueOf(0.9));
                                       if (Maths.matchIn(k, 0.0, minKnockback)
                                          && (Math.abs(k - lastk) < 0.85 || k == 0.0)
                                          && !VerticalVelocityCheck.this.isMotionLegit(k)
                                          && !p.isSprinting()
                                          && !p.isSneaking()) {
                                          result.addTag("k: " + k).setMod(2);
                                          Check.addVl(stats, VerticalVelocityCheck.this.cheat, result);
                                          stats.resetTimer("akbVlTracker");
                                       } else {
                                          stats.addViolations(VerticalVelocityCheck.this.cheat, -1);
                                       }

                                       Check.check(p, stats, e, VerticalVelocityCheck.this, result);
                                       this.cancel();
                                       stats.setStat("knockbackThread", null);
                                       stats.setStat("lastKnockback", k);
                                    }

                                    mpTick.addToTimings();
                                 }
                              };
                              knockbackThread.runTaskTimer(VerticalVelocityCheck.this.reflex, 1L, 1L);
                              stats.setStat("knockbackThread", knockbackThread);
                           }
                        }
                     }
                  }
               }
            }
         }
      );
   }

   private boolean isMotionLegit(double k) {
      return Maths.matchIn(k, 0.49485, 0.49486)
         || Maths.matchIn(k, 0.1695, 0.1696)
         || Maths.matchIn(k, 0.178, 0.179)
         || Maths.matchIn(k, 0.772, 0.789)
         || Maths.matchIn(k, 0.493, 0.494)
         || Maths.matchIn(k, 0.2048, 0.2049)
         || Maths.matchIn(k, 0.33642, 0.33643)
         || Maths.matchIn(k, 0.49485, 0.49486)
         || Maths.matchIn(k, 0.200000047, 0.200000049)
         || Maths.matchIn(k, 0.63, 0.64)
         || Maths.matchIn(k, 0.299, 0.2999)
         || Maths.matchIn(k, 0.197, 0.1995)
         || Maths.matchIn(k, 0.502, 0.504)
         || Maths.matchIn(k, 0.76, 0.799)
         || Maths.matchIn(k, 0.55, 0.579)
         || Maths.matchIn(k, 0.2523, 0.25258)
         || Maths.matchIn(k, 0.20299, 0.20499)
         || Maths.matchIn(k, 0.4899, 0.4935)
         || Maths.matchIn(k, 0.01, 0.0175)
         || Maths.matchIn(k, 0.15, 0.155)
         || Maths.matchIn(k, 0.8259, 0.8277)
         || Maths.matchIn(k, 0.7099, 0.713);
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      return null;
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
