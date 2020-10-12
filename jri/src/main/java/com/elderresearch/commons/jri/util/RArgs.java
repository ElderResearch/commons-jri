package com.elderresearch.commons.jri.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RArgs {
	@Getter
	private final String[] defaultArgs = { "--slave", "--vanilla" };
}
