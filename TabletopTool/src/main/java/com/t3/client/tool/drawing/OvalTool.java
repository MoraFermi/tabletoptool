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
package com.t3.client.tool.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.t3.client.ScreenPoint;
import com.t3.client.tool.ToolHelper;
import com.t3.client.ui.zone.ZoneRenderer;
import com.t3.model.ZonePoint;
import com.t3.model.drawing.DrawableColorPaint;
import com.t3.model.drawing.Pen;
import com.t3.model.drawing.ShapeDrawable;


/**
 * @author drice
 *
 */
public class OvalTool extends AbstractDrawingTool implements MouseMotionListener {
	private static final long serialVersionUID = 3258413928311830323L;

	protected Rectangle oval;
	private ZonePoint originPoint;

	public OvalTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("com/t3/client/image/tool/draw-blue-circle.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public String getTooltip() {
		return "tool.oval.tooltip";
	}

	@Override
	public String getInstructions() {
		return "tool.oval.instructions";
	}

	@Override
	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
		if (oval != null) {
			Pen pen = getPen();

			if (pen.isEraser()) {
				pen = new Pen(pen);
				pen.setEraser(false);
				pen.setPaint(new DrawableColorPaint(Color.white));
				pen.setBackgroundPaint(new DrawableColorPaint(Color.white));
			}

			paintTransformed(g, renderer, new ShapeDrawable(new Ellipse2D.Float(oval.x, oval.y, oval.width, oval.height)), pen);

			ToolHelper.drawBoxedMeasurement(renderer, g, ScreenPoint.fromZonePoint(renderer, oval.x, oval.y), ScreenPoint.fromZonePoint(renderer, oval.x + oval.width, oval.y+oval.height));
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {

		if (SwingUtilities.isLeftMouseButton(e)) {
			ZonePoint zp = getPoint(e);

			if (oval == null) {
				originPoint = zp;
				oval = createRect(zp, zp);
			} else {
				oval = createRect(originPoint, zp);

				// Draw from center if ALT is held down
				if ( e.isAltDown() ) {
					if (zp.x > originPoint.x)
						oval.x -= oval.width;

					if (zp.y > originPoint.y)
						oval.y -= oval.height;

					oval.width *= 2;
					oval.height *= 2;
				}

				completeDrawable(renderer.getZone().getId(), getPen(), new ShapeDrawable(new Ellipse2D.Float(oval.x, oval.y, oval.width, oval.height), true));
				oval = null;
			}

			setIsEraser(isEraser(e));
		}

		super.mousePressed(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (oval == null) {
			super.mouseDragged(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	 @Override
	 public void mouseMoved(MouseEvent e) {
		if (oval != null) {

			ZonePoint sp = getPoint(e);

			oval = createRect(originPoint, sp);

			// Draw from center if ALT is held down
			if ( e.isAltDown()) {
				if (sp.x > originPoint.x)
					oval.x -= oval.width;

				if (sp.y > originPoint.y)
					oval.y -= oval.height;

				oval.width *= 2;
				oval.height *= 2;
			}

			renderer.repaint();
		}
	 }

	 /**
	  * @see com.t3.client.ui.Tool#resetTool()
	  */
	 @Override
	 protected void resetTool() {

		 if (oval != null) {
			 oval = null;
			 renderer.repaint();
		 } else {
			 super.resetTool();
		 }
	 }
}
