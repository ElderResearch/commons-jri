package com.elderresearch.commons.jri;

import org.apache.commons.lang3.SystemUtils;

import lombok.Getter;

public enum PackageType {
	BINARY,
	SOURCE;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
	
	@Getter
	private static final PackageType defaultType;
	
	static {
		// Auto detect dependency type based on OS
		if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC) {
			defaultType = PackageType.BINARY;
		} else {
			defaultType = PackageType.SOURCE;
		}
	}
}
