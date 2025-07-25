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

package com.skyblockplus.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.skyblockplus.api.serversettings.automatedguild.ApplyRequirement;
import com.skyblockplus.api.serversettings.automatedguild.AutomatedGuild;
import com.skyblockplus.api.serversettings.automatedroles.RoleModel;
import com.skyblockplus.api.serversettings.automatedroles.RoleObject;
import com.skyblockplus.api.serversettings.blacklist.BlacklistEntry;
import com.skyblockplus.api.serversettings.eventnotif.EventObject;
import com.skyblockplus.api.serversettings.managers.ServerSettingsModel;
import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.CustomPaginator;
import com.skyblockplus.utils.command.PaginatorExtras;
import com.skyblockplus.utils.structs.HypixelResponse;
import com.skyblockplus.utils.structs.UsernameUuidStruct;
import com.vdurmont.emoji.EmojiParser;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.apache.groovy.util.Maps;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.skyblockplus.features.listeners.MainListener.guildMap;
import static com.skyblockplus.features.listeners.MainListener.onVerifyReload;
import static com.skyblockplus.utils.ApiHandler.*;
import static com.skyblockplus.utils.Constants.cropNameToEmoji;
import static com.skyblockplus.utils.utils.JsonUtils.*;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.*;

public class SettingsExecute {

	private static final List<String> validLogEvents = List.of("member_verify", "guild_sync", "bot_permission_error");
	private static final List<String> validApplyRequirements = List.of(
		"slayer",
		"skills",
		"catacombs",
		"weight",
		"lily_weight",
		"level",
		"networth",
		"farming_weight"
	);
	private static final List<String> validEvents = List.of(
		"bingo_start",
		"bingo_end",
		"zoo",
		"winter_island",
		"dark_auction",
		"new_year",
		"spooky_fishing",
		"spooky",
		"fishing_festival",
		"fallen_star",
		"bank_interest"
	);
	public static final Map<String, String> allAutomatedRoles = new LinkedHashMap<>(
		Maps.of(
			"wolf",
			null,
			"zombie",
			null,
			"spider",
			null,
			"blaze",
			null,
			"enderman",
			null,
			"vampire",
			"**A player's vampire slayer xp**\nExample: `/settings roles add vampire 2400 @vampire 5`\n",
			"alchemy",
			null,
			"combat",
			null,
			"fishing",
			null,
			"farming",
			null,
			"foraging",
			null,
			"carpentry",
			null,
			"mining",
			null,
			"taming",
			null,
			"enchanting",
			null,
			"social",
			null,
			"runecrafting",
			null,
			"catacombs",
			null,
			"healer",
			null,
			"mage",
			null,
			"berserk",
			null,
			"archer",
			null,
			"tank",
			null,
			"weight",
			"**A player's weight**\nExample: `/settings roles add weight 5000 @5k weight`\n",
			"guild_member",
			"**Member role for Hypixel guilds**\nExample: `/settings roles add guild_member skyblock_forceful @guild member`\n",
			"guild_ranks",
			"**If a player is in a guild setup in `/settings guild`, they will be given the corresponding rank role set there**\nNote: this role can only be linked here. To modify guild ranks use `/settings guild <name>`\n",
			"coins",
			"**Coins in a player's bank and purse**\nExample: `/settings roles add coins 1000000 @millionaire`\n",
			"fairy_souls",
			"**Amount of collected fairy souls**\nExample: `/settings roles add fairy_souls 50 @50 souls collected`\n",
			"minion_slots",
			"**Number of minion slots excluding upgrades**\nExample: `/settings roles add minion_slots 24 @maxed minion slots`\n",
			"total_slayer",
			"**A player's total slayer xp**\nExample: `/settings roles add total_slayer 1000000 @1m slayer`\n",
			"maxed_slayers",
			"**The number of maxed slayers a player has**\nExample: `/settings roles add maxed_slayers 6 @maxed slayers`\n",
			"skill_average",
			"**A player's skill average**\nExample: `/settings roles add skill_average 55 @maxed skills`\n",
			"pet_score",
			"**A player's pet score**\nExample: `/settings roles add pet_score 100 @100 pet score`\n",
			"dungeon_secrets",
			"**A player's dungeon secrets count**\nExample: `/settings roles add dungeon_secrets 25000 @secrets sweat`\n",
			"networth",
			"**A player's networth**\nExample: `/settings roles add networth 1000000000 @billionaire`\n",
			"gamemode",
			"**Playing on an ironman or stranded profile**\nExample: `/settings roles add gamemode stranded @stranded gamer`\n",
			"maxed_collections",
			"**Number of a player's individually maxed collections**\nExample: `/settings roles add maxed_collections 62 @all collections maxed`\n",
			"player_items",
			"**Items that a player has**\nExample: `/settings roles add player_items hyperion @mage gamer`\n",
			"class_average",
			"**A player's average dungeon classes level**\nExample: `/settings roles add class_average 50 @maxed classes`\n",
			"mage_reputation",
			"**A player's mage reputation**\nExample: `/settings roles add mage_reputation 1000 @1k mage rep`\n",
			"barbarian_reputation",
			"**A player's barbarian reputation**\nExample: `/settings roles add barbarian_reputation 1000 @1k barbarian rep`\n",
			"level",
			"**A player's Skyblock level**\nExample: `/settings roles add level 500 @maxed level`\n",
			// BELOW ROLE SHOULD ALWAYS BE LAST IN MAP
			"hotm_3_ironman",
			"Your computer may or may not self destruct\n"
		)
	);

	static {
		for (String roleName : allAutomatedRoles.keySet()) {
			String customDescription =
				switch (roleName) {
					case "wolf", "zombie", "spider", "enderman", "blaze" -> "**A player's " +
					roleName +
					" slayer xp**\nExample: `/settings roles add " +
					roleName +
					" 1000000 @" +
					roleName +
					" 9`\n";
					case "alchemy",
						"combat",
						"fishing",
						"farming",
						"foraging",
						"carpentry",
						"mining",
						"taming",
						"enchanting",
						"social",
						"runecrafting",
						"catacombs",
						"healer",
						"mage",
						"berserk",
						"archer",
						"tank" -> "**A player's " +
					roleName +
					" level**\nExample: `/settings roles add " +
					roleName +
					" 30 @" +
					roleName +
					" 30`\n";
					default -> null;
				};
			if (customDescription != null) {
				allAutomatedRoles.replace(roleName, customDescription);
			}
		}
	}

	private final Guild guild;
	private final User author;
	private final JsonObject serverSettings;
	private final InteractionHook interactionHook;

	public SettingsExecute(Guild guild, MessageReceivedEvent event) {
		this(guild, event.getAuthor(), null);
	}

	public SettingsExecute(Guild guild, User author, InteractionHook interactionHook) {
		this.guild = guild;
		this.author = author;
		this.interactionHook = interactionHook;

		if (database.getServerSettings(guild.getId()) == null) {
			database.newServerSettings(guild.getId(), new ServerSettingsModel(guild.getName(), guild.getId()));
		}
		this.serverSettings = database.getServerSettings(guild.getId()).getAsJsonObject();
	}

