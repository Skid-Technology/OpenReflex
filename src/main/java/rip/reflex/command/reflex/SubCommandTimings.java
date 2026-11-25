package rip.reflex.command.reflex;

import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.performance.timings.ReflexTimings;

public class SubCommandTimings extends ReflexSubCommand {
   private boolean saving;

   public SubCommandTimings() {
      super("timings", "reflex.command.timings");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getTimingsUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidOperation()), 0, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      if (!Reflex.getReflex().offlineMode) {
         if (!this.saving) {
            switch (args.get(0)) {
               case "on":
                  if (ReflexTimings.isEnabled()) {
                     cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getTimingsAlreadyActive()));
                  } else {
                     ReflexTimings.setEnabled(true);
                     cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getTimingsEnabled()));
                  }
                  break;
               case "save":
               case "paste":
                  if (!ReflexTimings.isEnabled()) {
                     cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getTimingsInactive()));
                  } else {
                     cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getTimingsSaving()));
                     this.saving = true;
                     ReflexTimings.saveAndReset();
                     cs.sendMessage(
                        Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getTimingsSaved())
                           .replace("%dir%", Reflex.getReflex().getWorkingDir() + "/timings/")
                           .colourizeAndGet()
                     );
                     this.saving = false;
                  }
                  break;
               default:
                  cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getCmdInvalidOperation()));
            }
         }
      }
   }
}
