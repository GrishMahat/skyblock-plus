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

import com.skyblockplus.Main;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.*;

import static com.skyblockplus.utils.utils.Utils.errorEmbed;

public class SlashCommandClient extends ListenerAdapter {

	private final List<SlashCommand> slashCommands;

	@Getter
	private final Map<String, Integer> commandUses = new HashMap<>();

	private String ownerId;

	public SlashCommandClient() {
		this.slashCommands = new ArrayList<>();
	}

	public SlashCommandClient addCommands(Collection<SlashCommand> commands) {
		for (SlashCommand command : commands) {
			if (slashCommands.stream().anyMatch(cmd -> cmd.getName().equalsIgnoreCase(command.getName()))) {
				Main.log.error(
					"",
					new IllegalArgumentException("Tried to add a command name that has already been indexed: " + command.getName())
				);
				throw new IllegalArgumentException("Tried to add a command name that has already been indexed: " + command.getName());
			} else {
				// Subcommands
				for (Class<?> declaredClass : command.getClass().getDeclaredClasses()) {
					if (declaredClass.getSuperclass() == Subcommand.class) {
						try {
							command.addSubcommand((Subcommand) declaredClass.getDeclaredConstructor().newInstance());
						} catch (Exception e) {
							Main.log.error("Error adding subcommand for " + command.getName(), e);
							throw new RuntimeException(e);
						}
					}
				}

				slashCommands.add(command);
			}
		}
		return this;
	}

	public SlashCommandClient setOwnerId(String ownerId) {
		this.ownerId = ownerId;
		return this;
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (!event.isFromGuild()) {
			event.replyEmbeds(errorEmbed("This command cannot be used in direct messages").build()).setEphemeral(true).queue();
			return;
		}
		if (event.getChannelType() != ChannelType.TEXT && !event.getChannelType().isThread()) {
			event.replyEmbeds(errorEmbed("This command can only be used in text channels or threads").build()).setEphemeral(true).queue();
			return;
		}

		SlashCommandEvent slashCommandEvent = new SlashCommandEvent(event, this);
		for (SlashCommand command : slashCommands) {
			if (command.getName().equals(event.getName())) {
				command.run(slashCommandEvent);
				return;
			}
		}

		event.replyEmbeds(slashCommandEvent.invalidCommandMessage().build()).setEphemeral(true).queue();
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		if (!event.getCommandType().equals(Command.Type.SLASH)) {
			return;
		}

		for (SlashCommand slashCommand : slashCommands) {
			if (slashCommand.getName().equals(event.getName())) {
				slashCommand.onAutoCompleteInternal(new AutoCompleteEvent(event));
				break;
			}
		}
	}

	public List<SlashCommand> getCommands() {
		return slashCommands;
	}

	public boolean isOwner(String userId) {
		return userId.equals(ownerId);
	}

	public void setCommandUses(Map<String, Integer> commandUsage) {
		commandUsage.forEach((key, value) -> commandUses.merge(key, value, Integer::sum));
	}
}
