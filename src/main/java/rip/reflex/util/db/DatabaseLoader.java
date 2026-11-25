package rip.reflex.util.db;

import java.io.File;
import me.flo.flogger.impl.DailyLogFilePublisher;
import rip.reflex.Reflex;
import rip.reflex.api.db.BanImpl;
import rip.reflex.api.db.DataImpl;
import rip.reflex.api.db.DatabaseClient;
import rip.reflex.util.db.ban.FileBanImpl;
import rip.reflex.util.db.ban.MySQLBanImpl;
import rip.reflex.util.db.client.MySQLClient;
import rip.reflex.util.db.conf.DatabaseConfig;
import rip.reflex.util.db.dataimpl.MySQLDataImpl;
import rip.reflex.util.db.logs.MySQLLogUtil;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.protocol.mcaccess.MCAccess;

public class DatabaseLoader {
   public static void loadDB(Reflex reflex) {
      DatabaseConfig config = DatabaseConfig.load(new File(reflex.getWorkingDir()));
      reflex.setUsedDB(config != null ? config.getType() : DatabaseType.UNKNOWN);
      if (reflex.getSetting("general.custom_db.enabled", false)) {
         reflex.setUsedDB(DatabaseType.CUSTOM);
         reflex.getLogger().info("Using custom database type. Implementations specified in the config will be loaded reflectively");
      }

      switch (reflex.getUsedDB()) {
         case MYSQL:
            MySQLClient client = new MySQLClient();
            client.connect(config);
            reflex.setClient(client);
            reflex.setDataImpl(new MySQLDataImpl(client));
            reflex.setBanImpl(new MySQLBanImpl(client));
            MySQLLogUtil.setClient(client);
            MySQLLogUtil.setStarttime(System.currentTimeMillis());
            break;
         case CUSTOM:
            try {
               String ic = "Invalid custom database configuration. Missing ";
               DatabaseClient dbClient = (DatabaseClient)MCAccess.constructWithDef(
                  reflex.getConfig().getString("general.customdb.client", "Invalid custom database configuration. Missing client implementation")
               );
               dbClient.connect(config);
               reflex.setClient(dbClient);
               reflex.setDataImpl(
                  (DataImpl)MCAccess.constructWithDef(
                     reflex.getConfig().getString("general.customdb.data", "Invalid custom database configuration. Missing data implementation")
                  )
               );
               reflex.setBanImpl(
                  (BanImpl)MCAccess.constructWithDef(
                     reflex.getConfig().getString("general.customdb.ban", "Invalid custom database configuration. Missing ban implementation")
                  )
               );
               reflex.setDlogp(
                  (DailyLogFilePublisher)MCAccess.constructWithDef(
                     reflex.getConfig().getString("general.customdb.log", "Invalid custom database configuration. Missing log implementation")
                  )
               );
            } catch (Exception ex) {
               Misc.sneaky(ex);
            }
            break;
         case MONGODB:
         case UNKNOWN:
            reflex.setBanImpl(new FileBanImpl(new File(new File(reflex.getWorkingDir()), "bannedPlayers.json")));
            reflex.getBanImpl().init();
      }
   }
}
