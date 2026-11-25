package rip.reflex.util.protocol.npc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import rip.reflex.Reflex;
import rip.reflex.util.data.Stats;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class NPCTeamTricks {
   private static Scoreboard scoreboard;
   private static Team team;
   private static boolean init = false;

   public static void init() {
      if (!init) {
         ReflexScheduler.sync().run(new ReflexRunnable("IB-757012") {
            @Override
            public void execute() throws Exception {
               NPCTeamTricks.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
               NPCTeamTricks.team = NPCTeamTricks.scoreboard.registerNewTeam(Strings.randomAbc(15));
               NPCTeamTricks.team.setCanSeeFriendlyInvisibles(false);
               NPCTeamTricks.team.setAllowFriendlyFire(true);
            }
         });
         init = true;
      }
   }

   static void disableCollisions(final Player p, final NPC npc) {
      if (VersionUtils.getClientVersion(p).getId() >= MCVersion.V1_9_X.getId() && Reflex.getReflex().getSetting("killaura.disable_collisions", true)) {
         final Stats stats = Stats.of(p);
         ReflexScheduler.sync().run(new ReflexRunnable("IB-68979281") {
            @Override
            public void execute() throws Exception {
               if (!stats.getStat("scoreboardSet", boolean.class)) {
                  stats.setStat("scoreboardSet", true);
                  p.setScoreboard(NPCTeamTricks.scoreboard);
               }

               if (!NPCTeamTricks.team.hasEntry(p.getName())) {
                  NPCTeamTricks.team.addEntry(p.getName());
               }

               if (!NPCTeamTricks.team.hasEntry(npc.username)) {
                  NPCTeamTricks.team.addEntry(npc.username);
               }
            }
         });
      }
   }

   public static Scoreboard getScoreboard() {
      return scoreboard;
   }
}
