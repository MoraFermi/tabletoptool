/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 * 
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */
package com.t3.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.GridView;
import com.t3.client.AppConstants;
import com.t3.client.AppUtil;
import com.t3.client.T3Util;
import com.t3.client.TabletopTool;
import com.t3.client.ui.macrobuttons.buttons.MacroButton;
import com.t3.language.I18N;
import com.t3.macro.api.functions.DialogFunctions;
import com.t3.macro.api.functions.InfoFunctions;
import com.t3.macro.api.functions.MapFunctions;
import com.t3.macro.api.functions.PathFunctions;
import com.t3.macro.api.functions.PlayerFunctions;
import com.t3.macro.api.functions.input.InputFunctions;
import com.t3.model.MacroButtonProperties;
import com.t3.model.Token;
import com.t3.swing.SwingUtil;
import com.t3.swing.preference.WindowPreferences;

public class MacroButtonDialog extends JDialog {

	FormPanel panel;
	MacroButton button;
	MacroButtonProperties properties;
	boolean isTokenMacro = false;
	int oldHashCode = 0;
	Boolean startingCompareGroup;
	Boolean startingCompareSortPrefix;
	Boolean startingCompareCommand;
	Boolean startingCompareIncludeLabel;
	Boolean startingCompareAutoExecute;
	Boolean startingCompareApplyToSelectedTokens;
	Boolean startingAllowPlayerEdits;
	private RSyntaxTextArea commandTextArea;

	public MacroButtonDialog() {

		super(TabletopTool.getFrame(), "", true);
		panel = new FormPanel("com/t3/client/ui/forms/macroButtonDialog.xml");
		setContentPane(panel);
		setSize(700, 400);
		SwingUtil.centerOver(this, TabletopTool.getFrame());

		installOKButton();
		installCancelButton();
		installHotKeyCombo();
		installColorCombo();
		installFontColorCombo();
		installFontSizeCombo();

		initCommandTextArea();

		getHotKeyCombo().setEnabled(!isTokenMacro);
		
		panel.getTextField("maxWidth").setEnabled(false); // can't get max-width to work, so temporarily disabling it.
		panel.getCheckBox("allowPlayerEditsCheckBox").setEnabled(TabletopTool.getPlayer().isGM());

		new WindowPreferences(AppConstants.APP_NAME, "editMacroDialog", this);
	}

	private void installHotKeyCombo() {
		String[] hotkeys = MacroButtonHotKeyManager.HOTKEYS;
		JComboBox<String> combo = getHotKeyCombo();
		for (int i = 0; i < hotkeys.length; i++)
			combo.insertItemAt(hotkeys[i], i);
	}

	private void installColorCombo() {
		JComboBox<String> combo = getColorComboBox();
		combo.setModel(new DefaultComboBoxModel<String>(T3Util.getColorNames().toArray(new String[0])));
		combo.insertItemAt("default", 0);
		combo.setSelectedItem("default");
		combo.setRenderer(new ColorComboBoxRenderer());
	}

	private void installFontColorCombo() {
		JComboBox<String> combo = getFontColorComboBox();
		combo.setModel(new DefaultComboBoxModel<String>(MacroButtonProperties.getFontColors()));
//		combo.insertItemAt("default", 0);
		combo.setSelectedItem("black");
		combo.setRenderer(new ColorComboBoxRenderer());
	}

	private void installFontSizeCombo() {
		String[] fontSizes = { "0.75em", "0.80em", "0.85em", "0.90em", "0.95em", "1.00em", "1.05em", "1.10em", "1.15em", "1.20em", "1.25em" };
		JComboBox<String> combo = getFontSizeComboBox();
		combo.setModel(new DefaultComboBoxModel<String>(fontSizes));
	}

