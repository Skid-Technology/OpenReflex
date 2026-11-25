package me.darksidecode.kantanj.db.mongo;

import org.bson.Document;

public interface Cacheable {
   void fetch(MongoManager manager, Object key);

   Document create();

   Document push();
}
