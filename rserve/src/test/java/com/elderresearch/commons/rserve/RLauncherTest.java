package com.elderresearch.commons.rserve;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import lombok.val;

// @Ignore("Until we configure CI to have R correctly installed and configured")
public class RLauncherTest {
	@Test
	public void testRConnection() throws REXPMismatchException, REngineException, IOException {
		val c = RLauncher.newLauncher().launch().connect();
		Assert.assertEquals(4, c.parseAndEval("2 + 2").asInteger());
		c.shutdown();
		c.close();
	}
	
	@Test
	public void testMultipleConnection() throws REXPMismatchException, REngineException, IOException {
		val c1 = RLauncher.newLauncher().launch().connect();
		val c2 = RLauncher.newLauncher().launch().connect();
		Assert.assertEquals(4, c1.parseAndEval("2 + 2").asInteger());
		Assert.assertEquals(6, c1.parseAndEval("2 * 3").asInteger());
		c1.shutdown();
		c2.shutdown();
		c1.close();
		c2.close();
	}
}
