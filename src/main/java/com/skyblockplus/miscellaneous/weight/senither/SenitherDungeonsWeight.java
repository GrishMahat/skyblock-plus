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

package com.skyblockplus.miscellaneous.weight.senither;

import com.skyblockplus.miscellaneous.weight.weight.DungeonsWeight;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.structs.SkillsStruct;
import com.skyblockplus.utils.structs.WeightStruct;

import static com.skyblockplus.utils.Constants.CATACOMBS_LEVEL_50_XP;
import static com.skyblockplus.utils.utils.JsonUtils.getWeightJson;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;

public class SenitherDungeonsWeight extends DungeonsWeight {

	public SenitherDungeonsWeight(Player.Profile player) {
		super(player);
	}

	public WeightStruct getClassWeight(String className) {
		SkillsStruct dungeonSkill = player.getDungeonClass(className);
		double currentClassLevel = dungeonSkill.getProgressLevel();
		double currentClassXp = dungeonSkill.totalExp();
		double base =
			Math.pow(currentClassLevel, 4.5) * higherDepth(getWeightJson(), "senither.dungeons.classes." + className).getAsDouble();

		if (currentClassXp <= CATACOMBS_LEVEL_50_XP) {
			return weightStruct.add(new WeightStruct(base));
		}

		double remaining = currentClassXp - CATACOMBS_LEVEL_50_XP;
		double splitter = (4 * CATACOMBS_LEVEL_50_XP) / base;
		return weightStruct.add(new WeightStruct(Math.floor(base), Math.pow(remaining / splitter, 0.968)));
	}

	@Override
	public WeightStruct getDungeonWeight() {
		return getDungeonWeight(player.getCatacombs());
	}

	@Override
	public WeightStruct getDungeonWeight(SkillsStruct catacombs) {
		double catacombsSkillXp = catacombs.totalExp();
		double level = catacombs.getProgressLevel();
		double base = Math.pow(level, 4.5) * higherDepth(getWeightJson(), "senither.dungeons.catacombs").getAsDouble();

		if (catacombsSkillXp <= CATACOMBS_LEVEL_50_XP) {
			return weightStruct.add(new WeightStruct(base));
		}

		double remaining = catacombsSkillXp - CATACOMBS_LEVEL_50_XP;
		double splitter = (4 * CATACOMBS_LEVEL_50_XP) / base;
		return weightStruct.add(new WeightStruct(Math.floor(base), Math.pow(remaining / splitter, 0.968)));
	}
}
