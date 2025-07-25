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

package com.skyblockplus.slayer;

import com.google.gson.JsonArray;
import com.skyblockplus.miscellaneous.weight.weight.Weight;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import com.skyblockplus.utils.structs.WeightStruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import static com.skyblockplus.utils.Constants.SLAYER_EMOJI_MAP;
import static com.skyblockplus.utils.Constants.profilesCommandOption;
import static com.skyblockplus.utils.utils.JsonUtils.getLevelingJson;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.errorEmbed;

@Component
public class CalcSlayerSlashCommand extends SlashCommand {

	public CalcSlayerSlashCommand() {
		this.name = "calcslayer";
	}

	public static EmbedBuilder getCalcSlayer(
		String username,
		String profileName,
		String slayerType,
		int targetLevel,
		long targetXp,
		Player.WeightType weightType
	) {
		if (targetXp <= 0 && targetLevel <= 0) {
			return errorEmbed("Target xp or target level must be provided");
		}
		if (slayerType.equals("vampire") && targetLevel > 5) {
			return errorEmbed("Target level must be between 1 and 5");
		}

		Player.Profile player = Player.create(username, profileName);
		if (player.isValid()) {
			int curXp = player.getSlayerXp(slayerType);
			targetXp =
				targetLevel != -1
					? higherDepth(getLevelingJson(), "slayer_xp." + slayerType + ".[" + (targetLevel - 1) + "]").getAsLong()
					: targetXp;

			if (curXp >= targetXp) {
				return errorEmbed("You already have " + roundAndFormat(targetXp) + " xp");
			}

			long xpNeeded = targetXp - curXp;
			JsonArray bossXpArr = higherDepth(getLevelingJson(), "slayer_boss_xp").getAsJsonArray();
			StringBuilder out = new StringBuilder();
			for (int i = 0; i < (slayerType.equals("rev") ? 5 : 4); i++) {
				double xpPerBoss = bossXpArr.get(i).getAsInt();
				int killsNeeded = (int) Math.ceil(xpNeeded / xpPerBoss);
				long cost = killsNeeded;
				if (slayerType.equals("blaze")) {
					cost *=
					switch (i) {
						case 0 -> 10000L;
						case 1 -> 25000L;
						case 2 -> 60000L;
						default -> 150000L;
					};
				} else if (slayerType.equals("vampire")) {
					cost *=
					switch (i) {
						case 0 -> 2000L;
						case 1 -> 4000L;
						case 2 -> 5000L;
						case 3 -> 7000L;
						default -> 10000L;
					};
				} else {
					cost *=
					switch (i) {
						case 0 -> 2000L;
						case 1 -> 7500L;
						case 2 -> 20000L;
						case 3 -> 50000L;
						default -> 100000L;
					};
				}

				boolean unlockedTier = i == 0 || player.getSlayerBossKills(slayerType, i - 1) > 0;

				out.append("\n").append(SLAYER_EMOJI_MAP.get(slayerType)).append(" ");
				if (!unlockedTier) {
					out.append("~~");
				}
				out
					.append("Tier ")
					.append(toRomanNumerals(i + 1).toUpperCase())
					.append(" ")
					.append(capitalizeString(slayerType))
					.append(": ")
					.append(formatNumber(killsNeeded))
					.append(" (")
					.append(formatNumber(cost))
					.append(slayerType.equals("vampire") ? " motes" : " coins")
					.append(")");
				if (!unlockedTier) {
					out.append("~~");
				}
			}

			Weight weight = Weight.of(weightType, player).calculateWeight(slayerType);
			Weight predictedWeight = Weight.of(weightType, player).calculateWeight(slayerType);
			WeightStruct pre = weight.getSlayerWeight().getSlayerWeight(slayerType);
			WeightStruct post = predictedWeight.getSlayerWeight().getSlayerWeight(slayerType, (int) targetXp);

			return player
				.defaultPlayerEmbed()
				.setDescription(
					"**Current XP:** " +
					roundAndFormat(curXp) +
					"\n**Target XP:** " +
					roundAndFormat(targetXp) +
					"\n**XP Needed:** " +
					formatNumber(xpNeeded)
				)
				.addField("Bosses Needed", out.toString(), false)
				.addField(
					"Weight Change",
					"Total: " +
					weight.getTotalWeight().getFormatted(false) +
					" ➜ " +
					predictedWeight.getTotalWeight().getFormatted(false) +
					"\n" +
					capitalizeString(slayerType) +
					": " +
					pre.getFormatted(false) +
					" ➜ " +
					post.getFormatted(false),
					false
				);
		}

		return player.getErrorEmbed();
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (event.invalidPlayerOption()) {
			return;
		}

		event.embed(
			getCalcSlayer(
				event.player,
				event.getOptionStr("profile"),
				event.getOptionStr("type"),
				event.getOptionInt("level", -1),
				event.getOptionInt("xp", -1),
				Player.WeightType.of(event.getOptionStr("system", "senither"))
			)
		);
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Calculate the number of slayer bosses needed to reach a certain level or xp amount")
			.addOptions(
				new OptionData(OptionType.STRING, "type", "Slayer type", true)
					.addChoice("Sven Packmaster", "wolf")
					.addChoice("Revenant Horror", "zombie")
					.addChoice("Tarantula Broodfather", "spider")
					.addChoice("Voidgloom Seraph", "enderman")
					.addChoice("Inferno Demonlord", "blaze")
					.addChoice("Riftstalker Bloodfiend", "vampire"),
				new OptionData(OptionType.INTEGER, "level", "Target slayer level").setRequiredRange(1, 9),
				new OptionData(OptionType.INTEGER, "xp", "Target slayer xp").setMinValue(1),
				new OptionData(OptionType.STRING, "system", "Weight system that should be used")
					.addChoice("Senither", "senither")
					.addChoice("Lily", "lily")
			)
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
