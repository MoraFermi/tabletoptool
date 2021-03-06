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
package com.t3.client.tool;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.SwingUtilities;

import com.t3.client.ScreenPoint;
import com.t3.client.TabletopTool;
import com.t3.client.ui.zone.ZoneRenderer;
import com.t3.util.GraphicsUtil;

/**
 * @author trevor
 */
public class ToolHelper {
	public static void drawBoxedMeasurement(ZoneRenderer renderer, Graphics2D g, ScreenPoint startPoint, ScreenPoint endPoint) {
		if (!TabletopTool.getFrame().isPaintDrawingMeasurement())
			return;

		// Calculations
		int left = (int) Math.min(startPoint.x, endPoint.x);
		int top = (int) Math.min(startPoint.y, endPoint.y);
		int right = (int) Math.max(startPoint.x, endPoint.x);
		int bottom = (int) Math.max(startPoint.y, endPoint.y);

		// HORIZONTAL Measure
		g.setColor(Color.black);
		g.drawLine(left, top - 15, right, top - 15);
		g.drawLine(left, top - 20, left, top - 10);
		g.drawLine(right, top - 20, right, top - 10);

		String displayString = String.format("%1.1f", euclideanDistance(renderer, new ScreenPoint(left, top), new ScreenPoint(right, top)));
		GraphicsUtil.drawBoxedString(g, displayString, left + (right - left) / 2, top - 15);

		// VETICAL Measure
		g.drawLine(right + 15, top, right + 15, bottom);
		g.drawLine(right + 10, top, right + 20, top);
		g.drawLine(right + 10, bottom, right + 20, bottom);

		displayString = String.format("%1.1f", euclideanDistance(renderer, new ScreenPoint(right, top), new ScreenPoint(right, bottom)));
		GraphicsUtil.drawBoxedString(g, displayString, right + 18, bottom + (top - bottom) / 2);
	}

	public static void drawMeasurement(ZoneRenderer renderer, Graphics2D g, ScreenPoint startPoint, ScreenPoint endPoint) {
		if (!TabletopTool.getFrame().isPaintDrawingMeasurement())
			return;

		boolean dirLeft = startPoint.x > endPoint.x;
		boolean dirUp = startPoint.y < endPoint.y;

		String displayString = String.format("%1.1f", euclideanDistance(renderer, startPoint, endPoint));

		GraphicsUtil.drawBoxedString(g, displayString,
									(int) endPoint.x + (dirLeft ? -15 : 10),
									(int) endPoint.y + (dirUp ? 15 : -15),
									dirLeft ? SwingUtilities.LEFT : SwingUtilities.RIGHT);
	}

	/**
	 * Draw a measurement on the passed graphics object.
	 * 
	 * @param g
	 *            Draw the measurement here.
	 * @param distance
	 *            The size of the measurement in feet
	 * @param x
	 *            The x location of the measurement
	 * @param y
	 *            The y location of the measurement
	 */
	public static void drawMeasurement(Graphics2D g, int distance, int x, int y) {
		if (!TabletopTool.getFrame().isPaintDrawingMeasurement())
			return;
		String radius = Integer.toString(distance);
		GraphicsUtil.drawBoxedString(g, radius, x, y);
	}

	private static double euclideanDistance(ZoneRenderer renderer, ScreenPoint p1, ScreenPoint p2) {
		double a = p2.x - p1.x;
		double b = p2.y - p1.y;

		return Math.sqrt(a * a + b * b) * renderer.getZone().getUnitsPerCell() / renderer.getScaledGridSize();
	}
}
