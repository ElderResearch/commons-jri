package com.elderresearch.commons.jri.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.maven.model.Developer;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import lombok.val;

class JarInstaller {
	private static final String MVN_INSTALL_CMD = "call mvn install:install-file -Dfile=\"${jar}\" -DpomFile=\"${pom}\" -DlocalRepositoryPath=\"lib\"";
	
	static Model convertDescriptionToPOM(Path rJavaHome) throws IOException {
		// Not perfect, but no readily available Debian Control File (DCF) parser for Java
		val props = new Properties();
		props.load(Files.newInputStream(rJavaHome.resolve("DESCRIPTION")));
		
		val model = new Model();
		model.setName(props.getProperty("Title"));
		model.setUrl(props.getProperty("URL"));
		model.setGroupId("org.rosuda");
		model.setArtifactId(props.getProperty("Package"));
		model.setVersion(props.getProperty("Version"));
		model.setDescription(props.getProperty("Description"));
		
		val issueMgmt = new IssueManagement();
		issueMgmt.setUrl(props.getProperty("BugReports"));
		model.setIssueManagement(issueMgmt);
		
		val license = new License();
		license.setName(props.getProperty("License"));
		model.setLicenses(Arrays.asList(license));	
		
		model.setDevelopers(Arrays.asList(
			toDeveloper(props, "Author"),
			toDeveloper(props, "Maintainer")
		));
		return model;
	}
	
	private static Developer toDeveloper(Properties p, String role) {
		val ret = new Developer();
		
		val s = p.getProperty(role);
		int i = s.indexOf('<');

		ret.setName(s.substring(0, i).strip());
		ret.setEmail(StringUtils.chop(s.substring(i + 1)));
		ret.setRoles(Arrays.asList(role));
		return ret;
	}
	
	private static void writePom(Model m, Path p) throws IOException {
		new MavenXpp3Writer().write(Files.newOutputStream(p), m);
	}
	
	public static void install(String rPackage, String jarFolder, String... jars) throws IOException {
		val pkgHome = Paths.get(System.getenv("R_HOME"), "library", rPackage);
		val model = convertDescriptionToPOM(pkgHome);
		
		val cmds = new LinkedList<String>();
		for (val artifact : Arrays.asList(jars)) {
			val pomPath = Paths.get("target", artifact + "-pom.xml");
			
			model.setArtifactId(artifact);
			writePom(model, pomPath);
			
			cmds.add(StrSubstitutor.replace(MVN_INSTALL_CMD, ImmutableMap.of(
				"jar", pkgHome.resolve(jarFolder).resolve(artifact + ".jar").toString(),
				"pom", pomPath.toString()
			)));
		}
		
		val batPath = Paths.get("target", "install.bat");
		Files.write(batPath, cmds);
		new ProcessBuilder(batPath.toString()).inheritIO().start();		
	}
}
