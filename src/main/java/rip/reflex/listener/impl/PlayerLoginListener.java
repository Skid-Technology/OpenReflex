package rip.reflex.listener.impl;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import rip.reflex.Reflex;
import rip.reflex.check.combat.killaura.ImpossibleHitCheck;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;

public class PlayerLoginListener extends EventListener {
   public PlayerLoginListener(Reflex reflex) {
      super(reflex, "PlayerLoginEvent");
   }

   @Override
   public void onEvent(Event event) {
      PlayerLoginEvent e = (PlayerLoginEvent)event;
      Player p = e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         if (this.reflex.isPlayerBanned(p)) {
            e.disallow(Result.KICK_OTHER, this.reflex.getBanInfo(p).getMessage());
         }

         try {
            if (ImpossibleHitCheck.integrityCheck <= 0 || Math.abs(System.currentTimeMillis() - this.reflex.getBeganStartup()) > TimeUnit.MINUTES.toMillis(15L)
               )
             {
               Class c = Class.forName("rip.reflex.util.time.Timer");
               Field fingerprint = c.getDeclaredField("integrityDataFingerprint_a");
               fingerprint.setAccessible(true);
               int iFingerprint = (Integer)fingerprint.get(null);
               fingerprint.setAccessible(false);
               if ((iFingerprint ^ -21938) != -721251022) {
               }

               Field arrInfo = c.getDeclaredField("integrityDataArrInfo_a");
               arrInfo.setAccessible(true);
               int iArrInfo = (Integer)arrInfo.get(null);
               arrInfo.setAccessible(false);
               if ((iArrInfo ^ 7857932) != -236221891) {
               }
            }
         } catch (Throwable ignored) {
         }
      }
   }
}
