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

package com.skyblockplus.dungeons;

import com.skyblockplus.miscellaneous.weight.weight.Weight;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.PaginatorExtras;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import com.skyblockplus.utils.structs.SkillsStruct;
import com.skyblockplus.utils.structs.WeightStruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import static com.skyblockplus.utils.Constants.profilesCommandOption;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.formatNumber;
import static com.skyblockplus.utils.utils.StringUtils.roundAndFormat;
import static com.skyblockplus.utils.utils.Utils.errorEmbed;

@Component
public class CalcRunsSlashCommand extends SlashCommand {

	public CalcRunsSlashCommand() {
		this.name = "calcruns";
	}

	public static Object getCalcRuns(
		String username,
		String profileName,
		int targetLevel,
		int floor,
		Player.WeightType weightType,
		SlashCommandEvent event
	) {
		if (targetLevel <= 0 || targetLevel > 50) {
			return errorEmbed("Target level must be between 1 and 50");
		}
		if (floor < 0 || floor > 14) {
			return errorEmbed("Invalid floor");
		}

		Player.Profile player = Player.create(username, profileName);
		if (player.isValid()) {
			EmbedBuilder regEmbed = getCalcRunsEmbed(player, targetLevel, floor, false, weightType);
			EmbedBuilder ringEmbed = getCalcRunsEmbed(player, targetLevel, floor, true, weightType)
				.setDescription("**Note:** Calculating with catacombs expert ring");

			event.paginate(
				event
					.getPaginator(PaginatorExtras.PaginatorType.EMBED_PAGES)
					.showPageNumbers(false)
					.updateExtras(extras ->
						extras
							.addEmbedPage(regEmbed)
							.addReactiveButtons(
								new PaginatorExtras.ReactiveButton(
									Button.primary("reactive_calc_runs_ring", "Calculate With Catacombs Expert Ring"),
									action -> {
										action
											.paginator()
											.getExtras()
											.setEmbedPages(ringEmbed)
											.toggleReactiveButton("reactive_calc_runs_ring", false)
											.toggleReactiveButton("reactive_calc_runs_reg", true);
									},
									true
								),
								new PaginatorExtras.ReactiveButton(
									Button.primary("reactive_calc_runs_reg", "Calculate Without Catacombs Expert Ring"),
									action -> {
										action
											.paginator()
											.getExtras()
											.setEmbedPages(regEmbed)
											.toggleReactiveButton("reactive_calc_runs_ring", true)
											.toggleReactiveButton("reactive_calc_runs_reg", false);
									},
									false
								)
							)
					)
			);
			return null;
		}

		return player.getErrorEmbed();
	}

	public static EmbedBuilder getCalcRunsEmbed(
		Player.Profile player,
		int targetLevel,
		int floor,
		boolean useRing,
		Player.WeightType weightType
	) {
		SkillsStruct current = player.getCatacombs();
		SkillsStruct target = player.skillInfoFromLevel(targetLevel, "catacombs");
		if (current.totalExp() >= target.totalExp()) {
			return errorEmbed("You are already level " + targetLevel);
		}

		int completions = higherDepth(
			player.profileJson(),
			floor > 7
				? "dungeons.dungeon_types.master_catacombs.tier_completions." + (floor - 7)
				: "dungeons.dungeon_types.catacombs.tier_completions." + floor,
			0
		);
		int runs = 0;

		int completionsCap =
			switch (floor) {
				case 0, 1, 2, 3, 4, 5 -> 150;
				case 6 -> 100;
				default -> 50;
			};
		int baseXp =
			switch (floor) {
				case 0 -> 50;
				case 1 -> 80;
				case 2 -> 160;
				case 3 -> 400;
				case 4 -> 1420;
				case 5 -> 2000;
				case 6 -> 4000;
				case 7 -> 20000;
				case 8 -> 10000;
				case 9 -> 15000;
				case 10 -> 36500;
				case 11 -> 48500;
				case 12 -> 70000;
				case 13 -> 100000;
				default -> 300000;
			};

		double xpNeeded = target.totalExp() - current.totalExp();
		for (int i = completions + 1; i <= completionsCap; i++) { // First 0 to completionsCap give different xp per run than after completionsCap
			double xpPerRun = (useRing ? 1.1 : 1.0) * baseXp * (i / 100.0 + 1);
			xpNeeded -= xpPerRun;
			if (xpNeeded <= 0) {
				runs = i;
				break;
			}
		}

		if (xpNeeded > 0) {
			double xpPerRun = (useRing ? 1.1 : 1.0) * baseXp * (completionsCap / 100.0 + 1);
			runs = Math.max(0, completionsCap - completions) + (int) Math.ceil(xpNeeded / xpPerRun);
		}

		Weight weight = Weight.of(weightType, player).calculateWeight("catacombs");
		Weight predictedWeight = Weight.of(weightType, player).calculateWeight("catacombs");
		WeightStruct pre = weight.getDungeonsWeight().getDungeonWeight();
		WeightStruct post = predictedWeight.getDungeonsWeight().getDungeonWeight(target);

		return player
			.defaultPlayerEmbed()
			.addField(
				"Current",
				"Level: " + roundAndFormat(current.getProgressLevel()) + "\nXP: " + formatNumber(current.totalExp()),
				false
			)
			.addField(
				"Target",
				"Level: " +
				target.currentLevel() +
				"\nXP: " +
				formatNumber(target.totalExp()) +
				" (+" +
				formatNumber(target.totalExp() - current.totalExp()) +
				")\n" +
				(floor > 7 ? "M" + (floor - 7) : "F" + floor) +
				" Runs Needed: " +
				formatNumber(runs),
				false
			)
			.addField(
				"Weight Change",
				"Total: " +
				weight.getTotalWeight().getFormatted(false) +
				" ➜ " +
				predictedWeight.getTotalWeight().getFormatted(false) +
				"\nCatacombs: " +
				pre.getFormatted(false) +
				" ➜ " +
				post.getFormatted(false),
				false
			);
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		if (event.invalidPlayerOption()) {
			return;
		}

		event.paginate(
			getCalcRuns(
				event.player,
				event.getOptionStr("profile"),
				event.getOptionInt("level", 1),
				event.getOptionInt("floor", 0),
				Player.WeightType.of(event.getOptionStr("system", "senither")),
				event
			)
		);
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands
			.slash(name, "Calculate the number of runs needed to reach a catacombs level")
			.addOptions(
				new OptionData(OptionType.INTEGER, "level", "Target catacombs level", true).setRequiredRange(1, 50),
				new OptionData(OptionType.INTEGER, "floor", "Catacombs or master catacombs floor", true)
					.addChoice("Entrance", 0)
					.addChoice("Floor 1", 1)
					.addChoice("Floor 2", 2)
					.addChoice("Floor 3", 3)
					.addChoice("Floor 4", 4)
					.addChoice("Floor 5", 5)
					.addChoice("Floor 6", 6)
					.addChoice("Floor 7", 7)
					.addChoice("Master Floor 1", 8)
					.addChoice("Master Floor 2", 9)
					.addChoice("Master Floor 3", 10)
					.addChoice("Master Floor 4", 11)
					.addChoice("Master Floor 5", 12)
					.addChoice("Master Floor 6", 13)
					.addChoice("Master Floor 7", 14),
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
