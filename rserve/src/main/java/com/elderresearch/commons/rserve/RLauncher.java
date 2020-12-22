package com.elderresearch.commons.rserve;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.elderresearch.commons.jri.util.RArgs;
import com.elderresearch.commons.jri.util.RPath;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Accessors(chain = true, fluent = true)
public class RLauncher {
	@Setter private String[] args = RArgs.getDefaultArgs();
	@Setter private RPath libraryPath = RPath.getDefaultLibraryPath();
	@Setter private String[] packages = ArrayUtils.EMPTY_STRING_ARRAY;
	
	@Getter private Process process;
	@Setter @Getter private int port = findFreePort();
	@Setter @Getter private String host = "localhost";
	
	/**
	 * Determines whether or not to continue trying to connect to R. The first argument is the number of connection
	 * attempts and the second is the time since the first connection attempt.
	 */
	@Setter private BiPredicate<Integer, Duration> retryPolicy = (nAttempts, elapsedTime) -> nAttempts < 5;

	private static BiFunction<String[], Integer, List<String>> batch(String rServeName) {
		return (a, p) -> {
			val cmd = Lists.newArrayList("R", "CMD", rServeName, "--RS-port", String.valueOf(p));
			for (val arg : a) { cmd.add(arg); }
			return cmd;
		};
	}
	
	@AllArgsConstructor
	public enum LaunchType {
		/** Uses {@code R CMD Rserve ...}, only available on Unix. */
		BATCH(batch("Rserve")),
		/** Uses {@code R CMD Rserve.dbg ...} debug/non-daemon mode, only available on Unix. */
		DEBUG(batch("Rserve.dbg")),
		/** Uses {@code R -e Rserve::Rserve(...)}, available on all platforms. */
		FROM_R((a, p) -> {
			// Add the R args for the R session invoking R serve
			val cmd = Lists.newArrayList("R");
			for (val arg : a) { cmd.add(arg); }
			
			// Add the args again for Rserve to pass to its session
			cmd.add("-e");
			String[] allArgs = ArrayUtils.addAll(a, "--RS-port", String.valueOf(p));
			cmd.add(String.format("Rserve::Rserve(FALSE, args='%s')", StringUtils.join(allArgs, ' ')));
			return cmd;
		});
		
		private BiFunction<String[], Integer, List<String>> argsToCmd;
	}
	
	public RLauncher enableRemoteAccess() {
		this.args = ArrayUtils.add(args, "--RS-enable-remote");
		return this;
	}
	
	public RConnectionWrapper launch() {
		return launch(LaunchType.FROM_R);
	}
	
	public RConnectionWrapper launch(LaunchType type) {
		try {
			process = new ProcessBuilder(type.argsToCmd.apply(args, port)).inheritIO().start();
		} catch (IOException e) {
			log.warn("Error starting R process", e);
			return null;
		}

		val start = Instant.now();
		int i = 0;
		RConnectionWrapper c = null;
		RserveException lastException = null;
		do {
			try {
				c = new RConnectionWrapper(new RConnection(host, port));
			} catch (RserveException ex) {
				lastException = ex;
				log.debug("Could not connect to R ({}). Trying again...", ex.getMessage());
				try {
					Thread.sleep(300L);
				} catch (InterruptedException ie) {
					// Ignore interruption
				}
			}
		} while (c == null && retryPolicy.test(++i, Duration.between(start, Instant.now())));
		
		if (c == null) {
			log.warn("Failed to connect to R", lastException);
			return null;
		}
		
		try {
			if (packages.length > 0) {
				c.assign("requiredPackages", packages);
				c.tryEval(".libPaths('%s')", libraryPath);
				c.tryEval("lapply(requiredPackages, require, character.only = TRUE)");
			}
		} catch (REngineException | REXPMismatchException e) {
			log.warn("Error loading packages {}", packages, e);
		}
		return c;
	}
	
	public RConnectionWrapper connect() {
		try {
			return new RConnectionWrapper(new RConnection(host, port));
		} catch (RserveException e) {
			log.warn("Error connecting to R", e);
			return null;
		}
	}
	
	public static RLauncher newLauncher(String... packages) {
		return new RLauncher().packages(packages);
	}
	
	private static int findFreePort() {
		try (val s = new ServerSocket(0)) {
			return s.getLocalPort();
        } catch (IOException ex) {
        	throw new IllegalStateException("Could not find a free port for Rserve", ex);
		}
	}
}
