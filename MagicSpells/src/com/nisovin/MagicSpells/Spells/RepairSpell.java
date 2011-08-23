package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class RepairSpell extends InstantSpell {

	private String[] toRepair;
	private String strNothingToRepair;
	
	public RepairSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		List<String> toRepairList = getConfigStringList("to-repair", null);
		if (toRepairList == null) {
			toRepairList = new ArrayList<String>();
		}
		if (toRepairList.size() == 0) {
			toRepairList.add("held");			
		}
		Iterator<String> iter = toRepairList.iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			if (!s.equals("held") && !s.equals("hotbar") && !s.equals("inventory") && !s.equals("helmet") && !s.equals("chestplate") && !s.equals("leggings") && !s.equals("boots")) {
				Bukkit.getServer().getLogger().severe("MagicSpells: repair: invalid to-repair option: " + s);
				iter.remove();
			}
		}
		toRepair = new String[toRepairList.size()];
		toRepair = toRepairList.toArray(toRepair);
		
		strNothingToRepair = getConfigString("str-nothing-to-repair", "Nothing to repair.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int repaired = 0;
			for (String s : toRepair) {
				if (s.equals("held")) {
					ItemStack item = player.getItemInHand();
					if (item != null && isRepairable(item.getType()) && item.getDurability() > 0) {
						item.setDurability((short)0);
						player.setItemInHand(item);
						repaired++;
					}
				} else if (s.equals("hotbar") || s.equals("inventory")) {
					int start, end;
					ItemStack[] items = player.getInventory().getContents();
					if (s.equals("hotbar")) {
						start = 0; 
						end = 9;
					} else {
						start = 9; 
						end = 36;
					}
					for (int i = start; i < end; i++) {
						ItemStack item = items[i];
						if (item != null && isRepairable(item.getType()) && item.getDurability() > 0) {
							item.setDurability((short)0);
							items[i] = item;
							repaired++;
						}
					}
					player.getInventory().setContents(items);
				} else if (s.equals("helmet")) {
					ItemStack item = player.getInventory().getHelmet();
					if (item != null && isRepairable(item.getType()) && item.getDurability() > 0) {
						item.setDurability((short)0);
						player.getInventory().setHelmet(item);
						repaired++;
					}
				} else if (s.equals("chestplate")) {
					ItemStack item = player.getInventory().getChestplate();
					if (item != null && isRepairable(item.getType()) && item.getDurability() > 0) {
						item.setDurability((short)0);
						player.getInventory().setChestplate(item);
						repaired++;
					}
				} else if (s.equals("leggings")) {
					ItemStack item = player.getInventory().getLeggings();
					if (item != null && isRepairable(item.getType()) && item.getDurability() > 0) {
						item.setDurability((short)0);
						player.getInventory().setLeggings(item);
						repaired++;
					}
				} else if (s.equals("boots")) {
					ItemStack item = player.getInventory().getBoots();
					if (item != null && isRepairable(item.getType()) && item.getDurability() > 0) {
						item.setDurability((short)0);
						player.getInventory().setBoots(item);
						repaired++;
					}
				}
			}
			if (repaired == 0) {
				sendMessage(player, strNothingToRepair);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean isRepairable(Material material) {
		String s = material.name();
		return 
				s.endsWith("HELMET") ||
				s.endsWith("CHESTPLATE") ||
				s.endsWith("LEGGINGS") ||
				s.endsWith("BOOTS") ||
				s.endsWith("AXE") ||
				s.endsWith("HOE") ||
				s.endsWith("PICKAXE") ||
				s.endsWith("SPADE") ||
				s.endsWith("SWORD");
	}

}
