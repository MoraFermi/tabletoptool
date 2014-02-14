/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model.campaign;

import java.io.Serializable;

public class TokenProperty implements Serializable {
	private String name;
	private String shortName;
	private boolean highPriority;
	private boolean ownerOnly;
	private boolean gmOnly;
	private Object defaultValue;
	private TokenPropertyType type;

	public TokenProperty() {
		// For serialization
		type=TokenPropertyType.TEXT; //there must always be a type set
	}

	public TokenProperty(String name) {
		this(TokenPropertyType.TEXT, name, null, false, false, false);
	}

	public TokenProperty(TokenPropertyType type, String name, String shortName) {
		this(type,name, shortName, false, false, false);
	}

	public TokenProperty(TokenPropertyType type, String name, boolean highPriority, boolean isOwnerOnly, boolean isGMOnly) {
		this(type,name, null, highPriority, isOwnerOnly, isGMOnly);
	}
	public TokenProperty(TokenPropertyType type, String name, String shortName, boolean highPriority, boolean isOwnerOnly, boolean isGMOnly) {
		this.name = name;
		this.shortName = shortName;
		this.highPriority = highPriority;
		this.ownerOnly = isOwnerOnly;
		this.gmOnly = isGMOnly;
		this.type=type;
	}
	public TokenProperty(String name, String shortName, boolean highPriority, boolean isOwnerOnly, boolean isGMOnly, String defaultValue) {
		this.name = name;
		this.shortName = shortName;
		this.highPriority = highPriority;
		this.ownerOnly = isOwnerOnly;
		this.gmOnly = isGMOnly;
		this.defaultValue= defaultValue;
	}

	public TokenProperty(TokenProperty p) {
		name=p.name;
		shortName=p.shortName;
		highPriority=p.highPriority;
		ownerOnly=p.ownerOnly;
		gmOnly=p.gmOnly;
		defaultValue=p.defaultValue;
		type=p.type;
	}

	public boolean isOwnerOnly() {
		return ownerOnly;
	}

	public void setOwnerOnly(boolean ownerOnly) {
		this.ownerOnly = ownerOnly;
	}

	public boolean isShowOnStatSheet() {
		return highPriority;
	}
	public void setShowOnStatSheet(boolean showOnStatSheet) {
		this.highPriority = showOnStatSheet;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public boolean isGMOnly() {
		return gmOnly;
	}

	public void setGMOnly(boolean gmOnly) {
		this.gmOnly = gmOnly;
	}

	public Object getDefaultValue()
	{
		return this.defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		if(defaultValue!=null && !type.isInstance(defaultValue))
			throw new RuntimeException("Default type does not match given type");
		this.defaultValue = defaultValue;
	}

	public TokenPropertyType getType() {
		return this.type;
	}
	
	public void setType(TokenPropertyType type) {
		this.type=type;
	}
}
