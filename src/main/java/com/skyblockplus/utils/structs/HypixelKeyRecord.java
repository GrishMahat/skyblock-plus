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

package com.skyblockplus.utils.structs;

import lombok.ToString;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@ToString
public final class HypixelKeyRecord {

	private final AtomicInteger remainingLimit;
	private final AtomicInteger timeTillReset;
	private Instant time;

	public HypixelKeyRecord(int remainingLimit, int timeTillReset) {
		this.remainingLimit = new AtomicInteger(remainingLimit);
		this.timeTillReset = new AtomicInteger(timeTillReset);
		this.time = Instant.now();
	}

	public boolean isRateLimited() {
		return remainingLimit.get() < 5 && getTimeTillReset() > 0;
	}

	public void update(int remainingLimit, int timeTillReset) {
		this.remainingLimit.set(remainingLimit);
		this.timeTillReset.set(timeTillReset);
		this.time = Instant.now();
	}

	public long getTimeTillReset() {
		return Math.max(0, Duration.between(Instant.now(), time.plusSeconds(timeTillReset.get())).toSeconds());
	}
}
