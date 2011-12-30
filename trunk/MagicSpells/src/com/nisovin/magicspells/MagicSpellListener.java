package com.nisovin.magicspells;

import java.util.HashSet;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.nisovin.magicspells.events.MagicEventType;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellListener;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.util.MagicListener;

public class MagicSpellListener extends SpellListener implements MagicListener {
	
	private boolean disabled = false;
	
	public MagicSpellListener(MagicSpells plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Normal, plugin);
	}

	@Override
	public void onSpellTarget(SpellTargetEvent event) {
		if (disabled) return;
		
		// check if target has notarget permission
		LivingEntity target = event.getTarget();
		if (target instanceof Player) {
			if (((Player)target).hasPermission("magicspells.notarget")) {
				event.setCancelled(true);
			}
		}
		
		HashSet<Spell> spells = MagicSpells.customListeners.get(MagicEventType.SPELL_TARGET);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onSpellTarget(event);
			}
		}
	}

	@Override
	public void onSpellCast(SpellCastEvent event) {	
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.customListeners.get(MagicEventType.SPELL_CAST);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onSpellCast(event);
			}
		}
	}

	@Override
	public void disable() {
		disabled = true;
	}	
	
}
