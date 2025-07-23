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

package com.skyblockplus.dev;

import com.google.gson.JsonElement;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.skyblockplus.api.serversettings.managers.ServerSettingsModel;
import com.skyblockplus.utils.command.CommandExecute;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.skyblockplus.utils.utils.Utils.*;

@Component
public class GetSettingsCommand extends Command {

	public GetSettingsCommand() {
		this.name = "d-getsettings";
		this.ownerCommand = true;
		this.botPermissions = defaultPerms();
	}

	@Override
	protected void execute(CommandEvent event) {
		new CommandExecute(event) {
			@Override
			protected void execute() {
				if (args.length == 1) {
					event.getChannel().sendMessage(getServerSettings(event.getGuild().getId())).queue();
				} else {
					if (args[1].equals("all")) {
						event.getChannel().sendMessage(getAllServerSettings()).queue();
					} else {
						event.getChannel().sendMessage(getServerSettings(args[1])).queue();
					}
				}
			}
		};
	}

	private String getAllServerSettings() {
		List<ServerSettingsModel> settings = database.getAllServerSettings();
		if (settings == null) {
			return "Unable to fetch all server settings";
		}

		return makeHastePost(formattedGson.toJson(settings));
	}

	private String getServerSettings(String guildId) {
		JsonElement currentSettings = database.getServerSettings(guildId);
		if (currentSettings == null) {
			return "No settings for provided guild id";
		}

		return makeHastePost(formattedGson.toJson(currentSettings));
	}
}
