package me.darksidecode.kantanj.db.file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import javax.crypto.BadPaddingException;
import me.darksidecode.kantanj.db.Database;
import me.darksidecode.kantanj.db.DatabaseAuthenticationException;
import me.darksidecode.kantanj.db.DatabaseConfiguration;
import me.darksidecode.kantanj.db.DatabaseObject;
import me.darksidecode.kantanj.system.FileUtils;
import me.darksidecode.kantanj.types.Check;

public class KantanjFileDatabase implements Database {
   public static final String FILE_HEADER = "![kantanj.sfdb]\n";
   private static final byte[] HEADER_BYTES = "![kantanj.sfdb]\n".getBytes(StandardCharsets.UTF_8);
   private final Object lock = new Object();
   private boolean prepared;
   private KantanjFileDatabaseConfiguration conf;
   private final Set<DatabaseObject> objects = new HashSet<>();
   private int initialHash;

   @Override
   public int prepare(DatabaseConfiguration dbConf) throws DatabaseAuthenticationException {
      Check.state(this.prepared, "already prepared");
      this.conf = Check.instanceOf(dbConf, KantanjFileDatabaseConfiguration.class, "illegal database configuration");
      if (this.conf.getSourceFile().exists()) {
         byte[] db;
         try {
            db = dbConf.getEncryptor() == null
               ? FileUtils.readGZIP(this.conf.getSourceFile())
               : FileUtils.readEncryptedGZIP(this.conf.getSourceFile(), this.conf.getEncryptor());
         } catch (Exception ex) {
            if (ex.getCause() instanceof RuntimeException && ex.getCause().getCause() instanceof BadPaddingException) {
               throw new DatabaseAuthenticationException("invalid credentials");
            }

            throw ex;
         }

         String dbStr = new String(db, StandardCharsets.UTF_8);
         if (!dbStr.startsWith("![kantanj.sfdb]\n")) {
            throw new IllegalArgumentException("not a kantanj SimpleFileDatabase file");
         } else {
            dbStr = dbStr.replace("![kantanj.sfdb]\n", "");
            String[] lines = dbStr.split("\n");
            synchronized (this.lock) {
               for (String line : lines) {
                  line = line.trim();
                  if (!line.isEmpty() && !line.startsWith("#")) {
                     DatabaseObject obj = DatabaseObject.fromString(line);
                     this.objects.add(obj);
                  }
               }

               this.prepared = true;
               this.initialHash = this.objects.hashCode();
               this.addShutdownHookIfEnabled();
               return this.objects.size();
            }
         }
      } else {
         if (this.conf.getEncryptor() == null) {
            FileUtils.writeGZIP(this.conf.getSourceFile(), HEADER_BYTES, FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);
         } else {
            FileUtils.writeEncryptedGZIP(this.conf.getSourceFile(), HEADER_BYTES, this.conf.getEncryptor(), FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);
         }

         this.prepared = true;
         this.initialHash = 0;
         this.addShutdownHookIfEnabled();
         return -1;
      }
   }

   private void addShutdownHookIfEnabled() {
      if (this.conf.isFlushOnExit()) {
         Runtime.getRuntime().addShutdownHook(new Thread(this::save, "KantanjFileDatabase Shutdown Hook | " + System.nanoTime()));
      }
   }

   @Override
   public <T extends Serializable> DatabaseObject<T> fetch(String uniqueId) {
      Check.notNull(uniqueId, "uniqueId cannot be null");
      return this.fetch(obj -> obj.getUniqueId().equals(uniqueId));
   }

