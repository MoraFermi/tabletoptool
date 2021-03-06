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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.t3.model.Token;

/**
 * Paints a single reduced alpha color over the token.
 * 
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-02 21:05:50 +0200 (Sun, 02 Jun 2013) $ $Author: azhrei_fje $
 */
public class ShadedTokenOverlay extends BooleanTokenOverlay {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /**
   * The color that is painted over the token.
   */
  private Color color;

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /**
   * Default constructor needed for XML encoding/decoding
   */
  public ShadedTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.RED);
  }

  /**
   * Create the new token overlay
   * 
   * @param aName Name of the new overlay.
   * @param aColor The color that is painted over the token. If the
   * alpha is 100%, it will be reduced to 25%.
   */
  public ShadedTokenOverlay(String aName, Color aColor) {
    super(aName);
    assert aColor != null : "A color is required but null was passed.";
    color = aColor;
    setOpacity(25);
  }

  /*---------------------------------------------------------------------------------------------
   * TokenOverlay Abstract Method Implementations
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see com.t3.client.ui.token.BooleanTokenOverlay#paintOverlay(java.awt.Graphics2D, com.t3.model.Token, Rectangle)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token aToken, Rectangle bounds) {
    Color temp = g.getColor();
    g.setColor(color);
    Composite tempComposite = g.getComposite();
    if (getOpacity() != 100)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)getOpacity()/100));
    g.fill(bounds);
    g.setColor(temp);
    g.setComposite(tempComposite);
  }

  /**
   * @see com.t3.client.ui.token.BooleanTokenOverlay#clone()
   */
  @Override
  public Object clone() {
      BooleanTokenOverlay overlay = new ShadedTokenOverlay(getName(), getColor());
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
   * Get the color for this ShadedTokenOverlay.
   *
   * @return Returns the current value of color.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Set the value of color for this ShadedTokenOverlay.
   *
   * @param aColor The color to set.
   */
  public void setColor(Color aColor) {
    color = aColor;
  }
}
