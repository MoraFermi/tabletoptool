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
package com.t3.client.ui.zone;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.t3.client.TabletopTool;
import com.t3.model.Zone;

public class ZonePopupMenu extends JPopupMenu {

	private Zone zone;
	
	public ZonePopupMenu(Zone zone) {
		super("Zone");
		
		this.zone = zone;

		Action action = null;
		if (zone.isVisible()) {
			action = new AbstractAction() {
				{
					putValue(NAME, "Hide from players");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					ZonePopupMenu.this.zone.setVisible(false);
					TabletopTool.serverCommand().setZoneVisibility(ZonePopupMenu.this.zone.getId(), false);
					TabletopTool.getFrame().getZoneMiniMapPanel().flush();
					TabletopTool.getFrame().refresh();
				}
			};
		} else {
			action = new AbstractAction() {
				{
					putValue(NAME, "Show to players");
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					
					ZonePopupMenu.this.zone.setVisible(true);
					TabletopTool.serverCommand().setZoneVisibility(ZonePopupMenu.this.zone.getId(), true);
					TabletopTool.getFrame().getZoneMiniMapPanel().flush();
					TabletopTool.getFrame().refresh();
				}
			};
		}
		add(new JMenuItem(action));
	}

	
	
}
