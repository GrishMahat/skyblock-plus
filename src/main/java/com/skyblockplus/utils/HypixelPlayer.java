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

package com.skyblockplus.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skyblockplus.utils.structs.HypixelResponse;
import com.skyblockplus.utils.structs.UsernameUuidStruct;
import com.skyblockplus.utils.utils.Utils;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

import static com.skyblockplus.utils.ApiHandler.playerFromUuid;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.cleanMcCodes;
import static com.skyblockplus.utils.utils.StringUtils.getAvatarUrl;
import static com.skyblockplus.utils.utils.Utils.defaultEmbed;

public class HypixelPlayer {

	private JsonObject playerJson;

	@Getter
	private String uuid;

	@Getter
	private String username;

	private boolean validPlayer = false;

	@Getter
	private String failCause = "Unknown fail cause";

	public HypixelPlayer(String username) {
		if (usernameToUuid(username)) {
			return;
		}

		try {
			HypixelResponse response = playerFromUuid(uuid);
			if (!response.isValid()) {
				failCause = response.failCause();
				return;
			}

			this.playerJson = response.response().getAsJsonObject();
		} catch (Exception e) {
			return;
		}

		this.validPlayer = true;
	}

	/* Getters */
	public boolean isValid() {
		return validPlayer;
	}

	public EmbedBuilder getDefaultEmbed() {
		return defaultEmbed(username, "https://plancke.io/hypixel/player/stats/" + uuid).setThumbnail(getAvatarUrl(uuid));
	}

	public EmbedBuilder getErrorEmbed() {
		return Utils.errorEmbed(failCause);
	}

	/* Hypixel */
	public double getHypixelLevel() {
		return (Math.sqrt((2 * higherDepth(playerJson, "networkExp", 0L)) + 30625) / 50) - 2.5;
	}

	public boolean isOnline() {
		return higherDepth(playerJson, "lastLogin", 0) > higherDepth(playerJson, "lastLogout", 0);
	}

	public JsonObject getSocialMediaLinks() {
		return higherDepth(playerJson, "socialMedia.links") != null
			? higherDepth(playerJson, "socialMedia.links").getAsJsonObject()
			: new JsonObject();
	}

	public int getAchievementPoints() {
		return higherDepth(playerJson, "achievementPoints", 0);
	}

	public int getKarma() {
		return higherDepth(playerJson, "karma", 0);
	}

	public Instant getLastLogin() {
		if (higherDepth(playerJson, "lastLogout") == null) {
			return null;
		}
		return Instant.ofEpochMilli(higherDepth(playerJson, "lastLogout").getAsLong());
	}

	public Instant getFirstLogin() {
		if (higherDepth(playerJson, "firstLogin") == null) {
			return null;
		}
		return Instant.ofEpochMilli(higherDepth(playerJson, "firstLogin").getAsLong());
	}

	public JsonElement get(String path) {
		return higherDepth(playerJson, path);
	}

	/* Helper methods */
	private boolean usernameToUuid(String username) {
		UsernameUuidStruct response = ApiHandler.usernameToUuid(username);
		if (!response.isValid()) {
			failCause = response.failCause();
			return true;
		}

		this.username = response.username();
		this.uuid = response.uuid();
		return false;
	}

	public String getRank() {
		String hypixelRank = "NONE";
		if (playerJson.has("prefix")) {
			return cleanMcCodes(higherDepth(playerJson, "prefix").getAsString());
		} else if (playerJson.has("rank") && !higherDepth(playerJson, "rank").getAsString().equals("NORMAL")) {
			hypixelRank = higherDepth(playerJson, "rank").getAsString();
		} else if (
			playerJson.has("monthlyPackageRank") && higherDepth(playerJson, "monthlyPackageRank").getAsString().equals("SUPERSTAR")
		) {
			hypixelRank = "MVP_PLUS_PLUS";
		} else if (playerJson.has("newPackageRank") && !higherDepth(playerJson, "newPackageRank").getAsString().equals("NONE")) {
			hypixelRank = higherDepth(playerJson, "newPackageRank").getAsString();
		} else if (playerJson.has("packageRank") && !higherDepth(playerJson, "packageRank").getAsString().equals("NONE")) {
			hypixelRank = higherDepth(playerJson, "packageRank").getAsString();
		}
		hypixelRank = hypixelRank.toUpperCase();

		if (!hypixelRank.equals("NONE")) {
			hypixelRank = "[" + hypixelRank.replace("_", "").replace("PLUS", "+") + "]";
		} else {
			hypixelRank = "None";
		}

		return hypixelRank;
	}
}
