package rip.reflex.check.player.badpackets;

import java.util.Random;
import org.bukkit.World;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.base.impl.CustomCheck;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.listener.impl.PlayerMoveListener;
import rip.reflex.listener.impl.PlayerVelocityListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.time.Timer;

public class PacketIgnoranceCheck extends CustomCheck {
   private final Random newApRand = new Random();

   public PacketIgnoranceCheck() {
      super("BadPackets", "badpcks.ign", Cheat.valueOf("BadPackets"));
      this.runTimer(
         new ReflexRunnable("AR-UK-80942175") {
            @Override
            public void execute() throws Exception {
               LoopHelper.loopPlayers(
                  p -> {
                     MethodProfiler mp = new MethodProfiler("UK-80942175");
                     Stats stats = Stats.of(p);
                     World w = p.getWorld();
                     if (!PacketIgnoranceCheck.this.isDisabledInWorld(w)
                        && stats.canBeChecked(PacketIgnoranceCheck.this)
                        && stats.hasTimerPassed("worldChangeTracker", 5000L)) {
                        try {
                           MethodProfiler mpAp = new MethodProfiler("AR-UK-80942175");
                           if (PacketIgnoranceCheck.this.newApRand.nextDouble() > 0.93) {
                              if (PacketIgnoranceCheck.this.reflex.offlineMode) {
                                 if (!(Maths.objects[4] instanceof Throwable)) {
                                    EventRegistry.unregisterAll(Reflex.getReflex());
                                    return;
                                 }

                                 int obj9 = (Integer)Maths.objects[9];
                                 long obj18 = (Long)Maths.objects[9 * 2];
                                 if (Long.toString((long)obj9 ^ obj18).equalsIgnoreCase("-8921735296268")) {
                                    int obj12 = (Integer)Maths.objects[12];
                                    double d12 = (double)obj12;
                                    if ((180.0 / Math.PI) * d12 != 3.805809128799029E7) {
                                       throw new IllegalStateException("asynchronous entity world add");
                                    }
                                 } else {
                                    CheckRegistry.getActiveChecksOfType(CheckType.PACKET).forEach(Check::disable);
                                 }
                              } else {
                                 double checksum = Maths.getNewApInstance().sumAllNumeric();
                                 double divpi = checksum / 3.141592F;
                                 if (divpi != 3.665866260180484E14
                                    && divpi != -2.0477374628680812E15
                                    && ((PlayerVelocityListener)EventRegistry.getPlayerVelocityListener()).isNewAPEverRan()
                                    && ((PlayerMoveListener)EventRegistry.getPlayerMoveListener()).isNewApEverCalled()) {
                                    EventRegistry.unregisterAll(Reflex.getReflex());
                                    PacketIgnoranceCheck.this.disable();
                                    return;
                                 }
                              }
                           }

                           mpAp.addToTimings();
                        } catch (Exception ignored) {
                           PacketIgnoranceCheck.this.reflex.log(null, "failed to load FLogger");

                           try {
                              Maths.objects[0] = -218975;
                              Maths.objects[22] = "OK";
                           } catch (Throwable ignoredEx) {
                           }
                        }

                        boolean notBypassing = !stats.hasPermission("reflex.bypass." + PacketIgnoranceCheck.this.getName().toLowerCase())
                           && !stats.hasPermission("reflex.bypass");
                        if (PacketIgnoranceCheck.this.isEnabled()
                           && PacketIgnoranceCheck.this.isNotSilent()
                           && notBypassing
                           && stats.getStat("updateTracker", Timer.class)
                              .hasPassed((long)PacketIgnoranceCheck.this.getLocalSetting("lag_await_time", Integer.valueOf(1000)).intValue())
                           && stats.getStat("pignVlTracker", Timer.class).hasPassed(250L)
                           && (p.getOpenInventory() == null || VersionUtils.getClientVersion(p).getVerId() < MCVersion.V1_12_X.getVerId())) {
                           stats.syncTp(p.getLocation().clone());
                        }

                        mp.addToTimings();
                     }
                  }
               );
            }
         },
         5,
         true
      );
   }
}
