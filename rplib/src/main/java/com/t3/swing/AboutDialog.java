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
package com.t3.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class AboutDialog extends JDialog {

	private JPanel jContentPane = null;
	private JPanel bottomPanel = null;
	private JButton okButton = null;
	private JPanel centerPanel = null;
	private JPanel creditPanel = null;
	private JLabel logoLabel = null;
	private JEditorPane creditEditorPane = null;
	/**
	 * This is the default constructor
	 */
	public AboutDialog(JFrame parent, Image logo, String credits) {
		super(parent, true);
		initialize();
		
		if (logo != null) {
			logoLabel.setIcon(new ImageIcon(logo));
		}
		creditEditorPane.setText(credits);
	}
	
	@Override
	public void setVisible(boolean b) {
		
		if (b) {
			SwingUtil.centerOver(this, getOwner());
		}
		super.setVisible(b);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(354, 354);
		this.setTitle("About");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getBottomPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getCenterPanel(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes bottomPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
			bottomPanel = new JPanel();
			bottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
			bottomPanel.setLayout(flowLayout);
			bottomPanel.add(getOkButton(), null);
		}
		return bottomPanel;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes centerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			logoLabel = new JLabel();
			logoLabel.setText("");
			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			centerPanel.setBackground(java.awt.Color.white);
			centerPanel.add(new JScrollPane(getCreditPanel()), java.awt.BorderLayout.CENTER);
			centerPanel.add(logoLabel, java.awt.BorderLayout.NORTH);
		}
		return centerPanel;
	}

	/**
	 * This method initializes creditPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCreditPanel() {
		if (creditPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			creditPanel = new JPanel();
			creditPanel.setLayout(gridLayout);
			creditPanel.setBackground(java.awt.Color.white);
			creditPanel.add(getCreditEditorPane(), null);
		}
		return creditPanel;
	}

	/**
	 * This method initializes creditEditorPane	
	 * 	
	 * @return javax.swing.JEditorPane	
	 */
	private JEditorPane getCreditEditorPane() {
		if (creditEditorPane == null) {
			creditEditorPane = new JEditorPane();
			creditEditorPane.setEditable(false);
			
			HTMLDocument document = new HTMLDocument();
			
			creditEditorPane.setEditorKit(new HTMLEditorKit());
			creditEditorPane.setDocument(document);
		}
		return creditEditorPane;
	}

}  //  @jve:decl-index=0:visual-constraint="200,16"
