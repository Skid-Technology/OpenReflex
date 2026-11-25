package rip.reflex.api.db;

public interface DataImpl {
   void saveData(String name, byte[] data);

   boolean contains();

   byte[] getData();

   String getHash();
}
