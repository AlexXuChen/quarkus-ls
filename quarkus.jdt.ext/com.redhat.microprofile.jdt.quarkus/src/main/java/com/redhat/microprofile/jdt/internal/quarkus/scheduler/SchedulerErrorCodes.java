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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4mp.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * The error code and message for the @Scheduled cron string
 *
 */
@SuppressWarnings("unchecked")
public enum SchedulerErrorCodes implements IJavaErrorCode {
	INVALID_CRON_SECOND(0, "Seconds must be within the range 0-59."),
	INVALID_CRON_MINUTE(1, "Minutes must be within the range 0-59."),
	INVALID_CRON_HOUR(2, "Hour must be within the range 0-23."),
	INVALID_CRON_DAY_OF_MONTH(3, "Day of month must be within the range 1-31."),
	INVALID_CRON_MONTH(4, "Month must be within the range 1-12."), INVALID_CRON_DAY_OF_WEEK(5, "Invalid day of week."),
	INVALID_CRON_YEAR(6, "Year must be within the range 19xx-20xx."),
	INVALID_CRON_LENGTH(7, "The cron expression must contain 6-7 parts, delimited by whitespace."),
	INVALID_DURATION_PARSE_PATTERN(8, "");

	private final int errorCode;

	private final String errorMessage;

	private static Map map = new HashMap<>();

	SchedulerErrorCodes(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	static {
		for (SchedulerErrorCodes faultType : SchedulerErrorCodes.values()) {
			map.put(faultType.errorCode, faultType);
		}
	}

	public static SchedulerErrorCodes valueOf(int faultType) {
		return (SchedulerErrorCodes) map.get(faultType);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String getCode() {
		return Integer.toString(errorCode);
	}

}