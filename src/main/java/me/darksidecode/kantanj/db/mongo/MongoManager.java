package me.darksidecode.kantanj.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.darksidecode.kantanj.types.Check;
import org.bson.Document;

public class MongoManager implements AutoCloseable {
   private static final int DEFAULT_MONGO_PORT = 27017;
   private MongoClient client;
   private MongoDatabase selected;

   public MongoManager noLogs() {
      Logger.getLogger("com.mongodb").setLevel(Level.OFF);
      Logger.getLogger("org.mongodb").setLevel(Level.OFF);
      return this;
   }

   public MongoManager connectLocal() {
      try {
         this.client = new MongoClient();
         return this;
      } catch (Exception ex) {
         throw new RuntimeException("failed to connect to default local database", ex);
      }
   }

   public MongoManager connectLocal(String username, String database, char[] password) {
      try {
         return this.connect("localhost", 27017, username, database, password);
      } catch (Exception e) {
         throw new RuntimeException("failed to connect to default local database", e);
      }
   }

   public MongoManager connect(String host) {
      if (host != null && !host.trim().isEmpty()) {
         try {
            this.client = new MongoClient(host);
            return this;
         } catch (Exception err) {
            throw new RuntimeException("failed to connect to remote database \"" + host + "\" (default port)", err);
         }
      } else {
         throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') host");
      }
   }

   public MongoManager connect(String host, int port) {
      if (host == null || host.trim().isEmpty()) {
         throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') host");
      } else if (port >= 1 && port <= 65535) {
         try {
            this.client = new MongoClient(host, port);
            return this;
         } catch (Exception ignored) {
            throw new RuntimeException(String.format("failed to connect to remote database \"%s\" at port %s", host, port));
         }
      } else {
         throw new IllegalArgumentException("invalid port " + port);
      }
   }

   public MongoManager connect(String host, int port, String username, String database, char[] password) {
      if (host == null || host.trim().isEmpty()) {
         throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') host");
      } else if (username == null || username.trim().isEmpty()) {
         throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') username");
      } else if (database == null || database.trim().isEmpty()) {
         throw new IllegalArgumentException("invalid (null, empty or 'spaces-only') database");
      } else if (password == null || password.length == 0) {
         throw new IllegalArgumentException("invalid (null or empty) password");
      } else if (port >= 1 && port <= 65535) {
         try {
            MongoCredential cred = MongoCredential.createCredential(username, database, password);

            for (int i = 0; i < password.length; i++) {
               password[i] = 0;
            }

            this.client = new MongoClient(new ServerAddress(host, port), Collections.singletonList(cred));
            return this;
         } catch (Exception ex) {
            throw new RuntimeException("failed to connect to default local database (with user&pass)", ex);
         }
      } else {
         throw new IllegalArgumentException("invalid port " + port);
      }
   }

   public MongoManager select(String database) {
      this.ensureConnected();
      this.selected = this.client.getDatabase(Check.notNull(database, "database name cannot be null"));
      return this;
   }

   public MongoDatabase getSelectedDatabase() {
      this.ensureSelected();
      return this.selected;
   }

   public boolean isDatabaseSelected() {
      this.ensureConnected();
      return this.selected != null;
   }

   public boolean isConnected() {
      return this.client != null;
   }

   private void ensureSelected() {
      this.ensureConnected();
      if (!this.isDatabaseSelected()) {
         throw new IllegalStateException("no database selected");
      }
   }

   private void ensureConnected() {
      if (!this.isConnected()) {
         throw new IllegalStateException("not connected");
      }
   }

   @Override
   public void close() {
      if (this.isConnected()) {
         try {
            this.client.close();
         } catch (Exception ignored) {
         }
      }
   }

   private <T> T checkStateAndReturn(T object) {
      this.ensureSelected();
      return object;
   }

   public MongoCollection<Document> getCollection(String collection) {
      return this.checkStateAndReturn(this.selected.getCollection(Check.notNull(collection, "collection name cannot be null")));
   }

   public FindIterable<Document> fetch(String collection, String key, Object val) {
      return this.getCollection(collection).find(new Document(Check.notNull(key, "key cannot be null"), val));
   }

   public Document fetchFirst(String collection, String key, Object val) {
      return (Document)this.fetch(collection, key, val).first();
   }

   public void insertOne(String collection, Document doc) {
      this.getCollection(collection).insertOne(Check.notNull(doc, "doc cannot be null"));
   }

   public void insertMany(String collection, List<Document> docs) {
      if (Check.notNull(docs, "docs list cannot be null").isEmpty()) {
         throw new IllegalArgumentException("nothing to insert");
      } else {
         MongoCollection<Document> col = this.getCollection(collection);
         if (docs.size() == 1) {
            col.insertOne(docs.get(0));
         } else {
            col.insertMany(docs);
         }
      }
   }

   public Document updateOne(String collection, String idKey, Object idVal, Document updatedData) {
      Check.notNull(idKey, "target entry ID key cannot be null");
      Check.notNull(updatedData, "updated data cannot be null");
      MongoCollection<Document> col = this.getCollection(collection);
      Document targetEntry = (Document)col.find(new Document(idKey, idVal)).first();
      Check.notNull(targetEntry, "no entries with \"%s\"=\"%s\" found", idKey, idVal);
      col.updateOne(targetEntry, new Document("$set", updatedData));
      return targetEntry;
   }

   public long deleteOne(String collection, String idKey, Object idVal) {
      Check.notNull(idKey, "target entry ID key cannot be null");
      MongoCollection<Document> col = this.getCollection(collection);
      return col.deleteOne(new Document(idKey, idVal)).getDeletedCount();
   }

   public long count(String collection) {
      return this.getCollection(collection).countDocuments();
   }
}
