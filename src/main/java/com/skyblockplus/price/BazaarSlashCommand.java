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

package com.skyblockplus.price;

import com.google.gson.JsonElement;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import com.skyblockplus.utils.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.skyblockplus.utils.utils.JsonUtils.*;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.defaultEmbed;
import static com.skyblockplus.utils.utils.Utils.errorEmbed;

@Component
public class BazaarSlashCommand extends SlashCommand {

	public BazaarSlashCommand() {
		this.name = "bazaar";
	}

	public static EmbedBuilder getBazaarItem(String itemNameU) {
		JsonElement bazaarItems = getBazaarJson();
		if (bazaarItems == null) {
			return errorEmbed("Error getting bazaar data");
		}

		String itemId = nameToId(itemNameU);
		if (higherDepth(bazaarItems, itemId) == null) {
			itemId = getClosestMatchFromIds(itemId, getJsonKeys(bazaarItems));
		}

		JsonElement itemInfo = higherDepth(bazaarItems, itemId);
		return defaultEmbed(idToName(itemId), "https://bazaartracker.com/product/" + itemId)
			.addField("Buy Price (Per)", simplifyNumber(higherDepth(itemInfo, "buy_summary", 0.0)), true)
			.addField("Sell Price (Per)", simplifyNumber(higherDepth(itemInfo, "sell_summary", 0.0)), true)
			.addBlankField(true)
			.addField("Buy Volume", simplifyNumber(higherDepth(itemInfo, "buyVolume", 0L)), true)
			.addField("Sell Volume", simplifyNumber(higherDepth(itemInfo, "sellVolume", 0L)), true)
			.addBlankField(true)
			.setThumbnail(getItemThumbnail(itemId));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.embed(getBazaarItem(event.getOptionStr("item")));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(name, "Get bazaar prices of an item").addOption(OptionType.STRING, "item", "Item name", true, true);
	}

	@Override
	public void onAutoComplete(AutoCompleteEvent event) {
		if (event.getFocusedOption().getName().equals("item")) {
			event.replyClosestMatch(
				event.getFocusedOption().getValue(),
				getBazaarJson().keySet().stream().map(StringUtils::idToName).distinct().collect(Collectors.toCollection(ArrayList::new))
			);
		}
	}
}
