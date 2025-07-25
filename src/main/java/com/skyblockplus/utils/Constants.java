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
import com.google.gson.reflect.TypeToken;
import com.skyblockplus.Main;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.collections4.ListUtils;
import org.apache.groovy.util.Maps;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static com.skyblockplus.utils.utils.HttpUtils.getJson;
import static com.skyblockplus.utils.utils.JsonUtils.*;
import static com.skyblockplus.utils.utils.StringUtils.capitalizeString;
import static com.skyblockplus.utils.utils.Utils.*;

public class Constants {

	/* Hardcoded constants */
	public static final List<String> essenceTypes = List.of("undead", "wither", "dragon", "spider", "ice", "crimson", "diamond", "gold");
	public static final List<String> allAttributes = List.of(
		"ARACHNO",
		"ARACHNO_RESISTANCE",
		"ATTACK_SPEED",
		"BLAZING",
		"BLAZING_FORTUNE",
		"BLAZING_RESISTANCE",
		"BREEZE",
		"COMBO",
		"DEADEYE",
		"DOMINANCE",
		"DOUBLE_HOOK",
		"ELITE",
		"ENDER",
		"ENDER_RESISTANCE",
		"EXPERIENCE",
		"FISHERMAN",
		"FISHING_EXPERIENCE",
		"FISHING_SPEED",
		"FORTITUDE",
		"HUNTER",
		"IGNITION",
		"INFECTION",
		"LIFE_RECOVERY",
		"LIFE_REGENERATION",
		"LIFELINE",
		"MAGIC_FIND",
		"MANA_POOL",
		"MANA_REGENERATION",
		"MANA_STEAL",
		"MIDAS_TOUCH",
		"SPEED",
		"TROPHY_HUNTER",
		"UNDEAD",
		"UNDEAD_RESISTANCE",
		"VETERAN",
		"VITALITY",
		"WARRIOR"
	);
	public static final Map<String, String> dungeonLootChestToEmoji = Maps.of(
		"wood",
		"<:dungeon_wood_chest:1108163538905739336>",
		"gold",
		"<:dungeon_gold_chest:1108163535437049927>",
		"diamond",
		"<:dungeon_diamond_chest:1108163532693975141>",
		"emerald",
		"<:dungeon_emerald_chest:1108163534124236830>",
		"obsidian",
		"<:dungeon_obsidian_chest:1108163537462906930>",
		"bedrock",
		"<:dungeon_bedrock_chest:1108163530596827227>"
	);
	public static final Map<String, String> cropNameToEmoji = Maps.of(
		"Wheat",
		getEmoji("WHEAT"),
		"Carrot",
		getEmoji("CARROT_ITEM"),
		"Potato",
		getEmoji("POTATO_ITEM"),
		"Pumpkin",
		getEmoji("PUMPKIN"),
		"Melon",
		getEmoji("MELON"),
		"Mushroom",
		getEmoji("BROWN_MUSHROOM"),
		"Cactus",
		getEmoji("CACTUS"),
		"Sugar Cane",
		getEmoji("SUGAR_CANE"),
		"Nether Wart",
		getEmoji("NETHER_STALK"),
		"Cocoa Beans",
		getEmoji("INK_SACK:3")
	);
	public static final Map<String, String> mayorNameToEmoji = Maps.of(
		"DERPY",
		"<:derpy:940083649129349150>",
		"FOXY",
		"<:foxy:940083649301315614>",
		"DANTE",
		"<:dante:940083649188081715>",
		"PAUL",
		"<:paul:940083649607508009>",
		"AATROX",
		"<:aatrox:940083649041293312>",
		"DIAZ",
		"<:diaz:940083649322303489>",
		"DIANA",
		"<:diana:940083649590739004>",
		"COLE",
		"<:cole:940083649565581362>",
		"BARRY",
		"<:barry:940083649200652338>",
		"JERRY",
		"<:jerry:940083649318125578>",
		"SCORPIUS",
		"<:scorpius:940083649687203951>",
		"MARINA",
		"<:marina:940083649783664660>",
		"FINNEGAN",
		"<:finnegan:1040460674247184474>"
	);
	public static final Map<String, Double> tuningStatToMultiplier = Maps.of(
		"health",
		5.0,
		"walk_speed",
		1.5,
		"critical_chance",
		0.2,
		"attack_speed",
		0.2,
		"intelligence",
		2.0
	); // Default to 1.0 if not present
	public static final Map<String, String> statToEmoji = Maps.of(
		"attack_speed",
		"<:attack_speed:1129404088497483866>",
		"critical_chance",
		"<:critical_chance:1129404089785127004>",
		"critical_damage",
		"<:critical_damage:1129404091240546534>",
		"defense",
		"<:defense:1129404092297531483>",
		"health",
		"<:health:1129404093362880692>",
		"intelligence",
		"<:intelligence:1129404095065772153>",
		"mending",
		"<:mending:1129404095799762977>",
		"walk_speed",
		"<:speed:1129404097284542616>",
		"strength",
		"<:strength:1129404099134247062>",
		"true_defense",
		"<:true_defense:1129404189026570303>",
		"vitality",
		"<:vitality:1129404207015936140>",
		"mining_speed",
		"<:mining_speed:1129406819467530300>",
		"mining_fortune",
		"<:mining_fortune:1129406817861111878>",
		"farming_fortune",
		"<:farming_fortune:1129406816082722977>",
		"magic_find",
		"<:magic_find:1129407997718831114>",
		"sea_creature_chance",
		"<:sea_creature_chance:1129408001900556308>",
		"pet_luck",
		"<:pet_luck:1129408000201863211>",
		"damage",
		"<:damage:1129408624209436672>",
		"ferocity",
		"<:ferocity:1129408626088476755>",
		"fishing_speed",
		"<:fishing_speed:1129411950003101838>"
	);
	public static final Map<String, Integer> rarityToMagicPower = Maps.of(
		"COMMON",
		3,
		"UNCOMMON",
		5,
		"RARE",
		8,
		"EPIC",
		12,
		"LEGENDARY",
		16,
		"MYTHIC",
		22,
		"SPECIAL",
		3,
		"VERY_SPECIAL",
		5
	);
	public static final Map<String, String> collectionNameToId = Maps.of(
		"cocoa_beans",
		"INK_SACK:3",
		"carrot",
		"CARROT_ITEM",
		"cactus",
		"CACTUS",
		"raw_chicken",
		"RAW_CHICKEN",
		"sugar_cane",
		"SUGAR_CANE",
		"pumpkin",
		"PUMPKIN",
		"wheat",
		"WHEAT",
		"seeds",
		"SEEDS",
		"mushroom",
		"MUSHROOM_COLLECTION",
		"raw_rabbit",
		"RABBIT",
		"nether_wart",
		"NETHER_STALK",
		"mutton",
		"MUTTON",
		"melon",
		"MELON",
		"potato",
		"POTATO_ITEM",
		"leather",
		"LEATHER",
		"raw_porkchop",
		"PORK",
		"feather",
		"FEATHER",
		"lapis_lazuli",
		"INK_SACK:4",
		"redstone",
		"REDSTONE",
		"coal",
		"COAL",
		"mycelium",
		"MYCEL",
		"end_stone",
		"ENDER_STONE",
		"nether_quartz",
		"QUARTZ",
		"sand",
		"SAND",
		"iron_ingot",
		"IRON_INGOT",
		"gemstone",
		"GEMSTONE_COLLECTION",
		"obsidian",
		"OBSIDIAN",
		"diamond",
		"DIAMOND",
		"cobblestone",
		"COBBLESTONE",
		"glowstone_dust",
		"GLOWSTONE_DUST",
		"gold_ingot",
		"GOLD_INGOT",
		"gravel",
		"GRAVEL",
		"hard_stone",
		"HARD_STONE",
		"mithril",
		"MITHRIL_ORE",
		"emerald",
		"EMERALD",
		"red_sand",
		"SAND:1",
		"ice",
		"ICE",
		"sulphur",
		"SULPHUR_ORE",
		"netherrack",
		"NETHERRACK",
		"ender_pearl",
		"ENDER_PEARL",
		"slimeball",
		"SLIME_BALL",
		"magma_cream",
		"MAGMA_CREAM",
		"ghast_tear",
		"GHAST_TEAR",
		"gunpowder",
		"SULPHUR",
		"rotten_flesh",
		"ROTTEN_FLESH",
		"spider_eye",
		"SPIDER_EYE",
		"bone",
		"BONE",
		"blaze_rod",
		"BLAZE_ROD",
		"string",
		"STRING",
		"acacia_wood",
		"LOG_2",
		"spruce_wood",
		"LOG:1",
		"jungle_wood",
		"LOG:3",
		"birch_wood",
		"LOG:2",
		"oak_wood",
		"LOG",
		"dark_oak_wood",
		"LOG_2:1",
		"lily_pad",
		"WATER_LILY",
		"prismarine_shard",
		"PRISMARINE_SHARD",
		"ink_sack",
		"INK_SACK",
		"raw_fish",
		"RAW_FISH",
		"pufferfish",
		"RAW_FISH:3",
		"clownfish",
		"RAW_FISH:2",
		"raw_salmon",
		"RAW_FISH:1",
		"magmafish",
		"MAGMA_FISH",
		"prismarine_crystals",
		"PRISMARINE_CRYSTALS",
		"clay",
		"CLAY_BALL",
		"sponge",
		"SPONGE"
	);
	public static final List<String> skyblockStats = List.of(
		"deaths",
		"deaths_void",
		"kills",
		"kills_emerald_slime",
		"auctions_bids",
		"auctions_highest_bid",
		"kills_zombie",
		"auctions_won",
		"auctions_bought_rare",
		"auctions_gold_spent",
		"kills_chicken",
		"deaths_zombie",
		"deaths_skeleton",
		"highest_crit_damage",
		"kills_skeleton",
		"kills_spider",
		"auctions_bought_uncommon",
		"kills_diamond_skeleton",
		"kills_diamond_zombie",
		"kills_zombie_villager",
		"kills_redstone_pigman",
		"kills_invisible_creeper",
		"kills_witch",
		"items_fished",
		"items_fished_normal",
		"kills_sea_walker",
		"kills_pond_squid",
		"items_fished_large_treasure",
		"kills_sea_guardian",
		"items_fished_treasure",
		"kills_unburried_zombie",
		"deaths_unburried_zombie",
		"kills_ruin_wolf",
		"kills_horseman_zombie",
		"kills_lapis_zombie",
		"deaths_fire",
		"kills_splitter_spider",
		"kills_weaver_spider",
		"kills_voracious_spider",
		"kills_splitter_spider_silverfish",
		"kills_jockey_shot_silverfish",
		"kills_dasher_spider",
		"kills_jockey_skeleton",
		"kills_spider_jockey",
		"kills_wither_skeleton",
		"deaths_wither_skeleton",
		"kills_fireball_magma_cube",
		"kills_rabbit",
		"kills_sheep",
		"end_race_best_time",
		"deaths_fall",
		"deaths_spider",
		"kills_pig",
		"kills_cow",
		"auctions_bought_epic",
		"kills_enderman",
		"kills_random_slime",
		"kills_respawning_skeleton",
		"auctions_created",
		"auctions_fees",
		"auctions_completed",
		"auctions_sold_common",
		"auctions_gold_earned",
		"kills_watcher",
		"kills_zealot_enderman",
		"kills_obsidian_wither",
		"kills_endermite",
		"deaths_unknown",
		"auctions_sold_epic",
		"kills_bat_pinata",
		"deaths_drowning",
		"kills_blaze",
		"auctions_sold_special",
		"kills_generator_ghast",
		"kills_old_wolf",
		"auctions_bought_common",
		"deaths_wolf",
		"kills_magma_cube",
		"kills_pigman",
		"kills_pack_spirit",
		"kills_howling_spirit",
		"kills_soul_of_the_alpha",
		"kills_night_respawning_skeleton",
		"auctions_sold_rare",
		"highest_critical_damage",
		"kills_sea_archer",
		"kills_zombie_deep",
		"kills_catfish",
		"kills_chicken_deep",
		"deaths_old_wolf",
		"auctions_sold_uncommon",
		"auctions_sold_legendary",
		"ender_crystals_destroyed",
		"kills_wise_dragon",
		"kills_unstable_dragon",
		"kills_strong_dragon",
		"kills_protector_dragon",
		"gifts_received",
		"gifts_given",
		"kills_liquid_hot_magma",
		"most_winter_snowballs_hit",
		"most_winter_damage_dealt",
		"most_winter_magma_damage_dealt",
		"deaths_player",
		"deaths_liquid_hot_magma",
		"deaths_magma_cube",
		"kills_night_squid",
		"deaths_sea_leech",
		"kills_old_dragon",
		"deaths_strong_dragon",
		"deaths_superior_dragon",
		"kills_sea_leech",
		"kills_brood_mother_spider",
		"kills_brood_mother_cave_spider",
		"auctions_no_bids",
		"kills_young_dragon",
		"kills_superior_dragon",
		"auctions_bought_legendary",
		"kills_cave_spider",
		"kills_player",
		"dungeon_hub_giant_mushroom_anything_no_return_best_time",
		"dungeon_hub_precursor_ruins_anything_no_return_best_time",
		"dungeon_hub_crystal_core_anything_no_return_best_time",
		"kills_zombie_grunt",
		"kills_skeleton_grunt",
		"kills_dungeon_respawning_skeleton",
		"kills_crypt_lurker",
		"kills_crypt_dreadlord",
		"kills_crypt_tank_zombie",
		"kills_scared_skeleton",
		"kills_diamond_guy",
		"deaths_lost_adventurer",
		"kills_crypt_souleater",
		"kills_skeleton_soldier",
		"kills_crypt_undead",
		"kills_watcher_summon_undead",
		"kills_bonzo_summon_undead",
		"kills_lost_adventurer",
		"deaths_blaze",
		"deaths_enderman",
		"deaths_lapis_zombie",
		"deaths_ruin_wolf",
		"deaths_emerald_slime",
		"deaths_weaver_spider",
		"deaths_dasher_spider",
		"deaths_diamond_zombie",
		"deaths_splitter_spider",
		"deaths_splitter_spider_silverfish",
		"deaths_redstone_pigman",
		"deaths_spider_jockey",
		"deaths_diamond_skeleton",
		"deaths_fireball_magma_cube",
		"deaths_zombie_deep",
		"deaths_watcher",
		"deaths_obsidian_wither",
		"deaths_endermite",
		"kills_generator_slime",
		"kills_slime",
		"kills_ghast",
		"deaths_generator_slime",
		"deaths_zealot_enderman",
		"deaths_old_dragon",
		"deaths_wise_dragon",
		"kills_forest_island_bat",
		"kills_magma_cube_boss",
		"deaths_magma_cube_boss",
		"kills_generator_magma_cube",
		"deaths_cave_spider",
		"kills_sea_witch",
		"kills_creeper",
		"kills_guardian_defender",
		"kills_deep_sea_protector",
		"deaths_water_hydra",
		"kills_water_hydra",
		"deaths_protector_dragon",
		"chicken_race_best_time",
		"kills_frozen_steve",
		"kills_frosty_the_snowman",
		"chicken_race_best_time_2",
		"kills_guardian_emperor",
		"kills_skeleton_emperor",
		"kills_carrot_king",
		"kills_yeti",
		"deaths_yeti",
		"deaths_pack_spirit",
		"deaths_soul_of_the_alpha",
		"shredder_bait",
		"shredder_fished",
		"kills_grinch",
		"deaths_guardian_emperor",
		"auctions_bought_special",
		"foraging_race_best_time",
		"pet_milestone_sea_creatures_killed",
		"pet_milestone_ores_mined",
		"deaths_revenant_zombie",
		"kills_revenant_zombie",
		"dungeon_hub_crystal_core_no_pearls_no_return_best_time",
		"dungeon_hub_crystal_core_no_abilities_no_return_best_time",
		"kills_cellar_spider",
		"kills_sniper_skeleton",
		"deaths_watcher_summon_undead",
		"deaths_crypt_lurker",
		"kills_corrupted_protector",
		"deaths_skeleton_emperor",
		"kills_horseman_bat",
		"deaths_corrupted_protector",
		"dungeon_hub_precursor_ruins_no_pearls_no_return_best_time",
		"dungeon_hub_precursor_ruins_anything_with_return_best_time",
		"dungeon_hub_precursor_ruins_no_pearls_with_return_best_time",
		"kills_horseman_horse",
		"kills_dungeon_secret_bat",
		"kills_skeleton_master",
		"deaths_scarf_warrior",
		"kills_scarf_warrior",
		"kills_scarf_mage",
		"deaths_crypt_dreadlord",
		"deaths_skeleton_soldier",
		"kills_parasite",
		"deaths_scarf",
		"kills_lonely_spider",
		"deaths_scarf_mage",
		"kills_scarf_priest",
		"kills_blaze_higher_or_lower",
		"kills_zombie_soldier",
		"deaths_skeletor",
		"deaths_skeleton_grunt",
		"kills_scarf_archer",
		"deaths_crypt_souleater",
		"deaths_skeleton_master",
		"kills_dungeon_respawning_skeleton_skull",
		"deaths_trap",
		"kills_crypt_undead_pieter",
		"kills_crypt_undead_valentin",
		"kills_shadow_assassin",
		"kills_skeletor",
		"deaths_shadow_assassin",
		"deaths_deathmite",
		"kills_watcher_bonzo",
		"kills_professor_guardian_summon",
		"deaths_professor",
		"kills_zombie_knight",
		"deaths_professor_mage_guardian",
		"deaths_scared_skeleton",
		"kills_crypt_undead_christian",
		"deaths_diamond_guy",
		"dungeon_hub_giant_mushroom_no_pearls_no_return_best_time",
		"kills_crypt_undead_nicholas",
		"kills_crypt_undead_bernhard",
		"kills_crypt_undead_friedrich",
		"kills_crypt_undead_alexander",
		"kills_crypt_undead_marius",
		"kills_king_midas",
		"dungeon_hub_giant_mushroom_anything_with_return_best_time",
		"dungeon_hub_giant_mushroom_no_pearls_with_return_best_time",
		"dungeon_hub_crystal_core_anything_with_return_best_time",
		"dungeon_hub_crystal_core_no_pearls_with_return_best_time",
		"kills_deathmite",
		"deaths_suffocation",
		"deaths_young_dragon",
		"deaths_deep_sea_protector",
		"kills_tarantula_spider",
		"deaths_tarantula_spider",
		"most_winter_cannonballs_hit",
		"deaths_dungeon_respawning_skeleton",
		"deaths_professor_guardian_summon",
		"deaths_sniper_skeleton",
		"deaths_wither",
		"deaths_unstable_dragon",
		"deaths_generator_ghast",
		"dungeon_hub_giant_mushroom_no_abilities_no_return_best_time",
		"dungeon_hub_giant_mushroom_nothing_no_return_best_time",
		"deaths_pigman",
		"deaths_catfish",
		"deaths_guardian_defender",
		"dungeon_hub_crystal_core_nothing_no_return_best_time",
		"dungeon_hub_crystal_core_no_abilities_with_return_best_time",
		"dungeon_hub_crystal_core_nothing_with_return_best_time",
		"dungeon_hub_giant_mushroom_no_abilities_with_return_best_time",
		"dungeon_hub_giant_mushroom_nothing_with_return_best_time",
		"dungeon_hub_precursor_ruins_no_abilities_no_return_best_time",
		"dungeon_hub_precursor_ruins_nothing_no_return_best_time",
		"dungeon_hub_precursor_ruins_no_abilities_with_return_best_time",
		"dungeon_hub_precursor_ruins_nothing_with_return_best_time",
		"deaths_zombie_villager",
		"deaths_howling_spirit",
		"deaths_professor_archer_guardian",
		"deaths_sea_guardian",
		"deaths_respawning_skeleton",
		"deaths_jockey_shot_silverfish",
		"kills_headless_horseman",
		"deaths_king_midas",
		"kills_super_archer",
		"kills_crypt_witherskeleton",
		"kills_spirit_wolf",
		"kills_spirit_bull",
		"kills_spirit_rabbit",
		"kills_spirit_chicken",
		"kills_spirit_bat",
		"kills_spirit_sheep",
		"deaths_spirit_chicken",
		"kills_spirit_miniboss",
		"deaths_spirit_bat",
		"kills_super_tank_zombie",
		"kills_watcher_scarf",
		"deaths_spirit_wolf",
		"kills_thorn",
		"deaths_spirit_miniboss",
		"deaths_spirit_rabbit",
		"deaths_spirit_bull",
		"deaths_watcher_scarf",
		"deaths_watcher_guardian",
		"deaths_zombie_knight",
		"deaths_crypt_tank_zombie",
		"deaths_zombie_soldier",
		"deaths_super_archer",
		"deaths_watcher_bonzo",
		"deaths_lonely_spider",
		"deaths_crypt_undead",
		"mythos_burrows_dug_next",
		"mythos_burrows_dug_next_common",
		"mythos_burrows_dug_combat",
		"mythos_burrows_dug_combat_common",
		"mythos_kills",
		"kills_siamese_lynx",
		"kills_minos_hunter",
		"mythos_burrows_dug_treasure",
		"mythos_burrows_dug_treasure_common",
		"mythos_burrows_chains_complete",
		"mythos_burrows_chains_complete_common",
		"mythos_burrows_dug_next_uncommon",
		"mythos_burrows_dug_combat_uncommon",
		"kills_minotaur",
		"mythos_burrows_dug_treasure_uncommon",
		"mythos_burrows_chains_complete_uncommon",
		"mythos_burrows_dug_next_rare",
		"mythos_burrows_dug_combat_rare",
		"kills_gaia_construct",
		"deaths_gaia_construct",
		"mythos_burrows_dug_next_legendary",
		"mythos_burrows_dug_combat_legendary",
		"deaths_minos_champion",
		"deaths_minotaur",
		"mythos_burrows_dug_treasure_legendary",
		"mythos_burrows_chains_complete_legendary",
		"kills_minos_champion",
		"kills_minos_inquisitor",
		"mythos_burrows_dug_next_null",
		"mythos_burrows_dug_combat_null",
		"mythos_burrows_dug_treasure_rare",
		"mythos_burrows_chains_complete_rare",
		"mythos_burrows_dug_next_epic",
		"mythos_burrows_dug_treasure_epic",
		"mythos_burrows_dug_combat_epic",
		"mythos_burrows_chains_complete_epic",
		"mythos_burrows_dug_treasure_null",
		"mythos_burrows_chains_complete_null",
		"deaths_minos_inquisitor",
		"kills_nurse_shark",
		"kills_tiger_shark",
		"kills_great_white_shark",
		"kills_blue_shark",
		"deaths_great_white_shark",
		"kills_tentaclees",
		"deaths_livid_clone",
		"deaths_livid",
		"deaths_tentaclees",
		"deaths_bonzo_summon_undead",
		"deaths_professor_warrior_guardian",
		"auctions_bought_mythic",
		"kills_watcher_livid",
		"deaths_sadan_statue",
		"kills_zombie_commander",
		"kills_sadan_statue",
		"kills_sadan_giant",
		"kills_sadan_golem",
		"kills_skeletor_prime",
		"deaths_sadan_giant",
		"deaths_sadan_golem",
		"deaths_sadan",
		"kills_mimic",
		"deaths_siamese_lynx",
		"deaths_bonzo",
		"deaths_skeletor_prime",
		"kills_wraith",
		"kills_wither_gourd",
		"kills_scary_jerry",
		"kills_phantom_spirit",
		"kills_trick_or_treater",
		"kills_bat_spooky",
		"kills_batty_witch",
		"kills_witch_bat",
		"kills_scarecrow",
		"kills_nightmare",
		"kills_phantom_fisherman",
		"kills_werewolf",
		"kills_grim_reaper",
		"auctions_sold_mythic",
		"deaths_crypt_witherskeleton",
		"kills_zombie_lord",
		"deaths_wither_miner",
		"kills_wither_miner",
		"kills_wither_guard",
		"deaths_maxor",
		"deaths_wither_guard",
		"kills_skeleton_lord",
		"kills_watcher_giant_laser",
		"kills_watcher_giant_boulder",
		"kills_watcher_giant_diamond",
		"kills_necron_guard",
		"deaths_watcher_livid",
		"deaths_necron_guard",
		"kills_watcher_giant_bigfoot",
		"deaths_watcher_giant_bigfoot",
		"deaths_watcher_giant_boulder",
		"deaths_super_tank_zombie",
		"deaths_crushed",
		"deaths_armor_stand",
		"kills_mayor_jerry_green",
		"kills_mayor_jerry_blue",
		"kills_mayor_jerry_purple",
		"kills_mayor_jerry_golden",
		"kills_ice_walker",
		"kills_goblin",
		"deaths_caverns_ghost",
		"kills_goblin_knife_thrower",
		"kills_goblin_weakling_melee",
		"kills_goblin_weakling_bow",
		"kills_treasure_hoarder",
		"kills_goblin_creepertamer",
		"kills_goblin_battler",
		"kills_goblin_murderlover",
		"kills_caverns_ghost",
		"kills_goblin_creeper",
		"kills_goblin_golem",
		"kills_crystal_sentry",
		"kills_powder_ghast",
		"deaths_goblin_murderlover",
		"deaths_goblin_knife_thrower",
		"deaths_ice_walker",
		"deaths_crypt_undead_hypixel",
		"deaths_crypt_undead_flameboy101",
		"deaths_goblin_weakling_bow",
		"kills_arachne_brood",
		"kills_arachne_keeper",
		"deaths_arachne",
		"kills_arachne",
		"deaths_arachne_brood",
		"deaths_arachne_keeper",
		"deaths_cellar_spider",
		"kills_master_sniper_skeleton",
		"kills_master_crypt_tank_zombie",
		"kills_master_zombie_grunt",
		"kills_master_crypt_lurker",
		"kills_master_scared_skeleton",
		"kills_master_skeleton_soldier",
		"kills_master_skeleton_grunt",
		"kills_master_crypt_souleater",
		"kills_master_dungeon_respawning_skeleton",
		"kills_master_lost_adventurer",
		"kills_master_crypt_dreadlord",
		"kills_master_cellar_spider",
		"kills_master_watcher_summon_undead",
		"deaths_master_watcher_summon_undead",
		"kills_master_bonzo_summon_undead",
		"deaths_master_bonzo",
		"deaths_spirit_sheep",
		"kills_master_crypt_undead",
		"kills_master_diamond_guy",
		"kills_master_skeleton_master",
		"deaths_master_scarf_archer",
		"kills_master_scarf_mage",
		"deaths_master_scarf",
		"kills_dante_goon",
		"kills_dante_slime_goon",
		"kills_rat",
		"kills_mushroom_cow",
		"kills_trapper_pig",
		"kills_trapper_chicken",
		"kills_trapper_sheep",
		"kills_trapper_cow",
		"kills_trapper_rabbit",
		"deaths_master_lost_adventurer",
		"deaths_zombie_grunt",
		"deaths_grim_reaper",
		"kills_oasis_sheep",
		"deaths_master_skeleton_soldier",
		"deaths_master_scarf_warrior",
		"kills_master_parasite",
		"kills_master_scarf_priest",
		"deaths_master_crypt_lurker",
		"kills_master_crypt_undead_bernhard",
		"kills_master_scarf_warrior",
		"kills_master_scarf_archer",
		"deaths_master_scarf_mage",
		"kills_master_skeletor",
		"deaths_master_skeletor",
		"kills_master_zombie_knight",
		"kills_master_zombie_soldier",
		"kills_master_lonely_spider",
		"deaths_master_watcher_bonzo",
		"kills_master_professor_guardian_summon",
		"deaths_master_professor_mage_guardian",
		"kills_master_super_tank_zombie",
		"kills_master_spirit_bat",
		"deaths_master_zombie_soldier",
		"deaths_master_livid_clone",
		"deaths_master_sniper_skeleton",
		"kills_master_watcher_bonzo",
		"deaths_master_spirit_bat",
		"deaths_master_spirit_sheep",
		"deaths_master_spirit_rabbit",
		"deaths_master_professor_guardian_summon",
		"kills_master_crypt_witherskeleton",
		"deaths_master_watcher_scarf",
		"kills_master_crypt_undead_pieter",
		"kills_master_spirit_wolf",
		"kills_master_spirit_rabbit",
		"kills_master_spirit_sheep",
		"kills_master_spirit_bull",
		"deaths_master_shadow_assassin",
		"deaths_master_spirit_chicken",
		"kills_master_tentaclees",
		"deaths_master_tentaclees",
		"kills_master_super_archer",
		"kills_master_shadow_assassin",
		"deaths_master_crypt_witherskeleton",
		"deaths_master_crypt_dreadlord",
		"deaths_master_skeleton_master",
		"deaths_master_zombie_knight",
		"deaths_master_livid",
		"deaths_master_crypt_souleater",
		"pumpkin_launcher_count",
		"kills_shrine_charged_creeper",
		"kills_shrine_skeleton_horseman",
		"deaths_master_diamond_guy",
		"deaths_master_dungeon_respawning_skeleton",
		"deaths_master_skeleton_grunt",
		"kills_oasis_rabbit",
		"kills_master_spirit_chicken",
		"kills_master_spirit_miniboss",
		"deaths_master_spirit_miniboss",
		"kills_voidling_fanatic",
		"kills_voidling_extremist",
		"deaths_voidling_extremist",
		"deaths_voidling_fanatic",
		"kills_voidling_enderman",
		"kills_thyst",
		"kills_sludge",
		"kills_automaton",
		"kills_key_guardian",
		"deaths_automaton",
		"kills_team_treasurite_viper",
		"kills_team_treasurite_sebastian",
		"kills_yog",
		"kills_goblin_flamethrower",
		"kills_team_treasurite_wendy",
		"kills_belle",
		"kills_fire_bat",
		"kills_worm",
		"kills_team_treasurite_grunt",
		"deaths_yog",
		"kills_silvo",
		"deaths_kalhuiki_tribe_man",
		"deaths_kalhuiki_tribe_woman",
		"kills_butterfly",
		"kills_cavitak",
		"deaths_sludge",
		"kills_trapped_sludge",
		"kills_team_treasurite_corleone",
		"kills_smog",
		"kills_vittomite",
		"kills_kalhuiki_elder",
		"kills_kalhuiki_tribe_man",
		"kills_kalhuiki_youngling",
		"kills_scatha",
		"deaths_master_crypt_tank_zombie",
		"kills_lava_pigman",
		"kills_lava_blaze",
		"kills_kalhuiki_tribe_woman",
		"deaths_zombie_commander",
		"deaths_entity",
		"kills_flaming_worm",
		"deaths_team_treasurite_corleone",
		"deaths_master_spirit_wolf",
		"kills_master_crypt_undead_valentin",
		"kills_master_crypt_undead_nicholas",
		"kills_master_crypt_undead_christian",
		"kills_master_crypt_undead_friedrich",
		"deaths_master_sadan_giant",
		"deaths_master_sadan_statue",
		"kills_master_skeletor_prime",
		"deaths_voracious_spider",
		"deaths_voidling_enderman",
		"total_pet_exp_gained",
		"highest_damage",
		"sea_creature_kills",
		"deaths_master_professor",
		"kills_water_worm",
		"kills_poisoned_water_worm",
		"deaths_goldor",
		"deaths_storm",
		"deaths_necron",
		"kills_master_sadan_statue",
		"deaths_master_skeletor_prime",
		"deaths_master_sadan_golem",
		"kills_master_mimic",
		"deaths_watcher_giant_diamond",
		"kills_master_crypt_undead_alexander",
		"kills_master_skeleton_lord",
		"kills_master_zombie_commander",
		"kills_master_wither_guard",
		"deaths_master_maxor",
		"kills_master_crypt_undead_marius",
		"deaths_master_super_archer",
		"kills_master_sadan_golem",
		"deaths_master_zombie_commander",
		"kills_master_wither_miner",
		"deaths_master_goldor",
		"kills_dive_ghast",
		"kills_intro_blaze",
		"kills_barbarian",
		"kills_fire_mage",
		"kills_charging_mushroom_cow",
		"kills_vanquisher",
		"kills_baby_magma_slug",
		"deaths_barbarians_guard",
		"kills_flaming_spider",
		"kills_magma_cube_rider",
		"kills_magma_slug",
		"kills_moogma",
		"kills_lava_leech",
		"deaths_barbarian_duke_x",
		"kills_dojo_knockback_zombie",
		"deaths_charging_mushroom_cow",
		"deaths_smoldering_blaze",
		"kills_fire_eel",
		"items_fished_trophy_fish",
		"kills_pyroclastic_worm",
		"kills_lava_flame",
		"kills_pig_rider",
		"kills_wither_spectre",
		"deaths_mage_outlaw",
		"deaths_magma_boss",
		"kills_magma_glare",
		"kills_unstable_magma",
		"kills_mage_skull",
		"kills_mage_outlaw",
		"deaths_ashfang",
		"kills_magma_boss",
		"deaths_magma_glare",
		"deaths_hellwisp",
		"kills_hellwisp",
		"deaths_unstable_magma",
		"kills_bezal",
		"kills_mutated_blaze",
		"deaths_bladesoul",
		"kills_bladesoul",
		"kills_wither_defender_guard",
		"kills_duelist_rollim",
		"deaths_kuudra_knocker",
		"kills_smoldering_blaze",
		"deaths_flare",
		"kills_barbarians_guard",
		"deaths_pyroclastic_worm",
		"deaths_pig_rider",
		"deaths_thunder",
		"kills_thunder",
		"deaths_moogma",
		"kills_livid_clone",
		"kills_livid",
		"kills_kuudra",
		"deaths_goliath_barbarian",
		"kills_ashfang_red_blaze",
		"deaths_kuudra_landmine",
		"deaths_kuudra_follower",
		"kills_barbarian_duke_x",
		"kills_matcho",
		"deaths_ashfang_red_blaze",
		"deaths_ashfang_blue_blaze",
		"kills_ashfang",
		"deaths_dive_ghast",
		"kills_flare",
		"deaths_lord_jawbus",
		"kills_pack_magma_cube",
		"kills_kada_knight",
		"kills_lord_jawbus",
		"deaths_magma_slug",
		"deaths_lava_flame",
		"deaths_master_wither_miner",
		"deaths_baby_magma_slug",
		"deaths_fire_eel",
		"kills_cinder_bat",
		"deaths_old_blaze",
		"kills_sadan",
		"deaths_lava_leech",
		"kills_taurus",
		"kills_bonzo",
		"deaths_ashfang_gray_blaze",
		"kills_inferno_blaze",
		"deaths_pig_zombie",
		"kills_old_blaze",
		"deaths_inferno_blaze",
		"deaths_wandering_blaze",
		"deaths_bezal",
		"glowing_mushrooms_broken",
		"kills_professor_archer_guardian",
		"kills_professor_priest_guardian",
		"kills_professor_mage_guardian",
		"kills_professor_warrior_guardian",
		"kills_professor",
		"kills_nest_endermite",
		"dragon_most_damage",
		"dragon_most_damage_unstable",
		"dragon_highest_rank",
		"dragon_highest_rank_unstable",
		"dragon_fastest_kill",
		"dragon_fastest_kill_unstable",
		"dragon_most_damage_protector",
		"dragon_highest_rank_protector",
		"dragon_fastest_kill_protector",
		"dragon_amount_summoned",
		"dragon_amount_summoned_protector",
		"dragon_summoning_eyes_contributed",
		"dragon_summoning_eyes_contributed_protector",
		"dragon_most_damage_wise",
		"dragon_highest_rank_wise",
		"dragon_fastest_kill_wise",
		"kills_zealot_bruiser",
		"dragon_amount_summoned_young",
		"dragon_summoning_eyes_contributed_young",
		"dragon_most_damage_young",
		"dragon_highest_rank_young",
		"dragon_fastest_kill_young",
		"deaths_giant",
		"dragon_fastest_kill_old",
		"summoning_eyes_collected",
		"deaths_aranya",
		"dragon_most_damage_strong",
		"dragon_fastest_kill_strong",
		"dragon_amount_summoned_wise",
		"dragon_summoning_eyes_contributed_wise",
		"dragon_most_damage_superior",
		"dragon_highest_rank_superior",
		"dragon_fastest_kill_superior",
		"dragon_highest_rank_strong",
		"dragon_amount_summoned_unstable",
		"dragon_summoning_eyes_contributed_unstable",
		"dragon_most_damage_old",
		"dragon_highest_rank_old",
		"deaths_omegagma",
		"deaths_magma_bacteria",
		"deaths_inferno_magma_cube",
		"kills_trapper_horse",
		"kills_master_bonzo",
		"kills_master_scarf",
		"kills_master_professor_priest_guardian",
		"kills_master_professor_warrior_guardian",
		"deaths_explosive_imp",
		"deaths_sea_walker",
		"deaths_generator_magma_cube",
		"deaths_wraith",
		"deaths_phantom_spirit",
		"deaths_scary_jerry",
		"deaths_phantom_fisherman",
		"deaths_witch_bat",
		"deaths_goblin_battler",
		"deaths_wither_gourd",
		"deaths_trick_or_treater",
		"kills_zombie_miner",
		"deaths_treasure_hoarder",
		"deaths_wither_defender_guard",
		"kills_ashfang_blue_blaze",
		"deaths_wither_spectre",
		"deaths_vanquisher",
		"deaths_zealot_bruiser",
		"dragon_amount_summoned_old",
		"dragon_summoning_eyes_contributed_old",
		"dragon_amount_summoned_superior",
		"dragon_summoning_eyes_contributed_superior",
		"dragon_amount_summoned_strong",
		"dragon_summoning_eyes_contributed_strong",
		"deaths_horseman_horse",
		"kills_chickzilla",
		"kills_ashfang_vehicle",
		"kills_nutcracker",
		"deaths_sea_witch",
		"kills_scarf",
		"deaths_mutated_blaze",
		"kills_crypt_undead_hypixel",
		"kills_crypt_undead_flameboy101",
		"deaths_matcho",
		"deaths_fire_mage",
		"deaths_kada_chicken",
		"deaths_pack_magma_cube",
		"deaths_bal_boss",
		"kills_goliath_barbarian",
		"deaths_sea_archer",
		"deaths_creeper",
		"deaths_random_slime",
		"deaths_mimic",
		"deaths_zombie_lord",
		"deaths_master_watcher_guardian",
		"kills_master_watcher_scarf",
		"deaths_master_crypt_undead",
		"kills_master_king_midas",
		"deaths_master_dungeon_respawning_skeleton_skull",
		"deaths_master_super_tank_zombie",
		"deaths_master_sadan",
		"kills_master_sadan_giant",
		"kills_master_watcher_livid",
		"deaths_master_mimic",
		"deaths_master_crypt_undead_nitroholic_",
		"deaths_master_watcher_livid",
		"deaths_master_deathmite",
		"deaths_master_lonely_spider",
		"deaths_master_king_midas",
		"deaths_master_storm",
		"kills_master_zombie_lord",
		"kills_master_wither_husk",
		"kills_master_necron_guard",
		"kills_master_wither_king_dragon",
		"deaths_master_wither_guard",
		"deaths_master_wither_king",
		"deaths_master_necron",
		"deaths_master_zombie_lord",
		"deaths_master_crypt_undead_hypixel",
		"kills_master_watcher_giant_diamond",
		"kills_master_watcher_giant_bigfoot",
		"kills_master_watcher_giant_boulder",
		"kills_master_watcher_giant_laser",
		"deaths_pulverized",
		"deaths_mages_guard",
		"deaths_chickzilla",
		"deaths_blight",
		"deaths_wither_sentry",
		"deaths_blazing_golem",
		"deaths_chaosmite",
		"deaths_kuudra_berserker",
		"kills_master_livid",
		"kills_master_livid_clone",
		"kills_crypt_undead_codename_b",
		"kills_crypt_undead_relenter",
		"kills_crypt_undead_sfarnham",
		"kills_crypt_undead_nitroholic_",
		"kills_crypt_undead_likaos",
		"kills_crypt_undead_jayavarmen",
		"kills_crypt_undead_dctr",
		"kills_crypt_undead_connorlinfoot",
		"kills_crypt_undead_minikloon",
		"kills_crypt_undead_apunch",
		"kills_crypt_undead_bembo",
		"kills_crypt_undead_robitybobity",
		"kills_crypt_undead_agentk",
		"kills_crypt_undead_thorlon",
		"kills_crypt_undead_luckykessie",
		"kills_crypt_undead_williamtiger",
		"kills_crypt_undead_orangemarshall",
		"kills_crypt_undead_sylent",
		"kills_crypt_undead_externalizable",
		"kills_crypt_undead_plancke",
		"kills_crypt_undead_jamiethegeek",
		"kills_crypt_undead_ladybleu",
		"kills_crypt_undead_rezzus",
		"kills_crypt_undead_skyerzz",
		"kills_crypt_undead_bloozing",
		"kills_crypt_undead_erosemberg",
		"kills_crypt_undead_judg3",
		"kills_crypt_undead_revengeee",
		"kills_crypt_undead_donpireso",
		"kills_crypt_undead_pjoke1",
		"kills_crypt_undead_cecer",
		"kills_crypt_undead_chilynn",
		"deaths_watcher_giant_laser",
		"deaths_master_professor_archer_guardian",
		"deaths_master_professor_priest_guardian",
		"kills_master_deathmite",
		"deaths_master_crypt_undead_likaos",
		"kills_krondor_necromancer",
		"kills_mages_guard",
		"kills_master_sadan",
		"kills_duelist_kaus",
		"deaths_skeleton_lord",
		"kills_master_thorn",
		"deaths_master_crypt_undead_relenter",
		"deaths_master_crypt_undead_apunch",
		"deaths_master_crypt_undead_codename_b",
		"deaths_master_parasite",
		"deaths_team_treasurite_viper",
		"deaths_master_crypt_undead_dctr",
		"deaths_master_watcher_giant_diamond",
		"deaths_master_wither_king_dragon",
		"deaths_master_wither_husk",
		"deaths_master_watcher_giant_bigfoot",
		"deaths_master_watcher_giant_laser",
		"deaths_master_skeleton_lord",
		"deaths_frosty_the_snowman",
		"deaths_scarf_archer",
		"deaths_kill",
		"deaths_master_crypt_undead_donpireso",
		"kills_master_crypt_undead_sfarnham",
		"kills_master_crypt_undead_likaos",
		"kills_master_crypt_undead_nitroholic_",
		"kills_master_crypt_undead_codename_b",
		"kills_master_crypt_undead_relenter",
		"kills_master_crypt_undead_minikloon",
		"kills_master_crypt_undead_apunch",
		"kills_master_crypt_undead_jayavarmen",
		"deaths_team_treasurite_wendy",
		"deaths_nutcracker",
		"deaths_master_bonzo_summon_undead",
		"deaths_krondor_necromancer",
		"deaths_ghast",
		"special_zealot_loot_collected",
		"kills_krondor_necromancer_summon",
		"kills_master_professor_archer_guardian",
		"kills_master_professor",
		"kills_master_professor_mage_guardian"
	);
	public static final Map<String, String> profileNameToEmoji = Maps.of(
		"apple",
		"\uD83C\uDF4E",
		"banana",
		"\uD83C\uDF4C",
		"blueberry",
		"\uD83E\uDED0",
		"coconut",
		"\uD83E\uDD65",
		"cucumber",
		"\uD83E\uDD52",
		"grapes",
		"\uD83C\uDF47",
		"kiwi",
		"\uD83E\uDD5D",
		"lemon",
		"\uD83C\uDF4B",
		"lime",
		"<:lime:828632854174498837>",
		"mango",
		"\uD83E\uDD6D",
		"orange",
		"<:orange:828634110360289331>",
		"papaya",
		"<:papaya:828633125370200085>",
		"peach",
		"\uD83C\uDF51",
		"pear",
		"\uD83C\uDF50",
		"pineapple",
		"\uD83C\uDF4D",
		"pomegranate",
		"<:pomegranate:828632397032456232>",
		"raspberry",
		"<:raspberry:828632035127853064>",
		"strawberry",
		"\uD83C\uDF53",
		"tomato",
		"\uD83C\uDF45",
		"watermelon",
		"\uD83C\uDF49",
		"zucchini",
		"<:zucchini:828636746358194206>"
	);
	public static final List<String> networthRunes = List.of(
		"MUSIC_RUNE;1",
		"MUSIC_RUNE;2",
		"MUSIC_RUNE;3",
		"MEOW_MUSIC_RUNE;3",
		"ENCHANT_RUNE;1",
		"ENCHANT_RUNE;2",
		"ENCHANT_RUNE;3",
		"GRAND_SEARING_RUNE;3",
		"SPELLBOUND_RUNE;3",
		"GRAND_FREEZING_RUNE;3",
		"PRIMAL_FEAR_RUNE;3",
		"GOLDEN_CARPET_RUNE;3",
		"ICE_SKATES_RUNE;3",
		"BARK_TUNES_RUNE;3",
		"SMITTEN_RUNE;3"
	);
	public static final List<String> raritiesWithColorCode = List.of(
		"§f§lCOMMON",
		"§a§lUNCOMMON",
		"§9§lRARE",
		"§5§lEPIC",
		"§6§lLEGENDARY",
		"§d§lMYTHIC",
		"§c§lSPECIAL",
		"§c§lVERY SPECIAL"
	);
	public static final OptionData profilesCommandOption = new OptionData(OptionType.STRING, "profile", "Profile name")
		.addChoices(
			profileNameToEmoji
				.keySet()
				.stream()
				.map(i -> new Command.Choice(capitalizeString(i), i))
				.collect(Collectors.toCollection(ArrayList::new))
		);
	public static final OptionData gamemodeCommandOption = new OptionData(OptionType.STRING, "gamemode", "Gamemode type")
		.addChoice("All", "all")
		.addChoice("Ironman", "ironman")
		.addChoice("Stranded", "stranded");
	/* Constants JSON file */
	public static JsonObject CONSTANTS;

