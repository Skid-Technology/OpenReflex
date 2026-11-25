package rip.reflex.command.reflex;

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

public class SubCommandKick extends ReflexSubCommand {
   public SubCommandKick() {
      super("kick", "reflex.command.kick");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getKickUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidPlayer()), 0, ArgumentType.PLAYER);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidMessage()), 1, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      if (Reflex.getReflex().getSetting("general.reflex_kick.broadcast", true)) {
         Bukkit.broadcastMessage(
            Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getKickBroadcast())
               .replace("%player%", args.getPlayer(0).getName())
               .replace("%break%", "\n")
               .replace("\\n", "\n")
               .placeholderApi(args.getPlayer(0))
               .colourizeAndGet()
         );
      }

      Stats.of(args.getPlayer(0)).kick(Strings.wrap(args.joinFrom(1)).replace("%break%", "\n").replace("\\n", "\n").colourize().toString());
      StatsCollector.collectKick();
   }
}
