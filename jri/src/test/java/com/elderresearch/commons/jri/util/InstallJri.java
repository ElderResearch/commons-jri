package com.elderresearch.commons.jri.util;

import java.io.IOException;

public class InstallJri {
	public static void main(String[] args) throws IOException {
		Installer.install("rJava", "jri", "JRI", "JRIEngine", "REngine");
	}
}
