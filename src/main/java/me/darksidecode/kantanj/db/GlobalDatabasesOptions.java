package me.darksidecode.kantanj.db;

public class GlobalDatabasesOptions {
   private static volatile boolean convertLinkedTreeMapToBsonDocument = true;

   public static boolean isConvertLinkedTreeMapToBsonDocument() {
      return convertLinkedTreeMapToBsonDocument;
   }

   public static void setConvertLinkedTreeMapToBsonDocument(boolean convertLinkedTreeMapToBsonDocument) {
      GlobalDatabasesOptions.convertLinkedTreeMapToBsonDocument = convertLinkedTreeMapToBsonDocument;
   }
}
