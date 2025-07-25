/*
 * Skyblock Plus - A Skyblock focused Discord bot with many commands and customizable features to improve the experience of Skyblock players and guild staff!
 * Copyright (c) 2021-2024 kr45732
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.skyblockplus.miscellaneous.craft;

import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.skyblockplus.miscellaneous.craft.CraftCommandHandler.ignoredCategories;
import static com.skyblockplus.utils.utils.JsonUtils.*;
import static com.skyblockplus.utils.utils.StringUtils.*;

@Component
public class CraftSlashCommand extends SlashCommand {

	private static List<String> craftItems;

	public CraftSlashCommand() {
		this.name = "craft";
		craftItems =
			getSkyblockItemsJson()
				.entrySet()
				.stream()
				.filter(e -> {
					String category = higherDepth(e.getValue(), "category", null);
					return category != null && !ignoredCategories.contains(category);
				})
				.map(e -> idToName(e.getKey()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public static EmbedBuilder getCraft(String item, SlashCommandEvent event) {
		String id = nameToId(item);

		if (higherDepth(getInternalJsonMappings(), id) == null) {
			id = getClosestMatchFromIds(item, getInternalJsonMappings().keySet());
		}

		new CraftCommandHandler(id, event);
		return null;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.paginate(getCraft(event.getOptionStr("item"), event));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Calculate the cost of an item and added upgrades")
			.addOption(OptionType.STRING, "item", "Item name", true, true);
	}

	@Override
	public void onAutoComplete(AutoCompleteEvent event) {
		if (event.getFocusedOption().getName().equals("item")) {
			event.replyClosestMatch(event.getFocusedOption().getValue(), craftItems);
		}
	}
}
