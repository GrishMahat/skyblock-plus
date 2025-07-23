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

package com.skyblockplus;

import club.minnced.discord.webhook.WebhookClientBuilder;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.skyblockplus.api.controller.ApiController;
import com.skyblockplus.api.serversettings.managers.ServerSettingsModel;
import com.skyblockplus.features.event.EventHandler;
import com.skyblockplus.features.fetchur.FetchurHandler;
import com.skyblockplus.features.jacob.JacobHandler;
import com.skyblockplus.features.listeners.MainListener;
import com.skyblockplus.features.mayor.MayorHandler;
import com.skyblockplus.price.AuctionTracker;
import com.skyblockplus.utils.ApiHandler;
import com.skyblockplus.utils.AuctionFlipper;
import com.skyblockplus.utils.Constants;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandClient;
import com.skyblockplus.utils.database.Database;
import com.skyblockplus.utils.exceptionhandler.ExceptionEventListener;
import com.skyblockplus.utils.exceptionhandler.GlobalExceptionHandler;
import com.skyblockplus.utils.oauth.OAuthClient;
import com.skyblockplus.utils.utils.HttpUtils;
import com.skyblockplus.utils.utils.Utils;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.groovy.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.skyblockplus.utils.utils.HttpUtils.okHttpClient;
import static com.skyblockplus.utils.utils.Utils.*;

@SpringBootApplication
public class Main {

	public static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IllegalArgumentException, IOException {
		globalExceptionHandler = new GlobalExceptionHandler();
		RestAction.setDefaultFailure(e -> globalExceptionHandler.uncaughtException(Thread.currentThread(), e));
		Message.suppressContentIntentWarning();

		Utils.initialize();
		Constants.initialize();
		botStatusWebhook =
			new WebhookClientBuilder(BOT_STATUS_WEBHOOK).setExecutorService(scheduler).setHttpClient(okHttpClient).buildJDA();

		SpringApplication springApplication = new SpringApplication(Main.class);
		springApplication.setDefaultProperties(Maps.of("spring.datasource.url", DATABASE_URL));
		springContext = springApplication.run(args);
		database = springContext.getBean(Database.class);
		waiter = new EventWaiter(scheduler, true);
		client =
			new CommandClientBuilder()
				.setPrefix(PREFIX)
				.setAlternativePrefix("@mention")
				.setOwnerId(OWNER_ID)
				.setEmojis("<:yes:948359788889251940>", "⚠️", "<:no:948359781125607424>")
				.useHelpBuilder(false)
				.setListener(
					new CommandListener() {
						@Override
						public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
							globalExceptionHandler.uncaughtException(event, command, throwable);
						}
					}
				)
				.setCommandPreProcessBiFunction((event, command) -> event.isFromGuild())
				.setActivity(Activity.playing("Loading..."))
				.setManualUpsert(true)
				.addCommands(springContext.getBeansOfType(Command.class).values().toArray(new Command[0]))
				.build();

		slashCommandClient =
			new SlashCommandClient().setOwnerId(OWNER_ID).addCommands(springContext.getBeansOfType(SlashCommand.class).values());

		oAuthClient = new OAuthClient(BOT_ID, CLIENT_SECRET);

		log.info(
			"Loaded " + client.getCommands().size() + " prefix commands and " + slashCommandClient.getCommands().size() + " slash commands"
		);

		if (!IS_DEV) {
			allServerSettings = database.getAllServerSettings().stream()
				.collect(Collectors.toMap(ServerSettingsModel::getServerId, Function.identity()));
		} else {
			// In dev mode, initialize with an empty HashMap if no settings exist
			allServerSettings = new HashMap<>();
			for (String id : Arrays.asList(PRIMARY_GUILD_ID, "782154976243089429", "869217817680044042")) {
				ServerSettingsModel settings = database.getServerSettingsModel(id);
				if (settings != null) {
					allServerSettings.put(settings.getServerId(), settings);
				}
			}
		}
		log.info("Loaded all server settings");

		DefaultShardManagerBuilder jdaBuilder = DefaultShardManagerBuilder
			.createDefault(BOT_TOKEN)
			.setStatus(OnlineStatus.DO_NOT_DISTURB)
			.addEventListeners(
				new ExceptionEventListener(waiter),
				client,
				new ExceptionEventListener(slashCommandClient),
				new ExceptionEventListener(new MainListener())
			)
			.setActivity(Activity.playing("Loading..."))
			.setMemberCachePolicy(MemberCachePolicy.DEFAULT)
			.disableCache(CacheFlag.VOICE_STATE, CacheFlag.STICKER, CacheFlag.FORUM_TAGS, CacheFlag.SCHEDULED_EVENTS)
			.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
			.setEnableShutdownHook(false);
		if (IS_DEV) {
			jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
		}
		jda = jdaBuilder.build();

		for (JDA shard : jda.getShards()) {
			try {
				shard.awaitReady();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			log.info("Waiting for JDA shards to be ready...");
			for (JDA shard : jda.getShards()) {
				shard.awaitReady();
			}
			log.info("All JDA shards are ready");

			if (!rendersDirectory.exists()) {
				log.info((rendersDirectory.mkdirs() ? "Successfully created" : "Failed to create") + " lore render directory");
			} else {
				File[] loreRendersDirFiles = rendersDirectory.listFiles();
				if (loreRendersDirFiles != null) {
					Arrays.stream(loreRendersDirFiles).forEach(File::delete);
				}
			}

			try {
				ApiHandler.initialize();
				AuctionTracker.initialize();
				AuctionFlipper.initialize(!IS_DEV);
				ApiController.initialize();
				FetchurHandler.initialize();
				scheduler.scheduleWithFixedDelay(MayorHandler::initialize, 1, 5, TimeUnit.MINUTES);
        JacobHandler.initialize();
				EventHandler.initialize();

				log.info("All services initialized successfully");
			} catch (Exception e) {
				log.error("Error during service initialization", e);
				// Continue running even if some initializations fail
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Failed to initialize bot: JDA initialization interrupted", e);
		}

		if (!IS_DEV) {
			scheduler.scheduleWithFixedDelay(
				() -> {
					if (Runtime.getRuntime().totalMemory() > 4500000000L) {
						System.gc();
					}
				},
				60,
				30,
				TimeUnit.SECONDS
			); // Sorry for the war crimes
		}

		Thread.setDefaultUncaughtExceptionHandler(globalExceptionHandler);
		log.info("Bot ready with " + jda.getShardsTotal() + " shards and " + jda.getGuilds().size() + " guilds");
	}

	@PreDestroy
	public void onExit() {
		// Hotswap Agent runs the PreDestroy hook for some reason
		if (IS_DEV) {
			return;
		}

		log.info("Stopping");

		botStatusWebhook.send(client.getSuccess() + " Restarting for an update");

		ApiHandler.updateCaches();
		HttpUtils.closeHttpClient();
		ApiHandler.leaderboardDatabase.close();

		log.info("Finished");
	}

	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
		return new HttpStatusRequestRejectedHandler();
	}
}
