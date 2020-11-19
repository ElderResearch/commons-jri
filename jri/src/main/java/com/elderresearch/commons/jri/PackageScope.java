package com.elderresearch.commons.jri;

import lombok.Getter;

public enum PackageScope {
	Depends,
	Imports,
	Suggests,
	Enhances,
	LinkingTo;
	
	@Getter
	private static final PackageScope[] defaultScopes = { Depends, Imports };
}
