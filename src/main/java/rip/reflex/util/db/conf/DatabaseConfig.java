package rip.reflex.util.db.conf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import rip.reflex.util.crypto.Encryptor;
import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.misc.Misc;

public class DatabaseConfig {
   private DatabaseType type;
   private int port;
   private String host;
   private String database;
   private String user;
   private String pw;

   public static DatabaseConfig load(File dir) {
      if (!dir.exists()) {
         dir.mkdir();
      }

      File dataFile = new File(dir, "dbconf");
      if (!dataFile.exists()) {
         return null;
      } else {
         try {
            FileInputStream fileIn = new FileInputStream(dataFile);
            CipherInputStream cipherIn = new CipherInputStream(fileIn, Encryptor.getCipher(2));
            DataInputStream in = new DataInputStream(cipherIn);
            DatabaseConfig config = new DatabaseConfig();
            config.type = DatabaseType.byString(in.readUTF());
            config.host = in.readUTF();

            try {
               config.port = in.readInt();
            } catch (Exception ex) {
               System.out.println("\n\nPlease reconfigure your reflex database!\n\n");
               throw new RuntimeException(ex);
            }

            config.database = in.readUTF();
            config.user = in.readUTF();
            config.pw = in.readUTF();
            Misc.close(in, cipherIn, fileIn);
            return config;
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   public void save(File dir) {
      if (!dir.exists()) {
         dir.mkdirs();
      }

      File dataFile = new File(dir, "dbconf");

      try {
         FileOutputStream fileOut = new FileOutputStream(dataFile);
         CipherOutputStream cipherOut = new CipherOutputStream(fileOut, Encryptor.getCipher(1));
         DataOutputStream out = new DataOutputStream(cipherOut);
         out.writeUTF(this.type.toString());
         out.writeUTF(this.host);
         out.writeInt(this.port);
         out.writeUTF(this.database);
         out.writeUTF(this.user);
         out.writeUTF(this.pw);
         Misc.close(out, cipherOut, fileOut);
      } catch (Exception err) {
         throw new RuntimeException(err);
      }
   }

   public DatabaseConfig(DatabaseType type, int port, String host, String database, String user, String pw) {
      this.type = type;
      this.port = port;
      this.host = host;
      this.database = database;
      this.user = user;
      this.pw = pw;
   }

   public DatabaseConfig() {
   }

   public DatabaseType getType() {
      return this.type;
   }

   public int getPort() {
      return this.port;
   }

   public String getHost() {
      return this.host;
   }

   public String getDatabase() {
      return this.database;
   }

   public String getUser() {
      return this.user;
   }

   public String getPw() {
      return this.pw;
   }

   public void setType(DatabaseType type) {
      this.type = type;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public void setDatabase(String database) {
      this.database = database;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public void setPw(String pw) {
      this.pw = pw;
   }

   @Override
   public String toString() {
      return "DatabaseConfig(type="
         + this.getType()
         + ", port="
         + this.getPort()
         + ", host="
         + this.getHost()
         + ", database="
         + this.getDatabase()
         + ", user="
         + this.getUser()
         + ", pw="
         + this.getPw()
         + ")";
   }
}
