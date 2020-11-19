package com.elderresearch.commons.jri.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor(staticName = "forPath")
public class RPath {
	private Path path;
	
	@Override
	public String toString() {
		// Regardless of OS, R wants paths to have forward slashes
        return path.toString().replace('\\', '/');
	}
	
	@Getter
	private static final RPath defaultLibraryPath;
	static {
		// Auto detect install default library path based on OS
		Path p = Paths.get("lib");
		if (SystemUtils.IS_OS_WINDOWS) {
			p = p.resolve("win");
		} else if (SystemUtils.IS_OS_MAC) {
			p = p.resolve("mac");
		} else {
			p = p.resolve("nix");
		}
		try {
			Files.createDirectories(p);
		} catch (IOException e) {
			log.warn("Error creating directories for R packages", e);
		}
		defaultLibraryPath = RPath.forPath(p);
	}
}
