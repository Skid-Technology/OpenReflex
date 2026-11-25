package rip.reflex.command.reflex;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.misc.Strings;

public class SubCommandReset extends ReflexSubCommand {
   public SubCommandReset() {
      super("reset", "reflex.command.reset");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getResetUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidPlayer()), 0, ArgumentType.PLAYER);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      Player p = args.getPlayer(0);
      Reflex.getReflex().reset(p);
      cs.sendMessage(
         Strings.wrap(Reflex.getReflex().getTheme(p).getPrefix() + Reflex.getTheme().getReflexReset())
            .replace("%player%", p.getName())
            .placeholderApi(p)
            .colourizeAndGet()
      );
   }
}