	/* Constants JSON */
	public static double CATACOMBS_LEVEL_50_XP;
	public static double SKILLS_LEVEL_50_XP;
	public static double SKILLS_LEVEL_60_XP;
	public static Map<String, Integer> RARITY_TO_NUMBER_MAP;
	public static List<Integer> CRAFTED_MINIONS_TO_SLOTS;
	public static List<String> COSMETIC_SKILL_NAMES;
	public static Map<String, String> SKILLS_EMOJI_MAP;
	public static List<String> DUNGEON_CLASS_NAMES;
	public static List<String> SLAYER_NAMES;
	public static List<Integer> GUILD_EXP_TO_LEVEL;
	public static Map<String, String> DUNGEON_EMOJI_MAP;
	public static List<String> FETCHUR_ITEMS;
	public static Map<String, String> HARP_SONG_ID_TO_NAME;
	public static Map<String, String> HOTM_PERK_ID_TO_NAME;
	public static Map<String, Integer> HOTM_PERK_MAX_LEVEL;
	public static Map<String, String> SLAYER_EMOJI_MAP;
	public static Map<String, String> ESSENCE_EMOJI_MAP;
	public static Map<String, Integer> IGNORED_ENCHANTS;
	public static Map<String, String> MAYOR_NAME_TO_SKIN;
	public static JsonObject ARMOR_PRESTIGE_COST;
	public static JsonObject POWER_TO_BASE_STATS;
	public static Map<String, String> V2_STATS_MAP;
	public static Map<String, String> EMBLEM_NAME_TO_ICON;

