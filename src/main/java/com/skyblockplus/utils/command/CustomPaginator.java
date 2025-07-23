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

package com.skyblockplus.utils.command;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.*;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.skyblockplus.utils.utils.Utils.defaultEmbed;

public class CustomPaginator {

	private static final Logger log = LoggerFactory.getLogger(CustomPaginator.class);
	private static final Consumer<Throwable> throwableConsumer = e -> {
		if (!(e instanceof ErrorResponseException ex && ex.getErrorResponse().equals(ErrorResponse.UNKNOWN_INTERACTION))) {
			log.error(e.getMessage(), e);
		}
	};
	private static final String LEFT = "paginator_left_button";
	private static final String RIGHT = "paginator_right_button";
	private final EventWaiter waiter;
	private final Set<User> users;
	private final long timeout;
	private final TimeUnit unit;
	private final Color color;
	private final int columns;
	private final int itemsPerPage;
	private final boolean showPageNumbers;
	private final Consumer<Message> finalAction;
	private final boolean wrapPageEnds;

	@Getter
	private final PaginatorExtras extras;

	private int pages;

	private CustomPaginator(
		EventWaiter waiter,
		Set<User> users,
		long timeout,
		TimeUnit unit,
		Color color,
		Consumer<Message> finalAction,
		int columns,
		int itemsPerPage,
		boolean showPageNumbers,
		boolean wrapPageEnds,
		PaginatorExtras extras
	) {
		this.waiter = waiter;
		this.users = users;
		this.timeout = timeout;
		this.unit = unit;
		this.color = color;
		this.columns = columns;
		this.itemsPerPage = itemsPerPage;
		this.extras = extras;
		this.showPageNumbers = showPageNumbers;
		this.finalAction = finalAction;
		this.wrapPageEnds = wrapPageEnds;
		calculatePages();
	}

	public void paginate(MessageChannel channel, int pageNum) {
		pageNum = Math.min(Math.max(pageNum, 1), pages);

		MessageCreateData msg = new MessageCreateBuilder().setEmbeds(getEmbedRender(pageNum)).build();
		initialize(channel.sendMessage(msg), pageNum);
	}

	public void paginate(InteractionHook hook, int pageNum) {
		pageNum = Math.min(Math.max(pageNum, 1), pages);

		MessageEditData msg = new MessageEditBuilder().setEmbeds(getEmbedRender(pageNum)).build();
		initialize(hook.editOriginal(msg), pageNum);
	}

	public void paginate(Message message, int pageNum) {
		pageNum = Math.min(Math.max(pageNum, 1), pages);

		MessageEditData msg = new MessageEditBuilder().setEmbeds(getEmbedRender(pageNum)).build();
		initialize(message.editMessage(msg), pageNum);
	}

	private void initialize(MessageRequest<?> action, int pageNum) {
		List<ActionRow> actionRows = new ArrayList<>();
		if (pages > 1) {
			actionRows.add(
				ActionRow.of(
					Button.primary(LEFT, Emoji.fromFormatted("<:left_button_arrow:885628386435821578>")).withDisabled(pageNum == 1),
					Button.primary(RIGHT, Emoji.fromFormatted("<:right_button_arrow:885628386578423908>")).withDisabled(pageNum == pages)
				)
			);
		}
		if (!extras.getButtons().isEmpty()) {
			actionRows.add(ActionRow.of(extras.getButtons()));
		}

		action.setComponents(actionRows);

		if (pages == 0) {
			((RestAction<?>) action.setEmbeds(defaultEmbed("No items to paginate").build())).queue();
		} else {
			((RestAction<?>) action).queue(m -> pagination((Message) m, pageNum), throwableConsumer);
		}
	}

	public void pagination(PaginatorExtras.ReactiveButton.ReactiveAction action) {
		pagination(action.event().getMessage(), action.page());
	}

	private void pagination(Message message, int pageNum) {
		waiter.waitForEvent(
			ButtonInteractionEvent.class,
			event -> checkButtonClick(event, message.getId()),
			event -> handleButtonClick(event, pageNum),
			timeout,
			unit,
			() -> finalAction.accept(message)
		);
	}

	private boolean checkButtonClick(ButtonInteractionEvent event, String messageId) {
		if (!event.isFromGuild()) {
			return false;
		}

		if (event.getUser().isBot()) {
			return false;
		}

		if (!event.getMessageId().equals(messageId)) {
			return false;
		}

		if (!users.isEmpty() && !users.contains(event.getUser())) {
			return false;
		}

		return (
			event.getComponentId().equals(LEFT) ||
			event.getComponentId().equals(RIGHT) ||
			extras.getReactiveButtons().stream().anyMatch(b -> b.isReacting() && event.getComponentId().equals(b.getId()))
		);
	}

