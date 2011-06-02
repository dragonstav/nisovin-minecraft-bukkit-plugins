package com.nisovin.MagicSpells.Spells;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Spellbook;
import com.nisovin.MagicSpells.Util.MagicLocation;

public class SpellbookSpell extends CommandSpell {
	
	private static final String SPELL_NAME = "spellbook";
	
	private int defaultUses;
	private boolean destroyBookcase;
	private Material spellbookBlock;
	private String strUsage;
	private String strNoSpell;
	private String strNoTarget;
	private String strHasSpellbook;
	private String strCantDestroy;
	private String strLearnError;
	private String strCantLearn;
	private String strAlreadyKnown;
	private String strLearned;
	
	private ArrayList<MagicLocation> bookLocations;
	private ArrayList<String> bookSpells;
	private ArrayList<Integer> bookUses;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new SpellbookSpell(config, spellName));
		}
	}
	
	public SpellbookSpell(Configuration config, String spellName) {
		super(config,spellName);
		
		defaultUses = config.getInt("spells." + spellName + ".default-uses", -1);
		destroyBookcase = config.getBoolean("spells." + spellName + ".destroy-when-used-up", false);
		spellbookBlock = Material.getMaterial(config.getInt("spell." + spellName + ".spellbook-block", Material.BOOKSHELF.getId()));
		strUsage = config.getString("spells." + spellName + ".str-usage", "Usage: /cast spellbook <spell> [uses]");
		strNoSpell = config.getString("spells." + spellName + ".str-no-spell", "You do not know a spell by that name.");
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You must target a bookcase to create a spellbook.");
		strHasSpellbook = config.getString("spells." + spellName + ".str-has-spellbook", "That bookcase already has a spellbook.");
		strCantDestroy = config.getString("spells." + spellName + ".str-cant-destroy", "You cannot destroy a bookcase with a spellbook.");
		strLearnError = config.getString("spells." + spellName + ".str-learn-error", "");
		strCantLearn = config.getString("spells." + spellName + ".str-cant-learn", "You cannot learn the spell in this spellbook.");
		strAlreadyKnown = config.getString("spells." + spellName + ".str-already-known", "You already know the %s spell.");
		strLearned = config.getString("spells." + spellName + ".str-learned", "You have learned the %s spell!");
		
		bookLocations = new ArrayList<MagicLocation>();
		bookSpells = new ArrayList<String>();
		bookUses = new ArrayList<Integer>();
		
		addListener(Event.Type.PLAYER_INTERACT);
		addListener(Event.Type.BLOCK_BREAK);
		
		loadSpellbooks();
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length < 1 || args.length > 2 || (args.length == 2 && !args[1].matches("^[0-9]+$"))) {
				// fail: show usage string
				sendMessage(player, strUsage);
			} else {
				Spell spell = MagicSpells.getSpellbook(player).getSpellByName(args[0]);
				if (spell == null) {
					// fail: no such spell
					sendMessage(player, strNoSpell);
				} else {
					Block target = player.getTargetBlock(null, 10);
					if (target == null || target.getType() != spellbookBlock) {
						// fail: must target a bookcase
						sendMessage(player, strNoTarget);
					} else if (bookLocations.contains(target.getLocation())) {
						// fail: already a spellbook there
						sendMessage(player, strHasSpellbook);
					} else {
						// create spellbook
						bookLocations.add(new MagicLocation(target.getLocation()));
						bookSpells.add(spell.getInternalName());
						if (args.length == 1) {
							bookUses.add(defaultUses);
						} else {
							bookUses.add(Integer.parseInt(args[1]));
						}
						saveSpellbooks();
						sendMessage(player, formatMessage(strCastSelf, "%s", spell.getName()));
						setCooldown(player);
						removeReagents(player);
					}
				}
			}
		}
		return true;
	}
	
	private void removeSpellbook(int index) {
		bookLocations.remove(index);
		bookSpells.remove(index);
		bookUses.remove(index);
		saveSpellbooks();
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.hasBlock() && event.getClickedBlock().getType() == spellbookBlock) {
			MagicLocation loc = new MagicLocation(event.getClickedBlock().getLocation());
			if (bookLocations.contains(loc)) {
				Player player = event.getPlayer();
				int i = bookLocations.indexOf(loc);
				Spellbook spellbook = MagicSpells.spellbooks.get(player.getName());
				Spell spell = MagicSpells.spells.get(bookSpells.get(i));
				if (spellbook == null || spell == null) {
					// fail: something's wrong
					sendMessage(player, strLearnError);
				} else if (!spellbook.canLearn(spell)) {
					// fail: can't learn
					sendMessage(player, formatMessage(strCantLearn, "%s", spell.getName()));
				} else if (spellbook.hasSpell(spell)) {
					// fail: already known
					sendMessage(player, formatMessage(strAlreadyKnown, "%s", spell.getName()));
				} else {
					// teach the spell
					spellbook.addSpell(spell);
					spellbook.save();
					sendMessage(player, formatMessage(strLearned, "%s", spell.getName()));
					int uses = bookUses.get(i);
					if (uses > 0) {
						uses--;
						if (uses == 0) {
							// remove the spellbook
							if (destroyBookcase) {
								bookLocations.get(i).getLocation().getBlock().setType(Material.AIR);
							}
							removeSpellbook(i);
						} else {
							bookUses.set(i, uses);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == spellbookBlock) {
			MagicLocation loc = new MagicLocation(event.getBlock().getLocation());
			if (bookLocations.contains(loc)) {
				if (event.getPlayer().isOp()) {
					// remove the bookcase
					int i = bookLocations.indexOf(loc);
					removeSpellbook(i);
				} else {
					// cancel it
					event.setCancelled(true);
					sendMessage(event.getPlayer(), strCantDestroy);
				}
			}			
		}
	}
	
	private void loadSpellbooks() {
		try {
			Scanner scanner = new Scanner(new File(MagicSpells.plugin.getDataFolder(), "books.txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					try {
						String[] data = line.split(":");
						MagicLocation loc = new MagicLocation(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
						int uses = Integer.parseInt(data[5]);
						bookLocations.add(loc);
						bookSpells.add(data[4]);
						bookUses.add(uses);
					} catch (Exception e) {
						MagicSpells.plugin.getServer().getLogger().severe("MagicSpells: Failed to load spellbook: " + line);
					}
				}
			}
		} catch (FileNotFoundException e) {
		} 
	}
	
	private void saveSpellbooks() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(MagicSpells.plugin.getDataFolder(), "books.txt"), false));
			MagicLocation loc;
			for (int i = 0; i < bookLocations.size(); i++) {
				loc = bookLocations.get(i);
				writer.write(loc.getWorld() + ":" + (int)loc.getX() + ":" + (int)loc.getY() + ":" + (int)loc.getZ() + ":");
				writer.write(bookSpells.get(i) + ":" + bookUses.get(i));
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			MagicSpells.plugin.getServer().getLogger().severe("MagicSpells: Error saving spellbooks");
		}
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
}