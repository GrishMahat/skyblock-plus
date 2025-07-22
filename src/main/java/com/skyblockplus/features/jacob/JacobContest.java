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

import static com.skyblockplus.utils.Constants.cropNameToEmoji;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class JacobContest {

	private long time;
	private List<String> crops;

	public boolean reminderHasPassed() {
		try {
			return Instant.now().isAfter(getTimeInstant().minusSeconds(301));
		} catch (Exception e) {
			log.error("Error checking if reminder has passed: " + e.getMessage());
			return true; // Consider it passed if there's an error
		}
	}

	public Instant getTimeInstant() {
		try {
			return Instant.ofEpochMilli(time);
		} catch (Exception e) {
			log.error("Error converting time to Instant: " + e.getMessage());
			return Instant.now(); // Return current time as fallback
		}
	}

	public Duration getDurationUntil() {
		try {
			return Duration.between(Instant.now(), getTimeInstant());
		} catch (Exception e) {
			log.error("Error calculating duration: " + e.getMessage());
			return Duration.ZERO; // Return zero duration as fallback
		}
	}

	public String getCropsFormatted() {
		if (crops == null) {
			log.error("Crops list is null");
			return "No crops available";
		}

		StringBuilder cropsFormatted = new StringBuilder();
		for (String crop : crops) {
			if (crop == null) {
				continue;
			}

			String emoji = cropNameToEmoji.get(crop);
			if (emoji != null) {
				cropsFormatted.append(emoji).append(" ");
			}
			cropsFormatted.append(crop).append("\n");
		}
		return cropsFormatted.length() > 0 ? cropsFormatted.toString() : "No crops available";
	}
}
