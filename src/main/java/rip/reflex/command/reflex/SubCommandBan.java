package rip.reflex.command.reflex;

import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.data.Stats;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.stat.StatsCollector;
import rip.reflex.util.time.TimeParser;

public class SubCommandBan extends ReflexSubCommand {
   public SubCommandBan() {
      super("ban", "reflex.command.ban");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getBanUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidPlayer()), 0, ArgumentType.PLAYER);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDuration()), 1, ArgumentType.STRING);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidMessage()), 2, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      if (Reflex.getReflex().getSetting("general.reflex_kick.broadcast", true)) {
         Bukkit.broadcastMessage(
            Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getBanBroadcast())
               .replace("%player%", args.getPlayer(0).getName())
               .replace("%break%", "\n")
               .replace("\\n", "\n")
               .placeholderApi(args.getPlayer(0))
               .colourizeAndGet()
         );
      }

      TimeParser tp = args.get(1).equals("-1") ? null : new TimeParser().parse(args.get(1));
      Stats.of(args.getPlayer(0))
         .ban(
            TimeUnit.MILLISECONDS,
            tp == null ? -1L : tp.get(),
            Strings.wrap(args.joinFrom(2)).replace("%break%", "\n").replace("\\n", "\n").colourize().toString()
         );
      StatsCollector.collectBan();
   }
}
