package com.nisovin.magicspells.spells.instant;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class FirenovaSpell extends InstantSpell {

	private int range;
	private int tickSpeed;
	private int blockType;
	private byte blockData;
	private boolean burnTallGrass;
	private boolean checkPlugins;
	
	private HashSet<Player> fireImmunity;
	
	public FirenovaSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		range = getConfigInt("range", 3);
		tickSpeed = getConfigInt("tick-speed", 5);
		burnTallGrass = getConfigBoolean("burn-tall-grass", true);
		checkPlugins = getConfigBoolean("check-plugins", true);
		
		String type = getConfigString("block-type", "51:15");
		if (type.contains(":")) {
			String[] data = type.split(":");
			blockType = Integer.parseInt(data[0]);
			blockData = Byte.parseByte(data[1]);
		} else {
			blockType = Integer.parseInt(type);
			blockData = (byte)0;
		}
		
		if (blockType == Material.FIRE.getId()) {
			fireImmunity = new HashSet<Player>();
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (fireImmunity != null) {
				fireImmunity.add(player);
			}
			new FirenovaAnimation(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled() || fireImmunity == null) return;
		if (event.getEntity() instanceof Player && (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK) && fireImmunity.size() > 0) {
			Player player = (Player)event.getEntity();
			if (fireImmunity.contains(player)) {
				// caster is taking damage, cancel it
				event.setCancelled(true);
				player.setFireTicks(0);
			} else if (checkPlugins) {
				// check if nearby players are taking damage
				Location loc = player.getLocation();
				for (Player p : fireImmunity) {
					if (Math.abs(p.getLocation().getX() - loc.getX()) < range+2 && Math.abs(p.getLocation().getZ() - loc.getZ()) < range+2 && Math.abs(p.getLocation().getY() - loc.getY()) < range) {
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
				if (block.getTypeId() == blockType) {
					block.setTypeIdAndData(0, (byte)0, false);
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
							if (b.getType() == Material.AIR || (burnTallGrass && b.getType() == Material.LONG_GRASS)) {
								Block under = b.getRelative(BlockFace.DOWN);
								if (under.getType() == Material.AIR || (burnTallGrass && under.getType() == Material.LONG_GRASS)) {
									b = under;
								}
								b.setTypeIdAndData(blockType, blockData, false);
								fireBlocks.add(b);
							} else if (b.getRelative(BlockFace.UP).getType() == Material.AIR || (burnTallGrass && b.getRelative(BlockFace.UP).getType() == Material.LONG_GRASS)) {
								b = b.getRelative(BlockFace.UP);
								b.setTypeIdAndData(blockType, blockData, false);
								fireBlocks.add(b);
							}
						}
					}
				}
			} else if (i > range+1) {
				// stop if done
				Bukkit.getServer().getScheduler().cancelTask(taskId);
				if (fireImmunity != null) {
					fireImmunity.remove(player);
				}
			}
		}
	}

}