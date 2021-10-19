/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.scheduler;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utility class for quarkus scheduler
 *
 */
public class SchedulerUtils {

	// Day of week and day of month cannot coexist
	private static String[] cronPatterns = { "[*?0-59]", "[*?0-59]", "[*?0-23]", "[*?1-31]", "[*?1-12]",
			"[*?a-zA-Z0-9]+", "^(19|20)[0-9]{2}$" };

	private SchedulerUtils() {
	}

	/**
	 * Validate the @Scheduled cron member with each cron string part and return an
	 * error message if necessary
	 *
	 * @param cronString the cron member value
	 * @return the error fault for the cron string validation and null if valid
	 */
	public static SchedulerErrorCodes validateCronPattern(String cronString) {
		String[] cronParts = cronString.split("\\s+");

		if (cronParts.length < 6 || cronParts.length > 7) {
			return SchedulerErrorCodes.INVALID_CRON_LENGTH;
		}

		for (int i = 0; i < cronParts.length; i++) {
			if (!Pattern.matches(String.format("(%s|^\\*\\/\\d+|\\$\\{.*\\})", cronPatterns[i]), cronParts[i])) {
				return SchedulerErrorCodes.valueOf(i);
			}
		}
		return null;
	}

	/**
	 * Validate the string from the @Scheduled member can be parsed to a Duration
	 * unit
	 *
	 * @param everyString
	 * @return the INVALID_DURATION_PARSE_PATTERN error code if invalid and null if
	 *         valid
	 */
	public static SchedulerErrorCodes validateDurationParse(String everyString) {
		try {
			Duration.parse(everyString);
			return null;
		} catch (DateTimeParseException e) {
			return SchedulerErrorCodes.INVALID_DURATION_PARSE_PATTERN;
		}
	}
}
