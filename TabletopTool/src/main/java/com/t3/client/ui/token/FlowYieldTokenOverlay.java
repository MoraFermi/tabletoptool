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
package com.t3.client.ui.token;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import com.t3.model.Token;

/**
 * Paint a square so that it doesn't overlay any other states being displayed in the same grid.
 * 
 * @author Jay
 */
public class FlowYieldTokenOverlay extends FlowColorDotTokenOverlay {


    /**
     * Default constructor needed for XML encoding/decoding
     */
    public FlowYieldTokenOverlay() {
      this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.RED, -1);
    }

    /**
     * Create a new dot token overlay
     * 
     * @param aName Name of the token overlay
     * @param aColor Color of the dot
     * @param aGrid Size of the overlay grid for this state. All states with the 
     * same grid size share the same overlay.
     */
    public FlowYieldTokenOverlay(String aName, Color aColor, int aGrid) {
      super(aName, aColor, aGrid);
    }

    /**
     * @see com.t3.client.ui.token.BooleanTokenOverlay#clone()
     */
    @Override
    public Object clone() {
        BooleanTokenOverlay overlay = new FlowYieldTokenOverlay(getName(), getColor(), getGrid());
        overlay.setOrder(getOrder());
        overlay.setGroup(getGroup());
        overlay.setMouseover(isMouseover());
        overlay.setOpacity(getOpacity());
        overlay.setShowGM(isShowGM());
        overlay.setShowOwner(isShowOwner());
        overlay.setShowOthers(isShowOthers());
        return overlay;
    }
    
    /**
     * @see com.t3.client.ui.token.FlowColorDotTokenOverlay#getShape(java.awt.Rectangle, com.t3.model.Token)
     */
    @Override
    protected Shape getShape(Rectangle bounds, Token token) {
        Rectangle2D r = getFlow().getStateBounds2D(bounds, token, getName());
        GeneralPath p = new GeneralPath();
        p.moveTo((float)r.getX(), (float)r.getY());
        p.lineTo((float)r.getCenterX(), (float)r.getMaxY());
        p.lineTo((float)r.getMaxX(), (float)r.getY());
        p.lineTo((float)r.getX(), (float)r.getY());
        p.closePath();
        return p;
    }
}
