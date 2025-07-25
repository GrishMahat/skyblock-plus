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

package com.skyblockplus.miscellaneous.weight.lily;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skyblockplus.miscellaneous.weight.weight.DungeonsWeight;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.structs.SkillsStruct;
import com.skyblockplus.utils.structs.WeightStruct;

import java.util.Map;

import static com.skyblockplus.utils.Constants.CATACOMBS_LEVEL_50_XP;
import static com.skyblockplus.utils.utils.JsonUtils.getWeightJson;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;

public class LilyDungeonsWeight extends DungeonsWeight {

	public LilyDungeonsWeight(Player.Profile player) {
		super(player);
	}

	@Override
	public WeightStruct getDungeonWeight() {
		return getDungeonWeight(player.getCatacombs());
	}

	@Override
	public WeightStruct getDungeonWeight(SkillsStruct catacombs) {
		double level = catacombs.getProgressLevel();
		long cataXP = catacombs.totalExp();

		double extra = 0;
		double n = 0;
		if (cataXP < 569809640) {
			n = 0.2 * Math.pow(level / 50, 1.538679118869934);
		} else {
			extra = 500.0 * Math.pow((cataXP - CATACOMBS_LEVEL_50_XP) / 142452410.0, 1.0 / 1.781925776625157);
		}

		if (level != 0) {
			if (cataXP < 569809640) {
				return weightStruct.add(
					new WeightStruct(
						higherDepth(getWeightJson(), "lily.dungeons.overall").getAsDouble() *
						((Math.pow(1.18340401286164044, (level + 1)) - 1.05994990217254) * (1 + n))
					)
				);
			} else {
				return weightStruct.add(new WeightStruct((4100 + extra) * 2));
			}
		} else {
			return new WeightStruct();
		}
	}

	public WeightStruct getDungeonCompletionWeight(String cataMode) {
		JsonObject dungeonCompletionWorth = higherDepth(getWeightJson(), "lily.dungeons.completion_worth").getAsJsonObject();
		JsonObject dungeonCompletionBuffs = higherDepth(getWeightJson(), "lily.dungeons.completion_buffs").getAsJsonObject();

		double max1000 = 0;
		double mMax1000 = 0;
		for (Map.Entry<String, JsonElement> dcwEntry : dungeonCompletionWorth.entrySet()) {
			if (dcwEntry.getKey().startsWith("catacombs_")) {
				max1000 += dcwEntry.getValue().getAsDouble();
			} else {
				mMax1000 += dcwEntry.getValue().getAsDouble();
			}
		}
		max1000 *= 1000;
		mMax1000 *= 1000;

		double upperBound = 1500;
		double score = 0;

		if (cataMode.equals("normal")) {
			if (higherDepth(player.profileJson(), "dungeons.dungeon_types.catacombs.tier_completions") == null) {
				return new WeightStruct();
			}

			for (Map.Entry<String, JsonElement> normalFloor : higherDepth(
				player.profileJson(),
				"dungeons.dungeon_types.catacombs.tier_completions"
			)
				.getAsJsonObject()
				.entrySet()) {
				if (higherDepth(dungeonCompletionWorth, "catacombs_" + normalFloor.getKey()) != null) {
					int amount = normalFloor.getValue().getAsInt();
					double excess = 0;
					if (amount > 1000) {
						excess = amount - 1000;
						amount = 1000;
					}

					double floorScore = amount * dungeonCompletionWorth.get("catacombs_" + normalFloor.getKey()).getAsDouble();
					if (excess > 0) floorScore *= Math.log(excess / 1000 + 1) / Math.log(7.5) + 1;
					score += floorScore;
				}
			}

			return weightStruct.add(new WeightStruct((score / max1000) * upperBound * 2));
		} else {
			if (higherDepth(player.profileJson(), "dungeons.dungeon_types.master_catacombs.tier_completions") == null) {
				return new WeightStruct();
			}

			for (Map.Entry<String, JsonElement> masterFloor : higherDepth(
				player.profileJson(),
				"dungeons.dungeon_types.master_catacombs.tier_completions"
			)
				.getAsJsonObject()
				.entrySet()) {
				if (higherDepth(dungeonCompletionBuffs, masterFloor.getKey()) != null) {
					int amount = masterFloor.getValue().getAsInt();
					double threshold = 20;
					if (amount >= threshold) {
						upperBound += higherDepth(dungeonCompletionBuffs, masterFloor.getKey()).getAsInt();
					} else {
						upperBound +=
						higherDepth(dungeonCompletionBuffs, masterFloor.getKey()).getAsInt() * Math.pow((amount / threshold), 1.840896416);
					}
				}

				if (higherDepth(dungeonCompletionWorth, "master_catacombs_" + masterFloor.getKey()) != null) {
					int amount = masterFloor.getValue().getAsInt();
					double excess = 0;
					if (amount > 1000) {
						excess = amount - 1000;
						amount = 1000;
					}

					double floorScore = amount * dungeonCompletionWorth.get("master_catacombs_" + masterFloor.getKey()).getAsDouble();
					if (excess > 0) {
						floorScore *= (Math.log((excess / 1000) + 1) / Math.log(6)) + 1;
					}
					score += floorScore;
				}
			}

			return weightStruct.add(new WeightStruct((score / mMax1000) * upperBound * 2));
		}
	}
}
