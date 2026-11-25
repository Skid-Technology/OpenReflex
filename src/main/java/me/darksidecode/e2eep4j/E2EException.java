package me.darksidecode.e2eep4j;

public class E2EException extends RuntimeException {
   public E2EException() {
   }

   public E2EException(String s) {
      super(s);
   }

   public E2EException(Exception e) {
      super(e);
   }

   public E2EException(String s, Exception e) {
      super(s, e);
   }
}
