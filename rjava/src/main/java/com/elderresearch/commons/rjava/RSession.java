package com.elderresearch.commons.rjava;

import org.apache.commons.lang3.ArrayUtils;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.JRI.JRIEngine;

import com.elderresearch.commons.jri.util.RPath;
import com.elderresearch.commons.rjava.util.InstallDependencies;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Setter @Accessors(chain = true, fluent = true)
public class RSession {
	private static final String[] DEF_ARGS = { "--slave", "--vanilla" };
	
	private String[] args = DEF_ARGS;
	private String[] packages = ArrayUtils.EMPTY_STRING_ARRAY;
	private RMainLoopCallbacks callback = RCallback.INSTANCE;
	private boolean interactive = false;
	
	@Getter
	private RPath libraryPath = InstallDependencies.DEF_INSTALL_PATH;
	
	@SuppressWarnings("hiding")
	public RSession args(String... args) {
		this.args = args;
		return this;
	}
	
	@SuppressWarnings("hiding")
	public RSession packages(String... packages) {
		this.packages = packages;
		return this;
	}
	
	/**
	 * Starts a new {@link REngine} and returns it. There can only be one instance per VM, so if one has already
	 * been started, it will be returned. If {@code restart} is {@code true}, the current session will be closed
	 * and a new one will be created (if the configuration has changed).
	 * @param restart whether or not to restart the session if one already exists
	 * @return the new R session (or the existing one if one already existed)
	 */
	public REngine start(boolean restart) {
		// Can only have one engine instance per VM
		REngine re = REngine.getLastEngine();
		if (re == null) {
			log.info("Creating R engine with args {}...", ArrayUtils.toString(args));
            try {
				re = new JRIEngine(args, callback, interactive);
				re.parseAndEval(String.format(".libPaths('%s')", libraryPath));
				for (val p : packages) {
					log.info("Loading package {}...", p);
					re.parseAndEval(String.format("library(%s)", p));
				}
				log.info("R engine ready");
			} catch (REngineException | REXPMismatchException | UnsatisfiedLinkError e) {
				throw new IllegalStateException("Error creating R engine", e);
			}
		} else if (restart) {
			re.close();
			return start(false);
		}
		return re;
	}
	
	public void stop() {
		get().close();
	}
	
	public static REngine get() {
		val ret = REngine.getLastEngine();
		if (ret == null) {
			throw new IllegalStateException("You must configure and start an RSession instance before accessing the global session");
		}
		return ret;
	}
}
