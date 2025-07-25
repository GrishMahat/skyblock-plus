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

package com.skyblockplus.inventory;

import com.google.gson.JsonElement;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.CustomPaginator;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

import static com.skyblockplus.utils.Constants.profilesCommandOption;
import static com.skyblockplus.utils.utils.HypixelUtils.getNpcSellPrice;
import static com.skyblockplus.utils.utils.JsonUtils.getBazaarJson;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.*;

@Component
public class SacksSlashCommand extends SlashCommand {

	public SacksSlashCommand() {
		this.name = "sacks";
	}

	public static Object getPlayerSacks(String username, String profileName, String source, SlashCommandEvent event) {
		Player.Profile player = Player.create(username, profileName);
		if (!player.isValid()) {
			return player.getErrorEmbed();
		}

		Map<String, Integer> sacksMap = player.getPlayerSacks();
		if (sacksMap == null) {
			return withApiHelpButton(errorEmbed(player.getEscapedUsername() + "'s inventory API is disabled"));
		}
		if (sacksMap.isEmpty()) {
			return errorEmbed(player.getEscapedUsername() + "'s sacks are empty");
		}

		CustomPaginator.Builder paginateBuilder = player.defaultPlayerPaginator(event.getUser()).setItemsPerPage(20);

		JsonElement bazaarPrices = getBazaarJson();

		// {bazaar, npc}
		final double[] total = { 0, 0 };
		sacksMap
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue() > 0)
			.sorted(
				Comparator.comparingDouble(entry -> {
					double bazaarPrice = higherDepth(bazaarPrices, entry.getKey() + ".sell_summary", 0.0);
					double npcPrice = Math.max(getNpcSellPrice(entry.getKey()), 0);

					return -(
						switch (source) {
							case "bazaar" -> bazaarPrice;
							case "npc" -> npcPrice;
							default -> Math.max(bazaarPrice, npcPrice);
						} *
						entry.getValue()
					);
				})
			)
			.forEach(entry -> {
				double bazaarPrice = higherDepth(bazaarPrices, entry.getKey() + ".sell_summary", 0.0);
				double npcPrice = Math.max(getNpcSellPrice(entry.getKey()), 0);

				int loc = 0;

				double sackPrice =
					switch (source) {
						case "bazaar" -> bazaarPrice;
						case "npc" -> {
							loc = 1;
							yield npcPrice;
						}
						default -> {
							if (npcPrice > bazaarPrice) {
								loc = 1;
								yield npcPrice;
							} else {
								yield bazaarPrice;
							}
						}
					} *
					entry.getValue();

				String id = entry.getKey();
				if (id.startsWith("RUNE_")) {
					id = id.substring(id.indexOf("_") + 1, id.lastIndexOf("_")) + "_RUNE;" + id.substring(id.lastIndexOf("_") + 1);
				}

				String emoji = getEmoji(id.equals("MUSHROOM_COLLECTION") ? "RED_MUSHROOM" : id, null);

				paginateBuilder.addStrings(
					(emoji != null ? emoji + " " : "") +
					"**" +
					idToName(id) +
					":** " +
					formatNumber(entry.getValue()) +
					" ➜ " +
					simplifyNumber(sackPrice) +
					(source.equals("bazaar_npc") && loc == 1 ? " (npc)" : "")
				);
				total[loc] += sackPrice;
			});

		paginateBuilder
			.getExtras()
			.setEveryPageText(
				"**Total value:** " +
				roundAndFormat(total[0] + total[1]) +
				(source.equals("bazaar_npc") ? " (" + simplifyNumber(total[0]) + " bazaar + " + simplifyNumber(total[1]) + " npc)" : "") +
				"\n"
			);
		event.paginate(paginateBuilder);
		return null;
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (event.invalidPlayerOption()) {
			return;
		}

		event.paginate(getPlayerSacks(event.player, event.getOptionStr("profile"), event.getOptionStr("source", "bazaar_npc"), event));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Get a player's sacks content represented in a list")
			.addOption(OptionType.STRING, "player", "Player username or mention", false, true)
			.addOptions(
				profilesCommandOption,
				new OptionData(OptionType.STRING, "source", "Source for prices")
					.addChoice("Bazaar & NPC (Default)", "bazaar_npc")
					.addChoice("Bazaar Only", "bazaar")
					.addChoice("NPC Only", "npc")
			);
	}

	@Override
	public void onAutoComplete(AutoCompleteEvent event) {
		if (event.getFocusedOption().getName().equals("player")) {
			event.replyClosestPlayer();
		}
	}
}
