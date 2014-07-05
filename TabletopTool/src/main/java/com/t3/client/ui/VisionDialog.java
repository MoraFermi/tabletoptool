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
package com.t3.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

import com.jeta.forms.components.panel.FormPanel;
import com.t3.client.TabletopTool;
import com.t3.language.I18N;
import com.t3.model.Token;
import com.t3.model.Vision;
import com.t3.model.Zone;
import com.t3.model.vision.BlockyRoundVision;
import com.t3.model.vision.FacingConicVision;
import com.t3.model.vision.RoundVision;
import com.t3.swing.SwingUtil;

public class VisionDialog extends JDialog {
	private JTextField nameTextField;
	private JTextField distanceTextField;
	private JCheckBox enabledCheckBox;
	private JComboBox<Vision> typeCombo;

	public VisionDialog(Zone zone, Token token) {
		this(zone, token, null);
	}

	public VisionDialog(Zone zone, Token token, Vision vision) {
		super(TabletopTool.getFrame(), I18N.getText("VisionDialog.msg.title"), true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		FormPanel panel = new FormPanel("com/t3/client/ui/forms/visionDialog.xml");

		initNameTextField(panel, vision);
		initEnabledCheckBox(panel, vision);
		initDistanceTextField(panel, vision);
		initTypeCombo(panel, token, vision);

		initDeleteButton(panel, token, vision);
		initOKButton(panel, zone, token);
		initCancelButton(panel);

		setContentPane(panel);
		pack();
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			SwingUtil.centerOver(this, TabletopTool.getFrame());
		}
		super.setVisible(b);
	}

	private void initNameTextField(FormPanel panel, Vision vision) {
		nameTextField = panel.getTextField("name");
		nameTextField.setText(vision != null ? vision.getName() : "");
	}

	private void initEnabledCheckBox(FormPanel panel, Vision vision) {
		enabledCheckBox = panel.getCheckBox("enabled");
		enabledCheckBox.setSelected(vision == null || vision.isEnabled());
	}

	private void initDistanceTextField(FormPanel panel, Vision vision) {
		distanceTextField = panel.getTextField("distance");
		distanceTextField.setText(vision != null ? Integer.toString(vision.getDistance()) : "");
	}

	private void initTypeCombo(FormPanel panel, Token token, Vision vision) {
		typeCombo = panel.getComboBox("typeCombo");
		Vision[] list = null;
		if (vision != null) {
			list = new Vision[] { vision };
		} else {
			list = new Vision[] { new RoundVision(), new FacingConicVision(), new BlockyRoundVision() };
		}
		typeCombo.setModel(new DefaultComboBoxModel<Vision>(list));
		typeCombo.setEnabled(vision == null);
		typeCombo.setSelectedIndex(0);
	}

	private void initOKButton(FormPanel panel, final Zone zone, final Token token) {
		JButton button = (JButton) panel.getButton("okButton");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (commit(zone, token)) {
					close();
				}
			}
		});
		getRootPane().setDefaultButton(button);
	}

	private void initDeleteButton(FormPanel panel, final Token token, final Vision vision) {
		JButton button = (JButton) panel.getButton("deleteButton");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				token.removeVision(vision);
				close();
			}
		});
		button.setEnabled(vision != null);
	}

	private void initCancelButton(FormPanel panel) {
		JButton button = (JButton) panel.getButton("cancelButton");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
	}

	private boolean commit(Zone zone, Token token) {
		Vision vision = (Vision) typeCombo.getSelectedItem();

		if (distanceTextField.getText().trim().length() == 0) {
			TabletopTool.showError("VisionDialog.error.EmptyDistance");
			return false;
		}
		int distance = 0;
		try {
			distance = Integer.parseInt(distanceTextField.getText());
		} catch (NumberFormatException nfex) {
			TabletopTool.showError("VisionDialog.error.numericDistanceOnly");
			return false;
		}
		vision.setName(nameTextField.getText());
		vision.setEnabled(enabledCheckBox.isSelected());
		vision.setDistance(distance);

//		token.addVision(vision);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		return true;
	}

	private void close() {
		setVisible(false);
	}
}
