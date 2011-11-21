package com.nisovin.magicspells;

import java.util.HashSet;

import org.bukkit.event.Event;

import com.nisovin.magicspells.events.MagicEventType;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellListener;
import com.nisovin.magicspells.events.SpellTargetEvent;

public class MagicSpellListener extends SpellListener {
	
	public MagicSpellListener(MagicSpells plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Normal, plugin);
	}

	@Override
	public void onSpellTarget(SpellTargetEvent event) {
		HashSet<Spell> spells = MagicSpells.customListeners.get(MagicEventType.SPELL_TARGET);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onSpellTarget(event);
			}
		}
	}

	@Override
	public void onSpellCast(SpellCastEvent event) {	
		HashSet<Spell> spells = MagicSpells.customListeners.get(MagicEventType.SPELL_CAST);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onSpellCast(event);
			}
		}
	}	
	
}