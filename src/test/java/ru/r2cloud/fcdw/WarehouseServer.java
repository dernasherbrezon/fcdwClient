package ru.r2cloud.fcdw;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WarehouseServer {

	private HttpServer server;
	private final String siteId;
	private String digest;
	private Integer failureCode;
	private String failureMessage;

	public WarehouseServer(String siteId) {
		this.siteId = siteId;
	}

	public void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
		server.start();
		server.createContext("/api/data/hex/" + siteId, new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				if (failureCode != null) {
					if (failureMessage != null) {
						byte[] responseBytes = failureMessage.getBytes(StandardCharsets.UTF_8);
						exchange.sendResponseHeaders(failureCode, responseBytes.length);
						OutputStream os = exchange.getResponseBody();
						os.write(responseBytes);
						os.close();
					} else {
						exchange.sendResponseHeaders(failureCode, 0L);
						exchange.close();
					}
					return;
				}
				Map<String, String> requestParameters = splitQuery(exchange.getRequestURI());
				digest = requestParameters.get("digest");
				exchange.sendResponseHeaders(200, 0L);
				exchange.close();
			}
		});
	}

	public String getDigest() {
		return digest;
	}

	public void setFailure(int statusCode, String message) {
		failureCode = statusCode;
		failureMessage = message;
	}

	public void stop() {
		if (server != null) {
			server.stop(0);
		}
	}

	public String getUrl() {
		return "http://" + server.getAddress().getHostName() + ":" + server.getAddress().getPort();
	}

	private static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
		Map<String, String> result = new LinkedHashMap<String, String>();
		String query = url.getQuery();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			if (idx == -1) {
				continue;
			}
			result.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return result;
	}

}
