package me.darksidecode.kantanj.ipapi;

import com.google.gson.Gson;
import java.io.Serializable;
import me.darksidecode.kantanj.formatting.CommonJson;

public class IPAddress implements Serializable {
   private static final long serialVersionUID = 1423606597409892108L;
   private String status;
   private String message;
   private String country;
   private String countryCode;
   private String region;
   private String regionName;
   private String city;
   private String district;
   private String zip;
   private float lat;
   private float lon;
   private String timezone;
   private String isp;
   private String org;
   private String as;
   private String reverse;
   private boolean cellular;
   private boolean proxy;
   private String query;

   @Override
   public String toString() {
      return this.toString(CommonJson.getPrettyGsonNoHtmlEsc());
   }

   public String toString(Gson gson) {
      return gson.toJson(this);
   }

   public String getStatus() {
      return this.status;
   }

   public String getMessage() {
      return this.message;
   }

   public String getCountry() {
      return this.country;
   }

   public String getCountryCode() {
      return this.countryCode;
   }

   public String getRegion() {
      return this.region;
   }

   public String getRegionName() {
      return this.regionName;
   }

   public String getCity() {
      return this.city;
   }

   public String getDistrict() {
      return this.district;
   }

   public String getZip() {
      return this.zip;
   }

   public float getLat() {
      return this.lat;
   }

   public float getLon() {
      return this.lon;
   }

   public String getTimezone() {
      return this.timezone;
   }

   public String getIsp() {
      return this.isp;
   }

   public String getOrg() {
      return this.org;
   }

   public String getAs() {
      return this.as;
   }

   public String getReverse() {
      return this.reverse;
   }

   public boolean isCellular() {
      return this.cellular;
   }

   public boolean isProxy() {
      return this.proxy;
   }

   public String getQuery() {
      return this.query;
   }
}
