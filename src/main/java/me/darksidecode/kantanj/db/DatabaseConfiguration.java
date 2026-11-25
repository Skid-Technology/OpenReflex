package me.darksidecode.kantanj.db;

import me.darksidecode.kantanj.crypto.Encryptor;

public interface DatabaseConfiguration {
   Encryptor getEncryptor();
}
