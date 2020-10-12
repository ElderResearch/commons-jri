package com.elderresearch.commons.rserve;

import java.io.IOException;

import com.elderresearch.commons.jri.util.JarInstaller;

public class InstallRServe {
	public static void main(String[] args) throws IOException {
		JarInstaller.install("Rserve", "java", "Rserve", "REngine");
	}
}