	/* Fetched from other sources */
	public static List<String> ENCHANT_NAMES;
	public static List<String> ALL_SKILL_NAMES;
	public static List<String> SKILL_NAMES;
	public static List<String> PET_NAMES;
	public static List<String> ESSENCE_ITEM_NAMES;
	public static List<String> PET_ITEM_NAMES;
	public static Set<String> ALL_TALISMANS;
	public static Map<Integer, String> NUMBER_TO_RARITY_MAP;
	public static List<String> SOULBOUND_ITEMS;
	public static Map<String, List<String>> CATEGORY_TO_REFORGES;
	public static Map<String, List<String>> MUSEUM_PARENTS;

	public static void initialize() {
		try {
			CONSTANTS = getJson("https://raw.githubusercontent.com/" + DATA_REPO_GITHUB + "/main/Constants.json").getAsJsonObject();

			Type mapStringString = new TypeToken<Map<String, String>>() {}.getType();
			Type listInteger = new TypeToken<List<Integer>>() {}.getType();
			Type listString = new TypeToken<List<String>>() {}.getType();
			Type mapStringInteger = new TypeToken<Map<String, Integer>>() {}.getType();

			/* CATACOMBS_LEVEL_50_XP */
			CATACOMBS_LEVEL_50_XP = higherDepth(CONSTANTS, "CATACOMBS_LEVEL_50_XP").getAsDouble();

			/* SKILLS_LEVEL_50_XP */
			SKILLS_LEVEL_50_XP = higherDepth(CONSTANTS, "SKILLS_LEVEL_50_XP").getAsDouble();

			/* SKILLS_LEVEL_60_XP */
			SKILLS_LEVEL_60_XP = higherDepth(CONSTANTS, "SKILLS_LEVEL_60_XP").getAsDouble();

			/* RARITY_TO_NUMBER_MAP */
			RARITY_TO_NUMBER_MAP = gson.fromJson(higherDepth(CONSTANTS, "RARITY_TO_NUMBER_MAP"), mapStringInteger);

			/* CRAFTED_MINIONS_TO_SLOTS */
			CRAFTED_MINIONS_TO_SLOTS = gson.fromJson(higherDepth(CONSTANTS, "CRAFTED_MINIONS_TO_SLOTS"), listInteger);

			/* COSMETIC_SKILL_NAMES */
			COSMETIC_SKILL_NAMES = gson.fromJson(higherDepth(CONSTANTS, "COSMETIC_SKILL_NAMES"), listString);

			/* SKILLS_EMOJI_MAP */
			SKILLS_EMOJI_MAP = gson.fromJson(higherDepth(CONSTANTS, "SKILLS_EMOJI_MAP"), mapStringString);

			/* DUNGEON_CLASS_NAMES */
			DUNGEON_CLASS_NAMES = gson.fromJson(higherDepth(CONSTANTS, "DUNGEON_CLASS_NAMES"), listString);

			/* SLAYER_NAMES */
			SLAYER_NAMES = gson.fromJson(higherDepth(CONSTANTS, "SLAYER_NAMES"), listString);

			/* GUILD_EXP_TO_LEVEL */
			GUILD_EXP_TO_LEVEL = gson.fromJson(higherDepth(CONSTANTS, "GUILD_EXP_TO_LEVEL"), listInteger);

			/* DUNGEON_EMOJI_MAP */
			DUNGEON_EMOJI_MAP = gson.fromJson(higherDepth(CONSTANTS, "DUNGEON_EMOJI_MAP"), mapStringString);

			/* FETCHUR_ITEMS */
			FETCHUR_ITEMS = gson.fromJson(higherDepth(CONSTANTS, "FETCHUR_ITEMS"), listString);

			/* HARP_SONG_ID_TO_NAME */
			HARP_SONG_ID_TO_NAME = gson.fromJson(higherDepth(CONSTANTS, "HARP_SONG_ID_TO_NAME"), mapStringString);

			/* HOTM_PERK_ID_TO_NAME */
			HOTM_PERK_ID_TO_NAME = gson.fromJson(higherDepth(CONSTANTS, "HOTM_PERK_ID_TO_NAME"), mapStringString);

			/* HOTM_PERK_MAX_LEVEL */
			HOTM_PERK_MAX_LEVEL = gson.fromJson(higherDepth(CONSTANTS, "HOTM_PERK_MAX_LEVEL"), mapStringInteger);

			/* SLAYER_EMOJI_MAP */
			SLAYER_EMOJI_MAP = gson.fromJson(higherDepth(CONSTANTS, "SLAYER_EMOJI_MAP"), mapStringString);

			/* ESSENCE_EMOJI_MAP */
			ESSENCE_EMOJI_MAP = gson.fromJson(higherDepth(CONSTANTS, "ESSENCE_EMOJI_MAP"), mapStringString);

			/* IGNORED_ENCHANTS */
			IGNORED_ENCHANTS = gson.fromJson(higherDepth(CONSTANTS, "IGNORED_ENCHANTS"), mapStringInteger);

			/* MAYOR_NAME_TO_SKIN */
			MAYOR_NAME_TO_SKIN = gson.fromJson(higherDepth(CONSTANTS, "MAYOR_NAME_TO_SKIN"), mapStringString);

			/* ARMOR_PRESTIGE_COST */
			ARMOR_PRESTIGE_COST = higherDepth(CONSTANTS, "ARMOR_PRESTIGE_COST").getAsJsonObject();

			/* POWER_TO_BASE_STATS */
			POWER_TO_BASE_STATS = higherDepth(CONSTANTS, "POWER_TO_BASE_STATS").getAsJsonObject();

			/* V2_STATS_MAP */
			V2_STATS_MAP = gson.fromJson(higherDepth(CONSTANTS, "V2_STATS_MAP"), mapStringString);

			/* EMBLEM_NAME_TO_ICON */
			EMBLEM_NAME_TO_ICON = gson.fromJson(higherDepth(CONSTANTS, "EMBLEM_NAME_TO_ICON"), mapStringString);

			/* ENCHANT_NAMES */
			HashSet<String> enchantNames = new HashSet<>();
			for (Map.Entry<String, JsonElement> enchantArr : higherDepth(getEnchantsJson(), "enchants").getAsJsonObject().entrySet()) {
				for (JsonElement enchantName : enchantArr.getValue().getAsJsonArray()) {
					enchantNames.add(enchantName.getAsString().toUpperCase());
				}
			}
			enchantNames.add("ULTIMATE_JERRY");
			ENCHANT_NAMES = new ArrayList<>(enchantNames);

			/* ALL_SKILL_NAMES */
			ALL_SKILL_NAMES = new ArrayList<>(higherDepth(getLevelingJson(), "leveling_caps").getAsJsonObject().keySet());
			ALL_SKILL_NAMES.remove("HOTM");
			ALL_SKILL_NAMES.remove("catacombs");

			/* SKILL_NAMES */
			SKILL_NAMES = new ArrayList<>(ALL_SKILL_NAMES);
			SKILL_NAMES.removeIf(COSMETIC_SKILL_NAMES::contains);

			/* PET_NAMES */
			PET_NAMES = new ArrayList<>(getPetNumsJson().getAsJsonObject().keySet());

			/* ESSENCE_ITEM_NAMES */
			ESSENCE_ITEM_NAMES = new ArrayList<>(getEssenceCostsJson().getAsJsonObject().keySet());

			/* PET_ITEM_NAMES */
			PET_ITEM_NAMES =
				getSkyblockItemsJson()
					.entrySet()
					.stream()
					.filter(e -> higherDepth(e.getValue(), "category", "").equals("PET_ITEM"))
					.map(Map.Entry::getKey)
					.collect(Collectors.toCollection(ArrayList::new));

			/* ALL_TALISMANS */
			ALL_TALISMANS =
				getSkyblockItemsJson()
					.entrySet()
					.stream()
					.filter(e -> higherDepth(e.getValue(), "category", "").equals("ACCESSORY"))
					.map(Map.Entry::getKey)
					.collect(Collectors.toCollection(HashSet::new));
			for (JsonElement extraAccessory : higherDepth(CONSTANTS, "EXTRA_ACCESSORIES").getAsJsonArray()) {
				ALL_TALISMANS.add(extraAccessory.getAsString());
			}
			for (JsonElement ignoredAccessory : higherDepth(CONSTANTS, "IGNORED_ACCESSORIES").getAsJsonArray()) {
				ALL_TALISMANS.remove(ignoredAccessory.getAsString());
			}

			/* NUMBER_TO_RARITY_MAP */
			NUMBER_TO_RARITY_MAP =
				RARITY_TO_NUMBER_MAP.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

			/* SOULBOUND_ITEMS */
			SOULBOUND_ITEMS =
				getSkyblockItemsJson()
					.entrySet()
					.stream()
					.filter(e -> higherDepth(e.getValue(), "soulbound", null) != null)
					.map(Map.Entry::getKey)
					.collect(Collectors.toCollection(ArrayList::new));
			SOULBOUND_ITEMS.add("KUUDRA_FOLLOWER_ARTIFACT");

			/* CATEGORY_TO_REFORGES */
			CATEGORY_TO_REFORGES = new HashMap<>();
			for (Map.Entry<String, JsonElement> reforgeStone : getReforgeStonesJson().entrySet()) {
				// Why... https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/pull/1117
				String[] itemTypes = higherDepth(
					reforgeStone.getValue(),
					"itemTypes",
					reforgeStone.getKey().equals("BOO_STONE") ? "ARMOR" : "SWORD"
				)
					.split("/");
				for (String itemType : itemTypes) {
					CATEGORY_TO_REFORGES.compute(
						itemType,
						(k, v) -> {
							(v = v != null ? v : new ArrayList<>()).add(
									higherDepth(reforgeStone.getValue(), "reforgeName").getAsString().toLowerCase()
								);
							return v;
						}
					);
				}
			}
			CATEGORY_TO_REFORGES.put("GAUNTLET", ListUtils.union(CATEGORY_TO_REFORGES.get("SWORD"), CATEGORY_TO_REFORGES.get("PICKAXE")));

			/* MUSEUM_PARENTS */
			MUSEUM_PARENTS = new HashMap<>();
			JsonObject museumChildren = higherDepth(getMuseumCategoriesJson(), "children").getAsJsonObject();
			Map<String, String> remainingChildren = new HashMap<>();
			for (Map.Entry<String, JsonElement> child : museumChildren.entrySet()) {
				if (!museumChildren.has(child.getValue().getAsString())) {
					MUSEUM_PARENTS.put(child.getValue().getAsString(), new ArrayList<>(List.of(child.getKey())));
				} else {
					remainingChildren.put(child.getValue().getAsString(), child.getKey());
				}
			}
			boolean foundChild;
			do {
				foundChild = false;
				for (Map.Entry<String, List<String>> child : MUSEUM_PARENTS.entrySet()) {
					String nestedChild = remainingChildren.get(child.getValue().get(child.getValue().size() - 1));
					if (nestedChild != null) {
						child.getValue().add(nestedChild);
						foundChild = true;
					}
				}
			} while (foundChild);
		} catch (Exception e) {
			Main.log.error("Exception while initializing constants", e);
		}
	}

	public static JsonElement getConstant(String key) {
		return higherDepth(CONSTANTS, key);
	}
}
