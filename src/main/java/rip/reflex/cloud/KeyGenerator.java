package rip.reflex.cloud;

import rip.reflex.util.misc.Strings;

public class KeyGenerator {
   private static final String CHARSET = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";

   public static void main(String[] args) {
      System.out.println(eGen() + "-" + eGen() + "-" + eGen());
   }

   private static String eGen() {
      return Strings.randomComp(10, "QWERTYUIOPASDFGHJKLZXCVBNM1234567890");
   }
}
