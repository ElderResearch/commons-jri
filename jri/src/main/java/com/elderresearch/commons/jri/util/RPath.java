package com.elderresearch.commons.jri.util;

import java.nio.file.Path;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "forPath")
public class RPath {
	private Path path;
	
	@Override
	public String toString() {
		// Regardless of OS, R wants paths to have forward slashes
        return path.toString().replace('\\', '/');
	}
}
