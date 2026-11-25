package rip.reflex.listener.base;

import com.comphenix.protocol.events.PacketEvent;
import rip.reflex.Reflex;

public abstract class PacketListener {
   protected final Reflex reflex;

   public abstract void onPacketReceiving(PacketEvent event);

   public abstract void onPacketSending(PacketEvent event);

   public PacketListener(Reflex reflex) {
      this.reflex = reflex;
   }
}
