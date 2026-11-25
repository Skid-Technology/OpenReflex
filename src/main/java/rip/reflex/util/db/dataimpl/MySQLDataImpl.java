package rip.reflex.util.db.dataimpl;

import java.sql.PreparedStatement;
import rip.reflex.api.db.DataImpl;
import rip.reflex.util.db.client.MySQLClient;
import rip.reflex.util.handler.ExceptionHandler;
import rip.reflex.util.handler.SaveConsumer;

public class MySQLDataImpl implements DataImpl {
   private final MySQLClient client;
   private boolean table = false;

   @Override
   public void saveData(String hash, byte[] data) {
      this.createTable();
      if (this.contains()) {
         this.client.executePrepared("UPDATE reflex_ds SET hash=?,data=? WHERE ky=?", (SaveConsumer<PreparedStatement>)ps -> {
            ps.setString(1, hash);
            ps.setBytes(2, data);
            ps.setBoolean(3, true);
         });
      } else {
         this.client.executePrepared("INSERT INTO reflex_ds (ky, hash, data) VALUES (?, ?, ?)", (SaveConsumer<PreparedStatement>)ps -> {
            ps.setBoolean(1, true);
            ps.setString(2, hash);
            ps.setBytes(3, data);
         });
      }
   }

   @Override
   public boolean contains() {
      this.createTable();
      return (Boolean)this.client.executeQuery("SELECT hash FROM reflex_ds WHERE ky=true", resultSet -> ExceptionHandler.save(Boolean.class, resultSet::next));
   }

   @Override
   public byte[] getData() {
      this.createTable();
      return (byte[])this.client
         .executeQuery("SELECT data FROM reflex_ds WHERE ky=true", rs -> ExceptionHandler.save(byte[].class, () -> rs.next() ? rs.getBytes("data") : null));
   }

   @Override
   public String getHash() {
      this.createTable();
      return (String)this.client
         .executeQuery("SELECT hash FROM reflex_ds WHERE ky=true", rs -> ExceptionHandler.save(String.class, () -> rs.next() ? rs.getString("hash") : null));
   }

   private void createTable() {
      if (!this.table) {
         this.table = true;
         this.client
            .execute(
               "CREATE TABLE IF NOT EXISTS `reflex_ds`\n(\n    `ky` BOOL NOT NULL,\n    `hash` TINYTEXT NOT NULL, \n    `data` MEDIUMBLOB NOT NULL\n    \n) \n    ENGINE = InnoDB;"
            );
      }
   }

   public MySQLDataImpl(MySQLClient client) {
      this.client = client;
   }
}
