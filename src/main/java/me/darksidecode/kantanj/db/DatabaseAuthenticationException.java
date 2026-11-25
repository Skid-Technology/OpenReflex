package me.darksidecode.kantanj.db;

public class DatabaseAuthenticationException extends Exception {
   public DatabaseAuthenticationException(String message) {
      super(message);
   }

   public DatabaseAuthenticationException(Throwable cause) {
      super(cause);
   }

   public DatabaseAuthenticationException(String message, Throwable cause) {
      super(message, cause);
   }
}
