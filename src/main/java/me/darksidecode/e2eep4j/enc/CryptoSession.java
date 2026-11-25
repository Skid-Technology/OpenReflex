package me.darksidecode.e2eep4j.enc;

public interface CryptoSession {
   CryptoSession run();

   CryptoSession end();

   byte[] result();
}