	private void handleButtonClick(ButtonInteractionEvent event, int pageNum) {
		switch (event.getComponentId()) {
			case LEFT -> {
				if (pageNum == 1 && wrapPageEnds) {
					pageNum = pages + 1;
				}
				if (pageNum > 1) {
					pageNum--;
				}
			}
			case RIGHT -> {
				if (pageNum == pages && wrapPageEnds) {
					pageNum = 0;
				}
				if (pageNum < pages) {
					pageNum++;
				}
			}
			default -> {
				PaginatorExtras.ReactiveButton button = extras
					.getReactiveButtons()
					.stream()
					.filter(b -> b.isReacting() && event.getComponentId().equals(b.getId()))
					.findFirst()
					.get();
				if (button.getAction().apply(new PaginatorExtras.ReactiveButton.ReactiveAction(this, event, pageNum))) {
					return;
				}
			}
		}
		calculatePages();
		pageNum = Math.min(pageNum, pages);

		List<ActionRow> actionRows = new ArrayList<>();
		if (pages > 1) {
			actionRows.add(
				ActionRow.of(
					Button.primary(LEFT, Emoji.fromFormatted("<:left_button_arrow:885628386435821578>")).withDisabled(pageNum == 1),
					Button.primary(RIGHT, Emoji.fromFormatted("<:right_button_arrow:885628386578423908>")).withDisabled(pageNum == pages)
				)
			);
		}
		if (!extras.getButtons().isEmpty()) {
			actionRows.add(ActionRow.of(extras.getButtons()));
		}

		int finalPageNum = pageNum;
		event
			.editMessageEmbeds(getEmbedRender(pageNum))
			.setComponents(actionRows)
			.queue(hook -> pagination(event.getMessage(), finalPageNum), throwableConsumer);
	}

	private MessageEmbed getEmbedRender(int pageNum) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		if (extras.getType() == PaginatorExtras.PaginatorType.EMBED_PAGES) {
			embedBuilder = extras.getEmbedPages().get(pageNum - 1);
		} else {
			try {
				String title;
				String titleUrl;

				if (extras.getEveryPageTitle() != null) {
					title = extras.getEveryPageTitle();
				} else {
					title = extras.getTitle(pageNum - 1);
				}

				if (extras.getEveryPageTitleUrl() != null) {
					titleUrl = extras.getEveryPageTitleUrl();
				} else {
					titleUrl = extras.getTitleUrl(pageNum - 1);
				}

				embedBuilder.setTitle(title, titleUrl);
			} catch (Exception ignored) {}

			try {
				if (extras.getEveryPageThumbnail() != null) {
					embedBuilder.setThumbnail(extras.getEveryPageThumbnail());
				}
			} catch (Exception ignored) {}

			try {
				embedBuilder.setDescription(extras.getEveryPageText());
			} catch (Exception ignored) {}

			int start = Math.max(0, (pageNum - 1) * itemsPerPage);
			int end = Math.min(extras.getStrings().size(), pageNum * itemsPerPage);
			if (extras.getType() == PaginatorExtras.PaginatorType.EMBED_FIELDS) {
				end = Math.min(extras.getEmbedFields().size(), pageNum * itemsPerPage);
				for (int i = start; i < end; i++) {
					embedBuilder.addField(extras.getEmbedFields().get(i));
				}
			} else if (columns == 1) {
				StringBuilder stringBuilder = new StringBuilder();
				for (int i = start; i < end; i++) {
					stringBuilder.append("\n").append(extras.getStrings().get(i));
				}
				embedBuilder.appendDescription(stringBuilder.toString());
			} else {
				int per = (int) Math.ceil((double) (end - start) / columns);
				for (int k = 0; k < columns; k++) {
					StringBuilder stringBuilder = new StringBuilder();
					for (int i = start + k * per; i < end && i < start + (k + 1) * per; i++) stringBuilder
						.append("\n")
						.append(extras.getStrings().get(i));
					embedBuilder.addField(
						(k == 0 && extras.getEveryPageFirstFieldTitle() != null ? extras.getEveryPageFirstFieldTitle() : ""),
						stringBuilder.toString(),
						true
					);
				}
			}
		}

		embedBuilder
			.setColor(color)
			.setFooter("SB+ is open source • sbplus.codes/gh" + (showPageNumbers ? " • Page " + pageNum + "/" + pages : ""))
			.setTimestamp(Instant.now());

