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

package com.skyblockplus.features.jacob;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

@Data
@Slf4j
public class JacobData {

	private boolean complete = true;
	private int year;
	private List<JacobContest> contests;

	public JacobContest getNextContest() {
		if (contests == null) {
			complete = false;
			log.error("Contests list is null");
			return null;
		}

		try {
			for (Iterator<JacobContest> iterator = contests.iterator(); iterator.hasNext();) {
				JacobContest contest = iterator.next();
				if (contest == null) {
					continue;
				}

				if (contest.reminderHasPassed()) {
					iterator.remove();
				} else {
					return contest;
				}
			}
		} catch (Exception e) {
			complete = false;
			log.error("Error processing contests: " + e.getMessage());
		}
		return null;
	}
}
