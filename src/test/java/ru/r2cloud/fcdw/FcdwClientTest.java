package ru.r2cloud.fcdw;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FcdwClientTest {

	private static final String SITE_ID = "M7RED";
	private static final String AUTH_CODE = "0a8baf2437ba5aa72b4bba2f94fdd98a";

	private WarehouseServer server;

	@Test(expected = AuthenticationException.class)
	public void testAuthFailure() throws Exception {
		server.setFailure(401, "expected auth failure");
		FcdwClient client = new FcdwClient(server.getUrl(), SITE_ID, AUTH_CODE);
		client.upload(UUID.randomUUID().toString());
	}

	@Test(expected = AuthenticationException.class)
	public void testAuthFailureNoMessage() throws Exception {
		server.setFailure(401, null);
		FcdwClient client = new FcdwClient(server.getUrl(), SITE_ID, AUTH_CODE);
		client.upload(UUID.randomUUID().toString());
	}

	@Test(expected = IOException.class)
	public void testIOException() throws Exception {
		FcdwClient client = new FcdwClient("http://" + UUID.randomUUID().toString() + ".com", SITE_ID, AUTH_CODE);
		client.upload(UUID.randomUUID().toString());
	}

	@Test(expected = IOException.class)
	public void testInternalServerError() throws Exception {
		server.setFailure(503, "internal server error");
		FcdwClient client = new FcdwClient(server.getUrl(), SITE_ID, AUTH_CODE);
		client.upload(UUID.randomUUID().toString());
	}

	@Test
	public void testUpload() throws Exception {
		FcdwClient client = new FcdwClient(server.getUrl(), SITE_ID, AUTH_CODE);
		String hexFrame = "9d6470cd32f4971a56a4d7c7714b40d3";
		client.upload(hexFrame);
		assertEquals("aa59205be50bf8dbc22a0dc107ce8781", server.getDigest());
	}

	@Test
	public void testUploadBytes() throws Exception {
		FcdwClient client = new FcdwClient(server.getUrl(), SITE_ID, AUTH_CODE);
		client.upload(new byte[] { (byte) 0x9d, 0x64, 0x70, (byte) 0xcd, 0x32, (byte) 0xf4, (byte) 0x97, 0x1a, 0x56, (byte) 0xa4, (byte) 0xd7, (byte) 0xc7, 0x71, 0x4b, 0x40, (byte) 0xd3 });
		assertEquals("aa59205be50bf8dbc22a0dc107ce8781", server.getDigest());
	}

	@Before
	public void start() throws Exception {
		server = new WarehouseServer(SITE_ID);
		server.start();
	}

	@After
	public void stop() throws Exception {
		server.stop();
	}
}
