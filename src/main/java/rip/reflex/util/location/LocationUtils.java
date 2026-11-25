package rip.reflex.util.location;

import com.comphenix.protocol.wrappers.BlockPosition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import rip.reflex.check.world.ScaffoldCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.math.Maths;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.compatibility.CompatibilityProvider;
import rip.reflex.util.time.Timer;

public class LocationUtils {
   public static boolean isGroundExact(Player p) {
      return checkNearbyChunks(p) && isGroundExact(p.getLocation().clone());
   }

   public static boolean isGroundExact(Location l) {
      return checkNearbyChunks(l) && isOnGround(l) && l.getY() % 0.0625 == 0.0;
   }

   public static boolean isOnGround(Player p) {
      return checkNearbyChunks(p) && isOnGround(p.getLocation().clone());
   }

   public static double getAbsoluteVelocityXZ(Player p) {
      return getAbsoluteVelocityXZ(p.getVelocity().clone());
   }

   public static double getAbsoluteVelocityXZ(Vector v) {
      return Math.abs(v.getX()) + Math.abs(v.getZ());
   }

   public static boolean isTowering(Player p, Stats stats) {
      if (stats.hasStat("recentlyPlaced") && !stats.getStat("recentlyPlaced", Storage.class).isEmpty()) {
         Object[] recentlyPlaced = stats.getStat("recentlyPlaced", Storage.class).asArray();
         int i = 0;
         int fx = 0;
         int fy = 0;
         int fz = 0;

         for (int j = 0; j < recentlyPlaced.length; j++) {
            Block b = (Block)recentlyPlaced[j];
            if (j == 0) {
               fx = b.getX();
               fy = b.getY();
               fz = b.getZ();
            } else if (b.getX() == fx && b.getY() > fy && b.getZ() == fz) {
               i++;
            }
         }

         return i >= 3;
      } else {
         return false;
      }
   }

   public static boolean isTowering(Storage recentPlaced) {
      if (recentPlaced.size() < 4) {
         return false;
      } else {
         BlockPosition first = (BlockPosition)recentPlaced.first();
         int startX = first.getX();
         int startZ = first.getZ();
         int lastY = -1;

         for (Object o : recentPlaced.values()) {
            BlockPosition pos = (BlockPosition)o;
            if (lastY != -1) {
               if (pos.getY() != lastY + 1 || pos.getX() != startX || pos.getZ() != startZ) {
                  return false;
               }

               lastY = pos.getY();
            } else {
               lastY = pos.getY();
            }
         }

         return true;
      }
   }

   public static ScaffoldCheck.BridgeState getBridgeState(Storage recentPlaced) {
      if (recentPlaced.size() >= 5 && Maths.objects[2] instanceof String) {
         BlockPosition prev = (BlockPosition)recentPlaced.first();
         int totalXChange = 0;
         int totalYChange = 0;
         int totalZChange = 0;
         int firstY = prev.getY();
         boolean hasYChange = false;

         for (int i = 1; i < 4; i++) {
            BlockPosition cur = (BlockPosition)recentPlaced.get(i);
            if (cur.getX() == prev.getX() == (cur.getZ() == prev.getZ())) {
               return ScaffoldCheck.BridgeState.NOT_BRIDGING;
            }

            if (cur.getY() != firstY) {
               hasYChange = true;
            }

            totalXChange += Math.abs(cur.getX() - prev.getX());
            totalYChange += cur.getY() == prev.getY() ? 0 : 1;
            totalZChange += Math.abs(cur.getZ() - prev.getZ());
            if (totalYChange > 1) {
               return ScaffoldCheck.BridgeState.NOT_BRIDGING;
            }

            prev = cur;
         }

         return Math.abs(totalXChange - totalZChange) > 1
            ? ScaffoldCheck.BridgeState.BRIDGING_HORIZONTALLY
            : (hasYChange ? ScaffoldCheck.BridgeState.NOT_BRIDGING : ScaffoldCheck.BridgeState.BRIDGING_DIAGONALLY);
      } else {
         return ScaffoldCheck.BridgeState.NOT_BRIDGING;
      }
   }

