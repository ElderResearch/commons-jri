package com.elderresearch.commons.rserve;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import lombok.val;

@Ignore("Until we configure CI to have R correctly installed and configured")
public class RLauncherTest {
	@Test
	public void testRConnection() throws REXPMismatchException, REngineException {
		val c = RLauncher.newLauncher().launch().connect();
		Assert.assertEquals(4, c.tryEval("2 + 2").asInteger());
		RLauncher.shutdownAndClose(c);
	}
	
	@Test
	public void testMultipleConnection() throws REXPMismatchException, REngineException {
		val c1 = RLauncher.newLauncher().launch().connect();
		val c2 = RLauncher.newLauncher().launch().connect();
		Assert.assertEquals(4, c1.tryEval("2 + 2").asInteger());
		Assert.assertEquals(6, c1.tryEval("2 * 3").asInteger());
		RLauncher.shutdownAndClose(c1);
		RLauncher.shutdownAndClose(c2);
	}
}
