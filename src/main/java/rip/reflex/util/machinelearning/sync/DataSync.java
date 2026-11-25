package rip.reflex.util.machinelearning.sync;

public interface DataSync {
   boolean needSync();

   void download();

   void loadLocal();

   int getVersion();

   String getLocalHash();
}
