package rip.reflex.check.combat.killaura;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.reflex.SubCommandBroadcast;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.Movement;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.mcaccess.MCAccess;

public class ImpossibleHitCheck extends CombatCheck {
   public static int integrityCheck = 10;

   public ImpossibleHitCheck() {
      super("KillAura", "ka.imp", false, Cheat.valueOf("KillAura"));
      this.setSync(true);
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-951687");
      CheckResult result = new CheckResult();
      if (integrityCheck > -5 && integrityCheck-- == 0) {
         try {
            CommandConditions.integrityRan[0] = CommandConditions.integrityRan[0] - 2913;
            String[] data = this.check("2");
            if (data != null && data.length == 2) {
               int fhash = data[1].hashCode() + "f".hashCode();
               int aihash = "ai".hashCode() - data[0].hashCode();
               Class intcheckA = Class.forName("rip.reflex.util.time.Timer");
               Field fingerprintA = intcheckA.getDeclaredField("integrityDataFingerprint_a");
               Field arrinfoA = intcheckA.getDeclaredField("integrityDataArrInfo_a");
               MCAccess.secureSetFinalStatic(fingerprintA, fhash);
               MCAccess.secureSetFinalStatic(arrinfoA, aihash);
               if (Maths.intcheckSetB("rip.reflex.util.performance.timings.TimingsEntry", fhash, aihash) == null) {
                  throw new IOException();
               }
            }
         } catch (Throwable ignored) {
         }
      }

      int vl = 0;
      if (p.isDead()) {
         result.addTag("dead");
         vl++;
      }

      if (p.isBlocking()) {
         result.addTag("blocking");
         vl++;
      }

      if (p.isSleeping() && p.getSleepTicks() > 10) {
         result.addTag("sleeping");
         vl++;
      }

      if (stats.isInventoryOpened() && stats.getTimerTicked("inventoryOpenTracker") > 15L) {
         result.addTag("inventory opened");
         vl++;
      }

      mp.addToTimings();
      return result.setMod(vl)
         .cancel(this.shouldCancel(stats, 5) && vl > 0 && (result.hasTag("dead") || result.hasTag("sleeping") || result.hasTag("inventory opened")));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }

   private String[] check(String g) {
      try {
         Class reflex = Class.forName("rip.reflex.Reflex");

         try {
            Method classFingerprint = reflex.getDeclaredMethod("classFingerprint", String.class);
            classFingerprint.setAccessible(true);
            String fingerprint = (String)classFingerprint.invoke(
               this.reflex, new String(SubCommandBroadcast.integrityShit()) + hex((long)Movement.getApinstance().getResource().hashCode())
            );
            classFingerprint.setAccessible(false);

            try {
               CommandConditions.integrityRan[4] = CommandConditions.integrityRan[4] - (-972);
               Method arrInfo = reflex.getDeclaredMethod("arrInfo", long.class);
               arrInfo.setAccessible(true);
               long info = (Long)arrInfo.invoke(this.reflex, System.nanoTime());
               arrInfo.setAccessible(false);
               String[] s = new String[Integer.parseInt(g, 10)];

               try {
                  s[1] = fingerprint;
                  s[0] = hex(info);
                  return s;
               } catch (Throwable ex) {
                  throw new RuntimeException("4", ex);
               }
            } catch (Throwable e) {
               throw new RuntimeException("3", e);
            }
         } catch (Throwable err) {
            throw new RuntimeException("2", err);
         }
      } catch (Throwable cause) {
         throw new RuntimeException("1", cause);
      }
   }

   private static String hex(long l) {
      return Long.toString(l, 16).replace("-", "").toUpperCase();
   }
}
