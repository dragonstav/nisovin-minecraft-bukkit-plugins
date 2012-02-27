package com.nisovin.magicspells.graphicaleffects;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;

import com.nisovin.magicspells.MagicSpells;

public class SmokeTrailEffect extends GraphicalEffect {

	@Override
	public void playEffect(Location location1, Location location2, String param) {
		int interval = 0;
		if (param != null) {
			try {
				interval = Integer.parseInt(param);
			} catch (NumberFormatException e) {			
			}
		}
		
		SmokeStreamEffect effect = new SmokeStreamEffect(location1, location2);
		if (interval > 0) {
			effect.start(interval);
		} else {
			effect.showNoAnimation();
		}
	}
	
	// Thanks to DrBowe for sharing the code
	private class SmokeStreamEffect implements Runnable {
		private Location startLoc;
		private Location endLoc;
		private ArrayList<Location> locationsForProjection;
		private World world;

		private int i;
		private int id;
		
		public SmokeStreamEffect(Location loc1, Location loc2) {
			this.startLoc = loc1;
			this.endLoc = loc2;
			this.world = startLoc.getWorld();

			this.locationsForProjection = calculateLocsForProjection();
			this.i = 0;
			
		}
		
		public void start(int interval) {
			this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, interval, interval);
		}

		public void showNoAnimation() {
			while (this.i < locationsForProjection.size()) {
				run();
			}
		}
		
		public void run() {
			if (i > locationsForProjection.size()-1) {
				Bukkit.getScheduler().cancelTask(id);
				return;
			}
			Location loc = locationsForProjection.get(i);
			for (int j = 0; j <= 8; j+=2) {
				world.playEffect(loc, Effect.SMOKE, j);
			}
			i++;			
		}
		
		private ArrayList<Location> calculateLocsForProjection() {
			double x1,y1,z1,x2,y2,z2,xVect,yVect,zVect;
			x1 = endLoc.getX();
			y1 = endLoc.getY();
			z1 = endLoc.getZ();
			x2 = startLoc.getX();
			y2 = startLoc.getY();
			z2 = startLoc.getZ();
			xVect = x2-x1;
			yVect = y2-y1;
			zVect = z2-z1;
			double distance = startLoc.distance(endLoc);
			ArrayList<Location> tmp = new ArrayList<Location>((int)Math.floor(distance));
			
			for (double t = 0; t <= 1; t+= 1/distance) {
				tmp.add(new Location(world, x2-(xVect*t), y2-(yVect*t)+1, z2-(zVect*t)));
			}
			return tmp;
		}
	}
	
}