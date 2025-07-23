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

package com.skyblockplus.features.event;

import com.skyblockplus.features.listeners.AutomaticGuild;
import com.skyblockplus.miscellaneous.CalendarSlashCommand;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.skyblockplus.features.listeners.MainListener.guildMap;
import static com.skyblockplus.features.mayor.MayorHandler.currentJerryMayor;
import static com.skyblockplus.features.mayor.MayorHandler.currentMayor;
import static com.skyblockplus.utils.utils.StringUtils.capitalizeString;
import static com.skyblockplus.utils.utils.StringUtils.getRelativeTimestamp;
import static com.skyblockplus.utils.utils.Utils.*;

public class EventHandler {

	public static final List<Long> eventTimers = new ArrayList<>();

	public static void initialize() {
		if (eventTimers.size() != 24) {
			setEventTimers(Collections.nCopies(24, Instant.now().toEpochMilli()));
		}

		if (IS_DEV) {
			return;
		}

		scheduler.scheduleAtFixedRate(EventHandler::updateEvents, 30, 30, TimeUnit.SECONDS);
	}

	public static void setEventTimers(List<Long> eventTimers) {
		if (eventTimers != null) {
			EventHandler.eventTimers.clear();
			EventHandler.eventTimers.addAll(eventTimers);
		}
	}

	public static void updateEvents() {
		try {
			ZonedDateTime nowDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
			Map<String, MessageEmbed> ebs = new HashMap<>();
			getEventEmbeds(nowDateTime, 0);
			ebs.putAll(getEventEmbeds(nowDateTime, 5));

			if (!ebs.isEmpty()) {
				for (AutomaticGuild guild : guildMap.values()) {
					guild.onEventNotif(ebs);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, MessageEmbed> getEventEmbeds(ZonedDateTime nowDateTime, int timeBefore) {
		// 0 - bingo start
		// 1 - bingo end
		// 2 - zoo early summer
		// 3 - zoo early winter
		// 4 - winter island open
		// 5 - dark auction open
		// 6 - new year celebration starts
		// 7 - spooky fishing starts
		// 8 - spooky event starts
		// 9 - fishing festival start
		// 10 - fallen star
		// 11 - bank interest

		nowDateTime = nowDateTime.plusMinutes(timeBefore);
		int index = timeBefore == 0 ? 0 : eventTimers.size() / 2;
		Map<String, MessageEmbed> ebs = new HashMap<>();

		long nowEpoch = nowDateTime.toInstant().toEpochMilli();
		LocalDate now = nowDateTime.toLocalDate();
		ZoneId z = nowDateTime.getZone();

		Instant startOfBingo = now.withDayOfMonth(1).atStartOfDay(z).toInstant();
		if (
			startOfBingo.toEpochMilli() <= nowEpoch &&
			nowEpoch <= startOfBingo.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < startOfBingo.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"bingo_start",
				defaultEmbed("Bingo Start").setDescription("Bingo starts " + getRelativeTimestamp(startOfBingo)).build()
			);
		} else if (nowEpoch > startOfBingo.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < startOfBingo.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"bingo_start",
				defaultEmbed("Bingo Start").setDescription("Bingo starts " + getRelativeTimestamp(startOfBingo)).build()
			);
		}

		index++;
		Instant endOfBingo = now.withDayOfMonth(1).atStartOfDay(z).toInstant().plus(7, ChronoUnit.DAYS);
		if (
			endOfBingo.toEpochMilli() <= nowEpoch &&
			nowEpoch <= endOfBingo.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < endOfBingo.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put("bingo_end", defaultEmbed("Bingo End").setDescription("Bingo ends " + getRelativeTimestamp(endOfBingo)).build());
		} else if (nowEpoch > endOfBingo.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < endOfBingo.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put("bingo_end", defaultEmbed("Bingo End").setDescription("Bingo ends " + getRelativeTimestamp(endOfBingo)).build());
		}

		index++;
		String[] pets = new String[] { "ELEPHANT;4", "GIRAFFE;4", "BLUE_WHALE;4", "TIGER;4", "LION;4", "MONKEY;4" };
		int zooIndex = 0;
		if ((CalendarSlashCommand.getSkyblockYear() - 2) % 3 == 0) {
			zooIndex = 2;
		} else if ((CalendarSlashCommand.getSkyblockYear() - 3) % 3 == 0) {
			zooIndex = 4;
		}
		Instant zooEarlySummer = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			3 * CalendarSlashCommand.MONTH_MS
		);
		if (
			zooEarlySummer.toEpochMilli() <= nowEpoch &&
			nowEpoch <= zooEarlySummer.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < zooEarlySummer.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"zoo",
				defaultEmbed("Traveling Zoo")
					.setDescription(
						"Traveling zoo opens " +
						getRelativeTimestamp(zooEarlySummer) +
						"\nLegendary Pet: " +
						getEmoji(pets[zooIndex]) +
						" " +
						capitalizeString(pets[zooIndex].split(";")[0].replace("_", " "))
					)
					.build()
			);
		} else if (nowEpoch > zooEarlySummer.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < zooEarlySummer.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"zoo",
				defaultEmbed("Traveling Zoo")
					.setDescription(
						"Traveling zoo opens " +
						getRelativeTimestamp(zooEarlySummer) +
						"\nLegendary Pet: " +
						getEmoji(pets[zooIndex]) +
						" " +
						capitalizeString(pets[zooIndex].split(";")[0].replace("_", " "))
					)
					.build()
			);
		}

