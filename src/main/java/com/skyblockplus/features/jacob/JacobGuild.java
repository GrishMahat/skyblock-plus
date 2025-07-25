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

package com.skyblockplus.features.jacob;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.skyblockplus.api.serversettings.automatedroles.RoleObject;
import com.skyblockplus.features.listeners.AutomaticGuild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.Utils.*;

public class JacobGuild {

	public final AutomaticGuild parent;
	public boolean enable = false;
	public List<RoleObject> wantedCrops;
	public TextChannel channel;

	public JacobGuild(JsonElement jacobSettings, AutomaticGuild parent) {
		this.parent = parent;
		reloadSettingsJson(jacobSettings);
	}

	public boolean onFarmingContest(List<String> crops, MessageEmbed embed) {
		try {
			if (enable) {
				if (!channel.canTalk()) {
					parent.logAction(
						"bot_permission_error",
						defaultEmbed("Jacob Notifications")
							.setDescription("Missing permissions to view or send messages in " + channel.getAsMention())
					);
					return false;
				}

				Set<String> roleMentions = new HashSet<>();
				for (RoleObject wantedCrop : wantedCrops) {
					if (crops.contains(wantedCrop.getValue())) {
						roleMentions.add("<@&" + wantedCrop.getRoleId() + ">");
					}
				}

				if (!roleMentions.isEmpty()) {
					channel.sendMessage(String.join(" ", roleMentions)).setEmbeds(embed).queue(ignore, ignore);
					return true;
				}
			}
		} catch (Exception e) {
			AutomaticGuild.getLog().error(parent.guildId, e);
		}
		return false;
	}

	public void reloadSettingsJson(JsonElement jacobSettings) {
		try {
			enable = higherDepth(jacobSettings, "enable", false);
			if (enable) {
				channel = jda.getGuildById(parent.guildId).getTextChannelById(higherDepth(jacobSettings, "channel").getAsString());
				channel.getId();
				wantedCrops =
					gson.fromJson(higherDepth(jacobSettings, "crops").getAsJsonArray(), new TypeToken<List<RoleObject>>() {}.getType());
			}
		} catch (Exception e) {
			enable = false;
			AutomaticGuild.getLog().error(parent.guildId, e);
		}
	}
}
