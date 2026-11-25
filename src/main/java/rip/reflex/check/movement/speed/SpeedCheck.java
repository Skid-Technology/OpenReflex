package rip.reflex.check.movement.speed;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class SpeedCheck extends PacketCheck {
   private final double defMaxAcceleration = 0.07;
   private final double defMaxSpeed = 0.0;

   public SpeedCheck() {
      super("Speed", "speed.mn", Cheat.valueOf("Speed"));
      this.setLagSensetive(false);
   }

   @Override
   public CheckResult checkIn(Player player, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (!Packets.isMove(packet)) {
         return null;
      } else {
         boolean hasMove = Packets.readBoolean(packet, 1);
         boolean ground = Packets.readBoolean(packet, 0);
         CheckResult result = new CheckResult();
         double lastX = stats.getStat("lastX", double.class);
         double lastZ = stats.getStat("lastZ", double.class);
         double lastLastX = stats.getStat("lastLastX", double.class);
         double lastLastZ = stats.getStat("lastLastZ", double.class);
         if (!hasMove) {
            return this.rt(lastX, lastZ, 0.0, 0.0, stats, result, ground);
         } else {
            double x = data.getX();
            double z = data.getZ();
            double speed = Math.sqrt(Maths.square(lastX - x) + Maths.square(lastZ - z)) / 0.98;
            if (stats.getStat("alreadyMoved", int.class) < 2) {
               return this.rt(x, z, speed, 0.0, stats, null, ground);
            } else {
               double acce = Maths.round(
                  Math.sqrt(Maths.square(x - lastX) - Maths.square(lastX - lastLastX) + (Maths.square(z - lastZ) - Maths.square(lastZ - lastLastZ))) / 0.98, 7
               );
               if (Double.isNaN(acce)) {
                  acce = 0.0;
               }

               if (acce == 0.0 && speed == 0.0) {
                  return this.rt(x, z, speed, acce, stats, result, ground);
               } else {
                  this.checkAccelertation(ground, acce, speed, player, result, stats);
                  player.sendMessage(
                     "Speed: " + Maths.round(speed, 8) + " / Acce: " + acce + " / " + ground + "/" + player.isSprinting() + "/" + stats.isSprinting()
                  );
                  return this.rt(x, z, speed, acce, stats, result, ground);
               }
            }
         }
      }
   }

   private Object checkAccelertation(boolean ground, double acce, double speed, Player player, CheckResult result, Stats stats) {
      double lastAcce = stats.getStat("lastAcce", double.class);
      boolean lastGround = stats.getStat("SlastOnGround", boolean.class);
      boolean jump = !ground && lastGround;
      player.sendMessage(
         "LastSpeed: "
            + (stats.getStat("lastSpeed", double.class) == 0.0)
            + "  / Speed: "
            + Maths.round(speed, 8)
            + " / Acce: "
            + acce
            + " / "
            + ground
            + "/"
            + player.isSprinting()
            + "/"
            + stats.isSprinting()
      );
      double maxAcce = 0.07;
      if (stats.getStat("lastSpeed", double.class) == 0.0) {
         if (stats.isSneaking() && !Maths.matchIn(acce, 0.04636, 0.04638)) {
            return this.cancel("invalid acceleration (sneaking): " + acce, 10, result);
         } else if (stats.isSprinting() && !Maths.matchIn(acce, 0.13, 0.128)) {
            return this.cancel("invalid acceleration (sprinting): " + acce, 10, result);
         } else {
            return !Maths.matchIn(acce, 0.098, 0.1) ? this.cancel("invalid acceleration (normal): " + acce, 10, result) : result;
         }
      } else {
         if (stats.isSprinting()) {
            maxAcce = jump ? 0.305 : 0.11;
         } else if (stats.isSneaking()) {
            maxAcce = jump ? 0.025 : 0.01;
         } else {
            maxAcce = jump ? 0.083 : maxAcce;
         }

         return acce > maxAcce ? this.cancel("high acceleration: " + acce, 1, result) : result;
      }
   }

   private Object cancel(String tag, int mod, CheckResult r) {
      System.out.println("cancel: " + tag);
      return r.addTag(tag).setMod(r.getViolationsMod() + mod).cancel(true);
   }

   private CheckResult rt(double x, double z, double speed, double acce, Stats stats, CheckResult r, boolean ground) {
      int i;
      if ((i = stats.getStat("alreadyMoved", int.class)) < 2) {
         stats.setStat("alreadyMoved", i + 1);
      }

      stats.copyStat("lastX", "lastLastX");
      stats.copyStat("lastZ", "lastLastZ");
      stats.setStat("lastX", x);
      stats.setStat("lastZ", z);
      stats.setStat("lastSpeed", speed);
      stats.setStat("lastAcce", acce);
      stats.setStat("SlastOnGround", ground);
      if (r == null) {
         System.out.println("Result is null, dafuq");
      }

      return r == null ? new CheckResult() : r;
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
