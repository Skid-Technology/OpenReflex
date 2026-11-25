package me.darksidecode.kantanj.db.file;

import java.io.File;
import java.nio.file.Path;
import me.darksidecode.kantanj.crypto.AES256Encryptor;
import me.darksidecode.kantanj.crypto.Encryptor;
import me.darksidecode.kantanj.db.DatabaseConfiguration;
import me.darksidecode.kantanj.types.Check;

public class KantanjFileDatabaseConfiguration implements DatabaseConfiguration {
   private File sourceFile;
   private boolean flushInstantly;
   private boolean flushOnExit;
   private Encryptor encryptor;

   public static KantanjFileDatabaseConfiguration.Builder builder() {
      return new KantanjFileDatabaseConfiguration.Builder();
   }

   public File getSourceFile() {
      return this.sourceFile;
   }

   public boolean isFlushInstantly() {
      return this.flushInstantly;
   }

   public boolean isFlushOnExit() {
      return this.flushOnExit;
   }

   @Override
   public Encryptor getEncryptor() {
      return this.encryptor;
   }

   public static final class Builder {
      private static final byte[] ENC_SALT = new byte[]{-128, 62, 60, 10, -11, -101, -70, -112};
      private final KantanjFileDatabaseConfiguration conf = new KantanjFileDatabaseConfiguration();
      private int aesIterations = 65536;

      Builder() {
      }

      public KantanjFileDatabaseConfiguration build() {
         return this.conf;
      }

      public KantanjFileDatabaseConfiguration.Builder file(String path) {
         return this.file(new File(path));
      }

      public KantanjFileDatabaseConfiguration.Builder file(Path path) {
         return this.file(path.toFile());
      }

      public KantanjFileDatabaseConfiguration.Builder file(File sourceFile) {
         this.conf.sourceFile = Check.notNull(sourceFile, "sourceFile cannot be null");
         return this;
      }

      public KantanjFileDatabaseConfiguration.Builder flushInstantly(boolean flushInstantly) {
         this.conf.flushInstantly = flushInstantly;
         return this;
      }

      public KantanjFileDatabaseConfiguration.Builder flushOnExit(boolean flushOnExit) {
         this.conf.flushOnExit = flushOnExit;
         return this;
      }

      public KantanjFileDatabaseConfiguration.Builder aesIterations(int aesIterations) {
         if (aesIterations < 1) {
            throw new IllegalArgumentException("must iterate at least once");
         } else {
            Check.state(this.conf.encryptor != null, "encryptor already set (aesIterations must be set before password)");
            this.aesIterations = aesIterations;
            return this;
         }
      }

      public KantanjFileDatabaseConfiguration.Builder password(char[] password) {
         this.conf.encryptor = new AES256Encryptor(password, ENC_SALT, this.aesIterations);
         return this;
      }

      public KantanjFileDatabaseConfiguration.Builder encryptor(Encryptor encryptor) {
         this.conf.encryptor = encryptor;
         return this;
      }
   }
}