	public Object getSettingsEmbed(String content, String[] args) {
		Object eb = null;

		if (args.length >= 4 && args[1].equals("set")) {
			eb =
				switch (args[2]) {
					case "guest_role" -> setApplyGuestRole(args[3]);
					case "fetchur_channel" -> setFetchurChannel(args[3]);
					case "fetchur_ping" -> setFetchurPing(args[3]);
					case "mayor_channel" -> setMayorChannel(args[3]);
					case "mayor_ping" -> setMayorPing(args[3]);
					case "sync_unlinked" -> switch (args[3]) {
						case "enable" -> setSyncUnlinked(true);
						case "disable" -> setSyncUnlinked(false);
						default -> getHelpEmbed("settings set sync_unlinked");
					};
					default -> getHelpEmbed("settings set");
				};
		} else if (args.length == 2 && args[1].equals("reset")) {
			eb =
				database.deleteServerSettings(guild.getId()) == 200
					? defaultSettingsEmbed("Successfully reset server settings")
					: errorEmbed("Error resetting server settings");
		} else if (args.length >= 2 && args[1].equals("blacklist")) {
			args = content.split("\\s+", 5);
			if (args.length == 2) {
				return displayPlayerBlacklist();
			} else if ((args.length >= 4) && args[2].equals("add")) {
				eb = addToBlacklist(args[3], args.length == 5 ? args[4] : "not provided", null);
			} else if ((args.length >= 4) && args[2].equals("ban")) {
				args = content.split("\\s+", 6);
				eb = addToBlacklist(args[3], args.length == 6 ? args[5] : "not provided", args[4]);
			} else if (args.length == 4) {
				eb =
					switch (args[2]) {
						case "share" -> shareBlacklist(args[3]);
						case "unshare" -> unshareBlacklist(args[3]);
						case "use" -> useBlacklist(args[3]);
						case "stop_using" -> stopUsingBlacklist(args[3]);
						case "remove" -> removeFromBlacklist(args[3]);
						case "search" -> searchBlacklist(args[3]);
						case "enable" -> enableBlacklistFeature(args[3]);
						case "disable" -> disableBlacklistFeature(args[3]);
						default -> null;
					};
			}

			if (eb == null) {
				eb = getHelpEmbed("settings blacklist");
			}
		} else if (args.length == 4 && args[1].equals("bot_manager")) {
			if (args[2].equals("add")) {
				eb = addBotManagerRole(args[3]);
			} else if (args[2].equals("remove")) {
				eb = removeBotManagerRole(args[3]);
			}

			if (eb == null) {
				eb = getHelpEmbed("settings bot_manager");
			}
		} else if (args.length == 4 && args[1].equals("log")) {
			eb =
				switch (args[2]) {
					case "channel" -> setLogChannel(args[3]);
					case "add" -> addLogEvent(args[3]);
					case "remove" -> removeLogEvent(args[3]);
					default -> null;
				};

			if (eb == null) {
				eb = getHelpEmbed("settings log");
			}
		} else if (args.length == 1) {
			eb =
				defaultSettingsEmbed()
					.addField("General Settings", "Use `/settings general` to see the current settings", false)
					.addField("Event Settings", "Use `/settings event` to see the current settings", false)
					.addField("Blacklist Settings", "Use `/settings blacklist` to see the current settings", false)
					.addField("Jacob Settings", "Use `/settings jacob` to see the current settings", false)
					.addField("Verify Settings", "Use `/settings verify` to see the current settings", false)
					.addField("Guild Settings", "Use `/settings guild` to see the current settings", false)
					.addField("Roles Settings", "Use `/settings roles` to see the current settings", false);
		} else if (args.length == 2 && args[1].equals("general")) {
			String fetchurChannel = higherDepth(serverSettings, "fetchurChannel", "none");
			String fetchurRole = higherDepth(serverSettings, "fetchurRole", "none");
			String mayorChannel = higherDepth(serverSettings, "mayorChannel", "none");
			String mayorRole = higherDepth(serverSettings, "mayorRole", "none");
			String applyGuestRole = higherDepth(serverSettings, "applyGuestRole", "none");
			String botManagerRoles = streamJsonArray(higherDepth(serverSettings, "botManagerRoles"))
				.map(r -> "<@&" + r.getAsString() + ">")
				.collect(Collectors.joining(" "));
			String logChannel = higherDepth(serverSettings, "logChannel", "none");
			String logEvents = streamJsonArray(higherDepth(serverSettings, "logEvents"))
				.map(JsonElement::getAsString)
				.collect(Collectors.joining(" "));

			eb =
				defaultSettingsEmbed()
					.addField(
						"Fetchur Notifications Channel",
						fetchurChannel.equals("none") || fetchurChannel.isEmpty() ? "None" : "<#" + fetchurChannel + ">",
						false
					)
					.addField(
						"Fetchur Ping Role",
						fetchurRole.equals("none") || fetchurRole.isEmpty() ? "None" : "<@&" + fetchurRole + ">",
						false
					)
					.addField(
						"Mayor Notifications Channel",
						mayorChannel.equals("none") || mayorChannel.isEmpty() ? "None" : "<#" + mayorChannel + ">",
						false
					)
					.addField("Mayor Ping Role", mayorRole.equals("none") || mayorRole.isEmpty() ? "None" : "<@&" + mayorRole + ">", false)
					.addField(
						"Guest Role",
						applyGuestRole.equals("none") || applyGuestRole.isEmpty() ? "None" : "<@&" + applyGuestRole + ">",
						false
					)
					.addField("Bot Manager Roles", botManagerRoles.isEmpty() ? "None" : botManagerRoles, false)
					.addField("Log Channel", logChannel.equals("none") || logChannel.isEmpty() ? "None" : "<#" + logChannel + ">", false)
					.addField("Log Events", logEvents.isEmpty() ? "None" : logEvents, false)
					.addField(
						"Sync Unlinked Members",
						higherDepth(serverSettings, "syncUnlinkedMembers", true) ? "Enabled" : "Disabled",
						false
					);
		} else if (args.length >= 2 && args[1].equals("jacob")) {
			if (args.length == 2) {
				eb = displayJacobSettings();
			} else if (args.length == 3) {
				if (args[2].equals("enable")) {
					eb = setJacobEnable(true);
				} else if (args[2].equals("disable")) {
					eb = setJacobEnable(false);
				}
			} else {
				eb =
					switch (args[2]) {
						case "add" -> addJacobCrop(args[3], args.length == 5 ? args[4] : null);
						case "remove" -> removeJacobCrop(args[3]);
						case "channel" -> setJacobChannel(args[3]);
						default -> null;
					};
			}

			if (eb == null) {
				eb = getHelpEmbed("settings jacob");
			}
		} else if (args.length >= 2 && args[1].equals("event")) {
			if (args.length == 2) {
				eb = displayEventSettings();
			} else if (args.length == 3) {
				if (args[2].equals("enable")) {
					eb = setEventEnable(true);
				} else if (args[2].equals("disable")) {
					eb = setEventEnable(false);
				}
			} else if (args.length == 4 && args[2].equals("remove")) {
				eb = removeEvent(args[3]);
			} else if (args.length >= 5 && args[2].equals("add")) {
				eb = addEvent(args[3], args[4], args.length >= 6 ? args[5] : null);
			}

			if (eb == null) {
				eb = getHelpEmbed("settings event");
			}
		} else if (args.length >= 2 && args[1].equals("roles")) {
			if (args.length == 2) {
				return displayRolesSettings(higherDepth(serverSettings, "automatedRoles"), 0);
			} else if (args.length == 3) {
				if (args[2].equals("enable")) {
					eb = setRolesEnable(true);
				} else if (args[2].equals("disable")) {
					eb = setRolesEnable(false);
				} else {
					return displayRoleSettings(args[2]);
				}
			} else if (args.length == 4) {
				if (args[2].equals("use_highest")) {
					if (args[3].equals("enable")) {
						eb = setRolesUseHighest(true);
					} else if (args[3].equals("disable")) {
						eb = setRolesUseHighest(false);
					}
				} else if (args[2].equals("sync")) {
					if (args[3].equals("enable")) {
						eb = setRolesAutomaticSyncEnable(true);
					} else if (args[3].equals("disable")) {
						eb = setRolesAutomaticSyncEnable(false);
					}
				}
			} else if (args.length == 5) {
				if (args[2].equals("remove")) {
					eb = removeRoleLevel(args[3], args[4]);
				} else if (args[2].equals("add") && args[3].equals("guild_ranks")) {
					eb = addRoleLevel(args[3], args[4], null);
				}
			} else if (args.length == 6 && args[2].equals("add")) {
				eb = addRoleLevel(args[3], args[4], args[5]);
			}

			if (eb == null) {
				eb = getHelpEmbed("settings roles");
			}
		} else if (content.split("\\s+", 4).length >= 2 && content.split("\\s+", 4)[1].equals("verify")) {
			args = content.split("\\s+", 4);
			if (args.length == 2) {
				eb = defaultSettingsEmbed(displayVerifySettings());
			} else if (args.length == 3) {
				eb =
					switch (args[2]) {
						case "enable" -> setVerifyEnable(true);
						case "disable" -> setVerifyEnable(false);
						default -> null;
					};
			} else if (args.length == 4) {
				switch (args[2]) {
					case "message" -> eb = setVerifyMessageText(args[3]);
					case "channel" -> eb = setVerifyMessageTextChannelId(args[3]);
					case "nickname" -> eb = setVerifyNickname(args[3]);
					case "roles" -> {
						args = content.split("\\s+");
						if (args.length == 5 && args[3].equals("add")) {
							eb = addVerifyRole(args[4]);
						} else if (args.length == 5 && args[3].equals("remove")) {
							eb = removeVerifyRole(args[4]);
						}
					}
					case "remove_role" -> eb = setVerifyRemoveRole(args[3]);
					case "sync" -> eb =
						switch (args[3]) {
							case "enable" -> setVerifySyncEnable(true);
							case "disable" -> setVerifySyncEnable(false);
							default -> null;
						};
					case "dm_on_join" -> eb =
						switch (args[3]) {
							case "enable" -> setVerifyDmOnSync(true);
							case "disable" -> setVerifyDmOnSync(false);
							default -> null;
						};
					case "roles_claim" -> eb =
						switch (args[3]) {
							case "enable" -> setVerifyRolesClaimEnable(true);
							case "disable" -> setVerifyRolesClaimEnable(false);
							default -> null;
						};
				}
			}

			if (eb == null) {
				eb = getHelpEmbed("settings verify");
			}
		} else if ((args.length >= 2) && args[1].equals("guild")) {
			if (content.split("\\s+", 4).length == 4 && (args[2].equals("create") || args[2].equals("remove"))) {
				args = content.split("\\s+", 4);
				eb =
					switch (args[2]) {
						case "create" -> createNewGuild(args[3]);
						case "remove" -> removeGuild(args[3]);
						default -> null;
					};
			} else if (args.length == 2) {
				JsonArray automatedGuilds = higherDepth(serverSettings, "automatedGuilds").getAsJsonArray();
				if (automatedGuilds.isEmpty()) {
					eb = defaultSettingsEmbed("No guilds setup");
				} else {
					EmbedBuilder eb1 = defaultSettingsEmbed();
					for (JsonElement automatedGuild : automatedGuilds) {
						eb1.addField(
							"Automatic Guild",
							"Name: " +
							higherDepth(automatedGuild, "guildName").getAsString() +
							"\nCommand: `/settings guild " +
							higherDepth(automatedGuild, "guildName").getAsString() +
							"`",
							false
						);
					}
					eb = eb1;
				}
			} else if (args.length == 3) {
				return getGuildSettings(args[2]);
			} else if (args.length == 5) {
				JsonElement guildSettings = database.getGuildSettings(guild.getId(), args[2]);
				if (guildSettings == null || guildSettings.isJsonNull()) {
					eb = errorEmbed("No automated guild is created for " + args[2]);
				} else if (args[3].equals("member_role")) {
					eb =
						switch (args[4]) {
							case "enable" -> setGuildMemberRoleEnable(guildSettings.getAsJsonObject(), true);
							case "disable" -> setGuildMemberRoleEnable(guildSettings.getAsJsonObject(), false);
							default -> setGuildMemberRole(guildSettings.getAsJsonObject(), args[4]);
						};
				} else if (args[3].equals("ranks")) {
					eb =
						switch (args[4]) {
							case "enable" -> setGuildRanksEnable(guildSettings.getAsJsonObject(), true);
							case "disable" -> setGuildRanksEnable(guildSettings.getAsJsonObject(), false);
							default -> null;
						};
				} else if (args[3].equals("counter")) {
					eb =
						switch (args[4]) {
							case "enable" -> setGuildCounterEnable(guildSettings.getAsJsonObject(), true);
							case "disable" -> setGuildCounterEnable(guildSettings.getAsJsonObject(), false);
							default -> null;
						};
				} else if (args[3].equals("apply")) {
					eb =
						switch (args[4]) {
							case "enable" -> setApplyEnable(guildSettings.getAsJsonObject(), true);
							case "disable" -> setApplyEnable(guildSettings.getAsJsonObject(), false);
							case "close" -> setApplyClose(guildSettings.getAsJsonObject(), true);
							case "open" -> setApplyClose(guildSettings.getAsJsonObject(), false);
							default -> null;
						};
				}
			} else if (
				(args = content.split("\\s+", 6)).length == 6 &&
				!(args[3].equals("ranks") && args[4].equals("add")) &&
				!(args[3].equals("apply") &&
					(args[4].equals("staff_roles") || args[4].equals("requirements") || args[4].equals("reqs") || args[4].equals("req")))
			) {
				JsonElement guildSettings = database.getGuildSettings(guild.getId(), args[2]);
				if (guildSettings == null || guildSettings.isJsonNull()) {
					eb = errorEmbed("No automated guild is created for " + args[2]);
				} else if (args[3].equals("ranks")) {
					if (args[4].equals("remove")) {
						eb = removeGuildRank(guildSettings.getAsJsonObject(), args[5]);
					}
				} else if (args[3].equals("apply")) {
					switch (args[4]) {
						case "message" -> eb = setApplyMessage(guildSettings.getAsJsonObject(), args[5]);
						case "channel" -> eb = setApplyChannel(guildSettings.getAsJsonObject(), args[5]);
						case "category" -> eb = setApplyCategory(guildSettings.getAsJsonObject(), args[5]);
						case "staff_channel" -> eb = setApplyStaffChannel(guildSettings.getAsJsonObject(), args[5]);
						case "waiting_channel" -> eb = setApplyWaitingChannel(guildSettings.getAsJsonObject(), args[5]);
						case "accept_message" -> eb = setApplyAcceptMessage(guildSettings.getAsJsonObject(), args[5]);
						case "waitlist_message" -> eb = setApplyWaitlistMessage(guildSettings.getAsJsonObject(), args[5]);
						case "deny_message" -> eb = setApplyDenyMessage(guildSettings.getAsJsonObject(), args[5]);
						case "gamemode" -> eb = setApplyGamemode(guildSettings.getAsJsonObject(), args[5]);
						case "scammer_check" -> {
							if (args[5].equals("enable")) {
								eb = setApplyScammerCheck(guildSettings.getAsJsonObject(), true);
							} else if (args[5].equals("disable")) {
								eb = setApplyScammerCheck(guildSettings.getAsJsonObject(), false);
							}
						}
						case "check_api" -> {
							if (args[5].equals("enable")) {
								eb = setApplyCheckApiEnable(guildSettings.getAsJsonObject(), true);
							} else if (args[5].equals("disable")) {
								eb = setApplyCheckApiEnable(guildSettings.getAsJsonObject(), false);
							}
						}
					}
				}
			} else if ((args = content.split("\\s+", 7)).length == 7) {
				JsonElement guildSettings = database.getGuildSettings(guild.getId(), args[2]);
				if (guildSettings == null || guildSettings.isJsonNull()) {
					eb = errorEmbed("No automated guild is created for " + args[2]);
				} else if (args[3].equals("ranks")) {
					if (args[4].equals("add")) {
						eb = addGuildRank(guildSettings.getAsJsonObject(), args[5], args[6]);
					}
				} else if (args[3].equals("apply")) {
					if (args[4].equals("staff_roles")) {
						eb =
							switch (args[5]) {
								case "add" -> addApplyStaffRole(guildSettings.getAsJsonObject(), args[6]);
								case "remove" -> removeApplyStaffRole(guildSettings.getAsJsonObject(), args[6]);
								default -> null;
							};
					} else if (args[4].equals("requirements") || args[4].equals("reqs") || args[4].equals("req")) {
						eb =
							switch (args[5]) {
								case "add" -> addApplyRequirement(guildSettings.getAsJsonObject(), args[6]);
								case "remove" -> removeApplyRequirement(guildSettings.getAsJsonObject(), args[6]);
								default -> null;
							};
					}
				}
			}

			if (eb == null) {
				eb = getHelpEmbed("settings guild" + (args.length >= 4 && args[3].equals("apply") ? " apply" : ""));
			}
		}

		return eb == null ? getHelpEmbed("settings") : eb;
	}

