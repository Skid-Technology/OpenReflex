package rip.reflex.command.reflex;

import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.data.Stats;
import rip.reflex.util.misc.Strings;

public class SubCommandBypass extends ReflexSubCommand {
   public SubCommandBypass() {
      super("bypass", "reflex.command.bypass");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getBypassUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidPlayer()), 0, ArgumentType.PLAYER);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidCheck()), 1, ArgumentType.REFLEX_CHECK);
      cc.checkOptionalArg(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidBoolean()), 2, ArgumentType.BOOLEAN);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      Stats stats = Stats.of(args.getPlayer(0));
      String check = Check.correctName(args.get(1));
      boolean bypassedPreviously = args.has(2) ? !args.getBool(2) : stats.getStat("canBypass" + check, boolean.class);
      stats.setStat("canBypass" + check, !bypassedPreviously);
      cs.sendMessage(
         Strings.wrap(
               Reflex.getReflex().getTheme(cs).getPrefix()
                  + (bypassedPreviously ? Reflex.getReflex().getTheme(cs).getForbadeBypass() : Reflex.getReflex().getTheme(cs).getAllowedBypass())
            )
            .replace("%player%", stats.getPlayer().getName())
            .replace("%check%", check)
            .placeholderApi(stats.getPlayer())
            .colourizeAndGet()
      );
   }
}
