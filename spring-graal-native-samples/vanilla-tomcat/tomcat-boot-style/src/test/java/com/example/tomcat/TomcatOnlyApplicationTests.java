package com.example.tomcat;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TomcatOnlyApplicationTests {

	private CountDownLatch latch = new CountDownLatch(1);
	private volatile boolean success = false;

	@Test
	public void tomcatStarts() throws Exception {
		TomcatOnlyApplication.main(new String[0]);
		Thread.sleep(250);
		new Thread(TomcatOnlyApplicationTests.this::checkTomcat).start();
		latch.await(2, TimeUnit.SECONDS);
		assertEquals(true, success);
	}


	public void checkTomcat() {
		try {
			URL url = new URL("http://localhost:8080");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			assertEquals(200, status);
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String content = reader.readLine();
			assertEquals("Hello from tomcat", content);
			success = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			latch.countDown();
		}
	}

}