		return embedBuilder.build();
	}

	private void calculatePages() {
		this.pages =
			switch (extras.getType()) {
				case DEFAULT -> (int) Math.ceil((double) extras.getStrings().size() / itemsPerPage);
				case EMBED_FIELDS -> (int) Math.ceil((double) extras.getEmbedFields().size() / itemsPerPage);
				case EMBED_PAGES -> extras.getEmbedPages().size();
			};
	}

	public static class Builder {

		private EventWaiter waiter;
		private final Set<User> users = new HashSet<>();
		private long timeout = 1;
		private TimeUnit unit = TimeUnit.MINUTES;

		@Getter
		private final PaginatorExtras extras = new PaginatorExtras(PaginatorExtras.PaginatorType.DEFAULT);

		private Color color = null;
		private Consumer<Message> finalAction = m -> m.delete().queue(null, throwableConsumer);
		private int columns = 1;

		@Getter
		private int itemsPerPage = 12;

		private boolean wrapPageEnds = false;
		private boolean showPageNumbers = true;

		public CustomPaginator build() {
			Checks.check(waiter != null, "Must set an EventWaiter");
			switch (extras.getType()) {
				case DEFAULT -> {
					if (extras.getStrings().isEmpty()) {
						log.error("Paginator type is DEFAULT but no strings were provided");
					}
					if (!extras.getEmbedFields().isEmpty()) {
						log.warn("Paginator type is DEFAULT but embed fields were also provided");
					}
					if (!extras.getEmbedPages().isEmpty()) {
						log.warn("Paginator type is DEFAULT but embed pages were also provided");
					}
				}
				case EMBED_FIELDS -> {
					if (extras.getEmbedFields().isEmpty()) {
						log.error("Paginator type is EMBED_FIELDS but no embed fields were provided");
					}
					if (!extras.getStrings().isEmpty()) {
						log.warn("Paginator type is EMBED_FIELDS but strings were also provided");
					}
					if (!extras.getEmbedPages().isEmpty()) {
						log.warn("Paginator type is EMBED_FIELDS but embed pages were also provided");
					}
				}
				case EMBED_PAGES -> {
					if (extras.getEmbedPages().isEmpty()) {
						log.error("Paginator type is EMBED_PAGES but no embed pages were provided");
					}
					if (!extras.getStrings().isEmpty()) {
						log.warn("Paginator type is EMBED_PAGES but strings were also provided");
					}
					if (!extras.getEmbedFields().isEmpty()) {
						log.warn("Paginator type is EMBED_PAGES but embed fields were also provided");
					}
				}
				default -> throw new IllegalArgumentException("Invalid paginator type");
			}

			return new CustomPaginator(
				waiter,
				users,
				timeout,
				unit,
				color,
				finalAction,
				columns,
				itemsPerPage,
				showPageNumbers,
				wrapPageEnds,
				extras
			);
		}

		public Builder setEventWaiter(EventWaiter waiter) {
			this.waiter = waiter;
			return this;
		}

		public Builder setUsers(User... users) {
			this.users.clear();
			this.users.addAll(Arrays.asList(users));
			return this;
		}

		public Builder setTimeout(long timeout, TimeUnit unit) {
			this.timeout = timeout;
			this.unit = unit;
			return this;
		}

		public Builder setColor(Color color) {
			this.color = color;
			return this;
		}

		public Builder setFinalAction(Consumer<Message> finalAction) {
			this.finalAction = finalAction;
			return this;
		}

		public Builder setColumns(int columns) {
			if (columns < 1 || columns > 3) {
				throw new IllegalArgumentException("Only 1, 2, or 3 columns are supported");
			}
			this.columns = columns;
			return this;
		}

		public Builder setItemsPerPage(int num) {
			if (num < 1) {
				throw new IllegalArgumentException("There must be at least one item per page");
			}
			this.itemsPerPage = num;
			return this;
		}

		public void addStrings(String... items) {
			extras.addStrings(items);
		}

		public Builder wrapPageEnds(boolean wrapPageEnds) {
			this.wrapPageEnds = wrapPageEnds;
			return this;
		}

		public Builder showPageNumbers(boolean showPageNumbers) {
			this.showPageNumbers = showPageNumbers;
			return this;
		}

		public Builder updateExtras(Function<PaginatorExtras, PaginatorExtras> extras) {
			extras.apply(this.extras);
			return this;
		}

		public int size() {
			return switch (extras.getType()) {
				case DEFAULT -> extras.getStrings().size();
				case EMBED_FIELDS -> extras.getEmbedFields().size();
				case EMBED_PAGES -> extras.getEmbedPages().size();
			};
		}
	}
}