	/* Jacob Settings */
	public EmbedBuilder displayJacobSettings() {
		JsonElement jacobSettings = getJacobSettings();
		String ebFieldString = "";
		ebFieldString += "**" + displaySettings(jacobSettings, "enable") + "**";
		ebFieldString += "\n• **Channel:** " + displaySettings(jacobSettings, "channel");
		ebFieldString += "\n• **Crops:** " + displaySettings(jacobSettings, "crops");
		return defaultSettingsEmbed(ebFieldString);
	}

	public EmbedBuilder setJacobChannel(String channelMention) {
		Object eb = checkTextChannel(channelMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}

		TextChannel channel = (TextChannel) eb;
		JsonObject jacobSettings = getJacobSettings();
		jacobSettings.addProperty("channel", channel.getId());

		int responseCode = database.setJacobSettings(guild.getId(), jacobSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).jacobGuild.reloadSettingsJson(jacobSettings);

		return defaultSettingsEmbed("Set jacob notification channel to " + channel.getAsMention());
	}

	public EmbedBuilder removeJacobCrop(String cropIn) {
		String crop = capitalizeString(cropIn.replace("_", " "));

		JsonObject jacobSettings = getJacobSettings();
		JsonArray crops = higherDepth(jacobSettings, "crops").getAsJsonArray();
		boolean removedCrop = crops.asList().removeIf(e -> higherDepth(e, "value").getAsString().equals(crop));

		if (!removedCrop) {
			return errorEmbed("Provided crop is not added");
		}

		if (crops.isEmpty()) {
			jacobSettings.addProperty("enable", false);
		}

		int responseCode = database.setJacobSettings(guild.getId(), jacobSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).jacobGuild.reloadSettingsJson(jacobSettings);

		return defaultSettingsEmbed("Removed jacob crop: " + crop);
	}

	public EmbedBuilder addJacobCrop(String crop, String roleMention) {
		crop = capitalizeString(crop.replace("_", " "));
		Role role = null;
		if (roleMention != null) {
			Object eb = checkRole(roleMention);
			if (eb instanceof EmbedBuilder e) {
				return e;
			}
			role = (Role) eb;
		}

		if (crop.equalsIgnoreCase("all")) {
			for (String validCrop : cropNameToEmoji.keySet()) {
				EmbedBuilder eb = addJacobCrop(validCrop, role != null ? role.getId() : null);
				if (
					!eb.build().getTitle().equalsIgnoreCase("Settings") &&
					!eb.build().getDescription().startsWith("You have already added the crop: ")
				) {
					return eb;
				}
			}
			return defaultSettingsEmbed("Added all jacob crops");
		}

		if (!cropNameToEmoji.containsKey(crop)) {
			return errorEmbed("Invalid crop\n\nValid crops are: " + String.join(", ", cropNameToEmoji.keySet()));
		}

		JsonObject jacobSettings = getJacobSettings();
		JsonArray crops = higherDepth(jacobSettings, "crops").getAsJsonArray();

		for (JsonElement cropJson : crops) {
			if (higherDepth(cropJson, "value").getAsString().equals(crop)) {
				return errorEmbed("You have already added the crop: " + crop);
			}
		}

		try {
			if (role == null) {
				role = guild.createRole().setName(crop).complete();
			}
		} catch (PermissionException e) {
			return errorEmbed("Missing permission `" + e.getPermission().getName() + "` to create a role for " + crop);
		}

		crops.add(gson.toJsonTree(new RoleObject(crop, role.getId())));

		int responseCode = database.setJacobSettings(guild.getId(), jacobSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).jacobGuild.reloadSettingsJson(jacobSettings);

		return defaultSettingsEmbed("Added jacob crop: " + crop + " - " + role.getAsMention());
	}

	public EmbedBuilder setJacobEnable(boolean enable) {
		JsonObject jacobSettings = getJacobSettings();
		if (enable) {
			try {
				guild.getTextChannelById(higherDepth(jacobSettings, "channel").getAsString()).getId();
				higherDepth(jacobSettings, "crops").getAsJsonArray().get(0);
			} catch (Exception e) {
				return errorEmbed("The channel and at least one crop must be set");
			}
		}

		jacobSettings.addProperty("enable", enable);

		int responseCode = database.setJacobSettings(guild.getId(), jacobSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).jacobGuild.reloadSettingsJson(jacobSettings);

		return defaultSettingsEmbed((enable ? "Enabled" : "Disabled") + " jacob notifications");
	}

	public JsonObject getJacobSettings() {
		return higherDepth(serverSettings, "jacobSettings").getAsJsonObject();
	}

	/* Event Settings */
	public EmbedBuilder displayEventSettings() {
		JsonElement eventSettings = getEventSettings();
		String ebFieldString = "";
		ebFieldString += "**" + displaySettings(eventSettings, "enable") + "**";
		ebFieldString += "\n• **Events:** " + displaySettings(eventSettings, "events");
		return defaultSettingsEmbed(ebFieldString);
	}

	public EmbedBuilder removeEvent(String eventIn) {
		String event = eventIn.toLowerCase().replace(" ", "_");

		JsonObject eventSettings = getEventSettings();
		JsonArray events = higherDepth(eventSettings, "events").getAsJsonArray();
		boolean removedEvent = events.asList().removeIf(e -> higherDepth(e, "value").getAsString().equals(event));

		if (!removedEvent) {
			return errorEmbed("Provided event is not added");
		}

		if (events.isEmpty()) {
			eventSettings.addProperty("enable", false);
		}

		int responseCode = database.setEventSettings(guild.getId(), eventSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).eventGuild.reloadSettingsJson(eventSettings);

		return defaultSettingsEmbed("Removed event notification: " + event);
	}

	public EmbedBuilder addEvent(String event, String channelMention, String roleMention) {
		Object channelEb = checkTextChannel(channelMention);
		if (channelEb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) channelEb;

		Role role = null;
		if (roleMention != null) {
			Object eb = checkRole(roleMention);
			if (eb instanceof EmbedBuilder e) {
				return e;
			}
			role = (Role) eb;
		}

		event = event.toLowerCase();

		if (event.equals("all")) {
			for (String validEvent : validEvents) {
				EmbedBuilder eb = addEvent(validEvent, channel.getId(), role != null ? role.getId() : null);
				if (
					!eb.build().getTitle().equalsIgnoreCase("Settings") &&
					!eb.build().getDescription().startsWith("You have already added the event: ")
				) {
					return eb;
				}
			}
			return defaultSettingsEmbed("Added all events");
		}

		if (!validEvents.contains(event)) {
			return errorEmbed("Invalid event\n\nValid events are: " + String.join(", ", validEvents));
		}

		JsonObject eventSettings = getEventSettings();
		JsonArray events = higherDepth(eventSettings, "events").getAsJsonArray();

		for (int i = events.size() - 1; i >= 0; i--) {
			if (higherDepth(events.get(i), "value").getAsString().equals(event)) {
				return errorEmbed("You have already added the event: " + event);
			}
		}

		try {
			if (role == null) {
				role = guild.createRole().setName(capitalizeString(event.replace("_", " "))).complete();
			}
		} catch (PermissionException e) {
			return errorEmbed("Missing permission `" + e.getPermission().getName() + "` to create a role for " + event);
		}

		events.add(gson.toJsonTree(new EventObject(event, role.getId(), channel != null ? channel.getId() : "")));
		eventSettings.add("events", events);
		int responseCode = database.setEventSettings(guild.getId(), eventSettings);

		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}
		guildMap.get(guild.getId()).eventGuild.reloadSettingsJson(eventSettings);

