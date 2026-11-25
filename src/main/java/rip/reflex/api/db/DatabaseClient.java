package rip.reflex.api.db;

import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.db.conf.DatabaseConfig;

public interface DatabaseClient {
   default void connect(DatabaseConfig config) {
      try {
         this.connect0(config);
      } catch (Exception ex) {
         throw new RuntimeException("Error while connecting to database!", ex);
      }
   }

   default boolean isConnected() {
      try {
         return this.isConnected0();
      } catch (Exception e) {
         throw new RuntimeException("Error while checking database connection status", e);
      }
   }

   default void disconnect() {
      try {
         this.disconnect0();
      } catch (Exception err) {
         throw new RuntimeException("Error while disconnecting from database!", err);
      }
   }

   void connect0(DatabaseConfig config) throws Exception;

   boolean isConnected0() throws Exception;

   void disconnect0() throws Exception;

   DatabaseType getType();
}
