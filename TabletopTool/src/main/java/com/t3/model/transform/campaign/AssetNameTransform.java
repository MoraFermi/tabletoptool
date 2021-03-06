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

import com.t3.ModelVersionTransformation;

/**
 * This transform is for asset filenames, not the actual XML data.  So the XML passed
 * to the {@link #transform(String)} method should be the asset's base name, typically
 * <code>ASSET_DIR + key</code>.  This means that this transform should <b>NOT</b>
 * be registered with any ModelVersionManager or it will be executed in the wrong
 * context.
 *
 * pre-1.3.51:  asset names had ".dat" tacked onto the end and held only binary data
 * 1.3.51-63:  assets were stored in XML under their asset name, no extension
 * 1.3.64+:  asset objects are in XML (name, MD5key), but the image is in another
 * file with the asset's image type as an extension (.jpeg, .png)
 *
 * @author frank
 */
public class AssetNameTransform implements ModelVersionTransformation {
	private final String regexOld;
	private final String regexNew;

	public AssetNameTransform(String from, String to) {
		regexOld = from;
		regexNew = to;
	}
	@Override
	public String transform(String name) {
		return name.replace(regexOld, regexNew);
	}
}
