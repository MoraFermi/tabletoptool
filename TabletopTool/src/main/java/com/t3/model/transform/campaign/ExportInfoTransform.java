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
package com.t3.model.transform.campaign;

import java.util.regex.Pattern;

import com.t3.ModelVersionTransformation;
/**
 * This should be applied to any campaign file version 1.3.74 and earlier
 * due to the deletion of the ExportInfo class afterwards.
 */
public class ExportInfoTransform implements ModelVersionTransformation {
	private static final String blockStart = "<exportInfo>";
	private static final String blockEnd   = "</exportInfo>";
	private static final String regex = blockStart + ".*" + blockEnd;
	private static final String replacement = "";

	private static final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

	/**
	 * Delete the block containing the now-obsolete exportInfo class data, since
	 * there is no place to put it (and therefore generates an XStream error)
	 */
	@Override
	public String transform(String xml) {
		// Same as: return xml.replaceAll(regex, replacement);
		// except that we can specify the flag DOTALL
		return pattern.matcher(xml).replaceAll(replacement);
	}
}
