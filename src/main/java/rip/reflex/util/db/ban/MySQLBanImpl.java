package rip.reflex.util.db.ban;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import rip.reflex.api.BanInfo;
import rip.reflex.api.db.BanImpl;
import rip.reflex.util.db.client.MySQLClient;
import rip.reflex.util.handler.ExceptionHandler;
import rip.reflex.util.handler.SaveConsumer;

public class MySQLBanImpl implements BanImpl {
   private final MySQLClient client;
   private boolean table = false;

   @Override
   public boolean ban(UUID uuid, String msg, long time, TimeUnit timeUnit) {
      this.createTable();
      BanInfo info = this.getBanInfo(uuid);
      if (info.getBanTime() != 0L) {
         return false;
      } else {
         this.client
            .executePrepared("INSERT INTO reflex_bans (uuid, endofban, banstart, message) VALUES (?, ?, ?, ?)", (SaveConsumer<PreparedStatement>)ps -> {
               ps.setString(1, uuid.toString());
               ps.setLong(2, System.currentTimeMillis() + timeUnit.toMillis(time));
               ps.setLong(3, System.currentTimeMillis());
               ps.setString(4, msg);
            });
         return true;
      }
   }

   @Override
   public boolean unban(UUID uuid) {
      this.createTable();
      BanInfo info = this.getBanInfo(uuid);
      if (info.getBanTime() == 0L) {
         return false;
      } else {
         this.client.executePrepared("DELETE FROM reflex_bans WHERE uuid=?", ps -> ExceptionHandler.save(Object.class, () -> {
               ps.setString(1, uuid.toString());
               return null;
            }));
         return true;
      }
   }

   @Override
   public BanInfo getBanInfo(UUID uuid) {
      this.createTable();
      this.client.execute("DELETE FROM reflex_bans WHERE uuid<-1");
      return (BanInfo)this.client
         .executeQuery(
            "SELECT banStart, endofban, message FROM reflex_bans WHERE uuid='" + uuid.toString() + "'",
            (ExceptionHandler.SafeFunction<ResultSet, Object>)resultSet -> {
               if (!resultSet.next()) {
                  return BanInfo.NOT_BANNED;
               } else {
                  long endOfBan = resultSet.getLong("endofban");
                  long banStart = resultSet.getLong("banStart");
                  String msg = resultSet.getString("message");
                  return new BanInfo(msg, endOfBan == -1L ? -1L : endOfBan - System.currentTimeMillis(), banStart);
               }
            }
         );
   }

   @Override
   public int getBanCount() {
      this.createTable();
      Object o = this.client.executeQuery("SELECT COUNT(1) FROM `reflex_bans`", (ExceptionHandler.SafeFunction<ResultSet, Object>)resultSet -> {
         resultSet.next();
         return resultSet.getInt(1);
      });
      return (Integer)o;
   }

   @Override
   public int getBannedCountIn(TimeUnit unit, long value) {
      this.createTable();
      long timeStamp = System.currentTimeMillis() - unit.toMillis(value);
      Object o = this.client
         .executeQuery("SELECT COUNT(1) FROM `reflex_bans` WHERE banstart > " + timeStamp, (ExceptionHandler.SafeFunction<ResultSet, Object>)rs -> {
            rs.next();
            return rs.getInt(1);
         });
      return (Integer)o;
   }

   private void createTable() {
      if (!this.table) {
         this.table = true;
         this.client
            .execute(
               "CREATE TABLE IF NOT EXISTS `reflex_bans`\n(\n    `uuid` TINYTEXT NOT NULL,\n    `endofban` BIGINT NOT NULL, \n    `banstart` BIGINT NOT NULL, \n    `message` TINYTEXT NOT NULL \n) \n    ENGINE = InnoDB;"
            );
      }
   }

   public MySQLBanImpl(MySQLClient client) {
      this.client = client;
   }
}
