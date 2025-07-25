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

import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.CustomPaginator;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.command.Subcommand;
import com.skyblockplus.utils.structs.ArmorStruct;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.skyblockplus.utils.Constants.profilesCommandOption;
import static com.skyblockplus.utils.utils.Utils.errorEmbed;
import static com.skyblockplus.utils.utils.Utils.withApiHelpButton;

@Component
public class WardrobeSlashCommand extends SlashCommand {

	public WardrobeSlashCommand() {
		this.name = "wardrobe";
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(name, "Main wardrobe bag command");
	}

	@Override
	public void onAutoComplete(AutoCompleteEvent event) {
		if (event.getFocusedOption().getName().equals("player")) {
			event.replyClosestPlayer();
		}
	}

	public static class ListSubcommand extends Subcommand {

		public ListSubcommand() {
			this.name = "list";
		}

		public static EmbedBuilder getPlayerWardrobeList(String username, String profileName, SlashCommandEvent event) {
			Player.Profile player = Player.create(username, profileName);
			if (player.isValid()) {
				Map<Integer, ArmorStruct> armorStructMap = player.getWardrobeList();
				if (armorStructMap != null) {
					CustomPaginator.Builder paginateBuilder = player.defaultPlayerPaginator(event.getUser()).setItemsPerPage(4);

					for (Map.Entry<Integer, ArmorStruct> currentArmour : armorStructMap.entrySet()) {
						paginateBuilder.addStrings(
							"**__Slot " +
							(currentArmour.getKey() + 1) +
							"__**\n" +
							currentArmour.getValue().getHelmet() +
							"\n" +
							currentArmour.getValue().getChestplate() +
							"\n" +
							currentArmour.getValue().getLeggings() +
							"\n" +
							currentArmour.getValue().getBoots() +
							"\n"
						);
					}
					event.paginate(paginateBuilder);
					return null;
				}
				return errorEmbed("API disabled");
			}
			return player.getErrorEmbed();
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			if (event.invalidPlayerOption()) {
				return;
			}

			event.paginate(getPlayerWardrobeList(event.player, event.getOptionStr("profile"), event));
		}

		@Override
		protected SubcommandData getCommandData() {
			return new SubcommandData(name, "Get a list of a player's wardrobe with lore")
				.addOption(OptionType.STRING, "player", "Player username or mention", false, true)
				.addOptions(profilesCommandOption);
		}
	}

	public static class EmojiSubcommand extends Subcommand {

		public EmojiSubcommand() {
			this.name = "emoji";
		}

		public static Object getPlayerWardrobe(String username, String profileName, SlashCommandEvent event) {
			Player.Profile player = Player.create(username, profileName);
			if (player.isValid()) {
				List<String[]> wardrobe = player.getWardrobe();
				if (wardrobe == null) {
					return withApiHelpButton(errorEmbed(player.getEscapedUsername() + "'s inventory API is disabled"));
				}

				new InventoryEmojiPaginator(wardrobe, "Wardrobe", player, event);
				return null;
			}
			return player.getErrorEmbed();
		}

		@Override
		protected void execute(SlashCommandEvent event) {
			if (event.invalidPlayerOption()) {
				return;
			}

			event.paginate(getPlayerWardrobe(event.player, event.getOptionStr("profile"), event));
		}

		@Override
		protected SubcommandData getCommandData() {
			return new SubcommandData(name, "Get a player's wardrobe represented in emojis")
				.addOption(OptionType.STRING, "player", "Player username or mention", false, true)
				.addOptions(profilesCommandOption);
		}
	}
}
