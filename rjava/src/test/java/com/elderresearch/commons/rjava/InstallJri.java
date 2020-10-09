package com.elderresearch.commons.rjava;

import java.io.IOException;

import com.elderresearch.commons.jri.util.JarInstaller;

public class InstallJri {
	public static void main(String[] args) throws IOException {
		JarInstaller.install("rJava", "jri", "JRI", "JRIEngine", "REngine");
	}
}