	private void installOKButton() {
		JButton button = (JButton) panel.getButton("okButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		getRootPane().setDefaultButton(button);
	}

	private void installCancelButton() {
		JButton button = (JButton) panel.getButton("cancelButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
	}

	public void show(MacroButton button) {
		initI18NSupport();
		this.button = button;
		this.isTokenMacro = button.getToken() == null ? false : true;
		this.properties = button.getProperties();
		oldHashCode = properties.hashCodeForComparison();
		
		Boolean playerCanEdit = !TabletopTool.getPlayer().isGM() && properties.getAllowPlayerEdits();
		Boolean onGlobalPanel = properties.getSaveLocation().equals("Global");
		Boolean allowEdits = onGlobalPanel || TabletopTool.getPlayer().isGM() || playerCanEdit;
		Boolean isCommonMacro = button.getPanelClass().equals("SelectionPanel") && TabletopTool.getFrame().getSelectionPanel().getCommonMacros().contains(properties);
		if (allowEdits) {
			this.setTitle(I18N.getText("component.dialogTitle.macro.macroID") + ": " + Integer.toString(this.properties.hashCodeForComparison()));

			getColorComboBox().setSelectedItem(properties.getColorKey());
			getHotKeyCombo().setSelectedItem(properties.getHotKey());
			getLabelTextField().setText(properties.getLabel());
			getGroupTextField().setText(properties.getGroup());
			getSortbyTextField().setText(properties.getSortby());
			commandTextArea.setText(properties.getCommand());
			commandTextArea.setCaretPosition(0);
			commandTextArea.discardAllEdits(); //this removes all edits, otherwise adding all the text is an edit itself

			getFontColorComboBox().setSelectedItem(properties.getFontColorKey());
			getFontSizeComboBox().setSelectedItem(properties.getFontSize());
			getMinWidthTextField().setText(properties.getMinWidth());
			getMaxWidthTextField().setText(properties.getMaxWidth());
			getCompareGroupCheckBox().setSelected(properties.getCompareGroup());
			getCompareSortPrefixCheckBox().setSelected(properties.getCompareSortPrefix());
			getCompareCommandCheckBox().setSelected(properties.getCompareCommand());
			getAllowPlayerEditsCheckBox().setSelected(properties.getAllowPlayerEdits());
			getToolTipTextField().setText(properties.getToolTip());

			if (isCommonMacro) {
				getColorComboBox().setEnabled(false);
				getHotKeyCombo().setEnabled(false);
				getGroupTextField().setEnabled(properties.getCompareGroup());
				getSortbyTextField().setEnabled(properties.getCompareSortPrefix());
				commandTextArea.setEnabled(properties.getCompareCommand());
				getFontColorComboBox().setEnabled(false);
				getFontSizeComboBox().setEnabled(false);
				getMinWidthTextField().setEnabled(false);
				getMaxWidthTextField().setEnabled(false);
			}
			startingCompareGroup = properties.getCompareGroup();
			startingCompareSortPrefix = properties.getCompareSortPrefix();
			startingCompareCommand = properties.getCompareCommand();
			startingAllowPlayerEdits = properties.getAllowPlayerEdits();

			setVisible(true);
		} else {
			TabletopTool.showWarning(I18N.getText("msg.warning.macro.playerChangesNotAllowed"));
		}
	}

	private void initCommandTextArea() {
		commandTextArea = new RSyntaxTextArea();
		commandTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
		commandTextArea.setCodeFoldingEnabled(true);
		commandTextArea.setAntiAliasingEnabled(true);
		commandTextArea.setHighlightCurrentLine(false);
		
		//new GroovyLanguageSupport().install(commandTextArea);
		
		CompletionProvider provider = createCompletionProvider();
		AutoCompletion ac = new AutoCompletion(provider);
		ac.setAutoActivationEnabled(true);
		ac.setParameterAssistanceEnabled(true);
		ac.setShowDescWindow(true);
		ac.install(commandTextArea);
		
		
		// Need to get rid of the tooltip, but abeille can't set it back to null, so we'll do it manually
		RTextScrollPane sp=new RTextScrollPane(commandTextArea);
		sp.setFoldIndicatorEnabled(true);
		panel.getFormAccessor("detailsPanel").replaceBean("command",sp);
		//.replaceBean("command", sp);
		//panel.reset();
	}
	
	private CompletionProvider createCompletionProvider() {
		// A DefaultCompletionProvider is the simplest concrete implementation
		// of CompletionProvider. This provider has no understanding of
		// language semantics. It simply checks the text entered up to the
		// caret position for a match against known completions. This is all
		// that is needed in the majority of cases.
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.setParameterizedCompletionParams('(', ", " , ')');
		provider.setAutoActivationRules(false, ".");
		
		// Add completions for all Java keywords. A BasicCompletion is just
		// a straightforward word completion.
		//TODO create a real xml file to parse from with comments
		provider.addCompletion(new BasicCompletion(provider,"print","Printing a String","<b>Printing</b> a string."));
		createDynamicCompletions(provider, "info", InfoFunctions.class);
		createDynamicCompletions(provider, "player", PlayerFunctions.class);
		createDynamicCompletions(provider, "map", MapFunctions.class);
		createDynamicCompletions(provider, "dialog", DialogFunctions.class);
		createDynamicCompletions(provider, "path", PathFunctions.class);
		createDynamicCompletions(provider, "input", InputFunctions.class);
		
		return provider;

	}
	
	private void createDynamicCompletions(DefaultCompletionProvider prov, String lib, Class<?> c) {
		for(Method m:c.getMethods()) {
			if(m.getDeclaringClass().equals(c)) {
				FunctionCompletion fc = new FunctionCompletion(prov, lib+'.'+m.getName(),m.getReturnType().getSimpleName());
				
				LinkedList<Parameter> params=new LinkedList<Parameter>();
				Class<?>[] pts=m.getParameterTypes();
				for(int i=0;i<pts.length;i++)
					params.add(new Parameter(pts[i].getSimpleName(), "arg"+i));
				if(!params.isEmpty())
					fc.setParams(params);
				prov.addCompletion(fc);
			}
		}
	}

	private void save() {
		String hotKey = getHotKeyCombo().getSelectedItem().toString();
		button.getHotKeyManager().assignKeyStroke(hotKey);
		button.setColor(getColorComboBox().getSelectedItem().toString());
		button.setText(this.button.getButtonText());
		properties.setHotKey(hotKey);
		properties.setColorKey(getColorComboBox().getSelectedItem().toString());
		properties.setLabel(getLabelTextField().getText());
		properties.setGroup(getGroupTextField().getText());
		properties.setSortby(getSortbyTextField().getText());
		properties.setCommand(commandTextArea.getText());
		properties.setFontColorKey(getFontColorComboBox().getSelectedItem().toString());
		properties.setFontSize(getFontSizeComboBox().getSelectedItem().toString());
		properties.setMinWidth(getMinWidthTextField().getText());
		properties.setMaxWidth(getMaxWidthTextField().getText());
		properties.setCompareGroup(getCompareGroupCheckBox().isSelected());
		properties.setCompareSortPrefix(getCompareSortPrefixCheckBox().isSelected());
		properties.setCompareCommand(getCompareCommandCheckBox().isSelected());
		properties.setAllowPlayerEdits(getAllowPlayerEditsCheckBox().isSelected());
		properties.setToolTip(getToolTipTextField().getText());

		properties.save();

		if (button.getPanelClass().equals("SelectionPanel")) {
			if (TabletopTool.getFrame().getSelectionPanel().getCommonMacros().contains(button.getProperties())) {
				Boolean changeAllowPlayerEdits = false;
				Boolean endingAllowPlayerEdits = false;
				if (startingAllowPlayerEdits) {
					if (!properties.getAllowPlayerEdits()) {
						Boolean confirmDisallowPlayerEdits = TabletopTool.confirm(I18N.getText("confirm.macro.disallowPlayerEdits"));
						if (confirmDisallowPlayerEdits) {
							changeAllowPlayerEdits = true;
							endingAllowPlayerEdits = false;
						} else {
							properties.setAllowPlayerEdits(true);
						}
					}
				} else {
					if (properties.getAllowPlayerEdits()) {
						Boolean confirmAllowPlayerEdits = TabletopTool.confirm(I18N.getText("confirm.macro.allowPlayerEdits"));
						if (confirmAllowPlayerEdits) {
							changeAllowPlayerEdits = true;
							endingAllowPlayerEdits = true;
						} else {
							properties.setAllowPlayerEdits(false);
						}
					}
				}
				Boolean trusted = true;
				for (Token nextToken : TabletopTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
					if (AppUtil.playerOwns(nextToken)) {
						trusted = true;
					} else {
						trusted = false;
					}
					boolean isGM = TabletopTool.getPlayer().isGM();
					for (MacroButtonProperties nextMacro : nextToken.getMacroList(trusted)) {
						if (isGM) { //FIXME or should this be if true?
							if (nextMacro.hashCodeForComparison() == oldHashCode) {
								nextMacro.setLabel(properties.getLabel());
								if (properties.getCompareGroup() && startingCompareGroup) {
									nextMacro.setGroup(properties.getGroup());
								}
								if (properties.getCompareSortPrefix() && startingCompareSortPrefix) {
									nextMacro.setSortby(properties.getSortby());
								}
								if (properties.getCompareCommand() && startingCompareCommand) {
									nextMacro.setCommand(properties.getCommand());
								}
								if (changeAllowPlayerEdits) {
									nextMacro.setAllowPlayerEdits(endingAllowPlayerEdits);
								}
								nextMacro.setCompareGroup(properties.getCompareGroup());
								nextMacro.setCompareSortPrefix(properties.getCompareSortPrefix());
								nextMacro.setCompareCommand(properties.getCompareCommand());
								nextMacro.save();
							}
						}
					}
				}
			}
			TabletopTool.getFrame().getSelectionPanel().reset();
		}
		if (button.getPanelClass().equals("CampaignPanel")) {
			TabletopTool.serverCommand().updateCampaignMacros(TabletopTool.getCampaign().getMacroButtonPropertiesArray());
			TabletopTool.getFrame().getCampaignPanel().reset();
		}
		setVisible(false);
//		dispose();
	}

	private void cancel() {
		setVisible(false);
//		dispose();
	}

	private JComboBox<String> getHotKeyCombo() {
		return panel.getComboBox("hotKey");
	}

	private JComboBox<String> getColorComboBox() {
		return panel.getComboBox("colorComboBox");
	}

	private JTextField getLabelTextField() {
		return panel.getTextField("label");
	}

	private JTextField getGroupTextField() {
		return panel.getTextField("group");
	}

	private JTextField getSortbyTextField() {
		return panel.getTextField("sortby");
	}
/*
	private RSyntaxTextArea getCommandTextArea() {
		return (RSyntaxTextArea) panel.getTextComponent("command");
	}*/

	private JComboBox<String> getFontColorComboBox() {
		return panel.getComboBox("fontColorComboBox");
	}

	private JComboBox<String> getFontSizeComboBox() {
		return panel.getComboBox("fontSizeComboBox");
	}

	private JTextField getMinWidthTextField() {
		return panel.getTextField("minWidth");
	}

	private JTextField getMaxWidthTextField() {
		return panel.getTextField("maxWidth");
	}

	private JCheckBox getAllowPlayerEditsCheckBox() {
		return panel.getCheckBox("allowPlayerEditsCheckBox");
	}

	private JTextField getToolTipTextField() {
		return panel.getTextField("toolTip");
	}

	// Begin comparison customization

	private JCheckBox getCompareGroupCheckBox() {
		return panel.getCheckBox("commonUseGroup");
	}

	private JCheckBox getCompareSortPrefixCheckBox() {
		return panel.getCheckBox("commonUseSortPrefix");
	}

	private JCheckBox getCompareCommandCheckBox() {
		return panel.getCheckBox("commonUseCommand");
	}

	// End comparison customization

	private void initI18NSupport() {
		panel.getTabbedPane("macroTabs").setTitleAt(0, I18N.getText("component.tab.macro.details"));
		panel.getTabbedPane("macroTabs").setTitleAt(1, I18N.getText("component.tab.macro.options"));
		panel.getLabel("macroLabelLabel").setText(I18N.getText("component.label.macro.label") + ":");
		getLabelTextField().setToolTipText(I18N.getText("component.tooltip.macro.label"));
		panel.getLabel("macroGroupLabel").setText(I18N.getText("component.label.macro.group") + ":");
		getGroupTextField().setToolTipText(I18N.getText("component.tooltip.macro.group"));
		panel.getLabel("macroSortPrefixLabel").setText(I18N.getText("component.label.macro.sortPrefix") + ":");
		getSortbyTextField().setToolTipText(I18N.getText("component.tooltip.macro.sortPrefix"));
		panel.getLabel("macroHotKeyLabel").setText(I18N.getText("component.label.macro.hotKey") + ":");
		getHotKeyCombo().setToolTipText(I18N.getText("component.tooltip.macro.hotKey"));
		panel.getLabel("macroCommandLabel").setText(I18N.getText("component.label.macro.command"));
		panel.getLabel("macroButtonColorLabel").setText(I18N.getText("component.label.macro.buttonColor") + ":");
		getColorComboBox().setToolTipText(I18N.getText("component.tooltip.macro.buttonColor"));
		panel.getLabel("macroFontColorLabel").setText(I18N.getText("component.label.macro.fontColor") + ":");
		getFontColorComboBox().setToolTipText(I18N.getText("component.tooltip.macro.fontColor"));
		panel.getLabel("macroFontSizeLabel").setText(I18N.getText("component.label.macro.fontSize") + ":");
		getFontSizeComboBox().setToolTipText(I18N.getText("component.tooltip.macro.fontSize"));
		panel.getLabel("macroMinWidthLabel").setText(I18N.getText("component.label.macro.minWidth") + ":");
		getMinWidthTextField().setToolTipText(I18N.getText("component.tooltip.macro.minWidth"));
		panel.getLabel("macroMaxWidthLabel").setText(I18N.getText("component.label.macro.maxWidth") + ":");
		getMaxWidthTextField().setToolTipText(I18N.getText("component.tooltip.macro.maxWidth"));
		panel.getLabel("macroToolTipLabel").setText(I18N.getText("component.label.macro.toolTip") + ":");
		getToolTipTextField().setToolTipText(I18N.getText("component.tooltip.macro.tooltip"));
		getAllowPlayerEditsCheckBox().setText(I18N.getText("component.label.macro.allowPlayerEdits"));
		getAllowPlayerEditsCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.allowPlayerEdits"));
		((TitledBorder) ((GridView) panel.getComponentByName("macroComparisonGridView")).getBorder()).setTitle(I18N.getText("component.label.macro.macroCommonality"));
		getCompareGroupCheckBox().setText(I18N.getText("component.label.macro.compareUseGroup"));
		getCompareGroupCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseGroup"));
		getCompareSortPrefixCheckBox().setText(I18N.getText("component.label.macro.compareUseSortPrefix"));
		getCompareSortPrefixCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseSortPrefix"));
		getCompareCommandCheckBox().setText(I18N.getText("component.label.macro.compareUseCommand"));
		getCompareCommandCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseCommand"));
	}
}
