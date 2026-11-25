package me.darksidecode.kantanj.db;

import java.io.Serializable;
import java.util.function.Predicate;

public interface Database {
   int prepare(DatabaseConfiguration config) throws DatabaseAuthenticationException;

   <T extends Serializable> DatabaseObject<T> fetch(String key);

   <T extends Serializable> DatabaseObject<T> fetch(Predicate<? super DatabaseObject> predicate);

   void insert(DatabaseObject object) throws IllegalStateException;

   boolean remove(DatabaseObject object);

   boolean remove(String key);

   boolean remove(Predicate<? super DatabaseObject> predicate);

   void update(DatabaseObject target, DatabaseObject replacement);

   void update(String key, DatabaseObject replacement);

   void update(Predicate<? super DatabaseObject> predicate, DatabaseObject replacement);

   boolean contains(DatabaseObject object);

   boolean contains(String key);

   boolean contains(Predicate<? super DatabaseObject> predicate);

   <T extends Serializable> DatabaseObject<T>[] allObjects();

   long size();

   default void drop() throws DropConfirmationException {
      throw new DropConfirmationException(this);
   }

   void __dropConfirm00__();

   boolean save();
}
