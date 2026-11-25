package rip.reflex.util.db.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import rip.reflex.api.db.DatabaseClient;
import rip.reflex.util.db.DatabaseException;
import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.db.conf.DatabaseConfig;

public class MySQLClient implements DatabaseClient {
   private Connection con;

   public void execute(String cmd) {
      if (this.isConnected()) {
         try {
            this.con.createStatement().execute(cmd);
         } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DatabaseException("MySQL / Error while executing command '" + cmd + "'", ex);
         }
      }
   }

   public Object executeQuery(String query, Function<ResultSet, Object> cons) {
      if (!this.isConnected()) {
         return null;
      } else {
         try {
            ResultSet set = this.con.createStatement().executeQuery(query);
            Object o = cons != null && set != null ? cons.apply(set) : null;
            if (set != null) {
               set.close();
            }

            return o;
         } catch (Exception e) {
            throw new DatabaseException("MySQL / Error while executing query: '" + query + "'", e);
         }
      }
   }

   public void executePrepared(String query, Consumer<PreparedStatement> cons) {
      if (this.isConnected()) {
         try {
            PreparedStatement ps = this.con.prepareStatement(query);
            cons.accept(ps);
            ps.executeUpdate();
         } catch (Exception ex) {
            throw new DatabaseException("MySQL / Error while executing prepared query: '" + query + "'", ex);
         }
      }
   }

   @Override
   public void connect0(DatabaseConfig c) throws Exception {
      this.con = DriverManager.getConnection(
         "jdbc:mysql://"
            + c.getHost()
            + ":"
            + c.getPort()
            + "/"
            + c.getDatabase()
            + "?user="
            + c.getUser()
            + "&password="
            + c.getPw()
            + "&autoReconnect=true&useUnicode=true&characterEncoding=utf-8"
      );
   }

   @Override
   public boolean isConnected0() throws Exception {
      return this.con != null && !this.con.isClosed();
   }

   @Override
   public void disconnect0() throws Exception {
      if (this.isConnected()) {
         this.con.close();
      }
   }

   @Override
   public DatabaseType getType() {
      return DatabaseType.MYSQL;
   }
}
