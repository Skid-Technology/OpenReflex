package rip.reflex.util.db.client;

import rip.reflex.api.db.DatabaseClient;
import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.db.conf.DatabaseConfig;

public class MongoDBClient implements DatabaseClient {
   @Override
   public void connect0(DatabaseConfig config) throws Exception {
   }

   @Override
   public boolean isConnected0() throws Exception {
      return false;
   }

   @Override
   public void disconnect0() throws Exception {
   }

   @Override
   public DatabaseType getType() {
      return DatabaseType.MONGODB;
   }
}
