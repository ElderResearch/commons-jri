package com.elderresearch.commons.rserve;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.elderresearch.commons.jri.util.RArgs;
import com.elderresearch.commons.jri.util.RPath;
import com.google.common.collect.Lists;

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
	
	public RConnectionWrapper launch() {
		val cmd = Lists.newArrayList("R");
		cmd.addAll(Arrays.asList(args));
		cmd.add("-e");

		String[] rserveArgs = {"--RS-port", String.valueOf(port)};
		cmd.add(String.format("Rserve::Rserve(FALSE, args='%s')", StringUtils.join(rserveArgs, ' ')));
		
		try {
			process = new ProcessBuilder(cmd).inheritIO().start();
		} catch (IOException e) {
			log.warn("Error starting R process", e);
			return null;
		}

		val c = connect();
		try {
			if (packages.length > 0 && c != null) {
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
			return new RConnectionWrapper(new RConnection("localhost", port));
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
