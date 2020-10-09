package com.elderresearch.commons.rserve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.elderresearch.commons.jri.util.RArgs;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Accessors(chain = true, fluent = true)
public class RLauncher {
	private static volatile int defaultPort = 6311;
	
	@Setter private String[] args = RArgs.getDefaultArgs();
	@Setter private String[] packages = ArrayUtils.EMPTY_STRING_ARRAY;
	
	@Getter private Process process;
	@Setter @Getter private int port = defaultPort++;
	
	public RLauncher launch() throws IOException {
		val cmd = new ArrayList<String>();
		cmd.add(SystemUtils.IS_OS_WINDOWS? "R" : "R");
		cmd.addAll(Arrays.asList(args));
		cmd.add("-e");

		String[] rserveArgs = {"--RS-port", String.valueOf(port)};
		cmd.add(String.format("Rserve::Rserve(FALSE, args='%s')", StringUtils.join(rserveArgs, ' ')));
		
		process = new ProcessBuilder()
			.inheritIO()
			.command(cmd.toArray(String[]::new))
			.start();
		
		try {
			if (packages.length > 0) {
				val c = connect();
				c.assign("requiredPackages", packages);
				c.parseAndEval("lapply(requiredPackages, require, character.only = TRUE)");
				c.close();
			}
		} catch (REngineException | REXPMismatchException e) {
			log.warn("Error loading packages {}", packages, e);
			e.printStackTrace();
		}
		
		return this;
	}
	
	public RConnection connect() throws RserveException {
		return new RConnection("localhost", port);
	}
	
	public static RLauncher newLauncher(String... packages) {
		return new RLauncher().packages(packages);
	}
}