   public static boolean isBridging(Player p, Stats stats) {
      if (stats.hasStat("recentlyPlaced") && !stats.getStat("recentlyPlaced", Storage.class).isEmpty()) {
         Object[] recentlyPlaced = stats.getStat("recentlyPlaced", Storage.class).asArray();
         int i = 0;
         int fx = 0;
         int fy = 0;
         int fz = 0;
         int xOffset = 0;
         int yOffset = 0;
         int zOffset = 0;

         for (int j = 0; j < recentlyPlaced.length; j++) {
            Block b = (Block)recentlyPlaced[j];
            if (j == 0) {
               fx = b.getX();
               fy = b.getY();
               fz = b.getZ();
            } else {
               if (b.getX() == fx || b.getZ() == fz) {
                  i++;
               }

               if (b.getX() != fx) {
                  xOffset += b.getX() - fx;
               }

               if (b.getY() != fy) {
                  yOffset += b.getY() - fy;
               }

               if (b.getZ() != fz) {
                  zOffset += b.getZ() - fz;
               }
            }
         }

         return i >= 3 && (Math.abs(xOffset) < 2 || Math.abs(yOffset) < 2 || Math.abs(zOffset) < 2);
      } else {
         return false;
      }
   }

   public static Set<Block> getDirectNeighbors(Block b) {
      Location l = b.getLocation().clone();
      if (!checkNearbyChunks(l)) {
         return new HashSet<>();
      } else {
         Set neighbors = new HashSet();
         Block xp = l.clone().add(1.0, 0.0, 0.0).getBlock();
         Block xs = l.clone().add(-1.0, 0.0, 0.0).getBlock();
         Block zp = l.clone().add(0.0, 0.0, 1.0).getBlock();
         Block zs = l.clone().add(0.0, 0.0, -1.0).getBlock();
         if (xp.getType() != Material.AIR) {
            neighbors.add(xp);
         }

         if (xs.getType() != Material.AIR) {
            neighbors.add(xs);
         }

         if (zp.getType() != Material.AIR) {
            neighbors.add(zp);
         }

         if (zs.getType() != Material.AIR) {
            neighbors.add(zs);
         }

         return neighbors;
      }
   }

   public static boolean isOnGround(Location l) {
      if (!checkNearbyChunks(l)) {
         return false;
      } else {
         Location temp = l.clone();
         temp.setY((double)((int)temp.getY()));
         temp.setX(temp.getX() - 1.0);
         temp.setZ(temp.getZ() + 1.0);
         ArrayList<Block> blocks = playerStandsOn(temp, l);
         return blocks.size() != 0;
      }
   }

   public static boolean isCollided(Player player) {
      return !isPushable(player) || !isVerticallyPushable(player);
   }

   public static boolean isVerticallyPushable(Player p) {
      if (!checkNearbyChunks(p)) {
         return false;
      } else {
         Location l = p.getLocation().clone();
         List<Block> blocks = new ArrayList<>();

         for (int x = 0; x < 3; x++) {
            Block b2 = l.add((double)x, (double)x, (double)x).getBlock();
            Block b3 = l.add((double)(-x), (double)x, (double)x).getBlock();
            Block b4 = l.add((double)x, (double)(-x), (double)x).getBlock();
            Block b5 = l.add((double)x, (double)x, (double)(-x)).getBlock();
            Block b6 = l.add((double)(-x), (double)x, (double)(-x)).getBlock();
            Block b7 = l.add((double)(-x), (double)(-x), (double)(-x)).getBlock();
            blocks.add(b2);
            blocks.add(b3);
            blocks.add(b4);
            blocks.add(b5);
            blocks.add(b6);
            blocks.add(b7);
         }

         for (Block b : blocks) {
            if (b.getType().toString().toLowerCase().contains("web")
               || b.getType().equals(Material.LADDER)
               || b.getType().equals(Material.VINE)
               || b.isLiquid()) {
               return false;
            }
         }

         return l.add(0.0, 2.0, 0.0).getBlock().getType().equals(Material.AIR) && !isInLiquid(p) && isOnGround(p);
      }
   }

   public static boolean isBelowBlocks(Player p) {
      return checkNearbyChunks(p) && isBelowBlocks(p.getLocation().clone());
   }

