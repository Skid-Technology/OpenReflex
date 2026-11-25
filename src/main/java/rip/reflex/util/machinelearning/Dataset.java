package rip.reflex.util.machinelearning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.serialization.ManualSerializable;

public class Dataset implements ManualSerializable {
   private final HashMap<String, ArrayList<Double>> data;
   private final String name;
   private boolean legit;

   public Dataset(String name, HashMap<String, ArrayList<Double>> data, boolean legit) {
      this.name = name;
      this.data = data;
      this.legit = legit;
   }

   public static ArrayList<Double> dList(Double... d) {
      ArrayList<Double> dl = new ArrayList<>();
      Collections.addAll(dl, d);
      return dl;
   }

   public static Dataset deserialize(List<? extends Serializable> list) {
      return new Dataset((String)list.remove(0), (HashMap<String, ArrayList<Double>>)list.remove(0), (Boolean)list.remove(0));
   }

   public static Dataset valueOf(String data) {
      String name = data.split("\n")[0];
      String legit = data.split("\n")[1];
      HashMap<String, ArrayList<Double>> values = new HashMap<>();
      String valuesStr = data.split(legit + "\n")[1];

      for (int i = 0; i < valuesStr.split("\n").length; i++) {
         values.put(new KeyInfo().getKey(i), parseValues(valuesStr.split("\n")[i]));
      }

      return new Dataset(name, values, Boolean.valueOf(legit));
   }

   private static ArrayList<Double> parseValues(String string) {
      return Arrays.stream(string.split(", ")).map(Double::valueOf).collect(Collectors.toCollection(ArrayList::new));
   }

   @Override
   public List<Serializable> serialize() {
      ArrayList<Serializable> list = new ArrayList<>();
      list.add(this.name);
      list.add((Serializable)Misc.<String, ArrayList<Double>>copyMap(this.data, false));
      list.add(this.legit);
      return list;
   }

   public Dataset setLegit(boolean legit) {
      this.legit = legit;
      return this;
   }

   public int compareValue() {
      Integer i = Integer.valueOf(this.name.substring(1));
      return i + (this.name.startsWith("P") ? 0 : 100000);
   }

   public String getName() {
      return this.name;
   }

   public boolean isLegit() {
      return this.legit;
   }

   public HashMap<String, ArrayList<Double>> getData() {
      return this.data;
   }

   public Dataset clone() {
      return new Dataset(this.name, (HashMap<String, ArrayList<Double>>)Misc.<String, ArrayList<Double>>copyMap(this.data, false), this.legit);
   }
}
