package io.github.jbaero.skcompat;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREInvalidWorldException;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * LazyAbstraction, 2/24/2016 1:40 AM
 *
 * @author jb_aero
 */
public class LazyAbstraction {

	ProtectedRegion lastRegion;
	World lastWorld;

	public Collection<String> allRegions(String worldName, Target t) {
		World world = Bukkit.getServer().getWorld(worldName);
		if (world == null) {
			throw new CREInvalidWorldException("Unknown world specified.", t);
		}
		return WorldGuardPlugin.inst().getRegionManager(world).getRegions().keySet();
	}

	public Collection<String> regionsAt(String worldName, int x, int y, int z) {

		lastWorld = Bukkit.getServer().getWorld(worldName);
		RegionManager mgr = WorldGuardPlugin.inst().getRegionManager(lastWorld);
		Vector pt = new Vector(x, y, z);
		ApplicableRegionSet set = mgr.getApplicableRegions(pt);

		List<ProtectedRegion> sortedRegions = new ArrayList<>();

		for (ProtectedRegion r : set) {
			boolean placed = false;
			for (int i = 0; i < sortedRegions.size(); i++) {
				if (sortedRegions.get(i).volume() < r.volume()) {
					sortedRegions.add(i, r);
					placed = true;
					break;
				}
			}
			if (!placed) {
				sortedRegions.add(r);
			}
		}

		List<String> ret = new ArrayList<>();
		for (ProtectedRegion r : sortedRegions) {
			ret.add(r.getId());
		}

		return ret;
	}

	public Collection<String> regionsAt(MCLocation loc) {
		return regionsAt(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public void lookupRegion(String regionName, String worldName, Target t) {

		lastWorld = Bukkit.getServer().getWorld(worldName);
		if (lastWorld == null) {
			throw new CREPluginInternalException("Unknown world specified", t);
		}
		RegionManager mgr = WorldGuardPlugin.inst().getRegionManager(lastWorld);
		lastRegion = mgr.getRegion(regionName);
		if (lastRegion == null) {
			throw new CREPluginInternalException("Region could not be found!", t);
		}
	}

	public Collection<MCLocation> getBounds() {
		List<MCLocation> points = new ArrayList<>();

		boolean first = true;
		if (lastRegion instanceof ProtectedPolygonalRegion) {
			for (BlockVector2D pt : ((ProtectedPolygonalRegion) lastRegion).getPoints()) {
				points.add(new BukkitMCLocation(new Location(lastWorld, pt.getX(),
						first ? lastRegion.getMaximumPoint().getY() : lastRegion.getMinimumPoint().getY(), pt.getZ())));
				first = false;
			}
		} else {
			points.add(new BukkitMCLocation(BukkitUtil.toLocation(lastWorld, lastRegion.getMaximumPoint())));
			points.add(new BukkitMCLocation(BukkitUtil.toLocation(lastWorld, lastRegion.getMinimumPoint())));
		}

		return points;
	}

	public Collection<UUID> getOwnerPlayers() {
		return lastRegion.getOwners().getUniqueIds();
	}

	public Collection<String> getOwnerNames() {
		return lastRegion.getOwners().getPlayers();
	}

	public Collection<String> getOwnerGroups() {
		return lastRegion.getOwners().getGroups();
	}

	public Collection<UUID> getMemberPlayers() {
		return lastRegion.getMembers().getUniqueIds();
	}

	public Collection<String> getMemberNames() {
		return lastRegion.getMembers().getPlayers();
	}

	public Collection<String> getMemberGroups() {
		return lastRegion.getMembers().getGroups();
	}

	public Map<String, String> getFlags() {

		Map<String, String> flags = new HashMap<>();
		for (Map.Entry<Flag<?>, Object> ent : lastRegion.getFlags().entrySet()) {
			flags.put(ent.getKey().getName(), String.valueOf(ent.getValue()));
		}

		return flags;
	}

	public int getPriority() {
		return lastRegion.getPriority();
	}

	public float getVolume() {
		return lastRegion.volume();
	}
}
