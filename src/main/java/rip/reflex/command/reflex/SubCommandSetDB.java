package rip.reflex.command.reflex;

import java.io.File;
import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.db.conf.DatabaseConfig;
import rip.reflex.util.misc.Strings;

public class SubCommandSetDB extends ReflexSubCommand {
   public SubCommandSetDB() {
      super("setdb", "reflex.command.setdb");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getSetdbUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDbType()), 0, ArgumentType.REFLEX_DBTYPE);
      cc.check("MongoDB is not supported yet", args -> DatabaseType.byString(args.get(0)) != DatabaseType.MONGODB);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDbHost()), 1, ArgumentType.STRING);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDbPort()), 2, ArgumentType.WEB_PORT);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDbName()), 3, ArgumentType.STRING);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDbUser()), 4, ArgumentType.STRING);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidDbPass()), 5, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      DatabaseConfig config = new DatabaseConfig();
      config.setType(DatabaseType.byString(args.get(0)));
      config.setHost(args.get(1));
      config.setPort(Integer.valueOf(args.get(2)));
      config.setDatabase(args.get(3));
      config.setUser(args.get(4));
      config.setPw(args.get(5));
      config.save(new File(Reflex.getReflex().getWorkingDir()));
      cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getSetdbSuccess()));
   }
}
