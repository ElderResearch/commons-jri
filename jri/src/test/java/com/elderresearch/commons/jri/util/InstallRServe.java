package com.elderresearch.commons.jri.util;

import java.io.IOException;

public class InstallRServe {
	public static void main(String[] args) throws IOException {
		JarInstaller.install("Rserve", "java", "Rserve", "REngine");
	}
}
