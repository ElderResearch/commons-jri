package com.elderresearch.commons.rjava.util;

import org.apache.commons.lang3.ArrayUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import com.elderresearch.commons.jri.PackageScope;
import com.elderresearch.commons.jri.PackageType;
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
	private static final String DEF_CRAN_URL = "https://cloud.r-project.org";
	
	private final RPath packagePath;
	private String cranUrl = DEF_CRAN_URL;
	private PackageScope[] scopes = PackageScope.getDefaultScopes();
	private PackageType type = PackageType.getDefaultType();
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
			re.assign("type", type.toString());
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
