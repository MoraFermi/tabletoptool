/*
 * Copyright (c) 2014 tabletoptool.com team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     rptools.com team - initial implementation
 *     tabletoptool.com team - further development
 */
package com.t3.networking;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.t3.client.AppPreferences;
import com.t3.client.walker.WalkerMetric;

public class ServerPolicy {
	private boolean strictTokenMovement;
	private boolean isMovementLocked;
	private boolean playersCanRevealVision;
	private boolean useIndividualViews;
	private boolean restrictedImpersonation;
	private boolean playersReceiveCampaignMacros;
	private boolean useToolTipsForDefaultRollFormat;
	private boolean useIndividualFOW;
	private boolean isAutoRevealOnMovement;
	private WalkerMetric movementMetric;

	public ServerPolicy() {
		// Default tool tip usage for inline rolls to user preferences.
		useToolTipsForDefaultRollFormat = AppPreferences.getUseToolTipForInlineRoll();
		// Default movement metric from preferences
		movementMetric = AppPreferences.getMovementMetric();
	}

	/**
	 * Whether token management can be done by everyone or only the GM and assigned tokens
	 * 
	 * @return
	 */
	public boolean useStrictTokenManagement() {
		return strictTokenMovement;
	}

	public void setUseStrictTokenManagement(boolean strict) {
		strictTokenMovement = strict;
	}

	public boolean isMovementLocked() {
		return isMovementLocked;
	}

	public void setIsMovementLocked(boolean locked) {
		isMovementLocked = locked;
	}

	public void setPlayersCanRevealVision(boolean flag) {
		playersCanRevealVision = flag;
	}

	public boolean getPlayersCanRevealVision() {
		return playersCanRevealVision;
	}

	public void setAutoRevealOnMovement(boolean revealFlag) {
		this.isAutoRevealOnMovement = revealFlag;
	}

	public boolean isAutoRevealOnMovement() {
		return isAutoRevealOnMovement;
	}

	public boolean isUseIndividualViews() {
		return useIndividualViews;
	}

	public void setUseIndividualViews(boolean useIndividualViews) {
		this.useIndividualViews = useIndividualViews;
	}

	public boolean isRestrictedImpersonation() {
		return restrictedImpersonation;
	}

	public void setRestrictedImpersonation(boolean restrictimp) {
		restrictedImpersonation = restrictimp;
	}

	public boolean playersReceiveCampaignMacros() {
		return playersReceiveCampaignMacros;
	}

	public void setPlayersReceiveCampaignMacros(boolean flag) {
		playersReceiveCampaignMacros = flag;
	}

	/**
	 * Sets if ToolTips should be used instead of extended output for [ ] rolls with no formatting option.
	 * 
	 * @param flag
	 *            true if tool tips should be used.
	 */
	public void setUseToolTipsForDefaultRollFormat(boolean flag) {
		useToolTipsForDefaultRollFormat = flag;
	}

	/**
	 * Gets if ToolTips should be used instead of extended output for [ ] rolls with no formatting option.
	 * 
	 * @returns true if tool tips should be used.
	 */
	public boolean getUseToolTipsForDefaultRollFormat() {
		return useToolTipsForDefaultRollFormat;
	}

	/**
	 * Gets the local server time
	 * 
	 */
	public long getSystemTime() {
		return System.currentTimeMillis();
	}

	private String getLocalTimeDate() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());
	}

	public String getTimeDate() {
		return getLocalTimeDate();
	}

	public void setMovementMetric(final WalkerMetric walkerMetric) {
		movementMetric = walkerMetric;
	}

	public WalkerMetric getMovementMetric() {
		return movementMetric;
	}

	public boolean isUseIndividualFOW() {
		return useIndividualFOW;
	}

	public void setUseIndividualFOW(boolean flag) {
		useIndividualFOW = flag;
	}
}
