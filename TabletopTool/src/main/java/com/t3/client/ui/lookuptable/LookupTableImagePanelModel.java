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
package com.t3.client.ui.lookuptable;

import java.awt.Image;
import java.awt.Paint;
import java.awt.datatransfer.Transferable;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.t3.client.AppStyle;
import com.t3.client.TabletopTool;
import com.t3.model.LookupTable;
import com.t3.swing.ImagePanelModel;
import com.t3.util.ImageManager;

public class LookupTableImagePanelModel implements ImagePanelModel {

	private static final Logger log = Logger.getLogger(LookupTableImagePanelModel.class);

	private final ImageObserver[] imageObservers;

	public LookupTableImagePanelModel(ImageObserver... observers) {
		imageObservers = observers;
	}

	@Override
	public int getImageCount() {
		return getFilteredLookupTable().size();
	}

	@Override
	public Transferable getTransferable(int arg0) {
		return null;
	}

	@Override
	public Object getID(int index) {
		if (index < 0) {
			return null;
		}

		return getLookupTableIDList().get(index);
	}

	@Override
	public Image getImage(Object id) {

		LookupTable table = getFilteredLookupTable().get(id);
		if (table == null) {
			log.debug("LookupTableImagePanelModel.getImage(" + id + "):  not resolved");
			return ImageManager.BROKEN_IMAGE;
		}

		Image image = AppStyle.lookupTableDefaultImage;
		if (table.getTableImage() != null) {
			image = ImageManager.getImage(table.getTableImage(), imageObservers);
		}

		return image;
	}

	@Override
	public Image getImage(int index) {
		return getImage(getID(index));
	}

	@Override
	public String getCaption(int index) {
		if (index < 0) {
			return "";
		}

		LookupTable table = getFilteredLookupTable().get(getID(index));

		return table.getName();
	}

	@Override
	public Paint getBackground(int arg0) {
		return null;
	}

	@Override
	public Image[] getDecorations(int arg0) {
		return null;
	}

	private List<String> getLookupTableIDList() {

		List<String> idList = new ArrayList<String>(getFilteredLookupTable().keySet());
		Collections.sort(idList);
		return idList;
	}

	/**Retrieves a Map containing tables and their names from campaign
	 * properties.
	 * @return Map&ltString, LookupTable&gt -- If the client belongs to a GM, all tables will
	 * be returned.  If the client belongs to a player, only non-
	 * hidden tables will be returned.
	 */
	private Map<String, LookupTable> getFilteredLookupTable() {
		if (TabletopTool.getPlayer() == null) {
			return new HashMap<String, LookupTable>();
		}

		Map<String, LookupTable> lookupTables = new HashMap<String, LookupTable>(TabletopTool.getCampaign().getLookupTableMap());
		if(!TabletopTool.getPlayer().isGM()) {
			for(String nextKey : TabletopTool.getCampaign().getLookupTableMap().keySet()) {
				if(!lookupTables.get(nextKey).getVisible()) {
					lookupTables.remove(nextKey);
				}
			}
		}
		return lookupTables;
	}
}