		index++;
		Instant zooEarlyWinter = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			9 * CalendarSlashCommand.MONTH_MS
		);
		if (
			zooEarlyWinter.toEpochMilli() <= nowEpoch &&
			nowEpoch <= zooEarlyWinter.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < zooEarlyWinter.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"zoo",
				defaultEmbed("Traveling Zoo")
					.setDescription(
						"Traveling zoo opens " +
						getRelativeTimestamp(zooEarlyWinter) +
						"\nLegendary pet: " +
						getEmoji(pets[zooIndex + 1]) +
						" " +
						capitalizeString(pets[zooIndex + 1].split(";")[0].replace("_", " "))
					)
					.build()
			);
		} else if (nowEpoch > zooEarlyWinter.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < zooEarlyWinter.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"zoo",
				defaultEmbed("Traveling Zoo")
					.setDescription(
						"Traveling zoo opens " +
						getRelativeTimestamp(zooEarlyWinter) +
						"\nLegendary pet: " +
						getEmoji(pets[zooIndex + 1]) +
						" " +
						capitalizeString(pets[zooIndex + 1].split(";")[0].replace("_", " "))
					)
					.build()
			);
		}

		index++;
		Instant jerryIslandOpen = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			11 * CalendarSlashCommand.MONTH_MS
		);
		if (
			jerryIslandOpen.toEpochMilli() <= nowEpoch &&
			nowEpoch <= jerryIslandOpen.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < jerryIslandOpen.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"winter_island",
				defaultEmbed("Winter Island").setDescription("Winter island opens " + getRelativeTimestamp(jerryIslandOpen)).build()
			);
		} else if (nowEpoch > jerryIslandOpen.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < jerryIslandOpen.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"winter_island",
				defaultEmbed("Winter Island").setDescription("Winter island opens " + getRelativeTimestamp(jerryIslandOpen)).build()
			);
		}

		index++;
		Instant darkAuctionOpen = nowDateTime.withMinute(55).withSecond(0).toInstant();
		if (
			darkAuctionOpen.toEpochMilli() <= nowEpoch &&
			nowEpoch <= darkAuctionOpen.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < darkAuctionOpen.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"dark_auction",
				defaultEmbed("Dark Auction").setDescription("Dark auction opens " + getRelativeTimestamp(darkAuctionOpen)).build()
			);
		} else if (nowEpoch > darkAuctionOpen.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < darkAuctionOpen.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"dark_auction",
				defaultEmbed("Dark Auction").setDescription("Dark auction opens " + getRelativeTimestamp(darkAuctionOpen)).build()
			);
		}

		index++;
		Instant newYearEvent = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			11 * CalendarSlashCommand.MONTH_MS +
			28 * CalendarSlashCommand.DAY_MS
		);
		if (
			newYearEvent.toEpochMilli() <= nowEpoch &&
			nowEpoch <= newYearEvent.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < newYearEvent.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"new_year",
				defaultEmbed("New Year Celebration")
					.setDescription("New year celebration starts " + getRelativeTimestamp(newYearEvent))
					.build()
			);
		} else if (nowEpoch > newYearEvent.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < newYearEvent.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"new_year",
				defaultEmbed("New Year Celebration")
					.setDescription("New year celebration starts " + getRelativeTimestamp(newYearEvent))
					.build()
			);
		}

		index++;
		Instant spookyFishing = Instant
			.ofEpochMilli(
				CalendarSlashCommand.YEAR_0 +
				(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
				7 * CalendarSlashCommand.MONTH_MS +
				28 * CalendarSlashCommand.DAY_MS
			)
			.minus(1, ChronoUnit.HOURS);
		if (
			spookyFishing.toEpochMilli() <= nowEpoch &&
			nowEpoch <= spookyFishing.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < spookyFishing.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"spooky_fishing",
				defaultEmbed("Spooky Fishing").setDescription("Spooky fishing starts " + getRelativeTimestamp(spookyFishing)).build()
			);
		} else if (nowEpoch > spookyFishing.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < spookyFishing.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"spooky_fishing",
				defaultEmbed("Spooky Fishing").setDescription("Spooky fishing starts " + getRelativeTimestamp(spookyFishing)).build()
			);
		}

		index++;
		Instant spookyEvent = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			7 * CalendarSlashCommand.MONTH_MS +
			28 * CalendarSlashCommand.DAY_MS
		);
		if (
			spookyEvent.toEpochMilli() <= nowEpoch &&
			nowEpoch <= spookyEvent.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < spookyEvent.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"spooky",
				defaultEmbed("Spooky Festival").setDescription("Spooky festival starts " + getRelativeTimestamp(spookyEvent)).build()
			);
		} else if (nowEpoch > spookyEvent.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < spookyEvent.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"spooky",
				defaultEmbed("Spooky Festival").setDescription("Spooky festival starts " + getRelativeTimestamp(spookyEvent)).build()
			);
		}

		index++;
		if (
			currentMayor.equalsIgnoreCase("marina") ||
			(currentMayor.equalsIgnoreCase("Jerry") && currentJerryMayor.equalsIgnoreCase("marina"))
		) {
			Instant fishingFestival = Instant.ofEpochMilli(
				CalendarSlashCommand.YEAR_0 +
				(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
				Math.floorDiv((nowEpoch - CalendarSlashCommand.YEAR_0) % CalendarSlashCommand.YEAR_MS, CalendarSlashCommand.MONTH_MS) *
					CalendarSlashCommand.MONTH_MS
			);
			if (
				fishingFestival.toEpochMilli() <= nowEpoch &&
				nowEpoch <= fishingFestival.plusSeconds(60).toEpochMilli() &&
				eventTimers.get(index) < fishingFestival.toEpochMilli()
			) {
				eventTimers.set(index, nowEpoch);
				ebs.put(
					"fishing_festival",
					defaultEmbed("Fishing Festival")
						.setDescription("Fishing festival starts " + getRelativeTimestamp(fishingFestival))
						.build()
				);
			} else if (
				nowEpoch > fishingFestival.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < fishingFestival.toEpochMilli()
			) {
				eventTimers.set(index, nowEpoch);
				ebs.put(
					"fishing_festival",
					defaultEmbed("Fishing Festival")
						.setDescription("Fishing festival starts " + getRelativeTimestamp(fishingFestival))
						.build()
				);
			}
		}

		index++;
		long currentOffset = (nowEpoch - CalendarSlashCommand.YEAR_0) % CalendarSlashCommand.YEAR_MS;
		int currentMonth = (int) Math.floorDiv(currentOffset, CalendarSlashCommand.MONTH_MS);
		int currentDay = (int) Math.floorDiv(
			(currentOffset - (long) currentMonth * CalendarSlashCommand.MONTH_MS) % CalendarSlashCommand.MONTH_MS,
			CalendarSlashCommand.DAY_MS
		);
		int out = 7;
		if (currentDay > 21) {
			out = 28;
		} else if (currentDay > 14) {
			out = 21;
		} else if (currentDay > 7) {
			out = 14;
		}
		Instant fallenStar = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			currentMonth * CalendarSlashCommand.MONTH_MS +
			(out - 1) * CalendarSlashCommand.DAY_MS
		);
		if (
			fallenStar.toEpochMilli() <= nowEpoch &&
			nowEpoch <= fallenStar.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < fallenStar.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"fallen_star",
				defaultEmbed("Cult Of Fallen Star")
					.setDescription("Cult of fallen star arrives " + getRelativeTimestamp(fallenStar))
					.build()
			);
		} else if (nowEpoch > fallenStar.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < fallenStar.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"fallen_star",
				defaultEmbed("Cult Of Fallen Star")
					.setDescription("Cult of fallen star arrives " + getRelativeTimestamp(fallenStar))
					.build()
			);
		}

		index++;
		int currentMonthBank = (int) Math.floorDiv(currentOffset, CalendarSlashCommand.MONTH_MS);
		if (currentMonthBank <= 2) {
			currentMonthBank = 2;
		} else if (currentMonthBank <= 5) {
			currentMonthBank = 5;
		} else if (currentMonthBank <= 8) {
			currentMonthBank = 8;
		} else {
			currentMonthBank = 11;
		}
		Instant bankStart = Instant.ofEpochMilli(
			CalendarSlashCommand.YEAR_0 +
			(CalendarSlashCommand.getSkyblockYear() - 1) * CalendarSlashCommand.YEAR_MS +
			currentMonthBank * CalendarSlashCommand.MONTH_MS +
			31 * CalendarSlashCommand.DAY_MS
		);
		if (
			bankStart.toEpochMilli() <= nowEpoch &&
			nowEpoch <= bankStart.plusSeconds(60).toEpochMilli() &&
			eventTimers.get(index) < bankStart.toEpochMilli()
		) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"bank_interest",
				defaultEmbed("Bank Interest").setDescription("Bank interest is deposited " + getRelativeTimestamp(spookyEvent)).build()
			);
		} else if (nowEpoch > spookyEvent.plusSeconds(60).toEpochMilli() && eventTimers.get(index) < spookyEvent.toEpochMilli()) {
			eventTimers.set(index, nowEpoch);
			ebs.put(
				"bank_interest",
				defaultEmbed("Bank Interest").setDescription("Bank interest is deposited " + getRelativeTimestamp(spookyEvent)).build()
			);
		}

		return ebs;
	}
}
