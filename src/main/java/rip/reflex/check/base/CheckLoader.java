package rip.reflex.check.base;

import rip.reflex.Reflex;
import rip.reflex.check.combat.CriticalsCheck;
import rip.reflex.check.combat.antiknockback.HorizontalVelocityCheck;
import rip.reflex.check.combat.antiknockback.VerticalVelocityCheck;
import rip.reflex.check.combat.killaura.AccuracyCheck;
import rip.reflex.check.combat.killaura.AutoBlockCheck;
import rip.reflex.check.combat.killaura.BalanceCheck;
import rip.reflex.check.combat.killaura.CPSCheck;
import rip.reflex.check.combat.killaura.CombinedCheck;
import rip.reflex.check.combat.killaura.DirectionCheck;
import rip.reflex.check.combat.killaura.FightspeedCheck;
import rip.reflex.check.combat.killaura.ForcefieldCheck;
import rip.reflex.check.combat.killaura.FrontEntityCheck;
import rip.reflex.check.combat.killaura.HeuristicsCheck;
import rip.reflex.check.combat.killaura.HitboxCheck;
import rip.reflex.check.combat.killaura.ImpossibleHitCheck;
import rip.reflex.check.combat.killaura.PacketRotsCheck;
import rip.reflex.check.combat.killaura.ReachCheck;
import rip.reflex.check.combat.killaura.SwingCheck;
import rip.reflex.check.combat.killaura.YawRateCheck;
import rip.reflex.check.combat.killaura.YawStatisticCheck;
import rip.reflex.check.movement.ElytraMoveCheck;
import rip.reflex.check.movement.InvMoveCheck;
import rip.reflex.check.movement.VehicleMoveCheck;
import rip.reflex.check.player.ClientControlCheck;
import rip.reflex.check.player.badpackets.InvalidMoveCheck;
import rip.reflex.check.player.badpackets.PacketIgnoranceCheck;
import rip.reflex.check.player.badpackets.PingSpoofCheck;
import rip.reflex.check.player.badpackets.RepetitiveItemSlotCheck;
import rip.reflex.check.player.badpackets.RepetitiveRotationCheck;
import rip.reflex.check.player.badpackets.ReverseTimerGlideCheck;
import rip.reflex.check.player.invactions.ClosedInvClickCheck;
import rip.reflex.check.player.invactions.FastInvClickCheck;
import rip.reflex.check.world.ScaffoldCheck;
import rip.reflex.check.world.TowerCheck;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;

public class CheckLoader {
   private static long i = -498162321798562193L;
   public static Check reachCheck = null;
   public static Check yawStatCheck = null;
   public static HeuristicsCheck heuristicsCheck = null;

   public static void loadAll(Reflex r) {
      if (i % 2L != 0L && r != null) {
         i = -i + (long)((int)Math.sqrt(25.0));
         CheckRegistry.registerCheck(new ScaffoldCheck());
         CheckRegistry.registerCheck(new TowerCheck());
         CheckRegistry.registerCheck(new SwingCheck());
         CheckRegistry.registerCheck(reachCheck = new ReachCheck());
         CheckRegistry.registerCheck(new YawRateCheck());
         CheckRegistry.registerCheck(new CriticalsCheck());
         if (!r.getSetting("killaura.no_npc_checks", false)) {
            CheckRegistry.registerCheck(new FrontEntityCheck());
            CheckRegistry.registerCheck(new ForcefieldCheck());
         }

         CheckRegistry.registerCheck(new CPSCheck());
         CheckRegistry.registerCheck(new FightspeedCheck());
         CheckRegistry.registerCheck(new DirectionCheck());
         CheckRegistry.registerCheck(new AccuracyCheck());
         CheckRegistry.registerCheck(new VerticalVelocityCheck());
         CheckRegistry.registerCheck(new HorizontalVelocityCheck());
         CheckRegistry.registerCheck(new PingSpoofCheck());
         CheckRegistry.registerCheck(yawStatCheck = new YawStatisticCheck());
         CheckRegistry.registerCheck(new CombinedCheck());
         if (VersionUtils.getServerVersion().isLessThan(MCVersion.V1_12_X)) {
            CheckRegistry.registerCheck(new AutoBlockCheck());
         }

         CheckRegistry.registerCheck(new ImpossibleHitCheck());
         CheckRegistry.registerCheck(new PacketRotsCheck());
         CheckRegistry.registerCheck(new RepetitiveRotationCheck());
         CheckRegistry.registerCheck(new RepetitiveItemSlotCheck());
         CheckRegistry.registerCheck(new InvalidMoveCheck());
         CheckRegistry.registerCheck(new ClientControlCheck());
         CheckRegistry.registerCheck(new InvMoveCheck());
         CheckRegistry.registerCheck(new ClosedInvClickCheck());
         CheckRegistry.registerCheck(new FastInvClickCheck());
         CheckRegistry.registerCheck(new PacketIgnoranceCheck());
         CheckRegistry.registerCheck(new BalanceCheck());
         CheckRegistry.registerCheck(new HitboxCheck());
         if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X)) {
            CheckRegistry.registerCheck(new VehicleMoveCheck());
         } else {
            r.info("The VehicleMove check only works on MC 1.9+. No worries: boat cheats also work on 1.9+ only.");
         }

         if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X)) {
            CheckRegistry.registerCheck(new ElytraMoveCheck());
         } else {
            r.info("The ElytraMove check only works on MC 1.9+. No worries: there are no elytras on 1.8 ;)");
         }

         CheckRegistry.registerCheck(new ReverseTimerGlideCheck());
         CheckRegistry.registerCheck(heuristicsCheck = new HeuristicsCheck());
      }
   }
}
