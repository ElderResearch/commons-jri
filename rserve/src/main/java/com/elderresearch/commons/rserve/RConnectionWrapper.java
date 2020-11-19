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
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
public class RConnectionWrapper {
	@Getter
	private RConnection delegate;
	
	// Rserve doesn't have a REPL so error messages are lost. Capture them in a try and throw an exception
	// See: http://rforge.net/Rserve/faq.html#errors
	public REXP tryEval(String s, Object... args) throws REngineException, REXPMismatchException {
		val exp = "try(" + String.format(s, args) + ", silent=TRUE)";
		log.debug("R: {}", exp);
		val ret = delegate.parseAndEval(exp);
		if (ret.inherits("try-error")) { throw new REngineException(delegate, ret.asString()); }
		return ret;
	}

	public void assign(String s, byte...   arr) throws REngineException { delegate.assign(s, arr); }
	public void assign(String s, int...    arr) throws REngineException { delegate.assign(s, arr); }
	public void assign(String s, double... arr) throws REngineException { delegate.assign(s, arr); }
	public void assign(String s, String... arr) throws REngineException {
		if (arr.length == 1) {
			delegate.assign(s, arr[0]);
		} else {
			delegate.assign(s, arr);
		}
	}
}
