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

package com.skyblockplus.api.miscellaneous;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.skyblockplus.utils.utils.Utils.BASE_URL;

@Component
public class SubdomainFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		MutableHttpServletRequestWrapper reqWrapper = new MutableHttpServletRequestWrapper(((HttpServletRequest) req));

		String serverName = req.getServerName();
		if (serverName.contains(BASE_URL) && !serverName.equals(BASE_URL)) {
			String subdomain = serverName.split("." + BASE_URL)[0];
			reqWrapper.addHeader("X-Subdomain-Internal", subdomain);
		}

		chain.doFilter(reqWrapper, res);
	}
}
