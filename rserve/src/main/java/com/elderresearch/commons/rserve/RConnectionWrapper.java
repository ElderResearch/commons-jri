package com.elderresearch.commons.rserve;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
public class RConnectionWrapper {
	@Getter
	private RConnection delegate;
	
	// Rserve doesn't have a REPL so error messages are lost. Capture them in a try and throw an exception
	// See: http://rforge.net/Rserve/faq.html#errors
	public REXP tryEval(String s, Object... args) throws REngineException, REXPMismatchException {
		val ret = delegate.parseAndEval("try(" + String.format(s, args) + ", silent=TRUE)");
		if (ret.inherits("try-error")) { throw new REngineException(delegate, ret.asString()); }
		return ret;
	}

	public void assign(String s, byte...   arr) throws REngineException { delegate.assign(s, arr); }
	public void assign(String s, int...    arr) throws REngineException { delegate.assign(s, arr); }
	public void assign(String s, double... arr) throws REngineException { delegate.assign(s, arr); }
	public void assign(String s, String... arr) throws REngineException { delegate.assign(s, arr); }
}