   public static boolean isBelowBlocks(Location l) {
      if (!checkNearbyChunks(l)) {
         return false;
      } else {
         Location x_ = new Location(l.getWorld(), (double)(l.getBlockX() + 1), (double)l.getBlockY(), (double)l.getBlockZ());
         Location _x = new Location(l.getWorld(), (double)(l.getBlockX() - 1), (double)l.getBlockY(), (double)l.getBlockZ());
         Location z_ = new Location(l.getWorld(), (double)l.getBlockX(), (double)l.getBlockY(), (double)(l.getBlockZ() + 1));
         Location _z = new Location(l.getWorld(), (double)l.getBlockX(), (double)l.getBlockY(), (double)(l.getBlockZ() - 1));
         return !l.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR)
            || !x_.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR)
            || !_x.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR)
            || !z_.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR)
            || !_z.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().equals(Material.AIR);
      }
   }

   public static boolean blocksAroundIsType(Block block, Material material) {
      if (!checkNearbyChunks(block.getLocation())) {
         return false;
      } else {
         for (Block b : blocksAround(block)) {
            if (b.getType() == material) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean blocksAroundIsSolidMinABU(Block block, int min, List<Material> list) {
      if (!checkNearbyChunks(block.getLocation())) {
         return false;
      } else {
         int i = 0;

         for (Block b : blocksAround(block)) {
            if (list.contains(b.getType())) {
               i++;
            }
         }

         return i >= min;
      }
   }

   public static Block findBlock(List<Block> blocks, Material type) {
      return blocks.stream().filter(b -> b.getType() == type).findFirst().orElse(null);
   }

   public static boolean containsBlock(List<Block> blocks, Material type) {
      return findBlock(blocks, type) != null;
   }

   public static boolean isStairInverted(Block block) {
      if (!block.getType().name().contains("STAIRS")) {
         throw new IllegalArgumentException("not a stairs block: " + block.getType());
      } else {
         CompatibilityProvider cp = VersionUtils.getCompatibilityProvider();
         return cp.getBisectedBlockHalf(block) == 0;
      }
   }

   public static ArrayList<Block> playerStandsOn(Location location, Location orig) {
      return checkNearbyChunks(location) && checkNearbyChunks(orig) ? playerStandsOn(location, new ArrayList<>(), orig) : new ArrayList<>();
   }

   public static ArrayList<Block> playerStandsOn(Location location, ArrayList<Material> dontremove, Location orig) {
      if (!checkNearbyChunks(location)) {
         return new ArrayList<>();
      } else {
         Block under = orig.getBlock().getRelative(BlockFace.DOWN);
         ArrayList<Block> allaround = blocksAround(under, true);
         ArrayList<Block> realarround = new ArrayList<>();

         for (Block block : allaround) {
            Location l = new Location(
               block.getWorld(),
               mid((double)block.getX()),
               max((double)block.getY()),
               mid((double)(isPositive((double)block.getZ()) ? block.getZ() - 1 : block.getZ() + 1))
            );
            if (Maths.distance(l, location) < 1.35) {
               realarround.add(block);
            }
         }

         allaround.clear();
         if (under.getType().isSolid()) {
            realarround.add(under);
         }

         for (Block blockx : (ArrayList<Block>)realarround.clone()) {
            Block up1 = blockx.getRelative(BlockFace.UP);
            if ((up1.getType().isSolid() || up1.getRelative(BlockFace.UP).getType().isSolid() || !blockx.getType().isSolid() || blockx.isLiquid())
               && !dontremove.contains(blockx.getType())) {
               realarround.remove(blockx);
            }
         }

         return realarround;
      }
   }

   public static double mid(double d) {
      return Double.parseDouble(("" + d).replaceAll("\\..{0,99}", ".5"));
   }

   private static double max(double d) {
      return Double.parseDouble(("" + d).replaceAll("\\..{0,99}", ".99"));
   }

   private static boolean isPositive(double d) {
      return !("" + d).startsWith("-");
   }

   public static ArrayList<Block> blocksAround(Block block) {
      return !checkNearbyChunks(block.getLocation()) ? new ArrayList<>() : blocksAround(block, true);
   }

   public static List<Block> blocksAround3x3x3(Block block) {
      if (!checkNearbyChunks(block.getLocation())) {
         return new ArrayList<>();
      } else {
         List<Block> blocks = new ArrayList<>();
         blocks.addAll(blocksAround(block));
         blocks.addAll(blocksAround(block.getRelative(BlockFace.UP)));
         blocks.addAll(blocksAround(block.getRelative(BlockFace.DOWN)));
         return blocks;
      }
   }

   public static ArrayList<Block> blocksAround(Block block, boolean includeSelf) {
      if (!checkNearbyChunks(block.getLocation())) {
         return new ArrayList<>();
      } else {
         ArrayList<Block> list = new ArrayList<>();
         if (includeSelf) {
            list.add(block);
         }

         list.add(block.getRelative(BlockFace.EAST));
         list.add(block.getRelative(BlockFace.SOUTH));
         list.add(block.getRelative(BlockFace.WEST));
         list.add(block.getRelative(BlockFace.NORTH));
         list.add(block.getRelative(BlockFace.NORTH_EAST));
         list.add(block.getRelative(BlockFace.SOUTH_EAST));
         list.add(block.getRelative(BlockFace.SOUTH_WEST));
         list.add(block.getRelative(BlockFace.NORTH_WEST));
         return list;
      }
   }

   public static boolean isOnIce(Player player) {
      if (checkNearbyChunks(player) && isOnGround(player)) {
         for (Block block : playerStandsOn(player.getLocation(), player.getLocation().clone())) {
            if (block.getType().toString().toLowerCase().contains("ice")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isPushable(Player player) {
      return checkNearbyChunks(player) && isPushable(player.getLocation().clone());
   }

   public static boolean isPushable(LivingEntity entity) {
      return checkNearbyChunks(entity.getLocation()) && isPushable(entity.getLocation().clone());
   }

   public static boolean isNearWalls(Player p) {
      for (Block block : getBlocksSurroundedBy(p)) {
         if (!isBlockPhasable(block, false)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isBlockPhasable(Block b, boolean includeLiquids) {
      if (b.getType() != Material.AIR && (!includeLiquids || !b.isLiquid())) {
         String t = b.getType().name().toLowerCase();
         return t.contains("flower") || t.contains("plant") || t.contains("tall") || t.contains("long_grass") || t.contains("rose") || t.contains("bush");
      } else {
         return true;
      }
   }

   public static List<Block> getBlocksSurroundedBy(Player p) {
      Location l = p.getEyeLocation().clone();
      if (!checkNearbyChunks(l)) {
         return new ArrayList<>();
      } else {
         List<Block> directlyAround = new ArrayList<>();
         Location below = l.clone();
         below.setY((double)Maths.floor(l.getY()) - 1.0);
         directlyAround.addAll(getDirectNeighbors(l.getBlock()));
         directlyAround.addAll(getDirectNeighbors(below.getBlock()));
         return directlyAround;
      }
   }

   private List<Block> getDirectNeighbours(Block b) {
      Location l = b.getLocation();
      List<Block> directNeighbours = new ArrayList<>();
      directNeighbours.add(l.getBlock().getRelative(BlockFace.NORTH));
      directNeighbours.add(l.getBlock().getRelative(BlockFace.EAST));
      directNeighbours.add(l.getBlock().getRelative(BlockFace.SOUTH));
      directNeighbours.add(l.getBlock().getRelative(BlockFace.WEST));
      return directNeighbours;
   }

   public static boolean isPushable(Location l) {
      if (!checkNearbyChunks(l)) {
         return false;
      } else {
         ArrayList<Block> blocksAround = new ArrayList<>();
         blocksAround.add(l.getBlock().getRelative(BlockFace.EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.EAST_NORTH_EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.EAST_SOUTH_EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.WEST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.WEST_NORTH_WEST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.WEST_SOUTH_WEST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.SOUTH));
         blocksAround.add(l.getBlock().getRelative(BlockFace.SOUTH_EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.SOUTH_SOUTH_EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.SOUTH_SOUTH_WEST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.NORTH));
         blocksAround.add(l.getBlock().getRelative(BlockFace.NORTH_EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.NORTH_NORTH_EAST));
         blocksAround.add(l.getBlock().getRelative(BlockFace.NORTH_NORTH_WEST));
         Location loc1 = new Location(l.getWorld(), (double)(l.getBlockX() + 1), (double)l.getBlockY(), (double)(l.getBlockZ() + 1));
         Location loc2 = new Location(l.getWorld(), (double)(l.getBlockX() + 1), (double)l.getBlockY(), (double)(l.getBlockZ() - 1));
         Location loc3 = new Location(l.getWorld(), (double)(l.getBlockX() - 1), (double)l.getBlockY(), (double)(l.getBlockZ() + 1));
         Location loc4 = new Location(l.getWorld(), (double)(l.getBlockX() - 1), (double)l.getBlockY(), (double)(l.getBlockZ() - 1));
         blocksAround.add(loc1.getBlock());
         blocksAround.add(loc2.getBlock());
         blocksAround.add(loc3.getBlock());
         blocksAround.add(loc4.getBlock());

         for (Block block : blocksAround) {
            if ((block.getType().isSolid() || block.getType().toString().toLowerCase().contains("web")) && !isBlockPhasable(block, true)) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isClimbing(Player player) {
      return checkNearbyChunks(player)
         && (player.getLocation().getBlock().getType().equals(Material.LADDER) || player.getLocation().getBlock().getType().equals(Material.VINE));
   }

   public static boolean isOnWater(Player player) {
      return checkNearbyChunks(player) && (player.getLocation().getBlock().isLiquid() || player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid());
   }

   public static Movement getMovement(Player p, Location from, Location to) {
      Vector dir = from.getDirection().clone();
      Location lLeft = from.clone();
      Location lRight = from.clone();
      lLeft.setYaw(lLeft.getYaw() - 90.0F);
      lRight.setYaw(lRight.getYaw() + 90.0F);
      Vector left = lLeft.clone().getDirection().clone();
      Vector right = lRight.clone().getDirection().clone();
      Vector forward = new Vector(from.getX() + dir.getX(), from.getY() + dir.getY(), from.getZ() + dir.getZ()).clone();
      Vector backward = new Vector(from.getX() - dir.getX(), from.getY() - dir.getY(), from.getZ() - dir.getZ()).clone();
      Vector strafeLeft = new Vector(from.getX() + left.getX(), from.getY() + left.getY(), from.getZ() + left.getZ()).clone();
      Vector strafeRight = new Vector(from.getX() + right.getX(), from.getY() + right.getY(), from.getZ() + right.getZ()).clone();
      Vector vTo = to.toVector().clone();
      double dForward = Maths.horizontalDistance(vTo, forward);
      double dBackward = Maths.horizontalDistance(vTo, backward);
      double dStrafing = Math.min(Maths.horizontalDistance(vTo, strafeLeft), Maths.horizontalDistance(vTo, strafeRight));
      double closest = Maths.min(dForward, dBackward, dStrafing);
      if (closest == dBackward) {
         return new Movement(MoveDirection.BACKWARD, dForward, dBackward, dStrafing);
      } else {
         return closest == dStrafing
               && !Maths.matchIn(dForward, 0.75, 0.85)
               && !Maths.matchIn(dForward, 1.001, 1.1999)
               && dForward >= 0.7
               && !Maths.matchEx(dForward, 0.925, 0.955)
            ? new Movement(MoveDirection.STRAFING, dForward, dBackward, dStrafing)
            : new Movement(MoveDirection.FORWARD, dForward, dBackward, dStrafing);
      }
   }

   public static boolean isKnockingBack(Player p, Stats stats) {
      Vector velocity = p.getVelocity().clone();
      return !stats.getStat("damageByEntityTracker", Timer.class).hasPassed(1000L) && !isZero(velocity) && !isNaturalGravity(velocity);
   }

   public static boolean isVerticalOnly(Vector v) {
      return v.getX() == 0.0 && v.getZ() == 0.0;
   }

   public static boolean isNaturalGravity(Vector v) {
      return Maths.matchIn(v.getY(), -0.07840000152589, -0.07840000152586);
   }

   public static boolean isZero(Vector v) {
      return v.getX() == 0.0 && v.getY() == 0.0 && v.getZ() == 0.0;
   }

   public static boolean isInLiquid(Player p) {
      if (!checkNearbyChunks(p)) {
         return false;
      } else {
         Location l = p.getLocation().clone();
         return isOnWater(p)
            || l.getBlock().isLiquid()
               && l.add(0.0, -1.0, 0.0).getBlock().isLiquid()
               && l.add(1.0, 0.0, 0.0).getBlock().isLiquid()
               && l.add(0.0, 0.0, 1.0).getBlock().isLiquid()
               && l.add(1.0, 0.0, 1.0).getBlock().isLiquid()
               && l.add(-1.0, 0.0, -1.0).getBlock().isLiquid()
               && l.add(0.0, 0.0, -1.0).getBlock().isLiquid()
               && l.add(-1.0, 0.0, 0.0).getBlock().isLiquid()
               && l.add(1.0, -1.0, 0.0).getBlock().isLiquid()
               && l.add(0.0, -1.0, 1.0).getBlock().isLiquid()
               && l.add(1.0, -1.0, 1.0).getBlock().isLiquid()
               && l.add(-1.0, -1.0, -1.0).getBlock().isLiquid()
               && l.add(0.0, -1.0, -1.0).getBlock().isLiquid()
               && l.add(-1.0, -1.0, 0.0).getBlock().isLiquid()
               && !isGroundExact(p);
      }
   }

   public static MoveType getMoveType(Location from, Location to) {
      double dy = to.getY() - from.getY();
      if (checkNearbyChunks(from) && checkNearbyChunks(to) && hasHorizontalMove(from, to)) {
         if (isGroundExact(from) && isGroundExact(to) || isOnUnstableBlock(from) || isOnUnstableBlock(to)) {
            return MoveType.GROUND;
         }

         if (isGroundExact(from) && !isGroundExact(to) && dy > 0.0) {
            return MoveType.JUMP;
         }

         if (isGroundExact(from) && !isGroundExact(to) && dy < 0.0 || !isGroundExact(from) && isGroundExact(to)) {
            return MoveType.FALL;
         }

         if (!isGroundExact(from) && !isGroundExact(to)) {
            return MoveType.MIDAIR;
         }
      } else if (checkNearbyChunks(from) && checkNearbyChunks(to)) {
         return MoveType.ROTATION;
      }

      return MoveType.UNKNOWN;
   }

   public static boolean isBackwardsMove(PlayerMoveEvent e) {
      return e != null && isBackwardsMove(e.getFrom(), e.getTo());
   }

   public static boolean isBackwardsMove(Location from, Location to) {
      double xDistance = to.getX() - from.getX();
      double zDistance = to.getX() - from.getX();
      double yaw = (double)from.getYaw();
      return xDistance < 0.0 && zDistance > 0.0 && yaw > 180.0 && yaw < 270.0
         || xDistance < 0.0 && zDistance < 0.0 && yaw > 270.0 && yaw < 360.0
         || xDistance > 0.0 && zDistance < 0.0 && yaw > 0.0 && yaw < 90.0
         || xDistance > 0.0 && zDistance > 0.0 && yaw > 90.0 && yaw < 180.0;
   }

   public static boolean hasHorizontalMove(Location from, Location to) {
      double dx = to.getX() - from.getX();
      double dz = to.getZ() - from.getZ();
      return dx != 0.0 || dz != 0.0;
   }

   public static JumpStage getJumpStage(Location from, Location to) {
      MoveType move = getMoveType(from, to);
      double dY = to.getY() - from.getY();
      if (Maths.matchIn(dY, 0.41, 0.42)) {
         return JumpStage.ACCELERATING_UP;
      } else if (!move.equals(MoveType.MIDAIR) && !move.equals(MoveType.FALL) && !move.equals(MoveType.JUMP)) {
         return JumpStage.NOT_JUMPING;
      } else if (Maths.matchIn(Math.abs(to.getY() - from.getY()), 0, 0.052947322753916)) {
         return JumpStage.FLOATING;
      } else {
         return from.getY() > to.getY() ? JumpStage.FALLING : JumpStage.ACCELERATING_UP;
      }
   }

   public static boolean isOnUnstableBlock(Player p) {
      return isOnUnstableBlock(p.getLocation().clone());
   }

   public static boolean isOnUnstableBlock(Location l) {
      if (!checkNearbyChunks(l)) {
         return false;
      } else {
         for (Block b : blocksAround3x3x3(l.getBlock())) {
            String t = b.getType().toString().toLowerCase();
            if (!b.getType().isSolid() && !b.getType().equals(Material.AIR) || t.contains("step") || t.contains("stair") || t.contains("web")) {
               return true;
            }
         }

         String t = l.getBlock().getType().toString().toLowerCase();
         String tDown = l.getBlock().getRelative(BlockFace.DOWN).getType().toString().toLowerCase();
         return tDown.contains("stair") || tDown.contains("web") || t.contains("step") || t.contains("web");
      }
   }

   public static boolean isNormalBlock(Block b) {
      return checkNearbyChunks(b.getLocation()) && isNormalBlock(b.getType());
   }

   public static boolean isNormalBlock(Material m) {
      String type = m.toString().toLowerCase();
      return m.isSolid()
         && !type.contains("step")
         && !type.contains("stair")
         && !type.contains("fence")
         && !type.contains("pane")
         && !type.contains("slab")
         && !type.contains("carpet")
         && !type.contains("door")
         && !type.contains("ladder")
         && !type.contains("vine")
         && !type.contains("torch")
         && !type.contains("anvil")
         && !type.contains("chest")
         && !type.contains("sign")
         && !type.contains("frame")
         && !type.contains("skull")
         && !type.contains("door")
         && !type.contains("comparator")
         && !type.contains("repeat")
         && !type.contains("sapling")
         && !type.contains("dead")
         && !type.contains("bed")
         && !type.contains("lever")
         && !type.contains("button")
         && !type.contains("trap")
         && !type.contains("enchant")
         && !type.contains("piston")
         && !type.contains("dispenser")
         && !type.contains("note")
         && !type.contains("detector")
         && !type.contains("dropper")
         && !type.contains("hopper")
         && !type.contains("beacon")
         && !type.contains("juke")
         && !type.contains("craft")
         && !type.contains("furnace")
         && !type.contains("redstone")
         && !type.contains("cauldron")
         && !type.contains("pot")
         && !type.contains("cake")
         && !type.contains("diode")
         && !type.contains("brewing");
   }

   public static boolean isNearbyBlock(Player p, Material type, int range) {
      Location l = getBlockLocation(p);

      for (int i = 0; i <= range; i++) {
         Block[] blocks = new Block[]{
            l.clone().add((double)i, (double)i, (double)i).getBlock(),
            l.clone().add((double)(-i), (double)i, (double)i).getBlock(),
            l.clone().add((double)i, (double)i, (double)(-i)).getBlock(),
            l.clone().add((double)(-i), (double)i, (double)(-i)).getBlock(),
            l.clone().add((double)i, (double)(-i), (double)i).getBlock(),
            l.clone().add((double)(-i), (double)(-i), (double)i).getBlock(),
            l.clone().add((double)i, (double)(-i), (double)(-i)).getBlock(),
            l.clone().add((double)(-i), (double)(-i), (double)(-i)).getBlock()
         };

         for (Block b : blocks) {
            if (b.getType() == type) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isNearbyBlocks(Player p, int range, Material... types) {
      Location l = getBlockLocation(p);

      for (int i = 0; i <= range; i++) {
         Block[] blocks = new Block[]{
            l.clone().add((double)i, (double)i, (double)i).getBlock(),
            l.clone().add((double)(-i), (double)i, (double)i).getBlock(),
            l.clone().add((double)i, (double)i, (double)(-i)).getBlock(),
            l.clone().add((double)(-i), (double)i, (double)(-i)).getBlock(),
            l.clone().add((double)i, (double)(-i), (double)i).getBlock(),
            l.clone().add((double)(-i), (double)(-i), (double)i).getBlock(),
            l.clone().add((double)i, (double)(-i), (double)(-i)).getBlock(),
            l.clone().add((double)(-i), (double)(-i), (double)(-i)).getBlock()
         };

         for (Block b : blocks) {
            for (Material type : types) {
               if (b.getType() == type) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static Location getBlockLocation(Player p) {
      Location l = p.getLocation().clone();
      return new Location(l.getWorld(), (double)l.getBlockX(), (double)l.getBlockY(), (double)l.getBlockZ());
   }

   public static boolean checkNearbyChunks(Player p) {
      return checkNearbyChunks(p.getLocation().clone());
   }

   public static boolean checkNearbyChunks(Entity e) {
      return checkNearbyChunks(e.getLocation().clone());
   }

   public static boolean checkNearbyChunks(Location l) {
      if (l == null) {
         return false;
      } else {
         Location location = l.clone();
         World world = location.getWorld();
         if (world == null) {
            return false;
         } else {
            int x = location.getBlockX() >> 4;
            int z = location.getBlockZ() >> 4;
            return world.isChunkLoaded(x, z)
               && world.isChunkLoaded(x + 1, z + 1)
               && world.isChunkLoaded(x - 1, z + 1)
               && world.isChunkLoaded(x + 1, z - 1)
               && world.isChunkLoaded(x - 1, z - 1);
         }
      }
   }
}
