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

import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.UsernameUuidStruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import static com.skyblockplus.utils.ApiHandler.usernameToUuid;
import static com.skyblockplus.utils.utils.Utils.defaultEmbed;
import static com.skyblockplus.utils.utils.Utils.errorEmbed;

@Component
public class UuidSlashCommand extends SlashCommand {

	public UuidSlashCommand() {
		this.name = "uuid";
	}

	public static EmbedBuilder getUuidPlayer(String username) {
		UsernameUuidStruct usernameUuid = usernameToUuid(username);
		if (!usernameUuid.isValid()) {
			return errorEmbed(usernameUuid.failCause());
		}

		return defaultEmbed(usernameUuid.username(), "https://plancke.io/hypixel/player/stats/" + usernameUuid.username())
			.setDescription("**Username:** " + usernameUuid.username() + "\n**Uuid:** " + usernameUuid.uuid())
			.setThumbnail(usernameUuid.getAvatarUrl());
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (event.invalidPlayerOption()) {
			return;
		}

		event.embed(getUuidPlayer(event.player));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Convert a username to UUID or UUID to username")
			.addOption(OptionType.STRING, "player", "Username or UUID");
	}
}
