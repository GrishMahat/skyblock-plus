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

package com.skyblockplus.features.mayor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.skyblockplus.features.listeners.AutomaticGuild;
import com.skyblockplus.miscellaneous.CalendarSlashCommand;
import com.skyblockplus.utils.ApiHandler;
import com.skyblockplus.utils.utils.HttpUtils;
import com.skyblockplus.utils.utils.JsonUtils;
import com.skyblockplus.utils.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.skyblockplus.features.listeners.MainListener.guildMap;
import static com.skyblockplus.miscellaneous.CalendarSlashCommand.YEAR_0;
import static com.skyblockplus.miscellaneous.CalendarSlashCommand.getSkyblockYear;
import static com.skyblockplus.utils.ApiHandler.getHypixelApiUrl;
import static com.skyblockplus.utils.Constants.MAYOR_NAME_TO_SKIN;
import static com.skyblockplus.utils.Constants.mayorNameToEmoji;
import static com.skyblockplus.utils.utils.HttpUtils.getJson;
import static com.skyblockplus.utils.utils.JsonUtils.*;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.*;

public class MayorHandler {
  private static final Logger log = LoggerFactory.getLogger(MayorHandler.class);

	public static String currentMayor = "";
	public static String currentJerryMayor = "";
	public static int currentMayorYear = 0;
	public static ScheduledFuture<?> jerryFuture;
	public static ScheduledFuture<?> mayorElectedFuture;
	public static MessageEmbed jerryEmbed = errorEmbed("Jerry is not currently mayor").build();

