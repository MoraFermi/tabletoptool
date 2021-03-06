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
package com.t3.client.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

import com.t3.client.TabletopTool;

/**
 * @author trevor
 */
public class ColorPickerButton extends JComponent {

	private Color color;
	private String title;
	
	public ColorPickerButton(String title, Color defaultColor) {
		color = defaultColor;
		this.title = title;
		
		addMouseListener(new MouseAdapter(){
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
	            Color oldColor = color;
	            Color newColor = JColorChooser.showDialog(TabletopTool.getFrame(), ColorPickerButton.this.title, oldColor);
	            
	            if (newColor != null) {
	                ColorPickerButton.this.color = newColor;
	                repaint();
	            }
			}
		});
		
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	}
	

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {

		Dimension mySize = getSize();
		
		g.setColor(color);
		g.fillRect(0, 0, mySize.width, mySize.height);
	}
	
	public Color getSelectedColor() {
		return color;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(16, 16);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}
}
