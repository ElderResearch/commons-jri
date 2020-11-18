package com.elderresearch.commons.rjava.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SystemUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import com.elderresearch.commons.jri.util.RPath;
import com.elderresearch.commons.rjava.RSession;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Setter @Accessors(chain = true, fluent = true)
@RequiredArgsConstructor(staticName = "ofPackage")
public class InstallDependencies {
	public enum PackageType {
		BINARY,
		SOURCE
	}
	
	public enum PackageScope {
		Depends,
		Imports,
		Suggests,
		Enhances,
		LinkingTo
	}

	private static final String DEF_CRAN_URL = "https://cloud.r-project.org";
	private static final PackageScope[] DEF_SCOPES = { PackageScope.Depends, PackageScope.Imports };
	private static final PackageType DEF_TYPE;
	public static final RPath DEF_INSTALL_PATH;
	
	static {
		// Auto detect install path and dependency type based on OS
		Path p = Paths.get("lib");
		if (SystemUtils.IS_OS_WINDOWS) {
			p = p.resolve("win");
			DEF_TYPE = PackageType.BINARY;
		} else if (SystemUtils.IS_OS_MAC) {
			p = p.resolve("mac");
			DEF_TYPE = PackageType.BINARY;
		} else {
			p = p.resolve("nix");
			DEF_TYPE = PackageType.SOURCE;
		}
		try {
			Files.createDirectories(p);
		} catch (IOException e) {
			log.warn("Error creating directories for R dependencies", e);
		}
		DEF_INSTALL_PATH = RPath.forPath(p);
	}

	private final RPath packagePath;
	private String cranUrl = DEF_CRAN_URL;
	private PackageScope[] scopes = DEF_SCOPES;
	private PackageType type = DEF_TYPE;
	private RSession session = new RSession();

	public InstallDependencies setScopes(PackageScope... scopes) {
		this.scopes = scopes;
		return this;
	}
	
	public void install() {
		// Create an R engine
		val re = session.start(false);
		try {
			// Use cloud CDN CRAN to download dependencies
			re.assign("url", cranUrl);
			// Binaries aren't available on *Nix, having the right toolchain to compile is hard on Mac/Win
			re.assign("type", type.name().toLowerCase());
			// Set the type of dependencies to install
			re.assign("deps", ArrayUtils.toStringArray(scopes));
			// Install the package to the specified local directory, which installs any transitive dependencies
			re.parseAndEval(String.format("remotes::install_local('%s', "
				+ "upgrade='never', "
				+ "dependencies=deps, "
				+ "lib='%s', "
				+ "type=type, "
				+ "repos=url)", packagePath, session.libraryPath()));
		} catch (REngineException | REXPMismatchException e) {
			log.warn("Error installing {}", packagePath, e);
		}
		re.close();
	}
}
