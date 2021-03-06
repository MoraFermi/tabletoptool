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
package com.t3.model.drawing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

import com.t3.client.TabletopTool;
import com.t3.model.Zone;
import com.t3.model.ZonePoint;

/**
 * The radius template draws a highlight over all the squares effected from a specific spine.
 * 
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-02 21:05:50 +0200 (Sun, 02 Jun 2013) $ $Author: azhrei_fje $
 */
public class RadiusTemplate extends AbstractTemplate {
	/**
	 * Paint the border at a specific radius.
	 * 
	 * @param g
	 *            Where to paint
	 * @param x
	 *            Distance from vertex along X axis in cell coordinates.
	 * @param y
	 *            Distance from vertex along Y axis in cell coordinates.
	 * @param xOff
	 *            Distance from vertex along X axis in screen coordinates.
	 * @param yOff
	 *            Distance from vertex along Y axis in screen coordinates.
	 * @param gridSize
	 *            The size of one side of the grid in screen coordinates.
	 * @param distance
	 *            The distance in cells from the vertex to the cell which is offset from the vertex by <code>x</code> &
	 *            <code>y</code>.
	 * @param radius
	 *            The radius where the border is painted.
	 * @see com.t3.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int, int, int, int, int,
	 *      int)
	 */
	protected void paintBorderAtRadius(Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance, int radius) {
		// At the border?
		if (distance == radius) {
			// Paint lines between vertical boundaries if needed
			if (getDistance(x + 1, y) > radius) {
				paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
				paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
				paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
				paintFarVerticalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
			} // endif

			// Paint lines between horizontal boundaries if needed
			if (getDistance(x, y + 1) > radius) {
				paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
				paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
				paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
				paintFarHorizontalBorder(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
			} // endif
		} // endif
	}

	/*---------------------------------------------------------------------------------------------
	 * Overridden AbstractTemplate Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see com.t3.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int, int, int, int, int,
	 *      int)
	 */
	@Override
	protected void paintBorder(Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
		paintBorderAtRadius(g, x, y, xOff, yOff, gridSize, distance, getRadius());

		// At the center?
		// FIXME This is wrong because it draws the filled rectangle at CellPoint(0,0) and it should be at the
		// origin of the radius template.  Perhaps the transform is missing a call to translate()?
//		if (x == 0 && y == 0)
//			g.fillRect(getVertex().x + xOff - 4, getVertex().y + yOff - 4, 7, 7);
	}

	/**
	 * @see com.t3.model.drawing.AbstractTemplate#paintArea(java.awt.Graphics2D, int, int, int, int, int,
	 *      int)
	 */
	@Override
	protected void paintArea(Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
		// Only squares w/in the radius
		if (distance <= getRadius()) {
			// Paint the squares
			paintArea(g, xOff, yOff, gridSize, Quadrant.NORTH_EAST);
			paintArea(g, xOff, yOff, gridSize, Quadrant.SOUTH_EAST);
			paintArea(g, xOff, yOff, gridSize, Quadrant.NORTH_WEST);
			paintArea(g, xOff, yOff, gridSize, Quadrant.SOUTH_WEST);
		} // endif
	}

	/*---------------------------------------------------------------------------------------------
	 * Drawable Interface Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see com.t3.model.drawing.Drawable#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		if (getZoneId() == null) {
			// This avoids a NPE when loading up a campaign
			return new Rectangle();
		}
		Zone zone = TabletopTool.getCampaign().getZone(getZoneId());
		if (zone == null) {
			return new Rectangle();
		}
		int gridSize = zone.getGrid().getSize();
		int quadrantSize = getRadius() * gridSize + BOUNDS_PADDING;
		ZonePoint vertex = getVertex();
		return new Rectangle(vertex.x - quadrantSize, vertex.y - quadrantSize, quadrantSize * 2, quadrantSize * 2);
	}

	@Override
	public Area getArea() {
		// I don't feel like figuring out the exact shape of this right now
		return null;
	}
}
