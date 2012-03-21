package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.PermissionContainer;

public class RemovePermission extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the permission node you want to remove:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		input = input.trim();
		obj.removePermission(world, input);
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Removed permission " + Menu.HIGHLIGHT + input + Menu.TEXT_COLOR + " from " + getType(context) + Menu.HIGHLIGHT + " " + obj.getName());
		if (world != null) {
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT + world);
		}
		return Menu.MODIFY_OPTIONS;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}