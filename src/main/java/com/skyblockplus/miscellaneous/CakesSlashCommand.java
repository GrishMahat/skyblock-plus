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

import com.google.gson.JsonElement;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.skyblockplus.utils.Constants.profilesCommandOption;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.capitalizeString;
import static com.skyblockplus.utils.utils.StringUtils.getRelativeTimestamp;
import static com.skyblockplus.utils.utils.Utils.getEmoji;

@Component
public class CakesSlashCommand extends SlashCommand {

	public CakesSlashCommand() {
		this.name = "cakes";
	}

	public static EmbedBuilder getCakes(String username, String profileName) {
		Player.Profile player = Player.create(username, profileName);
		if (player.isValid()) {
			EmbedBuilder eb = player.defaultPlayerEmbed();

			Map<String, String> cakeNameToId = new HashMap<>();
			cakeNameToId.put("cake_strength", "EPOCH_CAKE_RED");
			cakeNameToId.put("cake_pet_luck", "EPOCH_CAKE_PURPLE");
			cakeNameToId.put("cake_health", "EPOCH_CAKE_PINK");
			cakeNameToId.put("cake_walk_speed", "EPOCH_CAKE_YELLOW");
			cakeNameToId.put("cake_magic_find", "EPOCH_CAKE_BLACK");
			cakeNameToId.put("cake_ferocity", "EPOCH_CAKE_ORANGE");
			cakeNameToId.put("cake_defense", "EPOCH_CAKE_GREEN");
			cakeNameToId.put("cake_sea_creature_chance", "EPOCH_CAKE_BLUE");
			cakeNameToId.put("cake_intelligence", "EPOCH_CAKE_AQUA");
			cakeNameToId.put("cake_farming_fortune", "EPOCH_CAKE_BROWN");
			cakeNameToId.put("cake_foraging_fortune", "EPOCH_CAKE_WHITE");
			cakeNameToId.put("cake_mining_fortune", "EPOCH_CAKE_CYAN");
			cakeNameToId.put("cake_true_defense", "EPOCH_CAKE_GRAY");
			cakeNameToId.put("cake_vitality", "EPOCH_CAKE_DARK_GREEN");

			StringBuilder activeCakes = new StringBuilder();
			if (higherDepth(player.profileJson(), "player_data.temp_stat_buffs") != null) {
				for (JsonElement cake : higherDepth(player.profileJson(), "player_data.temp_stat_buffs").getAsJsonArray()) {
					Instant expires = Instant.ofEpochMilli(higherDepth(cake, "expire_at").getAsLong());
					if (expires.isAfter(Instant.now())) {
						String cakeName = higherDepth(cake, "key").getAsString();
						activeCakes
							.append(getEmoji(cakeNameToId.get(cakeName)))
							.append(" ")
							.append(capitalizeString(cakeName.split("cake_")[1].replace("_", " ")))
							.append(": expires ")
							.append(getRelativeTimestamp(higherDepth(cake, "expire_at").getAsLong()))
							.append("\n");
						cakeNameToId.remove(cakeName);
					}
				}
			}
			eb.appendDescription("**Active Cakes**\n" + (!activeCakes.isEmpty() ? activeCakes.toString() : "None\n"));

			StringBuilder missingCakesStr = new StringBuilder();
			for (Map.Entry<String, String> missingCake : cakeNameToId.entrySet()) {
				missingCakesStr
					.append(getEmoji(missingCake.getValue()))
					.append(" ")
					.append(capitalizeString(missingCake.getKey().split("cake_")[1].replace("_", " ")))
					.append("\n");
			}
			eb.appendDescription("\n**Inactive Cakes**\n" + (!missingCakesStr.isEmpty() ? missingCakesStr.toString() : "None"));

			return eb;
		}
		return player.getErrorEmbed();
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (event.invalidPlayerOption()) {
			return;
		}

		event.embed(getCakes(event.player, event.getOptionStr("profile")));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Get a player's active and inactive cake buffs")
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
