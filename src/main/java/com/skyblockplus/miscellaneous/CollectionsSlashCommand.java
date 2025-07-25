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

package com.skyblockplus.miscellaneous;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.CustomPaginator;
import com.skyblockplus.utils.command.PaginatorExtras;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.skyblockplus.utils.Constants.profilesCommandOption;
import static com.skyblockplus.utils.utils.JsonUtils.getCollectionsJson;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.getEmoji;

@Component
public class CollectionsSlashCommand extends SlashCommand {

	public CollectionsSlashCommand() {
		this.name = "collections";
	}

	public static EmbedBuilder getCollections(String username, String profileName, SlashCommandEvent event) {
		Player.Profile player = Player.create(username, profileName);
		if (player.isValid()) {
			CustomPaginator.Builder paginateBuilder = event.getPaginator(PaginatorExtras.PaginatorType.EMBED_PAGES);
			PaginatorExtras extras = paginateBuilder.getExtras();
			EmbedBuilder eb = player.defaultPlayerEmbed();

			int maxedCount = player.getNumMaxedCollections();
			int maxCountType = 0;
			int totalCountType = 0;
			String collectionType = null;
			for (Map.Entry<String, JsonElement> entry : getCollectionsJson().entrySet()) {
				String curCollectionType = higherDepth(entry.getValue(), "type").getAsString();
				if (collectionType == null) {
					collectionType = curCollectionType;
				}
				if (!curCollectionType.equals(collectionType)) {
					extras.addEmbedPage(
						eb.setDescription(
							"**Total Maxed Collections:** " +
							maxedCount +
							"/" +
							getCollectionsJson().size() +
							"\n**Maxed " +
							capitalizeString(collectionType) +
							" Collections:** " +
							maxCountType +
							"/" +
							totalCountType +
							"\n" +
							eb.getDescriptionBuilder()
						)
					);
					eb = player.defaultPlayerEmbed();
					collectionType = curCollectionType;
					maxCountType = totalCountType = 0;
				}

				JsonArray tiers = higherDepth(entry.getValue(), "tiers").getAsJsonArray();
				long amt = player.getCombinedCollection(entry.getKey());
				int level = 0;
				for (int i = 0; i < tiers.size(); i++) {
					if (amt >= tiers.get(i).getAsLong()) {
						level = i + 1;
					} else {
						break;
					}
				}
				if (level == tiers.size()) {
					maxCountType++;
				}

				eb.appendDescription(
					"\n" +
					getEmoji(entry.getKey().equals("MUSHROOM_COLLECTION") ? "RED_MUSHROOM" : entry.getKey()) +
					" " +
					idToName(entry.getKey()) +
					": " +
					level +
					"/" +
					tiers.size() +
					" (" +
					simplifyNumber(amt) +
					")"
				);
				totalCountType++;
			}

			event.paginate(paginateBuilder);
			return null;
		}
		return player.getErrorEmbed();
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (event.invalidPlayerOption()) {
			return;
		}

		event.paginate(getCollections(event.player, event.getOptionStr("profile"), event));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Get a player's collection amounts and levels")
			.addOption(OptionType.STRING, "player", "Player username or mention", false, true)
			.addOptions(profilesCommandOption);
	}

	@Override
	public void onAutoComplete(AutoCompleteEvent event) {
		if (event.getFocusedOption().getName().equals("player")) {
			event.replyClosestPlayer();
		}
	}
}
