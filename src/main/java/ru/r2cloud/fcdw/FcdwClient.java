package ru.r2cloud.fcdw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FcdwClient {

	private static final int HEX_0X0F = 0x0F;
	private static final int DEFAULT_TIMEOUT = 30_000;

	private final String host;
	private final String siteId;
	private final String authCode;
	private final int timeoutMillis;

	public FcdwClient(String host, String siteId, String authCode) {
		this(host, siteId, authCode, DEFAULT_TIMEOUT);
	}

	public FcdwClient(String host, String siteId, String authCode, int timeoutMillis) {
		this.host = host;
		this.siteId = siteId;
		this.authCode = authCode;
		this.timeoutMillis = timeoutMillis;
	}

	public void upload(byte[] hexFrame) throws IOException, AuthenticationException {
		upload(convertToHex(hexFrame));
	}

	public void upload(String hexFrameStr) throws IOException, AuthenticationException {
		String baseUrl = host + "/api/data/hex/" + siteId + "/?digest=" + calculateDigest(hexFrameStr);
		URL obj = new URL(baseUrl);
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
		conn.setRequestMethod("POST");
		setupRequest(conn);
		conn.setDoOutput(true);
		String body = "data=" + hexFrameStr;
		conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

		int responseCode = conn.getResponseCode();
		if (responseCode == 401) {
			String responseBody = readFully(conn);
			conn.disconnect();
			throw new AuthenticationException(responseBody);
		}
		if (responseCode != 200) {
			String responseBody = readFully(conn);
			conn.disconnect();
			throw new IOException("invalid response code: " + responseCode + ": " + responseBody);
		}
		conn.disconnect();
	}

	private void setupRequest(HttpURLConnection conn) {
		conn.setRequestProperty("Content-Type", "text/plain");
		conn.setRequestProperty("User-Agent", "FcdwClient 1.1");
		conn.setReadTimeout(timeoutMillis);
		conn.setConnectTimeout(timeoutMillis);
	}

	private static String readFully(HttpURLConnection conn) throws IOException {
		if (conn.getErrorStream() == null) {
			return "";
		}
		String curLine = null;
		StringBuilder result = new StringBuilder();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
			while ((curLine = r.readLine()) != null) {
				result.append(curLine).append("\n");
			}
		}
		return result.toString().trim();
	}

	private String calculateDigest(String hexFrame) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		md5.update(hexFrame.getBytes(StandardCharsets.UTF_8));
		md5.update(":".getBytes(StandardCharsets.UTF_8));
		return convertToHex(md5.digest(authCode.getBytes(StandardCharsets.UTF_8)));
	}

	private static String convertToHex(final byte[] data) {
		final StringBuilder buf = new StringBuilder();
		for (final byte element : data) {
			int halfbyte = (element >>> 4) & HEX_0X0F;
			int twoHalfs = 0;
			do {
				if (0 <= halfbyte && halfbyte <= 9) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = element & HEX_0X0F;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}
}
