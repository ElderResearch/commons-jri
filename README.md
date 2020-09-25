ERI Commons - Java/R Integration
------------------------------------------

![Build](https://github.com/ElderResearch/commons-jri/workflows/Java%20CI%20with%20Maven/badge.svg)

A library to make R/Java interaction (via rJava/JRI) simpler and more robust.

## Maven

You can install this library from [Jipack.io](https://jitpack.io). Add the following repository to your POM:

```xml
<repositories>
   <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
   </repository>
</repositories>
 ```

And then the following dependency:

```xml
<dependency>
	<groupId>com.elderresearch.commons-jri</groupId>
	<artifactId>commons-jri</artifactId>
	<version>0.0.1</version>
</dependency>
```

## Overview

This library helps with several aspects of using R from Java:
* Shading (packaging) the JRI .jars (which are not hosted officially on Maven central)
* Implements `RMainLoopCallbacks` with intelligent defaults, like sending R console output to Log4j
* Building `data.frame` REXPs using a more flexible API
* Providing a script to install all of a package's dependencies in a non-interactive way and to a folder local to your project. This is helpful for preparing self-contained Docker images with all of your R package's dependencies pre-installed.

## Prerequisites

This assumes the following is true of both the build host and the runtime host:
* R is installed and `R_HOME` is set (for example, `C:\Apps\R-4.0.2`)
* R's `/bin` folder is on the `PATH` (for example, `C:\Apps\R-4.0.2\bin\x64`)
* Packages `rJava` and `remotes` are installed
* rJava's folder is on the `PATH` and the `LD_LIBRARY_PATH` (for example, `C:\Apps\R-4.0.2\library\rJava\jri\x64`)

Here are a series of commands that will initialize a Linux environment and satisfy these requirements:

```sh
echo | sudo add-apt-repository ppa:marutter/c2d4u
sudo apt-get update
sudo apt-get install r-base r-cran-rjava
sudo R CMD javareconf
sudo Rscript -e "install.packages('remotes')"

PATH=/usr/lib/R/site-library/rJava/jri/:$PATH
LD_LIBRARY_PATH=/usr/lib/R/site-library/rJava/jri/
R_HOME=/usr/lib/R/
```

## Usage

* Optionally descend from the included parent POM (which pre-configures some properties and plugins for you). If that is not feasible, you can replicate the configuration in the provided parent POM in your POM instead:

```xml
<parent>
	<groupId>com.elderresearch.commons-jri</groupId>
	<artifactId>commons-jri-parent</artifactId>
	<version>0.0.1</version>
</parent>
```

* Create your R package in a folder. According to [our project template](https://gitlab.com/ElderResearch/devops/templates/project), we recommend putting it in `src/package.name`.
* Create a class with a main entry point that configures a `InstallDependencies` instance with the path to your package and invokes `install()`
* Specify the full class name to the property `r.package.installer`. This will automatically be run during the `prepare-pacakge` phase and will install the dependencies of your package to a folder (`/lib` by default) non-interactively.
* Add the folder with dependencies to your `.gitignore`.
* When packaging, be sure to include your R package and its dependencies in your assembly descriptor.
* Depend on the **shaded** artifact in your `pom.xml`:

```xml
<dependency>
	<groupId>${project.parent.groupId}</groupId>
	<artifactId>commons-jri</artifactId>
	<version>${project.parent.version}</version>
	<classifier>shaded</classifier>
</dependency>
```

## Contribution

We welcome pull requests and issues!
