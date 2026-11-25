package rip.reflex.command.reflex;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.data.Stats;
import rip.reflex.util.misc.Strings;

public class SubCommandVerbose extends ReflexSubCommand {
   public SubCommandVerbose() {
      super("verbose", "reflex.command.verbose");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.isPlayer(Strings.color(Reflex.getReflex().getTheme(cs).getCmdPlayersOnly()));
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      Stats stats = Stats.of((Player)cs);
      stats.setStat("isVerboseActive", !stats.getStat("isVerboseActive", boolean.class));
      cs.sendMessage(
         Strings.wrap(
               Reflex.getReflex().getTheme(cs).getPrefix()
                  + (
                     stats.getBoolean("isVerboseActive")
                        ? Reflex.getReflex().getTheme(cs).getEnabledVerbose()
                        : Reflex.getReflex().getTheme(cs).getDisabledVerbose()
                  )
            )
            .colourizeAndGet()
      );
   }
}
