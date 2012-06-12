package com.nisovin.magicspells.spells.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class HelpSpell extends CommandSpell {
	
	private boolean requireKnownSpell;
	private String strUsage;
	private String strNoSpell;
	private String strDescLine;
	private String strCostLine;

	public HelpSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		requireKnownSpell = getConfigBoolean("require-known-spell", true);
		strUsage = getConfigString("str-usage", "Usage: /cast " + name + " <spell>");
		strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		strDescLine = getConfigString("str-desc-line", "%s - %d");
		strCostLine = getConfigString("str-cost-line", "Cost: %c");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				sendMessage(player, strUsage);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				Spell spell = MagicSpells.getSpellByInGameName(args[0]);
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				if (spell == null || (requireKnownSpell && (spellbook == null || !spellbook.hasSpell(spell)))) {
					sendMessage(player, strNoSpell);
					return PostCastAction.ALREADY_HANDLED;
				} else {
					sendMessage(player, formatMessage(strDescLine, "%s", spell.getName(), "%d", spell.getDescription()));
					if (spell.getCostStr() != null && !spell.getCostStr().equals("")) {
						sendMessage(player, formatMessage(strCostLine, "%c", spell.getCostStr()));
					}
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public String tabComplete(CommandSender sender, String partial) {
		String [] args = Util.splitParams(partial);
		if (sender instanceof Player && args.length == 1) {
			Spellbook spellbook = MagicSpells.getSpellbook((Player)sender);
			return spellbook.tabComplete(partial);
		}
		return null;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

}