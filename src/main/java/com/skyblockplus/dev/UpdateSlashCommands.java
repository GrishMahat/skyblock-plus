package com.skyblockplus.dev;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.skyblockplus.utils.command.CommandExecute;
import com.skyblockplus.utils.command.SlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.skyblockplus.utils.utils.Utils.*;

@Component
public class UpdateSlashCommands extends Command {

	public UpdateSlashCommands() {
		this.name = "d-slash";
		this.ownerCommand = true;
		this.botPermissions = defaultPerms();
	}

	@Override
	protected void execute(CommandEvent event) {
		new CommandExecute(event) {
			@Override
			protected void execute() {
				Guild guild = event.getGuild();
				if (args.length == 1) {
					// Register guild‑only commands
					guild.updateCommands()
							.addCommands(generateSlashCommands())
							.queue(cmds -> event.getChannel()
									.sendMessageEmbeds(defaultEmbed(
											"Success – added " + cmds.size() + " slash commands for this guild")
											.build())
									.queue()
							);
				} else {
					String sub = args[1].toLowerCase();
					switch (sub) {
						case "clear":
							guild.updateCommands()
									.queue(unused -> event.getChannel()
											.sendMessageEmbeds(defaultEmbed(
													"Success – cleared commands for this guild")
													.build())
											.queue()
									);
							break;
						case "global":
							// Register global commands
							jda.getShards().get(0).updateCommands()
									.addCommands(generateSlashCommands())
									.queue(cmds -> event.getChannel()
											.sendMessageEmbeds(defaultEmbed(
													"Success – added " + cmds.size() + " slash commands globally")
													.build())
											.queue()
									);
							break;
						default:
							event.getChannel()
									.sendMessage("Unknown option: `" + args[1] + "`. Use `clear` or `global`.")
									.queue();
					}
				}
			}
		};
	}

	private List<SlashCommandData> generateSlashCommands() {
		// No setGuildOnly – scope is based on registration target
		return slashCommandClient.getCommands().stream()
				.map(SlashCommand::getFullCommandData)
				.collect(Collectors.toList());
	}
}
