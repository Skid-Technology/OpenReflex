package rip.reflex.util.protocol.mcaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;

public class BoundingBox {
   private double minX;
   private double minY;
   private double minZ;
   private double maxX;
   private double maxY;
   private double maxZ;

   public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      this.minX = Math.min(minX, maxX);
      this.minY = Math.min(minY, maxY);
      this.minZ = Math.min(minZ, maxZ);
      this.maxX = Math.max(minX, maxX);
      this.maxY = Math.max(minY, maxY);
      this.maxZ = Math.max(minZ, maxZ);
   }

   public BoundingBox(Vector a, Vector b) {
      this(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
   }

   public static BoundingBox of(Entity e) {
      MethodProfiler mp = new MethodProfiler("CI-48217482749");
      Objects.requireNonNull(e, "84193481942");
      Object handle = MCAccess.invokeSneaky(ReflectionCache.getEntityHandle, e);
      Object bb = MCAccess.invokeSneaky(ReflectionCache.getBBMethod, handle);
      BoundingBox boundingBox = new BoundingBox(
         (Double)MCAccess.getFromField(ReflectionCache.boxField_a, bb),
         (Double)MCAccess.getFromField(ReflectionCache.boxField_b, bb),
         (Double)MCAccess.getFromField(ReflectionCache.boxField_c, bb),
         (Double)MCAccess.getFromField(ReflectionCache.boxField_d, bb),
         (Double)MCAccess.getFromField(ReflectionCache.boxField_e, bb),
         (Double)MCAccess.getFromField(ReflectionCache.boxField_f, bb)
      );
      mp.addToTimings();
      return boundingBox;
   }

   public static BoundingBox of(Block b) {
      Object handle = MCAccess.invokeSneaky(ReflectionCache.getNMSBlock, b);
      return new BoundingBox(
         (double)b.getX() + MCAccess.getFromField(ReflectionCache.blockMinX, handle, double.class),
         (double)b.getY() + MCAccess.getFromField(ReflectionCache.blockMinY, handle, double.class),
         (double)b.getZ() + MCAccess.getFromField(ReflectionCache.blockMinZ, handle, double.class),
         (double)b.getX() + MCAccess.getFromField(ReflectionCache.blockMaxX, handle, double.class),
         (double)b.getY() + MCAccess.getFromField(ReflectionCache.blockMaxY, handle, double.class),
         (double)b.getZ() + MCAccess.getFromField(ReflectionCache.blockMaxZ, handle, double.class)
      );
   }

   public static BoundingBox of(Object nmsBox) {
      return new BoundingBox(
         MCAccess.getFromField(ReflectionCache.boxField_a, nmsBox, double.class),
         MCAccess.getFromField(ReflectionCache.boxField_b, nmsBox, double.class),
         MCAccess.getFromField(ReflectionCache.boxField_c, nmsBox, double.class),
         MCAccess.getFromField(ReflectionCache.boxField_d, nmsBox, double.class),
         MCAccess.getFromField(ReflectionCache.boxField_e, nmsBox, double.class),
         MCAccess.getFromField(ReflectionCache.boxField_f, nmsBox, double.class)
      );
   }

   public static List<BoundingBox> getCubes(BoundingBox box, Entity entity) {
      Object nmsWorld = MCAccess.invokeSneaky(ReflectionCache.getWorldHandle, entity.getWorld());
      Object nmsBox = MCAccess.newInstance(
         ReflectionCache.boxConstructor, box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMinY(), box.getMaxZ()
      );
      List boxes = (List)MCAccess.invokeSneaky(
         ReflectionCache.getCubesMethod,
         ReflectionCache.nmsWorldClass.cast(nmsWorld),
         ReflectionCache.entityClass.cast(MCAccess.invokeSneaky(ReflectionCache.getEntityHandle, entity)),
         ReflectionCache.boundingBoxClass.cast(nmsBox)
      );
      List<BoundingBox> wrappedBoxes = new ArrayList<>();
      boxes.forEach(o -> wrappedBoxes.add(of(o)));
      return wrappedBoxes;
   }

   public Location min(World world) {
      return new Location(world, this.minX, this.minY, this.minZ).clone();
   }

   public Location mid(World world) {
      double[] mid = (double[])this.midpoint().clone();
      return new Location(world, mid[0], mid[1], mid[2]).clone();
   }

   public Location max(World world) {
      return new Location(world, this.maxX, this.maxY, this.maxZ).clone();
   }

   public BoundingBox expand(double m) {
      this.minX -= m;
      this.maxX += m;
      this.minY -= m;
      this.maxY += m;
      this.minZ -= m;
      this.maxZ += m;
      return this;
   }

   public BoundingBox expand(double mx, double my, double mz) {
      this.minX -= mx;
      this.maxX += mx;
      this.minY -= my;
      this.maxY += my;
      this.minZ -= mz;
      this.maxZ += mz;
      return this;
   }

   public BoundingBox expandUp(double m) {
      this.maxY += m;
      return this;
   }

   public BoundingBox expandDown(double m) {
      this.minY -= m;
      return this;
   }

   public boolean isWithinBoundingBox(int x, int y, int z) {
      return (double)x >= this.minX
         && (double)x <= this.maxX
         && (double)y >= this.minY
         && (double)y <= this.maxY
         && (double)z >= this.minZ
         && (double)z <= this.maxZ;
   }

   public boolean isWithinBoundingBox(double x, double y, double z) {
      return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
   }

   public boolean intersects(Location l, double precision) {
      return this.intersects(l.getX(), l.getZ(), precision);
   }

   public boolean intersects(double x, double z, double precision) {
      double dx = Maths.minabs(x - this.minX, x - this.maxX);
      double dz = Maths.minabs(z - this.minZ, z - this.maxZ);
      return dx <= precision || dz <= precision;
   }

   public double distance(Location l) {
      double[] mid = this.midpoint();
      return l.distance(new Location(l.getWorld(), mid[0], mid[1], mid[2]));
   }

   public boolean isWithinBoundingBox(Vector v) {
      return this.isWithinBoundingBox(v.getX(), v.getY(), v.getZ());
   }

   public boolean isWithinBoundingBox(Location l) {
      return this.isWithinBoundingBox(l.getX(), l.getY(), l.getZ());
   }

   public Block blockAtMid(World world) {
      double[] mid = this.midpoint();
      return world.getBlockAt(Maths.floor(mid[0]), Maths.floor(mid[1]), Maths.floor(mid[2]));
   }

   public double calculateYOffset(BoundingBox box, double y) {
      if (box.maxX <= this.minX || box.minX >= this.maxX || box.maxZ <= this.minZ || box.minZ >= this.maxZ) {
         return y;
      } else if (y > 0.0 && box.maxY <= this.minY) {
         return this.minY - box.maxY < y ? this.minY - box.maxY : y;
      } else if (y < 0.0 && box.minY >= this.maxY) {
         return this.maxY - box.minY > y ? this.maxY - box.minY : y;
      } else {
         return y;
      }
   }

   public BoundingBox addCoords(double x, double y, double z) {
      return new BoundingBox(
         this.minX + (x < 0.0 ? x : 0.0),
         this.minY + (y < 0.0 ? y : 0.0),
         this.minZ + (z < 0.0 ? z : 0.0),
         this.maxX + (x > 0.0 ? x : 0.0),
         this.maxY + (y > 0.0 ? y : 0.0),
         this.maxZ + (z > 0.0 ? z : 0.0)
      );
   }

   public double[] midpoint() {
      return new double[]{(this.minX + this.maxX) / 2.0, (this.minY + this.maxY) / 2.0, (this.minZ + this.maxZ) / 2.0};
   }

   public double getMinX() {
      return this.minX;
   }

   public double getMinY() {
      return this.minY;
   }

   public double getMinZ() {
      return this.minZ;
   }

   public double getMaxX() {
      return this.maxX;
   }

   public double getMaxY() {
      return this.maxY;
   }

   public double getMaxZ() {
      return this.maxZ;
   }

   @Override
   public String toString() {
      return "BoundingBox(minX="
         + this.getMinX()
         + ", minY="
         + this.getMinY()
         + ", minZ="
         + this.getMinZ()
         + ", maxX="
         + this.getMaxX()
         + ", maxY="
         + this.getMaxY()
         + ", maxZ="
         + this.getMaxZ()
         + ")";
   }
}
