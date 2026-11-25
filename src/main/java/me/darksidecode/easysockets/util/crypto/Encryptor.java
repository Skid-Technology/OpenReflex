package me.darksidecode.easysockets.util.crypto;

public interface Encryptor {
   byte[] encrypt(byte[] input);

   byte[] decrypt(byte[] input);
}
