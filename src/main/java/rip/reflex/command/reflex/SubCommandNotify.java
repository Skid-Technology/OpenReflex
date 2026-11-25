package rip.reflex.command.reflex;

import me.flo.flogger.types.FlogLevel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.misc.Strings;

public class SubCommandNotify extends ReflexSubCommand {
   public SubCommandNotify() {
      super("notify", "reflex.command.notify");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getNotifyUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidMessage()), 0, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      String sender = cs instanceof Player ? cs.getName() : Reflex.getReflex().getTheme(cs).getConsole();
      String message = Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getNotifyFormat())
         .replace("%sender%", sender)
         .replace("%message%", args.joinFrom(0))
         .replace("%break%", "\n")
         .replace("\\n", "\n")
         .colourizeAndGet();
      LoopHelper.loopPermitted(p -> p.sendMessage(message), "reflex.notify");
      Reflex.getReflex().log(FlogLevel.INFO, "(NOTIFY) " + Strings.removeColorCodes(message));
   }
}