		return defaultSettingsEmbed("Added event notification: " + event);
	}

	public EmbedBuilder setEventEnable(boolean enable) {
		JsonObject eventSettings = getEventSettings();
		if (enable) {
			try {
				higherDepth(eventSettings, "events").getAsJsonArray().get(0);
			} catch (Exception e) {
				return errorEmbed("At least one event must be added");
			}
		}

		eventSettings.addProperty("enable", enable);

		int responseCode = database.setEventSettings(guild.getId(), eventSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}
		guildMap.get(guild.getId()).eventGuild.reloadSettingsJson(eventSettings);

		return defaultSettingsEmbed((enable ? "Enabled" : "Disabled") + " event notifications");
	}

	public JsonObject getEventSettings() {
		return higherDepth(serverSettings, "eventNotif").getAsJsonObject();
	}

	/* Guild Settings */
	public EmbedBuilder createNewGuild(String guildName) {
		HypixelResponse guildResponse = getGuildFromName(guildName);
		if (!guildResponse.isValid()) {
			return guildResponse.getErrorEmbed();
		}

		guildName = guildResponse.get("name").getAsString();
		String guildNameStripped = guildName.toLowerCase().replace(" ", "_");
		if (!database.getGuildSettings(guild.getId(), guildNameStripped).isJsonNull()) {
			return errorEmbed("An automated guild already exists for this guild");
		}
		AutomatedGuild guildSettings = new AutomatedGuild(guildNameStripped, guildResponse.get("_id").getAsString());

		int responseCode = database.setGuildSettings(guild.getId(), gson.toJsonTree(guildSettings));
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Successfully created a new automatic guild for `" + guildName + "`");
	}

	public EmbedBuilder setGuildMemberRoleEnable(JsonObject guildSettings, boolean enable) {
		if (enable && !isValidRole(higherDepth(guildSettings, "guildMemberRole", ""))) {
			return errorEmbed("A valid guild member role must be set");
		}

		guildSettings.addProperty("guildMemberRoleEnable", enable);

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed((enable ? "Enabled" : "Disabled") + " automatic guild member role");
	}

	public EmbedBuilder setGuildMemberRole(JsonObject guildSettings, String roleMention) {
		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		guildSettings.addProperty("guildMemberRole", role.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set guild member role to " + role.getAsMention());
	}

	public EmbedBuilder setGuildRanksEnable(JsonObject guildSettings, boolean enable) {
		if (enable && higherDepth(guildSettings, "guildRanks").getAsJsonArray().isEmpty()) {
			return errorEmbed("At least one guild rank must be set");
		}

		guildSettings.addProperty("guildRanksEnable", enable);

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed((enable ? "Enabled" : "Disabled") + " automatic guild ranks");
	}

	public EmbedBuilder setGuildCounterEnable(JsonObject guildSettings, boolean enable) {
		if (!enable) {
			try {
				guild.getVoiceChannelById(guildSettings.get("guildCounterChannel").getAsString()).delete().queue();
			} catch (Exception ignored) {}

			guildSettings.addProperty("guildCounterEnable", false);

			int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			return defaultSettingsEmbed("Disabled automatic guild member counter");
		}

		HypixelResponse guildJson = getGuildFromId(higherDepth(guildSettings, "guildId").getAsString());
		if (!guildJson.isValid()) {
			return guildJson.getErrorEmbed();
		}

		VoiceChannel guildMemberCounterChannel;
		try {
			guildMemberCounterChannel =
				guild
					.createVoiceChannel(
						guildJson.get("name").getAsString() + " Members: " + guildJson.get("members").getAsJsonArray().size() + "/125"
					)
					.addPermissionOverride(guild.getPublicRole(), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.VOICE_CONNECT))
					.addMemberPermissionOverride(
						Long.parseLong(BOT_ID),
						EnumSet.of(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT),
						null
					)
					.complete();
		} catch (PermissionException e) {
			return errorEmbed("Missing permission: " + e.getPermission().getName() + " for VC");
		}
		guildSettings.addProperty("guildCounterEnable", true);
		guildSettings.addProperty("guildCounterChannel", guildMemberCounterChannel.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Enabled automatic guild member counter");
	}

	public EmbedBuilder addGuildRank(JsonObject guildSettings, String rankName, String roleMention) {
		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		HypixelResponse guildJson = getGuildFromId(higherDepth(guildSettings, "guildId").getAsString());
		if (!guildJson.isValid()) {
			return guildJson.getErrorEmbed();
		}

		JsonArray guildRanks = guildJson.get("ranks").getAsJsonArray();
		for (JsonElement guildRank : guildRanks) {
			if (higherDepth(guildRank, "name").getAsString().equalsIgnoreCase(rankName.replace("_", " "))) {
				JsonArray currentGuildRanks = guildSettings.get("guildRanks").getAsJsonArray();
				currentGuildRanks.asList().removeIf(e -> higherDepth(e, "value").getAsString().equalsIgnoreCase(rankName));

				currentGuildRanks.add(gson.toJsonTree(new RoleObject(rankName.toLowerCase(), role.getId())));
				guildSettings.add("guildRanks", currentGuildRanks);

				int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
				if (responseCode != 200) {
					return apiFailMessage(responseCode);
				}

				return defaultSettingsEmbed(
					"Added guild rank: " + higherDepth(guildRank, "name").getAsString() + " - " + role.getAsMention()
				);
			}
		}

		return errorEmbed(
			"Invalid guild rank. " +
			(!guildRanks.isEmpty()
					? "Valid guild ranks are: " +
					streamJsonArray(guildRanks)
						.map(r -> higherDepth(r, "name").getAsString().replace(" ", "_"))
						.collect(Collectors.joining(", "))
					: "No guild ranks found")
		);
	}

	public EmbedBuilder removeGuildRank(JsonObject guildSettings, String rankName) {
		JsonArray currentGuildRanks = guildSettings.get("guildRanks").getAsJsonArray();

		boolean removedRank = currentGuildRanks.asList().removeIf(e -> higherDepth(e, "value").getAsString().equalsIgnoreCase(rankName));
		if (!removedRank) {
			return errorEmbed("No role set for the provided rank");
		}

		if (currentGuildRanks.isEmpty()) {
			guildSettings.addProperty("guildCounterEnable", false);
		}

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Removed guild rank: " + rankName);
	}

	public EmbedBuilder setApplyEnable(JsonObject guildSettings, boolean enable) {
		if (enable) {
			if (
				!isValidTextChannel(higherDepth(guildSettings, "applyMessageChannel", "")) ||
				!isValidTextChannel(higherDepth(guildSettings, "applyStaffChannel", "")) ||
				!isValidChannelCategory(higherDepth(guildSettings, "applyCategory", "")) ||
				higherDepth(guildSettings, "applyMessage", "").isEmpty() ||
				higherDepth(guildSettings, "applyAcceptMessage", "").isEmpty() ||
				higherDepth(guildSettings, "applyDenyMessage", "").isEmpty()
			) {
				return errorEmbed(
					"""
					All required application settings must be valid before enabling

					Required settings: channel, staff_channel, category, message, accept_message, deny_message
					"""
				);
			}
		}

		guildSettings.addProperty("applyEnable", enable);
		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed(
			(enable ? "Enabled" : "Disabled") +
			" automated applications for " +
			higherDepth(guildSettings, "guildName").getAsString().replace("_", " ") +
			"\n\nRun `/reload` for the changes to take effect"
		);
	}

	public EmbedBuilder setApplyClose(JsonObject guildSettings, boolean close) {
		if (!higherDepth(guildSettings, "applyEnable", false)) {
			return errorEmbed("Automatic application not enabled");
		}

		guildSettings.addProperty("applyClosed", close);

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed(
			(close ? "Closed" : "Opened") +
			" automated applications for " +
			higherDepth(guildSettings, "guildName").getAsString().replace("_", " ") +
			"\n\nRun `/reload` for the changes to take effect"
		);
	}

	public EmbedBuilder setApplyMessage(JsonObject guildSettings, String message) {
		message = message.replace("\\n", "\n");
		if (message.isEmpty() || EmojiParser.parseToAliases(message).length() > 1500) {
			return errorEmbed("Message must not exceed 1500 characters");
		}

		guildSettings.addProperty("applyMessage", EmojiParser.parseToAliases(message));

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply message to: " + message);
	}

	public EmbedBuilder setApplyScammerCheck(JsonObject guildSettings, boolean enable) {
		guildSettings.addProperty("applyScammerCheck", enable);

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Apply SkyblockZ scammer check: " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder setApplyCheckApiEnable(JsonObject guildSettings, boolean enable) {
		guildSettings.addProperty("applyCheckApi", enable);

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Apply check all APIs are enabled: " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder setApplyChannel(JsonObject guildSettings, String textChannel) {
		Object eb = checkTextChannel(textChannel);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) eb;

		guildSettings.addProperty("applyMessageChannel", channel.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply message channel to: " + channel.getAsMention());
	}

	public EmbedBuilder setApplyCategory(JsonObject guildSettings, String categoryMention) {
		Object eb = checkChannelCategory(categoryMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Category category = (Category) eb;

		guildSettings.addProperty("applyCategory", category.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply category to: " + category.getAsMention());
	}

	public EmbedBuilder setApplyStaffChannel(JsonObject guildSettings, String textChannel) {
		Object eb = checkTextChannel(textChannel);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) eb;

		guildSettings.addProperty("applyStaffChannel", channel.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply staff channel to: " + channel.getAsMention());
	}

	public EmbedBuilder setApplyWaitingChannel(JsonObject guildSettings, String textChannel) {
		TextChannel channel = null;
		if (!textChannel.equalsIgnoreCase("none")) {
			Object eb = checkTextChannel(textChannel);
			if (eb instanceof EmbedBuilder e) {
				return e;
			}
			channel = (TextChannel) eb;
		}

		guildSettings.addProperty("applyWaitingChannel", channel == null ? "none" : channel.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply waiting for invite channel to: " + (channel == null ? "none" : channel.getAsMention()));
	}

	public EmbedBuilder setApplyAcceptMessage(JsonObject guildSettings, String acceptMessage) {
		if (acceptMessage.isEmpty() || EmojiParser.parseToAliases(acceptMessage).length() > 1500) {
			return errorEmbed("Message must not exceed 1500 characters");
		}

		guildSettings.addProperty("applyAcceptMessage", EmojiParser.parseToAliases(acceptMessage));

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply accept message to: " + acceptMessage);
	}

	public EmbedBuilder setApplyWaitlistMessage(JsonObject guildSettings, String waitlistMessage) {
		if (waitlistMessage.isEmpty() || EmojiParser.parseToAliases(waitlistMessage).length() > 1500) {
			return errorEmbed("Message must not exceed 1500 characters");
		}

		guildSettings.addProperty(
			"applyWaitlistMessage",
			waitlistMessage.equalsIgnoreCase("none") ? "none" : EmojiParser.parseToAliases(waitlistMessage)
		);

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply waitlist message to: " + waitlistMessage);
	}

	public EmbedBuilder setApplyDenyMessage(JsonObject guildSettings, String denyMessage) {
		if (denyMessage.isEmpty() || EmojiParser.parseToAliases(denyMessage).length() > 1500) {
			return errorEmbed("Message must not exceed 1500 characters");
		}

		guildSettings.addProperty("applyDenyMessage", EmojiParser.parseToAliases(denyMessage));

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Apply deny message set to: " + denyMessage);
	}

	public EmbedBuilder setApplyGamemode(JsonObject guildSettings, String gamemode) {
		try {
			Player.Gamemode.of(gamemode);
		} catch (Exception e) {
			return errorEmbed("Invalid gamemode");
		}

		guildSettings.addProperty("applyGamemode", gamemode.toLowerCase());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Set apply gamemode requirement to: " + gamemode.toLowerCase());
	}

	public EmbedBuilder addApplyStaffRole(JsonObject guildSettings, String roleMention) {
		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		JsonArray staffRoles = higherDepth(guildSettings, "applyStaffRoles").getAsJsonArray();
		if (staffRoles.size() >= 3) {
			return defaultEmbed("You have reached the max number of staff ping roles (3/3)");
		}

		staffRoles.asList().removeIf(e -> e.getAsString().equals(role.getId()) || !isValidRole(e.getAsString()));
		staffRoles.add(role.getId());

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Added apply staff role: " + role.getAsMention());
	}

	public EmbedBuilder removeApplyStaffRole(JsonObject guildSettings, String roleMention) {
		Object eb = checkRole(roleMention, true);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = ((Role) eb);

		JsonArray staffRoles = higherDepth(guildSettings, "applyStaffRoles").getAsJsonArray();
		staffRoles.asList().removeIf(e -> e.getAsString().equals(role.getId()) || !isValidRole(e.getAsString()));

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Removed apply staff role: " + role.getAsMention());
	}

	public EmbedBuilder addApplyRequirement(JsonObject guildSettings, String reqArgs) {
		JsonArray currentReqs = guildSettings.getAsJsonArray("applyReqs");

		if (currentReqs.size() >= 3) {
			return errorEmbed("You have reached the max number of requirements (3/3)");
		}

		ApplyRequirement newReq = new ApplyRequirement();

		for (String req : reqArgs.split("\\s+")) {
			String[] reqSplit = req.split(":");

			String reqType = reqSplit[0].trim();
			if (!validApplyRequirements.contains(reqType)) {
				return errorEmbed("Invalid requirement type provided");
			}

			long amount;
			try {
				amount = Long.parseLong(reqSplit[1].trim());
			} catch (Exception e) {
				return errorEmbed("Invalid requirement amount provided");
			}

			if (amount <= 0) {
				return errorEmbed("Requirement amount must be positive");
			}

			newReq.addRequirement(reqType, "" + amount);
		}

		currentReqs.add(gson.toJsonTree(newReq));

		int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed(
			"Added an apply requirement:" +
			newReq
				.getRequirements()
				.entrySet()
				.stream()
				.map(e -> "\n• " + capitalizeString(e.getKey().replace("_", " ")) + " - " + e.getValue())
				.collect(Collectors.joining())
		);
	}

	public EmbedBuilder removeApplyRequirement(JsonObject guildSettings, String reqNumber) {
		JsonArray currentReqs = guildSettings.getAsJsonArray("applyReqs");

		try {
			JsonElement removedReq = currentReqs.remove(Integer.parseInt(reqNumber) - 1);

			int responseCode = database.setGuildSettings(guild.getId(), guildSettings);
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			return defaultSettingsEmbed(
				"Removed an apply requirement:" +
				higherDepth(removedReq, "requirements")
					.getAsJsonObject()
					.entrySet()
					.stream()
					.map(e -> "\n• " + capitalizeString(e.getKey().replace("_", " ")) + " - " + e.getValue().getAsString())
					.collect(Collectors.joining())
			);
		} catch (Exception e) {
			return errorEmbed("Invalid requirement index. Run `/settings guild <name>` to see the current apply requirements");
		}
	}

	public EmbedBuilder removeGuild(String name) {
		JsonElement guildSettings = database.getGuildSettings(guild.getId(), name);
		if (guildSettings == null || guildSettings.isJsonNull()) {
			return errorEmbed("No automated guild set up for " + name);
		}

		int responseCode = database.removeGuildSettings(guild.getId(), name);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Removed automated guild for " + name);
	}

	public EmbedBuilder getGuildSettings(String name) {
		JsonElement settings = database.getGuildSettings(guild.getId(), name);
		if (settings == null || settings.isJsonNull()) {
			return defaultSettingsEmbed("Invalid setting name. Use `/settings guild` to see all current guild settings.");
		}

		CustomPaginator.Builder paginateBuilder = defaultPaginator(author);
		PaginatorExtras extras = paginateBuilder.getExtras().setType(PaginatorExtras.PaginatorType.EMBED_PAGES);
		extras.addEmbedPage(
			defaultSettingsEmbed("**" + displaySettings(settings, "applyEnable").replace("•", "").trim() + "**")
				.addField("Button Message Channel", displaySettings(settings, "applyMessageChannel"), true)
				.addField("Staff Message Channel", displaySettings(settings, "applyStaffChannel"), true)
				.addField("Waiting For Invite Channel", displaySettings(settings, "applyWaitingChannel"), true)
				.addField("Staff Ping Roles", displaySettings(settings, "applyStaffRoles"), true)
				.addField("New Channel Category", displaySettings(settings, "applyCategory"), true)
				.addField("Gamemode", displaySettings(settings, "applyGamemode"), true)
				.addField("Button Message Text", displaySettings(settings, "applyMessage"), true)
				.addField("Accepted Message", displaySettings(settings, "applyAcceptMessage"), true)
				.addField("Waitlisted Message", displaySettings(settings, "applyWaitlistMessage"), true)
				.addField("Denied Message", displaySettings(settings, "applyDenyMessage"), true)
				.addField("Scammer Check", displaySettings(settings, "applyScammerCheck"), true)
				.addField("Check APIs Enabled", displaySettings(settings, "applyCheckApi"), true)
				.addField("Requirements", displaySettings(settings, "applyReqs"), true)
		);

		EmbedBuilder eb = defaultSettingsEmbed()
			.addField(
				"Guild Role",
				displaySettings(settings, "guildMemberRoleEnable") +
				"\n• Guild Name: " +
				displaySettings(settings, "guildId") +
				"\n• Guild Member Role: " +
				displaySettings(settings, "guildMemberRole"),
				false
			);

		StringBuilder guildRanksString = new StringBuilder();
		for (JsonElement guildRank : higherDepth(settings, "guildRanks").getAsJsonArray()) {
			guildRanksString
				.append("\n• ")
				.append(higherDepth(guildRank, "value").getAsString())
				.append(" - <@&")
				.append(higherDepth(guildRank, "roleId").getAsString())
				.append(">");
		}
		eb.addField(
			"Guild Ranks",
			displaySettings(settings, "guildRanksEnable") +
			(!guildRanksString.isEmpty() ? guildRanksString.toString() : "\n• No guild ranks set"),
			false
		);

		eb.addField(
			"Guild Counter",
			"• " +
			(higherDepth(settings, "guildCounterEnable", false) ? "Enabled" : "Disabled") +
			(higherDepth(settings, "guildCounterEnable", false)
					? "\n• Counter Channel: " + displaySettings(settings, "guildCounterChannel")
					: ""),
			false
		);
		extras.addEmbedPage(eb);

		paginateBuilder.build().paginate(interactionHook, 1);
		return null;
	}

	/* Roles Settings */
	public EmbedBuilder displayRoleSettings(String roleName) {
		if (allAutomatedRoles.containsKey(roleName)) {
			List<String> allRoles = new ArrayList<>(allAutomatedRoles.keySet());
			Map<String, Integer> rolePageMap = IntStream
				.range(0, allRoles.size())
				.boxed()
				.collect(Collectors.toMap(allRoles::get, i -> i + 2, (a, b) -> b));

			return displayRolesSettings(database.getRolesSettings(guild.getId()), rolePageMap.get(roleName));
		} else {
			try {
				return displayRolesSettings(database.getRolesSettings(guild.getId()), Integer.parseInt(roleName));
			} catch (Exception ignored) {}
		}

		return errorEmbed("Invalid role name or page number");
	}

	public EmbedBuilder displayRolesSettings(JsonElement rolesSettings, int pageNum) {
		CustomPaginator.Builder paginateBuilder = defaultPaginator(author);

		List<String> pageTitles = new ArrayList<>(List.of("Roles Settings"));

		List<String> roleNames = new ArrayList<>(allAutomatedRoles.keySet());
		StringBuilder pageNumbers = new StringBuilder();
		// -1 to ignore hotm_3_ironman
		for (int i = 0; i < roleNames.size() - 1; i++) {
			pageNumbers.append("\n**Page ").append(i + 2).append(":** ").append(roleNames.get(i));
		}
		paginateBuilder.addStrings(
			"**Automated Roles:** " +
			(higherDepth(rolesSettings, "enable", false) ? "enabled" : "disabled") +
			"\n**Use highest:** " +
			higherDepth(rolesSettings, "useHighest", false) +
			"\n**Automatic sync enabled:** " +
			higherDepth(rolesSettings, "enableAutomaticSync", false) +
			"\n" +
			pageNumbers
		);

		for (Entry<String, String> roleDesc : allAutomatedRoles.entrySet()) {
			// -1 (last element) +2 (starts on page 2)
			if (roleDesc.getKey().equals("hotm_3_ironman") && pageNum != roleNames.size() + 1) {
				continue;
			}

			StringBuilder ebFieldString = new StringBuilder().append(roleDesc.getValue()).append("\nSettings");
			JsonElement roleSettings = streamJsonArray(higherDepth(rolesSettings, "roles"))
				.filter(e -> higherDepth(e, "name", "").equals(roleDesc.getKey()))
				.findFirst()
				.orElse(null);

			if (roleDesc.getKey().equals("guild_ranks")) {
				if (higherDepth(roleSettings, "levels.[0]") == null) {
					ebFieldString.append("\n• No ranks added");
				} else {
					for (JsonElement roleLevel : higherDepth(roleSettings, "levels").getAsJsonArray()) {
						String rName = higherDepth(roleLevel, "value").getAsString();
						ebFieldString
							.append("\n• ")
							.append(rName)
							.append(" (view the ranks using `/settings guild ")
							.append(rName)
							.append("`)");
					}
				}
			} else {
				if (higherDepth(roleSettings, "levels.[0]") == null) {
					ebFieldString.append("\n• No ranks added");
				} else {
					if (roleDesc.getKey().equals("guild_member")) {
						for (JsonElement roleLevel : higherDepth(roleSettings, "levels").getAsJsonArray()) {
							String guildId = higherDepth(roleLevel, "value").getAsString();
							HypixelResponse guildJson = getGuildFromId(guildId);
							if (guildJson.isValid()) {
								ebFieldString
									.append("\n• ")
									.append(guildJson.get("name").getAsString())
									.append(" - ")
									.append("<@&")
									.append(higherDepth(roleLevel, "roleId").getAsString())
									.append(">");
							} else {
								ebFieldString
									.append("\n• ")
									.append("Invalid guild")
									.append(" - ")
									.append("<@&")
									.append(higherDepth(roleLevel, "roleId").getAsString())
									.append(">");
							}
						}
					} else {
						for (JsonElement roleLevel : higherDepth(roleSettings, "levels").getAsJsonArray()) {
							ebFieldString
								.append("\n• ")
								.append(higherDepth(roleLevel, "value").getAsString())
								.append(" - ")
								.append("<@&")
								.append(higherDepth(roleLevel, "roleId").getAsString())
								.append(">");
						}
					}
				}
			}

			pageTitles.add(roleDesc.getKey());
			paginateBuilder.addStrings(ebFieldString.toString());
		}

		paginateBuilder.getExtras().setTitles(pageTitles);
		paginateBuilder.build().paginate(interactionHook, pageNum);
		return null;
	}

	public EmbedBuilder setRolesEnable(boolean enable) {
		if (enable && !allowRolesEnable()) {
			return errorEmbed("At least one automatic role must be setup before enabling");
		}

		JsonObject rolesSettings = database.getRolesSettings(guild.getId()).getAsJsonObject();
		rolesSettings.addProperty("enable", enable);

		int responseCode = database.setRolesSettings(guild.getId(), rolesSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("**Automatic roles:** " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder setRolesUseHighest(boolean enable) {
		JsonObject rolesSettings = database.getRolesSettings(guild.getId()).getAsJsonObject();
		rolesSettings.addProperty("useHighest", enable);

		int responseCode = database.setRolesSettings(guild.getId(), rolesSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("**Use highest amount:** " + enable);
	}

	public EmbedBuilder setRolesAutomaticSyncEnable(boolean enable) {
		JsonObject rolesSettings = database.getRolesSettings(guild.getId()).getAsJsonObject();

		if (enable) {
			if (!higherDepth(rolesSettings, "enable", false)) {
				return errorEmbed("Automatic roles must be enabled");
			}
		}

		rolesSettings.addProperty("enableAutomaticSync", enable);

		int responseCode = database.setRolesSettings(guild.getId(), rolesSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Automatic roles sync " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder addRoleLevel(String roleName, String roleValue, String roleMention) {
		if (!allAutomatedRoles.containsKey(roleName)) {
			return errorEmbed("Invalid role name. Refer to `/settings roles` for a list of all role names");
		}

		String guildName = "";
		switch (roleName) {
			case "guild_ranks" -> {
				JsonElement guildRoleSettings = database.getGuildSettings(guild.getId(), roleValue);
				if (!higherDepth(guildRoleSettings, "guildRanksEnable", false)) {
					return errorEmbed("Provided automatic guild name is invalid or automatic guild ranks are not enabled");
				}
				RoleModel roleSettings = database.getRoleSettings(guild.getId(), "guild_ranks");
				if (roleSettings == null) {
					roleSettings = new RoleModel(roleName);
				}
				roleSettings.addLevel(roleValue, roleValue);
				int responseCode = database.setRoleSettings(guild.getId(), roleSettings);
				if (responseCode != 200) {
					return apiFailMessage(responseCode);
				}
				return defaultSettingsEmbed("Added guild ranks for automatic guild - `" + roleValue + "`");
			}
			case "guild_member" -> {
				HypixelResponse guildJson = getGuildFromName(roleValue);
				if (!guildJson.isValid()) {
					return guildJson.getErrorEmbed();
				}
				roleValue = guildJson.get("_id").getAsString();
				guildName = guildJson.get("name").getAsString();
			}
			case "gamemode" -> {
				if (!roleValue.equals("ironman") && !roleValue.equals("stranded")) {
					return errorEmbed("Mode must be ironman or stranded");
				}
			}
			case "player_items" -> {
				roleValue = roleValue.replace("_", " ");
				String itemId = nameToId(roleValue, true);
				if (itemId == null) {
					return errorEmbed(
						"No item with the name `" +
						roleValue +
						"` exists. Perhaps you meant one of the following: " +
						FuzzySearch
							.extractTop(
								roleValue,
								getInternalJsonMappings()
									.entrySet()
									.stream()
									.map(e -> higherDepth(e.getValue(), "name", ""))
									.collect(Collectors.toCollection(ArrayList::new)),
								5
							)
							.stream()
							.map(e -> e.getString().replace(" ", "_"))
							.collect(Collectors.joining(", "))
					);
				}
				roleValue = itemId;
			}
			default -> {
				try {
					Long.parseLong(roleValue);
				} catch (Exception e) {
					return errorEmbed("Role value must be an integer");
				}
			}
		}

		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		int totalRoleCount = streamJsonArray(higherDepth(database.getRolesSettings(guild.getId()), "roles"))
			.mapToInt(e -> higherDepth(e, "levels").getAsJsonArray().size())
			.sum();
		if (totalRoleCount >= 175) {
			return errorEmbed("You have reached the max number of total roles (175/175)");
		}

		RoleModel roleSettings = database.getRoleSettings(guild.getId(), roleName);
		if (roleSettings == null) {
			roleSettings = new RoleModel(roleName);
		}

		// "one level role"
		if (roleName.equals("hotm_3_ironman")) {
			if (roleSettings.getLevels().size() == 1) {
				return errorEmbed("your computer self destructed");
			}
			roleValue = "1";
		}

		roleSettings.addLevel(roleValue, role.getId());

		int responseCode = database.setRoleSettings(guild.getId(), roleSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed(
			"Set " + roleName + " " + (roleName.equals("guild_member") ? guildName : roleValue) + " to " + role.getAsMention()
		);
	}

	public EmbedBuilder removeRoleLevel(String roleName, String value) {
		if (!allAutomatedRoles.containsKey(roleName)) {
			return errorEmbed("Invalid role name. Refer to `/settings roles` for a list of all role names");
		}

		RoleModel roleSettings = database.getRoleSettings(guild.getId(), roleName);

		for (Iterator<RoleObject> iter = roleSettings.getLevels().iterator(); iter.hasNext();) {
			RoleObject level = iter.next();

			String levelValue = level.getValue();
			if (roleName.equals("guild_member")) {
				HypixelResponse guildJson = getGuildFromId(levelValue);
				if (!guildJson.isValid()) {
					iter.remove();
				}
				levelValue = guildJson.get("name").getAsString();
			}

			if (levelValue.equalsIgnoreCase(value.replace("_", " ")) || roleName.equals("hotm_3_ironman")) {
				try {
					iter.remove();
				} catch (Exception ignored) {
					// Catches already removed invalid guild values (see above lines)
				}

				int responseCode;
				if (roleSettings.getLevels().isEmpty()) {
					responseCode = database.removeRoleSettings(guild.getId(), roleName);
				} else {
					responseCode = database.setRoleSettings(guild.getId(), roleSettings);
				}
				if (responseCode != 200) {
					return apiFailMessage(responseCode);
				}

				if (!allowRolesEnable()) {
					setRolesEnable(false);
				}

				return defaultSettingsEmbed("Removed " + roleName + " " + value);
			}
		}
		return errorEmbed("Invalid role value");
	}

	public boolean allowRolesEnable() {
		return streamJsonArray(higherDepth(database.getRolesSettings(guild.getId()), "roles"))
			.anyMatch(role -> higherDepth(role, "levels.[0]") != null);
	}

	/* Verify Settings */
	public String displayVerifySettings() {
		JsonElement verifySettings = higherDepth(serverSettings, "automatedVerify");
		String ebFieldString = "";
		ebFieldString += "**" + displaySettings(verifySettings, "enable") + "**";
		ebFieldString += "\n• **Message Text:** " + displaySettings(verifySettings, "messageText");
		ebFieldString += "\n• **Channel:** " + displaySettings(verifySettings, "messageTextChannelId");
		ebFieldString += "\n• **Verified Role(s):** " + displaySettings(verifySettings, "verifiedRoles");
		ebFieldString += "\n• **Verified Remove Role:** " + displaySettings(verifySettings, "verifiedRemoveRole");
		ebFieldString += "\n• **Nickname Template:** " + displaySettings(verifySettings, "verifiedNickname");
		ebFieldString += "\n• **Automatic Sync:** " + displaySettings(verifySettings, "enableAutomaticSync");
		if (higherDepth(verifySettings, "enableAutomaticSync", false)) {
			ebFieldString += "\n• **DM On Join Sync:** " + displaySettings(verifySettings, "dmOnSync");
		}
		ebFieldString += "\n• **Automatic SB Roles Claim On Join/Link:** " + displaySettings(verifySettings, "enableRolesClaim");
		return ebFieldString;
	}

	public EmbedBuilder setVerifyEnable(boolean enable) {
		if (enable) {
			JsonObject currentSettings = database.getVerifySettings(guild.getId()).getAsJsonObject();

			if (
				higherDepth(currentSettings, "messageText", "").isEmpty() ||
				!isValidTextChannel(higherDepth(currentSettings, "messageTextChannelId", "")) ||
				!isValidRole(higherDepth(currentSettings, "verifiedRoles.[0]", ""))
			) {
				return errorEmbed(
					"""
					All required verification settings must be valid before enabling

					Required settings: message, channel, roles
					"""
				);
			}
		}

		int responseCode = updateVerifySettings("enable", "" + enable);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		onVerifyReload(guild.getId());

		return defaultSettingsEmbed("**Verify:** " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder setVerifyMessageText(String verifyText) {
		verifyText = verifyText.replace("\\n", "\n");
		if (verifyText.isEmpty() || EmojiParser.parseToAliases(verifyText).length() > 1500) {
			return errorEmbed("Text must be between 1 to 1500 characters");
		}

		int responseCode = updateVerifySettings("messageText", EmojiParser.parseToAliases(verifyText));
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("**Verify message set to:** " + verifyText);
	}

	public EmbedBuilder setVerifyMessageTextChannelId(String textChannel) {
		Object eb = checkTextChannel(textChannel);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) eb;

		int responseCode = updateVerifySettings("messageTextChannelId", channel.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		try {
			channel.getManager().putPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.MESSAGE_SEND)).queue();
		} catch (Exception ignored) {}

		return defaultSettingsEmbed("**Verify text channel set to:** " + channel.getAsMention());
	}

	public EmbedBuilder setVerifyNickname(String nickname) {
		if (nickname.equalsIgnoreCase("none")) {
			int responseCode = updateVerifySettings("verifiedNickname", "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			return defaultSettingsEmbed("**Verify nickname disabled**");
		}

		if (!guild.getSelfMember().hasPermission(Permission.NICKNAME_MANAGE)) {
			return errorEmbed("Missing permission to modify nicknames");
		}

		if (!nickname.contains("[IGN]")) {
			return errorEmbed("Nickname template must contain [IGN]");
		}

		String origNick = nickname;
		Matcher matcher = nicknameTemplatePattern.matcher(nickname);
		while (matcher.find()) {
			String category = matcher.group(1).toUpperCase();
			String type = matcher.group(2).toUpperCase();

			if (category.equals("GUILD") && (type.equals("NAME") || type.equals("TAG") || type.equals("RANK"))) {
				if (
					database
						.getAllGuildSettings(guild.getId())
						.stream()
						.noneMatch(g -> g != null && g.getGuildRanksEnable() != null && g.getGuildRanksEnable().equalsIgnoreCase("true"))
				) {
					return errorEmbed("At least one guild ranks must be enabled in `/settings guild <name>` to use " + matcher.group(0));
				}
				nickname = nickname.replace(matcher.group(0), "");
			} else if (
				category.equals("PLAYER") &&
				(type.equals("SKILLS") ||
					type.equals("CATACOMBS") ||
					type.equals("SLAYER") ||
					type.equals("WEIGHT") ||
					type.equals("CLASS") ||
					type.equals("LEVEL") ||
					type.equals("IRONMAN") ||
					type.equals("EMBLEM"))
			) {
				nickname = nickname.replace(matcher.group(0), "");
			}
		}

		if (nickname.replace("[IGN]", "").length() > 15) {
			return errorEmbed("Nickname prefix and/or postfix, excluding templates, must be less than or equal to 15 letters");
		}

		int responseCode = updateVerifySettings("verifiedNickname", origNick);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("**Verify nickname set to:** " + origNick);
	}

	public EmbedBuilder removeVerifyRole(String roleMention) {
		Object eb = checkRole(roleMention, true);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		JsonElement verifySettings = database.getVerifySettings(guild.getId());
		JsonArray currentVerifyRoles = higherDepth(verifySettings, "verifiedRoles").getAsJsonArray();
		currentVerifyRoles.asList().removeIf(e -> e.getAsString().equals(role.getId()) || !isValidRole(e.getAsString()));

		int responseCode = database.setVerifyRolesSettings(guild.getId(), currentVerifyRoles);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).verifyGuild.reloadSettingsJson(verifySettings);

		if (currentVerifyRoles.isEmpty()) {
			updateVerifySettings("enable", "false");
		}

		return defaultSettingsEmbed("**Removed verify role:** " + role.getAsMention());
	}

	public EmbedBuilder addVerifyRole(String roleMention) {
		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = ((Role) eb);

		JsonElement verifySettings = database.getVerifySettings(guild.getId());

		if (higherDepth(verifySettings, "verifiedRemoveRole", "").equals(role.getId())) {
			return errorEmbed("This is already set as the verify remove role");
		}

		JsonArray currentVerifyRoles = higherDepth(verifySettings, "verifiedRoles").getAsJsonArray();
		if (currentVerifyRoles.size() >= 5) {
			return errorEmbed("You have reached the max number of verify roles (5/5)");
		}

		currentVerifyRoles.asList().removeIf(e -> e.getAsString().equals(role.getId()) || !isValidRole(e.getAsString()));
		currentVerifyRoles.add(role.getId());

		int responseCode = database.setVerifyRolesSettings(guild.getId(), currentVerifyRoles);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).verifyGuild.reloadSettingsJson(verifySettings);

		return defaultSettingsEmbed("**Verify role added:** " + role.getAsMention());
	}

	public EmbedBuilder setVerifyRemoveRole(String roleMention) {
		if (roleMention.equalsIgnoreCase("none")) {
			JsonObject verifySettings = database.getVerifySettings(guild.getId()).getAsJsonObject();
			verifySettings.addProperty("verifiedRemoveRole", "none");

			int responseCode = database.setVerifySettings(guild.getId(), verifySettings);
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).verifyGuild.reloadSettingsJson(verifySettings);

			return defaultSettingsEmbed("Unset verify remove role");
		}

		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		JsonObject verifySettings = database.getVerifySettings(guild.getId()).getAsJsonObject();
		if (streamJsonArray(higherDepth(verifySettings, "verifiedRoles")).anyMatch(r -> r.getAsString().equals(role.getId()))) {
			return errorEmbed("This is already added as a verified role");
		}

		verifySettings.addProperty("verifiedRemoveRole", role.getId());

		int responseCode = database.setVerifySettings(guild.getId(), verifySettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).verifyGuild.reloadSettingsJson(verifySettings);

		return defaultSettingsEmbed("**Verify remove role set:** " + role.getAsMention());
	}

	public EmbedBuilder setVerifySyncEnable(boolean enable) {
		JsonObject currentSettings = database.getVerifySettings(guild.getId()).getAsJsonObject();

		if (enable) {
			String nickname = higherDepth(currentSettings, "verifiedNickname").getAsString();
			if (!nickname.contains("[IGN]") && higherDepth(currentSettings, "verifiedRoles").getAsJsonArray().isEmpty()) {
				return errorEmbed("You must set at least on verify role or a nickname template before enabling");
			}
		}

		int responseCode = updateVerifySettings("enableAutomaticSync", "" + enable);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Automatic sync " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder setVerifyDmOnSync(boolean enable) {
		int responseCode = updateVerifySettings("dmOnSync", "" + enable);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("DM on sync " + (enable ? "enabled" : "disabled"));
	}

	public EmbedBuilder setVerifyRolesClaimEnable(boolean enable) {
		if (enable && !higherDepth(database.getRolesSettings(guild.getId()), "enable", false)) {
			return errorEmbed("Automatic roles must be enabled");
		}

		int responseCode = updateVerifySettings("enableRolesClaim", "" + enable);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Automatic roles claim sync " + (enable ? "enabled" : "disabled"));
	}

	public int updateVerifySettings(String key, String newValue) {
		JsonObject newVerifySettings = database.getVerifySettings(guild.getId()).getAsJsonObject();
		newVerifySettings.addProperty(key, newValue);
		guildMap.get(guild.getId()).verifyGuild.reloadSettingsJson(newVerifySettings);
		return database.setVerifySettings(guild.getId(), newVerifySettings);
	}

	/* Miscellaneous */
	public EmbedBuilder displayPlayerBlacklist() {
		JsonElement blacklistSettings = getBlacklistSettings();
		JsonArray currentBlacklist = higherDepth(blacklistSettings, "blacklist").getAsJsonArray();

		CustomPaginator.Builder paginateBuilder = defaultPaginator(author).setItemsPerPage(30);
		paginateBuilder.getExtras().setEveryPageTitle("Settings");
		String canUse = streamJsonArray(higherDepth(blacklistSettings, "canUse"))
			.map(g -> jda.getGuildById(g.getAsString()))
			.filter(Objects::nonNull)
			.map(Guild::getName)
			.collect(Collectors.joining(", "));
		String isUsing = streamJsonArray(higherDepth(blacklistSettings, "isUsing"))
			.map(g -> jda.getGuildById(g.getAsString()))
			.filter(Objects::nonNull)
			.map(Guild::getName)
			.collect(Collectors.joining(", "));
		String features = streamJsonArray(higherDepth(blacklistSettings, "features"))
			.map(JsonElement::getAsString)
			.collect(Collectors.joining(", "));

		paginateBuilder.addStrings(
			"• Shared with: " + (canUse.isEmpty() ? "none" : canUse),
			"• Using: " + (isUsing.isEmpty() ? "none" : isUsing),
			"• Enabled for features: " + (features.isEmpty() ? "none" : features),
			"• Blacklist size (this server): " + currentBlacklist.size()
		);
		paginateBuilder.addStrings(Collections.nCopies(26, "").toArray(new String[0]));

		streamJsonArray(higherDepth(blacklistSettings, "isUsing"))
			.map(g -> higherDepth(database.getBlacklistSettings(g.getAsString()), "blacklist").getAsJsonArray())
			.forEach(currentBlacklist::addAll);

		for (JsonElement blacklisted : currentBlacklist) {
			paginateBuilder.addStrings(
				"• " +
				nameMcHyperLink(higherDepth(blacklisted, "username").getAsString(), higherDepth(blacklisted, "uuid").getAsString()) +
				" - " +
				higherDepth(blacklisted, "reason").getAsString()
			);
		}

		paginateBuilder.build().paginate(interactionHook, 1);
		return null;
	}

	public EmbedBuilder removeFromBlacklist(String username) {
		UsernameUuidStruct uuidStruct = usernameToUuid(username);
		if (!uuidStruct.isValid()) {
			return errorEmbed(uuidStruct.failCause());
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray currentBlacklist = higherDepth(blacklistSettings, "blacklist").getAsJsonArray();
		for (int i = 0; i < currentBlacklist.size(); i++) {
			if (
				higherDepth(currentBlacklist.get(i), "uuid").getAsString().equals(uuidStruct.uuid()) ||
				higherDepth(currentBlacklist.get(i), "username").getAsString().equals(uuidStruct.username())
			) {
				currentBlacklist.remove(i);

				int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
				if (responseCode != 200) {
					return apiFailMessage(responseCode);
				}

				guildMap.get(guild.getId()).setBlacklist(currentBlacklist);
				return defaultSettingsEmbed("Removed " + uuidStruct.nameMcHyperLink() + " from the blacklist");
			}
		}

		return errorEmbed(uuidStruct.nameMcHyperLink() + " is not blacklisted");
	}

	public EmbedBuilder addToBlacklist(String username, String reason, String memberMention) {
		UsernameUuidStruct uuidStruct = usernameToUuid(username);
		if (!uuidStruct.isValid()) {
			return errorEmbed(uuidStruct.failCause());
		}

		if (memberMention != null) {
			Object eb = checkMember(memberMention);
			if (eb instanceof EmbedBuilder e) {
				return e;
			}
			try {
				guild.ban((Member) eb, 0, TimeUnit.SECONDS).reason(reason).complete();
			} catch (Exception e) {
				return errorEmbed("Unable to ban member");
			}
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray currentBlacklist = higherDepth(blacklistSettings, "blacklist").getAsJsonArray();
		JsonElement blacklistedUser = streamJsonArray(currentBlacklist)
			.filter(blacklist ->
				higherDepth(blacklist, "uuid").getAsString().equals(uuidStruct.uuid()) ||
				higherDepth(blacklist, "username").getAsString().equals(uuidStruct.username())
			)
			.findFirst()
			.orElse(null);
		if (blacklistedUser != null) {
			return errorEmbed(
				uuidStruct.nameMcHyperLink() +
				" is already blacklisted with reason `" +
				higherDepth(blacklistedUser, "reason").getAsString() +
				"`"
			);
		}

		currentBlacklist.add(gson.toJsonTree(new BlacklistEntry(uuidStruct.username(), uuidStruct.uuid(), reason)));

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setBlacklist(currentBlacklist);
		return defaultSettingsEmbed("Blacklisted " + uuidStruct.nameMcHyperLink() + " with reason `" + reason + "`");
	}

	public EmbedBuilder searchBlacklist(String username) {
		List<JsonElement> blacklist = streamJsonArray(guildMap.get(guild.getId()).getBlacklist())
			.collect(Collectors.toCollection(ArrayList::new));
		if (blacklist.isEmpty()) {
			return errorEmbed("Blacklist is empty");
		}

		EmbedBuilder eb = defaultSettingsEmbed();
		for (BoundExtractedResult<JsonElement> match : FuzzySearch.extractTop(
			username,
			blacklist,
			element -> higherDepth(element, "username").getAsString(),
			5
		)) {
			JsonElement referent = match.getReferent();
			String thisUser = higherDepth(referent, "username").getAsString();
			eb.addField(
				escapeText(thisUser),
				"Reason: " +
				higherDepth(referent, "reason").getAsString() +
				"\nNameMC: " +
				nameMcHyperLink(thisUser, higherDepth(referent, "uuid").getAsString()),
				false
			);
		}
		return eb;
	}

	public EmbedBuilder enableBlacklistFeature(String feature) {
		feature = feature.toLowerCase();
		if (!feature.equals("verify") && !feature.equals("apply")) {
			return errorEmbed("Invalid feature type");
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray features = higherDepth(blacklistSettings, "features").getAsJsonArray();
		if (!features.contains(new JsonPrimitive(feature))) {
			features.add(feature);
		}

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}
		guildMap.get(guild.getId()).setBlacklistFeatures(features);

		return defaultSettingsEmbed("Blacklist enabled for the " + feature + " feature");
	}

	public EmbedBuilder disableBlacklistFeature(String feature) {
		feature = feature.toLowerCase();
		if (!feature.equals("verify") && !feature.equals("apply")) {
			return errorEmbed("Invalid feature type");
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray features = higherDepth(blacklistSettings, "features").getAsJsonArray();
		features.remove(new JsonPrimitive(feature));

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}
		guildMap.get(guild.getId()).setBlacklistFeatures(features);

		return defaultSettingsEmbed("Blacklist disabled for the " + feature + " feature");
	}

	public EmbedBuilder useBlacklist(String serverId) {
		JsonElement otherBlacklist = database.getBlacklistSettings(serverId);
		if (otherBlacklist == null || otherBlacklist.isJsonNull()) {
			return errorEmbed("Invalid server");
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray isUsing = higherDepth(blacklistSettings, "isUsing").getAsJsonArray();

		if (isUsing.size() == 6) {
			return errorEmbed("You have reached the max number of shared blacklists (6/6)");
		}

		if (streamJsonArray(isUsing).anyMatch(g -> g.getAsString().equals(serverId))) {
			return errorEmbed("You are already using the provided servers blacklist");
		}

		if (streamJsonArray(higherDepth(otherBlacklist, "canUse")).noneMatch(g -> g.getAsString().equals(guild.getId()))) {
			return errorEmbed("The provided server has not shared their blacklist with this server");
		}

		isUsing.add(serverId);

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}
		guildMap.get(guild.getId()).setIsUsing(isUsing);

		return defaultSettingsEmbed("Using the blacklist of " + jda.getGuildById(serverId).getName());
	}

	public EmbedBuilder shareBlacklist(String serverId) {
		Guild toShareGuild = null;
		try {
			toShareGuild = jda.getGuildById(serverId);
		} catch (Exception ignored) {}
		if (toShareGuild == null) {
			return errorEmbed("Invalid server id provided");
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray canUse = higherDepth(blacklistSettings, "canUse").getAsJsonArray();

		if (canUse.size() == 6) {
			return errorEmbed("You have reached the max number of shared blacklists (6/6)");
		}

		if (streamJsonArray(canUse).anyMatch(g -> g.getAsString().equals(serverId))) {
			return errorEmbed("You are already sharing the blacklist with the provided server");
		}

		canUse.add(serverId);

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Shared blacklist with " + toShareGuild.getName());
	}

	public EmbedBuilder unshareBlacklist(String serverId) {
		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray canUse = higherDepth(blacklistSettings, "canUse").getAsJsonArray();

		if (streamJsonArray(canUse).noneMatch(g -> g.getAsString().equals(serverId))) {
			return errorEmbed("You are not sharing the blacklist with the provided server");
		}

		canUse.remove(new JsonPrimitive(serverId));

		JsonObject otherBlacklist = database.getBlacklistSettings(serverId).getAsJsonObject();
		JsonArray isUsing = higherDepth(otherBlacklist, "isUsing").getAsJsonArray();
		isUsing.remove(new JsonPrimitive(guild.getId()));
		database.setBlacklistSettings(serverId, otherBlacklist);
		guildMap.get(serverId).setIsUsing(isUsing);

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Stopped sharing blacklist with " + jda.getGuildById(serverId).getName());
	}

	public EmbedBuilder stopUsingBlacklist(String serverId) {
		JsonElement otherBlacklist = database.getBlacklistSettings(serverId);
		if (otherBlacklist == null || otherBlacklist.isJsonNull()) {
			return errorEmbed("Invalid server");
		}

		JsonObject blacklistSettings = getBlacklistSettings();
		JsonArray isUsing = higherDepth(blacklistSettings, "isUsing").getAsJsonArray();

		if (streamJsonArray(isUsing).noneMatch(g -> g.getAsString().equals(serverId))) {
			return errorEmbed("You are not using the provided servers blacklist");
		}

		isUsing.remove(new JsonPrimitive(serverId));

		int responseCode = database.setBlacklistSettings(guild.getId(), blacklistSettings);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(serverId).setIsUsing(isUsing);

		return defaultSettingsEmbed("Stopped using the blacklist of " + jda.getGuildById(serverId).getName());
	}

	public JsonObject getBlacklistSettings() {
		return higherDepth(serverSettings, "blacklist").getAsJsonObject();
	}

	public EmbedBuilder setFetchurChannel(String channelMention) {
		if (channelMention.equalsIgnoreCase("none")) {
			int responseCode = database.setFetchurChannel(guild.getId(), "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).setFetchurChannel(null);

			return defaultSettingsEmbed("**Fetchur notifications disabled**");
		}

		Object eb = checkTextChannel(channelMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) eb;

		int responseCode = database.setFetchurChannel(guild.getId(), channel.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setFetchurChannel(channel);

		return defaultSettingsEmbed("**Fetchur notifications channel set to:** " + channel.getAsMention());
	}

	public EmbedBuilder setApplyGuestRole(String roleMention) {
		if (roleMention.equalsIgnoreCase("none")) {
			int responseCode = database.setApplyGuestRole(guild.getId(), "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).setApplyGuestRole(null);

			return defaultSettingsEmbed("Set guest role to: none");
		}

		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		if (
			database
				.getAllGuildSettings(guild.getId())
				.stream()
				.noneMatch(g -> g != null && g.getGuildMemberRoleEnable() != null && g.getGuildMemberRoleEnable().equals("true"))
		) {
			return errorEmbed("There must be at least one enabled guild member role to set a guest role");
		}

		int responseCode = database.setApplyGuestRole(guild.getId(), role.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setApplyGuestRole(role);

		return defaultSettingsEmbed("Set guest role to: " + role.getAsMention());
	}

	public EmbedBuilder setFetchurPing(String roleMention) {
		if (roleMention.equalsIgnoreCase("none")) {
			int responseCode = database.setFetchurRole(guild.getId(), "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).setFetchurPing(null);

			return defaultSettingsEmbed("Set fetchur ping to: none");
		}

		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		int responseCode = database.setFetchurRole(guild.getId(), role.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setFetchurPing(role);

		return defaultSettingsEmbed("Set fetchur ping to: " + role.getAsMention());
	}

	public EmbedBuilder setMayorChannel(String channelMention) {
		if (channelMention.equalsIgnoreCase("none")) {
			int responseCode = database.setMayorChannel(guild.getId(), "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).setMayorChannel(null);

			return defaultSettingsEmbed("**Mayor notifications disabled**");
		}

		Object eb = checkTextChannel(channelMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) eb;

		int responseCode = database.setMayorChannel(guild.getId(), channel.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setMayorChannel(channel);

		return defaultSettingsEmbed("**Mayor notifications channel set to:** " + channel.getAsMention());
	}

	public EmbedBuilder setLogChannel(String channelMention) {
		if (channelMention.equalsIgnoreCase("none")) {
			int responseCode = database.setLogChannel(guild.getId(), "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).setLogChannel(null);

			return defaultSettingsEmbed("**Logging disabled**");
		}

		Object eb = checkTextChannel(channelMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		TextChannel channel = (TextChannel) eb;

		int responseCode = database.setLogChannel(guild.getId(), channel.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		validLogEvents.forEach(this::addLogEvent);

		guildMap.get(guild.getId()).setLogChannel(channel);
		return defaultSettingsEmbed("**Log channel set to:** " + channel.getAsMention());
	}

	public EmbedBuilder addLogEvent(String logEvent) {
		if (!validLogEvents.contains(logEvent)) {
			return errorEmbed("Invalid log event");
		}

		JsonArray curEvents = higherDepth(serverSettings, "logEvents").getAsJsonArray();
		curEvents.remove(new JsonPrimitive(logEvent));
		curEvents.add(logEvent);

		int responseCode = database.setLogEvents(guild.getId(), curEvents);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setLogEvents(gson.fromJson(curEvents, new TypeToken<List<String>>() {}.getType()));

		return defaultSettingsEmbed("**Added log event:** " + logEvent);
	}

	public EmbedBuilder removeLogEvent(String logEvent) {
		if (!validLogEvents.contains(logEvent)) {
			return errorEmbed("Invalid log event");
		}

		JsonArray curEvents = higherDepth(serverSettings, "logEvents").getAsJsonArray();
		curEvents.remove(new JsonPrimitive(logEvent));

		int responseCode = database.setLogEvents(guild.getId(), curEvents);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setLogEvents(gson.fromJson(curEvents, new TypeToken<List<String>>() {}.getType()));

		return defaultSettingsEmbed("**Removed log event:** " + logEvent);
	}

	public EmbedBuilder setMayorPing(String roleMention) {
		if (roleMention.equalsIgnoreCase("none")) {
			int responseCode = database.setMayorRole(guild.getId(), "none");
			if (responseCode != 200) {
				return apiFailMessage(responseCode);
			}

			guildMap.get(guild.getId()).setMayorPing(null);

			return defaultSettingsEmbed("Set mayor ping to: none");
		}

		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		int responseCode = database.setMayorRole(guild.getId(), role.getId());
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setMayorPing(role);

		return defaultSettingsEmbed("Set mayor ping to: " + role.getAsMention());
	}

	public EmbedBuilder setSyncUnlinked(boolean syncUnlinked) {
		int responseCode = database.setSyncUnlinkedMembers(guild.getId(), "" + syncUnlinked);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		return defaultSettingsEmbed("Sync unlinked members: " + (syncUnlinked ? "enabled" : "disabled"));
	}

	public EmbedBuilder addBotManagerRole(String roleMention) {
		Object eb = checkRole(roleMention);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = (Role) eb;

		JsonArray curBotRoles = higherDepth(serverSettings, "botManagerRoles").getAsJsonArray();
		curBotRoles.asList().removeIf(e -> e.getAsString().equals(role.getId()) || !isValidRole(e.getAsString()));
		curBotRoles.add(role.getId());

		int responseCode = database.setBotManagerRoles(guild.getId(), curBotRoles);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setBotManagerRoles(gson.fromJson(curBotRoles, new TypeToken<List<String>>() {}.getType()));

		return defaultSettingsEmbed("Added bot manager role: " + role.getAsMention());
	}

	public EmbedBuilder removeBotManagerRole(String roleMention) {
		Object eb = checkRole(roleMention, true);
		if (eb instanceof EmbedBuilder e) {
			return e;
		}
		Role role = ((Role) eb);

		JsonArray curBotRoles = higherDepth(serverSettings, "botManagerRoles").getAsJsonArray();
		curBotRoles.asList().removeIf(e -> e.getAsString().equals(role.getId()) || !isValidRole(e.getAsString()));

		int responseCode = database.setBotManagerRoles(guild.getId(), curBotRoles);
		if (responseCode != 200) {
			return apiFailMessage(responseCode);
		}

		guildMap.get(guild.getId()).setBotManagerRoles(gson.fromJson(curBotRoles, new TypeToken<List<String>>() {}.getType()));

		return defaultSettingsEmbed("Removed bot manager role: " + role.getAsMention());
	}

	/* Helper functions */
	public String displaySettings(JsonElement jsonSettings, String settingName) {
		if (higherDepth(jsonSettings, settingName) != null) {
			switch (settingName) {
				case "applyReqs" -> {
					JsonArray reqs = higherDepth(jsonSettings, settingName).getAsJsonArray();
					if (reqs.isEmpty()) {
						return "None";
					}
					StringBuilder reqsString = new StringBuilder();
					for (int i = 0; i < reqs.size(); i++) {
						reqsString.append("\n").append("`").append(i + 1).append(")`");

						for (Entry<String, JsonElement> reqEntry : higherDepth(reqs.get(i), "requirements").getAsJsonObject().entrySet()) {
							reqsString
								.append(formatNumber(reqEntry.getValue().getAsLong()))
								.append(" ")
								.append(
									switch (reqEntry.getKey()) {
										case "catacombs" -> "cata";
										case "level" -> "lvl";
										case "networth" -> "nw";
										default -> reqEntry.getKey().replace("_", " ");
									}
								)
								.append(", ");
						}
					}
					String reqsStr = reqsString.toString().trim();
					if (reqsStr.endsWith(",")) {
						reqsStr = reqsStr.substring(0, reqsStr.length() - 1);
					}
					return reqsStr;
				}
				case "verifiedRoles", "applyStaffRoles" -> {
					JsonArray roles = higherDepth(jsonSettings, settingName).getAsJsonArray();
					StringBuilder ebStr = new StringBuilder();
					for (JsonElement role : roles) {
						ebStr.append("<@&").append(role.getAsString()).append(">").append(" ");
					}

					if (ebStr.isEmpty()) {
						ebStr = new StringBuilder("None");
					}

					return ebStr.toString();
				}
				case "crops" -> {
					JsonArray roles = higherDepth(jsonSettings, settingName).getAsJsonArray();
					List<String> ebStr = new ArrayList<>();
					for (JsonElement role : roles) {
						ebStr.add(
							"• " +
							higherDepth(role, "value").getAsString() +
							" - " +
							"<@&" +
							higherDepth(role, "roleId").getAsString() +
							">"
						);
					}

					if (ebStr.isEmpty()) {
						return "None";
					}

					return "\n\u200B \u200B  " + String.join("\n\u200B \u200B  ", ebStr);
				}
				case "events" -> {
					JsonArray roles = higherDepth(jsonSettings, settingName).getAsJsonArray();
					List<String> ebStr = new ArrayList<>();
					for (JsonElement role : roles) {
						ebStr.add(
							"• " +
							higherDepth(role, "value").getAsString() +
							" - " +
							"<#" +
							higherDepth(role, "channelId").getAsString() +
							"> - " +
							"<@&" +
							higherDepth(role, "roleId").getAsString() +
							">"
						);
					}

					if (ebStr.isEmpty()) {
						return "None";
					}

					return "\n\u200B \u200B  " + String.join("\n\u200B \u200B  ", ebStr);
				}
			}

			String currentSettingValue = higherDepth(jsonSettings, settingName).getAsString();
			if (currentSettingValue.equals("none")) {
				return "None";
			}
			if (!currentSettingValue.isEmpty()) {
				switch (settingName) {
					case "applyMessageChannel",
						"applyWaitingChannel",
						"applyStaffChannel",
						"messageTextChannelId",
						"channel",
						"applyCategory",
						"guildCounterChannel" -> {
						return "<#" + currentSettingValue + ">";
					}
					case "roleId", "guildMemberRole", "verifiedRemoveRole" -> {
						return "<@&" + currentSettingValue + ">";
					}
					case "applyEnable", "enable", "guildMemberRoleEnable", "guildRanksEnable" -> {
						return currentSettingValue.equals("true") ? "• Enabled" : "• Disabled";
					}
					case "guildId" -> {
						try {
							return getGuildFromId(currentSettingValue).get("name").getAsString();
						} catch (Exception e) {
							return ("Error finding Hypixel guild associated with id: `" + currentSettingValue + "`");
						}
					}
					case "applyGamemode" -> {
						return currentSettingValue.replace("_", ", ");
					}
				}
				return currentSettingValue;
			}
		}
		return "None";
	}

	public EmbedBuilder apiFailMessage(int responseCode) {
		return errorEmbed("API returned response code of `" + responseCode + "`. Please report this to the developer.");
	}

	public EmbedBuilder defaultSettingsEmbed() {
		return defaultSettingsEmbed(null);
	}

	public EmbedBuilder defaultSettingsEmbed(String description) {
		return defaultEmbed("Settings").setDescription(description);
	}

	public boolean isValidRole(String roleMention) {
		return checkRole(roleMention) instanceof Role;
	}

	public Object checkRole(String roleMention) {
		return checkRole(roleMention, false);
	}

	public Object checkRole(String roleMention, boolean isRemove) {
		Role role;
		try {
			role = guild.getRoleById(roleMention.replaceAll("[<@&>]", ""));
		} catch (Exception e) {
			try {
				role = guild.getRolesByName(roleMention.replaceAll("[<@&>]", ""), true).get(0);
			} catch (Exception ex) {
				return errorEmbed("The provided role is invalid");
			}
		}

		if (role == null) {
			return errorEmbed("The provided role does not exist");
		} else if (!isRemove) {
			if (role.isPublicRole()) {
				return errorEmbed("The role cannot be the everyone role");
			} else if (role.isManaged()) {
				return errorEmbed("The role cannot be a managed role");
			}
		}

		return role;
	}

	public Object checkMember(String memberMention) {
		Member member;
		try {
			member = guild.getMemberById(memberMention.replaceAll("[<@!>]", ""));
		} catch (Exception e) {
			try {
				member = guild.getMembersByName(memberMention.replaceAll("[<@!>]", ""), true).get(0);
			} catch (Exception ex) {
				return errorEmbed("The provided member is invalid");
			}
		}

		if (member == null) {
			return errorEmbed("The provided member does not exist");
		} else if (member.getUser().isBot()) {
			return errorEmbed("The member cannot be a bot");
		}

		return member;
	}

	public boolean isValidChannelCategory(String categoryMention) {
		return checkChannelCategory(categoryMention) instanceof Category;
	}

	public Object checkChannelCategory(String categoryMention) {
		Category category;
		try {
			category = guild.getCategoryById(categoryMention.replaceAll("[<#>]", ""));
		} catch (Exception e) {
			try {
				category = guild.getCategoriesByName(categoryMention.replaceAll("[<#>]", ""), true).get(0);
			} catch (Exception ex) {
				return errorEmbed("The provided channel category is invalid");
			}
		}

		if (category == null) {
			return errorEmbed("The provided channel category does not exist");
		}

		return category;
	}

	public boolean isValidTextChannel(String channelMention) {
		return checkTextChannel(channelMention) instanceof TextChannel;
	}

	public Object checkTextChannel(String channelMention) {
		TextChannel channel;
		try {
			channel = guild.getTextChannelById(channelMention.replaceAll("[<#>]", ""));
		} catch (Exception e) {
			try {
				channel = guild.getTextChannelsByName(channelMention.replaceAll("[<#>]", ""), true).get(0);
			} catch (Exception ex) {
				return errorEmbed("The provided text channel is invalid");
			}
		}

		if (channel == null) {
			return errorEmbed("The provided text channel does not exist");
		} else if (!channel.canTalk()) {
			return errorEmbed("I am missing the necessary permissions to send messages in the provided channel");
		}

		return channel;
	}

	public MessageEditBuilder getHelpEmbed(String name) {
		return new MessageEditBuilder()
			.setEmbeds(errorEmbed("Invalid input. Run `/help " + name + "` or press the button below for help").build())
			.setActionRow(Button.primary("s_help_" + name, "Help"));
	}
}
