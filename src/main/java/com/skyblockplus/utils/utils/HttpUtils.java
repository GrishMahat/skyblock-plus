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

package com.skyblockplus.utils.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.skyblockplus.utils.SkyblockProfilesParser;
import com.skyblockplus.utils.exceptionhandler.ExceptionExecutor;
import com.skyblockplus.utils.structs.JsonResponse;
import okhttp3.OkHttpClient;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.skyblockplus.utils.utils.JsonUtils.higherDepth;
import static com.skyblockplus.utils.utils.Utils.*;

public class HttpUtils {

	public static final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
	public static final OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	private static final HttpClient asyncHttpClient = HttpClient
		.newBuilder()
		.executor(new ExceptionExecutor(20, 20, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()).setAllowCoreThreadTimeOut(true))
		.build();

	public static String getUrl(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("content-type", "application/json; charset=UTF-8");

			try (
				CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
				InputStreamReader in = new InputStreamReader(httpResponse.getEntity().getContent());
				BufferedReader buff = new BufferedReader(in)
			) {
				return buff.lines().parallel().collect(Collectors.joining("\n"));
			}
		} catch (Exception ignored) {}
		return null;
	}

	public static JsonElement getJson(URIBuilder jsonUrl) {
		return getJson(jsonUrl.toString());
	}

	public static JsonElement getJson(String jsonUrl) {
		JsonResponse response = getJsonResponse(jsonUrl);
		return response != null ? response.response() : null;
	}

	public static JsonResponse getJsonResponse(String jsonUrl) {
		try {
			if (jsonUrl.contains(HYPIXEL_API_KEY) && hypixelRateLimiter.isRateLimited()) {
				long timeTillReset = hypixelRateLimiter.getTimeTillReset();
				log.info("Sleeping for " + timeTillReset + " seconds");
				TimeUnit.SECONDS.sleep(timeTillReset);
			}
		} catch (Exception ignored) {}

		try {
			HttpGet httpGet = new HttpGet(jsonUrl);
			if (jsonUrl.contains("raw.githubusercontent.com")) {
				httpGet.setHeader("Authorization", "token " + GITHUB_TOKEN);
			}
			httpGet.addHeader("content-type", "application/json; charset=UTF-8");

			try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
				if (jsonUrl.contains("api.hypixel.net")) {
					if (jsonUrl.contains(HYPIXEL_API_KEY)) {
						try {
							hypixelRateLimiter.update(
								Integer.parseInt(httpResponse.getFirstHeader("RateLimit-Remaining").getValue()),
								Integer.parseInt(httpResponse.getFirstHeader("RateLimit-Reset").getValue())
							);
						} catch (Exception ignored) {}
					}

					if (httpResponse.getStatusLine().getStatusCode() == 502) {
						JsonObject obj = new JsonObject();
						obj.addProperty("cause", "Hypixel API returned 502 bad gateway. The API may be down.");
						return new JsonResponse(obj, httpResponse.getStatusLine().getStatusCode());
					} else if (httpResponse.getStatusLine().getStatusCode() == 522) {
						JsonObject obj = new JsonObject();
						obj.addProperty("cause", "Hypixel API returned 522 connection timed out. The API may be down.");
						return new JsonResponse(obj, httpResponse.getStatusLine().getStatusCode());
					}
				}

				try (
					InputStreamReader in = new InputStreamReader(httpResponse.getEntity().getContent());
					JsonReader jsonIn = new JsonReader(in)
				) {
					JsonElement json = httpGet.getURI().getPath().equals("/v2/skyblock/profiles")
						? SkyblockProfilesParser.parse(jsonIn, httpGet.getURI().getQuery().split("uuid=")[1])
						: JsonParser.parseReader(jsonIn);

					if (jsonUrl.contains("api.hypixel.net") && higherDepth(json, "throttle", false) && higherDepth(json, "global", false)) {
						JsonObject obj = new JsonObject();
						obj.addProperty(
							"cause",
							"Hypixel API returned 429 too many requests. The API is globally throttled and may be down."
						);
						return new JsonResponse(obj, httpResponse.getStatusLine().getStatusCode());
					}

					return new JsonResponse(json, httpResponse.getStatusLine().getStatusCode());
				}
			}
		} catch (Exception ignored) {}
		return null;
	}

	public static JsonObject getJsonObject(URIBuilder url) {
		return getJsonObject(url.toString());
	}

	public static JsonObject getJsonObject(String url) {
		try {
			return getJson(url).getAsJsonObject();
		} catch (Exception e) {
			return null;
		}
	}

	public static CompletableFuture<HttpResponse<InputStream>> asyncGet(String url) {
		return asyncHttpClient.sendAsync(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofInputStream());
	}

	public static CompletableFuture<JsonElement> asyncGetJson(String url) {
		return asyncGet(url)
			.thenApplyAsync(
				r -> {
					try (InputStreamReader in = new InputStreamReader(r.body())) {
						return JsonParser.parseReader(in);
					} catch (Exception e) {
						return null;
					}
				},
				executor
			);
	}

	/**
	 * @param headers name, value, name, value, ...
	 */
	public static CompletableFuture<HttpResponse<InputStream>> asyncPutJson(String url, JsonElement body, String... headers) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).PUT(HttpRequest.BodyPublishers.ofString(body.toString()));
		if (headers.length > 0) {
			builder.headers(headers);
		}
		return asyncHttpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
	}

	public static JsonElement postUrl(String url, Object body) {
		try {
			HttpPost httpPost = new HttpPost(url);

			StringEntity entity = new StringEntity(body.toString(), "UTF-8");
			httpPost.setEntity(entity);

			try (
				CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
				InputStreamReader in = new InputStreamReader(httpResponse.getEntity().getContent())
			) {
				return JsonParser.parseReader(in);
			}
		} catch (Exception ignored) {}
		return null;
	}

	public static JsonElement postJson(String url, JsonElement body, Header... headers) {
		try {
			HttpPost httpPost = new HttpPost(url);

			StringEntity entity = new StringEntity(body.toString(), "UTF-8");
			httpPost.setEntity(entity);
			httpPost.setHeaders(headers);
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Accept", "application/json");

			try (
				CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
				InputStreamReader in = new InputStreamReader(httpResponse.getEntity().getContent())
			) {
				return JsonParser.parseReader(in);
			}
		} catch (Exception ignored) {}
		return null;
	}

	public static JsonElement deleteUrl(String url, Header... headers) {
		try {
			HttpDelete httpDelete = new HttpDelete(url);

			httpDelete.setHeader("Content-Type", "application/json");
			httpDelete.setHeader("Accept", "application/json");
			httpDelete.setHeaders(headers);

			try (
				CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
				InputStreamReader in = new InputStreamReader(httpResponse.getEntity().getContent())
			) {
				return JsonParser.parseReader(in);
			}
		} catch (Exception ignored) {}
		return null;
	}

	public static void closeHttpClient() {
		try {
			log.info("Closing Http Client");
			httpClient.close();
			log.info("Successfully Closed Http Client");
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