   @Override
   public <T extends Serializable> DatabaseObject<T> fetch(Predicate<? super DatabaseObject> predicate) {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         return (DatabaseObject<T>)this.objects.stream().filter(Check.notNull(predicate, "predicate cannot be null")).findFirst().orElse(null);
      }
   }

   @Override
   public void insert(DatabaseObject obj) throws IllegalStateException {
      Check.state(!this.prepared, "database must be prepared first");
      Check.notNull(obj, "cannot insert null object into database");
      synchronized (this.lock) {
         Check.state(this.contains(obj.getUniqueId()), "object with uniqueId " + obj.getUniqueId() + " is already present in the database");
         this.objects.add(obj);
         if (this.conf.isFlushInstantly()) {
            this.save();
         }
      }
   }

   @Override
   public boolean remove(DatabaseObject obj) {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         boolean anythingRemoved = this.objects.remove(Check.notNull(obj, "obj cannot be null"));
         if (this.conf.isFlushInstantly()) {
            this.save();
         }

         return anythingRemoved;
      }
   }

   @Override
   public boolean remove(String uniqueId) {
      Check.notNull(uniqueId, "uniqueId cannot be null");
      return this.remove(obj -> obj.getUniqueId().equals(uniqueId));
   }

   @Override
   public boolean remove(Predicate<? super DatabaseObject> predicate) {
      synchronized (this.lock) {
         DatabaseObject obj = this.fetch(predicate);
         if (obj != null) {
            this.objects.remove(obj);
         }

         if (this.conf.isFlushInstantly()) {
            this.save();
         }

         return obj != null;
      }
   }

   @Override
   public void update(DatabaseObject oldObj, DatabaseObject newObj) {
      Check.state(!this.prepared, "database must be prepared first");
      Check.notNull(oldObj, "oldObj cannot be null");
      Check.notNull(newObj, "newObj cannot be null");
      synchronized (this.lock) {
         if (this.objects.contains(oldObj)) {
            this.objects.remove(oldObj);
            this.objects.add(newObj);
            if (this.conf.isFlushInstantly()) {
               this.save();
            }
         } else {
            throw new NoSuchElementException("no such object in the database");
         }
      }
   }

   @Override
   public void update(String uniqueId, DatabaseObject newObj) {
      Check.notNull(uniqueId, "uniqueId cannot be null");
      this.update(obj -> obj.getUniqueId().equals(uniqueId), newObj);
   }

   @Override
   public void update(Predicate<? super DatabaseObject> oldObjPredicate, DatabaseObject newObj) {
      synchronized (this.lock) {
         DatabaseObject oldObj = this.fetch(oldObjPredicate);
         if (oldObj != null) {
            this.objects.remove(oldObj);
            this.objects.add(newObj);
            if (this.conf.isFlushInstantly()) {
               this.save();
            }
         } else {
            throw new NoSuchElementException("no such object in the database");
         }
      }
   }

   @Override
   public boolean contains(DatabaseObject obj) {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         return this.objects.contains(Check.notNull(obj, "obj cannot be null"));
      }
   }

   @Override
   public boolean contains(String uniqueId) {
      Check.notNull(uniqueId, "uniqueId cannot be null");
      return this.contains(obj -> obj.getUniqueId().equals(uniqueId));
   }

   @Override
   public boolean contains(Predicate<? super DatabaseObject> predicate) {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         return this.objects.stream().anyMatch(Check.notNull(predicate, "predicate cannot be null"));
      }
   }

   @Override
   public <T extends Serializable> DatabaseObject<T>[] allObjects() {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         return this.objects.toArray(new DatabaseObject[0]);
      }
   }

   @Override
   public long size() {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         return (long)this.objects.size();
      }
   }

   @Override
   public void __dropConfirm00__() {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         this.objects.clear();
         if (this.conf.isFlushInstantly()) {
            this.save();
         }
      }
   }

   @Override
   public boolean save() {
      Check.state(!this.prepared, "database must be prepared first");
      synchronized (this.lock) {
         if (this.objects.hashCode() == this.initialHash) {
            return false;
         } else {
            try {
               StringBuilder dbStr = new StringBuilder();
               dbStr.append("![kantanj.sfdb]\n");

               for (DatabaseObject obj : this.objects) {
                  dbStr.append(obj.toString()).append('\n');
               }

               byte[] dbBytes = dbStr.toString().getBytes(StandardCharsets.UTF_8);
               File newFile = new File(this.conf.getSourceFile().getAbsolutePath() + ".tmp" + System.nanoTime() + '~');
               if (this.conf.getEncryptor() == null) {
                  FileUtils.writeGZIP(newFile, dbBytes, FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);
               } else {
                  FileUtils.writeEncryptedGZIP(newFile, dbBytes, this.conf.getEncryptor(), FileUtils.OverwriteMode.THROW_STATE_EXCEPTION);
               }

               Files.move(newFile.toPath(), this.conf.getSourceFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
               return true;
            } catch (IOException e) {
               throw new RuntimeException("failed to save the database to file", e);
            }
         }
      }
   }
}
