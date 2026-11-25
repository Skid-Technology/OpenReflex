package me.darksidecode.kantanj.crypto;

public interface Encryptor {
   byte[] encrypt(byte[] input);

   byte[] decrypt(byte[] input);
}
