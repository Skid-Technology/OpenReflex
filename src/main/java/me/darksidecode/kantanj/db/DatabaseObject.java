package me.darksidecode.kantanj.db;

import com.google.gson.internal.LinkedTreeMap;
import java.io.Serializable;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.formatting.Formatting;
import me.darksidecode.kantanj.types.Check;
import org.bson.Document;

public class DatabaseObject<T extends Serializable> implements Serializable, Cloneable {
   private static final long serialVersionUID = 6177749799489717775L;
   private static final String DB_OBJ_LINE = "\u0000";
   private String uniqueId;
   private T value;

   @Override
   public String toString() {
      String json = Formatting.unixLines(CommonJson.toJson(this));
      if (json.contains("\u0000")) {
         throw new UnknownError("json contains invalid null chars");
      } else {
         return json.replace("\n", "\u0000");
      }
   }

   public void setUniqueId(String uniqueId) {
      this.uniqueId = Check.notNull(uniqueId, "uniqueId cannot be null");
   }

   public void setValue(T value) {
      this.value = Check.notNull(value, "value cannot be null");
   }

   public DatabaseObject clone() {
      return new DatabaseObject(this.uniqueId, this.value);
   }

   public static <T extends Serializable> DatabaseObject<T> fromString(String dbJson) {
      if (dbJson.contains("\n")) {
         throw new IllegalArgumentException("json contains illegal non-db line chars");
      } else {
         DatabaseObject<T> obj = CommonJson.fromJson(dbJson.replace("\u0000", "\n"), DatabaseObject.class);
         if (obj.getValue() instanceof LinkedTreeMap && GlobalDatabasesOptions.isConvertLinkedTreeMapToBsonDocument()) {
            try {
               obj.setValue((T)(new Document((LinkedTreeMap)obj.getValue())));
            } catch (ClassCastException ex) {
               throw new RuntimeException(
                  "failed to convert a LinkedTreeMap to a BSON Document (unexpected T generic type); see me.darksidecode.kantanj.db.GlobalDatabasesOptions#convertLinkedTreeMapToBsonDocument",
                  ex
               );
            }
         }

         return obj;
      }
   }

   public DatabaseObject(String uniqueId, T value) {
      this.uniqueId = uniqueId;
      this.value = value;
   }

   public String getUniqueId() {
      return this.uniqueId;
   }

   public T getValue() {
      return this.value;
   }
}
