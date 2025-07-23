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

package com.skyblockplus.inventory;

import com.skyblockplus.utils.Player;
import com.skyblockplus.utils.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.skyblockplus.utils.utils.Utils.*;

public class InventoryEmojiPaginator {

	private final List<String[]> inventoryPages;
	private final User user;
	private final int maxPageNumber;
	private Message pagePart1;
	private Message pagePart2;
	private int pageNumber = 0;
	private Instant lastEdit;

	public InventoryEmojiPaginator(List<String[]> inventoryPages, String type, Player.Profile player, SlashCommandEvent event) {
		this.inventoryPages = inventoryPages;
		this.user = event.getUser();
		this.maxPageNumber = inventoryPages.size() - 1;
		this.lastEdit = Instant.now();

		event
			.getHook()
			.editOriginal(inventoryPages.get(0)[0])
			.setEmbeds()
			.queue(m1 -> {
				pagePart1 = m1;
				event
					.getChannel()
					.sendMessage(inventoryPages.get(0)[1])
					.setActionRow(
						Button
							.primary("inv_paginator_left_button", Emoji.fromFormatted("<:left_button_arrow:885628386435821578>"))
							.withDisabled(pageNumber == 0),
						Button
							.primary("inv_paginator_right_button", Emoji.fromFormatted("<:right_button_arrow:885628386578423908>"))
							.withDisabled(pageNumber == (maxPageNumber)),
						Button.link(player.skyblockStatsLink(), player.getUsername() + "'s " + type + " • Page 1/" + (maxPageNumber + 1))
					)
					.queue(m2 -> {
						pagePart2 = m2;
						waitForEvent();
					});
			});
	}

	private boolean condition(ButtonInteractionEvent event) {
		return (event.isFromGuild() && event.getUser().getId().equals(user.getId()) && event.getMessageId().equals(pagePart2.getId()));
	}

	public void action(ButtonInteractionEvent event) {
		if (Instant.now().minusSeconds(2).isBefore(lastEdit)) {
			event
				.reply(client.getError() + " Please wait between switching pages")
				.setEphemeral(true)
				.queue(ignored -> waitForEvent(), ignored -> waitForEvent());
		} else {
			lastEdit = Instant.now();
			if (event.getComponentId().equals("inv_paginator_left_button")) {
				if ((pageNumber - 1) >= 0) {
					pageNumber -= 1;
				}
			} else if (event.getComponentId().equals("inv_paginator_right_button")) {
				if ((pageNumber + 1) <= maxPageNumber) {
					pageNumber += 1;
				}
			}

			pagePart1.editMessage(inventoryPages.get(pageNumber)[0]).queue(ignore, ignore);

			List<Button> curButtons = event.getMessage().getButtons();
			Button leftButton = curButtons.get(0).withDisabled(pageNumber == 0);
			Button rightButton = curButtons.get(1).withDisabled(pageNumber == maxPageNumber);
			Button linkButton = curButtons
				.get(2)
				.withLabel(curButtons.get(2).getLabel().split("•")[0] + "• Page " + (pageNumber + 1) + "/" + (maxPageNumber + 1));
			event
				.editMessage(inventoryPages.get(pageNumber)[1])
				.setActionRow(leftButton, rightButton, linkButton)
				.queue(ignored -> waitForEvent(), ignored -> waitForEvent());
		}
	}

	private void waitForEvent() {
		waiter.waitForEvent(
			ButtonInteractionEvent.class,
			this::condition,
			this::action,
			1,
			TimeUnit.MINUTES,
			() ->
				pagePart2
					.editMessageComponents(
						ActionRow.of(
							pagePart2
								.getButtons()
								.get(2)
								.withLabel(
									pagePart2.getButtons().get(2).getLabel().split("•")[0] +
									"• Page " +
									(pageNumber + 1) +
									"/" +
									(maxPageNumber + 1)
								)
						)
					)
					.queue(ignore, ignore)
		);
	}
}