	public static void initialize() {
		try {
			if (currentMayor.isEmpty()) {
				JsonElement mayorJson = getJson(getHypixelApiUrl("/v2/resources/skyblock/election", false));
				currentMayor = higherDepth(mayorJson, "mayor.name", "");
				currentMayorYear = higherDepth(mayorJson, "mayor.election.year", 0);
			}

			long msTillElected = YEAR_0 + 446400000L * (getSkyblockYear() - 1) + 105600000 - Instant.now().toEpochMilli() + 300000;
			if (mayorElectedFuture == null && msTillElected > 0) {
				mayorElectedFuture = scheduler.schedule(MayorHandler::mayorElected, msTillElected, TimeUnit.MILLISECONDS);
			}

			updateCurrentElection();


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void mayorElected() {
		MessageEmbed embed;
		Button button = null;

		try {
			JsonElement cur = higherDepth(getJson(getHypixelApiUrl("/v2/resources/skyblock/election", false)), "mayor");
			JsonArray mayors = collectJsonArray(
				streamJsonArray(higherDepth(cur, "election.candidates"))
					.sorted(Comparator.comparingInt(m -> -higherDepth(m, "votes").getAsInt()))
			);

			currentMayor = higherDepth(cur, "name").getAsString();
			currentMayorYear = higherDepth(cur, "election.year").getAsInt();
			double totalVotes = streamJsonArray(mayors).mapToInt(m -> higherDepth(m, "votes").getAsInt()).sum();

			EmbedBuilder eb = defaultEmbed("Mayor Elected | Year " + currentMayorYear);
			eb.setDescription("**Year:** " + currentMayorYear + "\n**Total Votes:** " + formatNumber(totalVotes));
			eb.setThumbnail("https://mc-heads.net/body/" + MAYOR_NAME_TO_SKIN.get(currentMayor.toUpperCase()) + "/left");
			StringBuilder ebStr = new StringBuilder();
			for (JsonElement curMayor : mayors) {
				String name = higherDepth(curMayor, "name").getAsString();
				int votes = higherDepth(curMayor, "votes").getAsInt();

				if (higherDepth(curMayor, "name").getAsString().equals(currentMayor)) {
					eb.addField(
						mayorNameToEmoji.get(name.toUpperCase()) +
						" Mayor " +
						name +
						" | " +
						roundProgress(votes / totalVotes) +
						" (" +
						formatNumber(votes) +
						")",
						streamJsonArray(higherDepth(curMayor, "perks"))
							.map(e ->
								"\n➜ " +
								higherDepth(e, "name").getAsString() +
								": " +
								cleanMcCodes(higherDepth(e, "description").getAsString())
							)
							.collect(Collectors.joining()),
						false
					);
				} else {
					ebStr
						.append("\n")
						.append(mayorNameToEmoji.get(name.toUpperCase()))
						.append(" **")
						.append(name)
						.append(":** ")
						.append(roundProgress(votes / totalVotes))
						.append(" (")
						.append(formatNumber(votes))
						.append(")");
				}
			}
			eb.addField("Losing Mayors", ebStr.toString(), false);
			eb.addField("Next Election", "Opens " + getRelativeTimestamp(YEAR_0 + 446400000L * (getSkyblockYear() - 1) + 217200000), false);

			embed = eb.build();
			if (currentMayor.equals("Jerry")) {
				button = Button.primary("mayor_jerry_button", "Current Jerry Mayor");
			}
		} catch (Exception e) {
			e.printStackTrace();
			scheduler.schedule(MayorHandler::mayorElected, 5, TimeUnit.MINUTES);
			return;
		}

		try {
			int updateCount = 0;
			for (AutomaticGuild guild : guildMap.values()) {
				if (guild.onMayorElected(embed, button)) { // Send and ping
					updateCount++;
				}

				if (updateCount != 0 && updateCount % 12 == 0) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (Exception ignored) {}
				}
			}

			scheduler.schedule(() -> mayorElectedFuture = null, 30, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	public static void updateCurrentElection() {
		try {
			JsonElement response = getJson(getHypixelApiUrl("/v2/resources/skyblock/election", false));
			if (response == null) {
				log.error("Failed to fetch election data from Hypixel API");
				return;
			}

			JsonElement cur = higherDepth(response, "current");
			if (cur == null) {
				log.warn("No current election data found");
				return;
			}
			if (higherDepth(cur, "candidates") == null) { // Election not open
				return;
			}

			JsonElement candidatesElement = higherDepth(cur, "candidates");
			if (!(candidatesElement instanceof JsonArray)) {
				log.warn("Candidates data is not an array");
				return;
			}
			JsonArray candidates = candidatesElement.getAsJsonArray();
			if (candidates == null) {
				log.warn("No candidates found in election data");
				return;
			}

			JsonArray curMayors = collectJsonArray(
				streamJsonArray(candidates)
					.filter(m -> higherDepth(m, "votes") != null)
					.sorted(Comparator.comparingInt(m -> {
						JsonElement votes = higherDepth(m, "votes");
						return votes != null ? -votes.getAsInt() : 0;
					}))
			);

			if (curMayors.isEmpty()) {
				log.warn("No valid mayor candidates found");
				return;
			}

			double totalVotes = streamJsonArray(curMayors)
				.mapToInt(m -> {
					JsonElement votes = higherDepth(m, "votes");
					return votes != null ? votes.getAsInt() : 0;
				})
				.sum();

			JsonElement yearElement = higherDepth(cur, "year");
			if (yearElement == null) {
				log.warn("No year found in election data");
				return;
			}
			int year = yearElement.getAsInt();
			EmbedBuilder eb = defaultEmbed("Mayor Election Open | Year " + year);
			eb.setDescription(
				"**Year:** " +
				year +
				"\n**Total Votes:** " +
				formatNumber(totalVotes) +
				"\n**Closes:** " +
				getRelativeTimestamp(
					YEAR_0 + 446400000L * (year == getSkyblockYear() ? getSkyblockYear() : getSkyblockYear() - 1) + 105600000
				)
			);
			for (JsonElement curMayor : curMayors) {
				StringBuilder perksStr = new StringBuilder();
				for (JsonElement perk : higherDepth(curMayor, "perks").getAsJsonArray()) {
					perksStr
						.append("\n➜ ")
						.append(higherDepth(perk, "name").getAsString())
						.append(": ")
						.append(cleanMcCodes(higherDepth(perk, "description").getAsString()));
				}

				int votes = higherDepth(curMayor, "votes").getAsInt();
				String name = higherDepth(curMayor, "name").getAsString();
				eb.addField(
					mayorNameToEmoji.get(name.toUpperCase()) +
					" " +
					name +
					" | " +
					formatNumber(votes) +
					" (" +
					roundProgress(votes / totalVotes) +
					")",
					perksStr.toString(),
					false
				);
			}

			File mayorGraphFile = null;
			try {
				mayorGraphFile = new File(rendersDirectory + "/mayor_graph.png");
				ImageIO.write(
					ImageIO.read(
						new URIBuilder("https://quickchart.io/chart")
							.addParameter("bkg", "#2b2d31")
							.addParameter(
								"c",
								"{ type: 'bar', data: { labels: [" +
								streamJsonArray(curMayors)
									.map(m -> "'" + higherDepth(m, "name").getAsString() + "'")
									.collect(Collectors.joining(",")) +
								"], datasets: [{ data: [" +
								streamJsonArray(curMayors)
									.map(m -> higherDepth(m, "votes").getAsString())
									.collect(Collectors.joining(",")) +
								"], backgroundColor: getGradientFillHelper('vertical', [\"#023020\"," +
								" \"#32CD32\"]), }] }, options: { title: { display: true, text:" +
								" 'Mayor Election Graph | Year " +
								year +
								"' }, legend: { display: false, } } }"
							)
							.build()
							.toURL()
					),
					"png",
					mayorGraphFile
				);
			} catch (Exception ignored) {}

			MessageEmbed embed;
			if (mayorGraphFile == null || !mayorGraphFile.exists()) {
				embed = eb.build();
			} else {
				embed = eb.setImage("attachment://mayor_graph.png").build();
			}

			int updateCount = 0;
			for (AutomaticGuild guild : guildMap.values()) {
				if (guild.onMayorElection(embed, mayorGraphFile, year)) { // Send or update message
					updateCount++;
				}

				if (updateCount != 0 && updateCount % 12 == 0) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (Exception ignored) {}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  public static EmbedBuilder getMayorEmbed() {
        JsonElement response = HttpUtils.getJson(ApiHandler.getHypixelApiUrl("/v2/resources/skyblock/election", false));
        EmbedBuilder eb = new EmbedBuilder();

        if (response == null) {
            eb.setDescription("Failed to fetch election data from Hypixel API.");
            return eb;
        }

        JsonElement mayorData = JsonUtils.higherDepth(response, "mayor");
        JsonElement electionData = JsonUtils.higherDepth(response, "mayor.election");
        JsonElement currentElectionData = JsonUtils.higherDepth(response, "current");

        if (mayorData != null) {
            String mayorName = JsonUtils.higherDepth(mayorData, "name").getAsString();
            eb.setTitle("\uD83D\uDC51 Current Mayor: " + mayorName);

            StringBuilder perksStr = new StringBuilder();
            if (JsonUtils.higherDepth(mayorData, "perks") instanceof JsonArray perksArray) {
                for (JsonElement perk : perksArray) {
                    perksStr.append("\n\u279C ")
                            .append(JsonUtils.higherDepth(perk, "name").getAsString())
                            .append(": ")
                            .append(StringUtils.cleanMcCodes(JsonUtils.higherDepth(perk, "description").getAsString()));
                }
            }
            eb.addField("Perks", perksStr.toString(), false);
        }

        if (electionData != null) {
            JsonArray candidates = JsonUtils.higherDepth(electionData, "candidates").getAsJsonArray();
            for (JsonElement candidate : candidates) {
                JsonArray perks = JsonUtils.higherDepth(candidate, "perks").getAsJsonArray();
                for (JsonElement perk : perks) {
                    if (JsonUtils.higherDepth(perk, "minister") != null && JsonUtils.higherDepth(perk, "minister").getAsBoolean()) {
                        String ministerName = JsonUtils.higherDepth(candidate, "name").getAsString();
                        eb.addField("\uD83D\uDCBC Minister: " + ministerName, "\u279C " + JsonUtils.higherDepth(perk, "name").getAsString() + ": " + StringUtils.cleanMcCodes(JsonUtils.higherDepth(perk, "description").getAsString()), false);
                    }
                }
            }
        }

        if (currentElectionData != null && currentElectionData.isJsonObject()) {
            eb.addField("Election Status", "\uD83D\uDDF3 Ongoing", false);
            if (JsonUtils.higherDepth(currentElectionData, "candidates") instanceof JsonArray candidatesArray) {
                for (JsonElement candidate : candidatesArray) {
                    String candidateName = JsonUtils.higherDepth(candidate, "name").getAsString();
                    int votes = JsonUtils.higherDepth(candidate, "votes").getAsInt();
                    StringBuilder candidatePerksStr = new StringBuilder();
                    if (JsonUtils.higherDepth(candidate, "perks") instanceof JsonArray candidatePerksArray) {
                        for (JsonElement perk : candidatePerksArray) {
                            candidatePerksStr.append("\n\u279C ")
                                    .append(JsonUtils.higherDepth(perk, "name").getAsString())
                                    .append(": ")
                                    .append(StringUtils.cleanMcCodes(JsonUtils.higherDepth(perk, "description").getAsString()));
                        }
                    }
                    eb.addField(candidateName + " (" + StringUtils.formatNumber(votes) + " votes)", candidatePerksStr.toString(), false);
                }
            }
        } else {
            eb.addField("Election Status", "No ongoing election", false);
        }

        return eb;
    }

        public static EmbedBuilder getSpecialMayors() {
        long newYearToElectionOpen = 217200000;
        long newYearToElectionClose = 105600000;
        int year = getSkyblockYear();
        int nextSpecial = year % 8 == 0 ? year : ((year + 8) - (year % 8));

        String[] mayorNames = new String[]{"Scorpius", "Derpy", "Jerry"};
        EmbedBuilder eb = defaultEmbed("Next Special Mayors");
        for (int i = nextSpecial; i < nextSpecial + 24; i += 8) {
            int mayorIndex = 0;
            if ((i - 8) % 24 == 0) {
                mayorIndex = 1;
            } else if ((i - 16) % 24 == 0) {
                mayorIndex = 2;
            }
            eb.addField(
                    mayorNameToEmoji.get(mayorNames[mayorIndex].toUpperCase()) + " " + mayorNames[mayorIndex],
                    "Election Opens: " +
                            getRelativeTimestamp((YEAR_0 + CalendarSlashCommand.YEAR_MS * (i - 1)) + newYearToElectionOpen) +
                            "\nElection Closes: " +
                            getRelativeTimestamp((YEAR_0 + CalendarSlashCommand.YEAR_MS * (i)) + newYearToElectionClose),
                    false
            );
        }
        return eb;
    }


}
