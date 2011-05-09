package com.nisovin.MagicSpells;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.Spells.*;

public class MagicSpells extends JavaPlugin {

	public static MagicSpells plugin;

	public static ChatColor textColor;
	public static int broadcastRange;
	public static List<String> castForFree;
	public static String strCastUsage;
	public static String strUnknownSpell;
	public static String strSpellChange;
	public static String strOnCooldown;
	public static String strMissingReagents;
	
	public static HashMap<String,Spell> spells; // map internal names to spells
	public static HashMap<String,Spell> spellNames; // map configured names to spells
	public static HashMap<String,Spellbook> spellbooks; // player spellbooks
	
	public static HashMap<Event.Type,HashSet<Spell>> listeners;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		// create storage stuff
		spells = new HashMap<String,Spell>();
		spellNames = new HashMap<String,Spell>();
		spellbooks = new HashMap<String,Spellbook>();
		listeners = new HashMap<Event.Type,HashSet<Spell>>();
		
		// make sure directories are created
		this.getDataFolder().mkdir();
		new File(this.getDataFolder(), "spellbooks").mkdir();
		
		// load config
		Configuration config = this.getConfiguration();
		textColor = ChatColor.getByCode(config.getInt("general.text-color", ChatColor.DARK_AQUA.getCode()));
		broadcastRange = config.getInt("general.broadcast-range", 20);
		strCastUsage = config.getString("general.str-cast-usage", "Usage: /cast <spell>. Use /cast list to see a list of spells.");
		strUnknownSpell = config.getString("general.str-unknown-spell", "You do not know a spell with that name.");
		strSpellChange = config.getString("general.str-spell-change", "You are now using the %s spell.");
		strOnCooldown = config.getString("general.str-on-cooldown", "That spell is on cooldown.");
		strMissingReagents = config.getString("general.str-missing-reagents", "You do not have the reagents for that spell.");
		castForFree = config.getStringList("general.cast-for-free", null);
		if (castForFree != null) {
			for (int i = 0; i < castForFree.size(); i++) {
				castForFree.set(i, castForFree.get(i).toLowerCase());
			}
		}
		
		// load listeners
		new MagicPlayerListener(this);
		new MagicEntityListener(this);
		new MagicBlockListener(this);
		
		// load spells
		BlinkSpell.load(config);
		CombustSpell.load(config);
		EntombSpell.load(config);
		ExplodeSpell.load(config);
		ForcepushSpell.load(config);
		GillsSpell.load(config);
		HelpSpell.load(config);
		LightningSpell.load(config);
		ListSpell.load(config);
		MarkSpell.load(config);
		PrayerSpell.load(config);
		RecallSpell.load(config);
		SafefallSpell.load(config);
		TeachSpell.load(config);
		ZapSpell.load(config);
		
		// load spell copies
		List<String> copies = config.getStringList("spellcopies", null);
		if (copies != null && copies.size() > 0) {
			for (String copy : copies) {
				String[] data = copy.split("=");
				Spell spell = spells.get(data[1]);
				if (spell != null) {
					try {
						spell.getClass().getMethod("load", Configuration.class, String.class).invoke(null, config, data[0]);
					} catch (Exception e) {
						getServer().getLogger().severe("Unable to create spell copy: " + copy);
					}
				}
			}
		}
		
		// load in-game spell names
		for (Spell spell : spells.values()) {
			spellNames.put(spell.getName(), spell);
		}
		
		// load online player spellbooks
		for (Player p : getServer().getOnlinePlayers()) {
			spellbooks.put(p.getName(), new Spellbook(p, this));
		}
		
		getServer().getLogger().info("MagicSpells v" + this.getDescription().getVersion() + " loaded!");
	}
	
	public static Spellbook getSpellbook(Player player) {
		return spellbooks.get(player.getName());
	}
	
	public static void addSpellListener(Event.Type eventType, Spell spell) {
		HashSet<Spell> spells = listeners.get(eventType);
		if (spells == null) {
			spells = new HashSet<Spell>();
			listeners.put(eventType, spells);
		}
		spells.add(spell);
	}
	
	public static void removeSpellListener(Event.Type eventType, Spell spell) {
		HashSet<Spell> spells = listeners.get(eventType);
		if (spells != null) {
			spells.remove(spell);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (command.getName().equalsIgnoreCase("cast")) {
			if (args.length == 0) {
				sender.sendMessage(textColor + strCastUsage);
			} else if (sender.isOp() && args[0].equals("reload")) {
				onDisable();
				onEnable();
				sender.sendMessage(textColor + "MagicSpells config reloaded.");
			} else if (sender instanceof Player) {
				Player player = (Player)sender;
				Spellbook spellbook = spellbooks.get(player.getName());
				Spell spell = null;
				if (spellbook != null) {
					spell = spellbook.getSpellByName(args[0]);
				}
				if (spell != null && spell.canCastByCommand()) {
					String[] spellArgs = null;
					if (args.length > 1) {
						spellArgs = new String[args.length-1];
						for (int i = 1; i < args.length; i++) {
							spellArgs[i-1] = args[i];
						}
					}
					spell.cast(player, spellArgs);
				} else {
					player.sendMessage(textColor + strUnknownSpell);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onDisable() {
		spells = null;
		spellNames = null;
		spellbooks = null;
		listeners = null;
	}
	
}
