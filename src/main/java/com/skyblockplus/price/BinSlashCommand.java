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

package com.skyblockplus.price;

import com.google.gson.JsonObject;
import com.skyblockplus.utils.command.SlashCommand;
import com.skyblockplus.utils.command.SlashCommandEvent;
import com.skyblockplus.utils.rendering.LoreRenderer;
import com.skyblockplus.utils.structs.AutoCompleteEvent;
import com.skyblockplus.utils.utils.StringUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.skyblockplus.utils.ApiHandler.queryLowestBin;
import static com.skyblockplus.utils.ApiHandler.uuidToUsername;
import static com.skyblockplus.utils.utils.JsonUtils.getLowestBinJson;
import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.StringUtils.*;
import static com.skyblockplus.utils.utils.Utils.defaultEmbed;
import static com.skyblockplus.utils.utils.Utils.errorEmbed;

@Component
public class BinSlashCommand extends SlashCommand {

	public BinSlashCommand() {
		this.name = "bin";
	}

	public static Object getLowestBin(String item) {
		JsonObject lowestBinJson = getLowestBinJson();
		if (lowestBinJson == null) {
			return errorEmbed("Error fetching lowest bin prices");
		}

		String itemId = nameToId(item, true);
		if (itemId == null) {
			itemId =
				getClosestMatchFromIds(item, lowestBinJson.keySet().stream().filter(e -> !e.contains("+")).collect(Collectors.toSet()));
		}

		JsonObject lowestBin = queryLowestBin(itemId);
		if (lowestBin == null) {
			return errorEmbed("No bins found for " + idToName(item));
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BufferedImage loreRender = LoreRenderer.renderLore(
				Arrays.stream(higherDepth(lowestBin, "lore").getAsString().split("\n")).toList()
			);
			ImageIO.write(loreRender, "png", baos);
		} catch (Exception ignored) {}

		int count = higherDepth(lowestBin, "count").getAsInt();

		return new MessageEditBuilder()
			.setEmbeds(
				defaultEmbed(idToName(itemId))
					.setDescription(
						"**Price:** " +
						formatNumber(higherDepth(lowestBin, "starting_bid").getAsLong()) +
						(count > 1 ? "\n**Count:** " + count : "") +
						"\n**Seller:** " +
						uuidToUsername(higherDepth(lowestBin, "auctioneer").getAsString()).username() +
						"\n**Ends:** " +
						getRelativeTimestamp(higherDepth(lowestBin, "end_t").getAsLong()) +
						"\n**Command:** `/viewauction " +
						higherDepth(lowestBin, "uuid").getAsString() +
						"`"
					)
					.setThumbnail(getItemThumbnail(itemId))
					.setImage("attachment://lore.png")
					.build()
			)
			.setFiles(FileUpload.fromData(baos.toByteArray(), "lore.png"));
	}

	@Override
	protected void execute(SlashCommandEvent event) {
		event.embed(getLowestBin(event.getOptionStr("item")));
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(name, "Get the lowest bin of an item").addOption(OptionType.STRING, "item", "Item name", true, true);
	}

	@Override
	public void onAutoComplete(AutoCompleteEvent event) {
		if (event.getFocusedOption().getName().equals("item")) {
			if (getLowestBinJson() != null) {
				event.replyClosestMatch(
					event.getFocusedOption().getValue(),
					getLowestBinJson()
						.keySet()
						.stream()
						.filter(e -> !e.contains("+"))
						.map(StringUtils::idToName)
						.distinct()
						.collect(Collectors.toCollection(ArrayList::new))
				);
			}
		}
	}
}
