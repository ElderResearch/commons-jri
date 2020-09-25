package com.elderresearch.commons.jri;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import lombok.val;

@Ignore("Until we configure CI to have R correctly installed and configured")
public class RSessionTest {
	@Test
	public void testRSession() throws REXPMismatchException, REngineException {
		val session = new RSession().start(false);
		Assert.assertEquals(4, session.parseAndEval("2 + 2").asInteger());
		session.close();
	}
}
