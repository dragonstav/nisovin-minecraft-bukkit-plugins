package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class FirenovaSpell extends InstantSpell {

	private static final String SPELL_NAME = "firenova";

	private int tickSpeed;
	private boolean checkPlugins;
	
	private HashSet<Player> fireImmunity;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FirenovaSpell(config, spellName));
		}
	}
	
	public FirenovaSpell(Configuration config, String spellName) {
		super(config, spellName);
		addListener(Event.Type.ENTITY_DAMAGE);
		
		tickSpeed = config.getInt("spells." + spellName + ".tick-speed", 5);
		checkPlugins = config.getBoolean("spells." + spellName + ".check-plugins", true);
		
		fireImmunity = new HashSet<Player>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			fireImmunity.add(player);
			new FirenovaAnimation(player);
		}
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) && fireImmunity.size() > 0) {
			Player player = (Player)event.getEntity();
			if (fireImmunity.contains(player)) {
				// caster is taking damage, cancel it
				event.setCancelled(true);
				player.setFireTicks(0);
			} else if (checkPlugins) {
				// check if nearby players are taking damage
				Vector v = player.getLocation().toVector();
				for (Player p : fireImmunity) {
					if (p.getLocation().toVector().distanceSquared(v) < range*range) {
						// nearby, check plugins for pvp
						EntityDamageByEntityEvent evt = new EntityDamageByEntityEvent(p, player, DamageCause.ENTITY_ATTACK, event.getDamage());
						Bukkit.getServer().getPluginManager().callEvent(evt);
						if (evt.isCancelled()) {
							event.setCancelled(true);
							player.setFireTicks(0);
							break;
						}
					}
				}
			}
		}
	}
	
	private class FirenovaAnimation implements Runnable {
		Player player;
		int i;
		Block center;
		HashSet<Block> fireBlocks;
		int taskId;
		
		public FirenovaAnimation(Player player) {
			this.player = player;
			
			i = 0;
			center = player.getLocation().getBlock();
			fireBlocks = new HashSet<Block>();
			
			taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, 0, tickSpeed);
		}
		
		public void run() {
			// remove old fire blocks
			for (Block block : fireBlocks) {
				if (block.getType() == Material.FIRE) {
					block.setType(Material.AIR);
				}
			}
			fireBlocks.clear();
						
			i += 1;
			if (i <= range) {
				// set next ring on fire
				int bx = center.getX();
				int y = center.getY();
				int bz = center.getZ();
				for (int x = bx - i; x <= bx + i; x++) {
					for (int z = bz - i; z <= bz + i; z++) {
						if (Math.abs(x-bx) == i || Math.abs(z-bz) == i) {
							Block b = center.getWorld().getBlockAt(x,y,z);
							if (b.getType() == Material.AIR) {
								Block under = b.getRelative(BlockFace.DOWN);
								if (under.getType() == Material.AIR) {
									b = under;
								}
								b.setType(Material.FIRE);
								fireBlocks.add(b);
							} else if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
								b = b.getRelative(BlockFace.UP);
								b.setType(Material.FIRE);
								fireBlocks.add(b);
							}
						}
					}
				}
			} else {				
				// stop if done
				Bukkit.getServer().getScheduler().cancelTask(taskId);
				fireImmunity.remove(player);
			}
		}
	}

}