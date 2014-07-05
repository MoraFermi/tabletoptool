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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jidesoft.swing.FolderChooser;
import com.t3.FileUtil;
import com.t3.client.AppPreferences;
import com.t3.client.AppSetup;
import com.t3.client.RemoteFileDownloader;
import com.t3.client.TabletopTool;
import com.t3.client.WebDownloader;
import com.t3.client.swing.AbeillePanel;
import com.t3.client.swing.GenericDialog;
import com.t3.language.I18N;

public class AddResourceDialog extends AbeillePanel<AddResourceDialog.Model> {

	private static final Logger log = Logger.getLogger(AddResourceDialog.class);

	private static final String LIBRARY_URL = "http://library.tabletoptool.com/legacy";
	private static final String LIBRARY_LIST_URL = LIBRARY_URL + "/listArtPacks";

	public enum Tab {
		LOCAL, WEB, TABLETOPTOOL_SITE
	}

	private GenericDialog dialog;
	private Model model;
	private boolean downloadLibraryListInitiated;

	private boolean install = false;

	public AddResourceDialog() {
		super("com/t3/client/ui/forms/addResourcesDialog.xml");

		setPreferredSize(new Dimension(550, 300));

		panelInit();
	}

	public boolean getInstall() {
		return install;
	}

	public void showDialog() {
		dialog = new GenericDialog("Add Resource to Library", TabletopTool.getFrame(), this);

		model = new Model();

		bind(model);

		getRootPane().setDefaultButton(getInstallButton());
		dialog.showDialog();
	}

	@Override
	public Model getModel() {
		return model;
	}

	public JButton getInstallButton() {
		return (JButton) getComponent("installButton");
	}

	public JTextField getBrowseTextField() {
		return (JTextField) getComponent("@localDirectory");
	}

	public JList<LibraryRow> getLibraryList() {
		return (JList<LibraryRow>) getComponent("@tabletoptoolList");
	}

	public void initLibraryList() {
		JList list = getLibraryList();
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		list.setModel(new MessageListModel(I18N.getText("dialog.addresource.downloading")));
	}

	public void initTabPane() {

		final JTabbedPane tabPane = (JTabbedPane) getComponent("tabPane");

		tabPane.getModel().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Hmmm, this is fragile (breaks if the order changes) rethink this later
				switch (tabPane.getSelectedIndex()) {
				case 0:
					model.tab = Tab.LOCAL;
					break;
				case 1:
					model.tab = Tab.WEB;
					break;
				case 2:
					model.tab = Tab.TABLETOPTOOL_SITE;
					downloadLibraryList();
					break;
				}
			}
		});
	}

	public void initLocalDirectoryButton() {
		final JButton button = (JButton) getComponent("localDirectoryButton");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				FolderChooser folderChooser = new FolderChooser();
				folderChooser.setCurrentDirectory(TabletopTool.getFrame().getLoadFileChooser().getCurrentDirectory());
				folderChooser.setRecentListVisible(false);
				folderChooser.setFileHidingEnabled(true);
				folderChooser.setDialogTitle(I18N.getText("msg.title.loadAssetTree"));

				int result = folderChooser.showOpenDialog(button.getTopLevelAncestor());
				if (result == FolderChooser.APPROVE_OPTION) {
					File root = folderChooser.getSelectedFolder();
					getBrowseTextField().setText(root.getAbsolutePath());
				}
			}
		});
	}

	public void initInstallButton() {
		JButton button = (JButton) getComponent("installButton");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				install = true;
				if (commit()) {
					close();
				}
			}
		});
	}

	public void initCancelButton() {
		JButton button = (JButton) getComponent("cancelButton");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
	}

	private void downloadLibraryList() {
		if (downloadLibraryListInitiated) {
			return;
		}

		// This pattern is safe because it is only called on the EDT
		downloadLibraryListInitiated = true;

		new SwingWorker<Object, Object>() {
			ListModel model;

			@Override
			protected Object doInBackground() throws Exception {
				String result = null;
				try {
					WebDownloader downloader = new WebDownloader(new URL(LIBRARY_LIST_URL));
					result = downloader.read();
				} finally {
					if (result == null) {
						model = new MessageListModel(I18N.getText("dialog.addresource.errorDownloading"));
						return null;
					}
				}
				DefaultListModel<LibraryRow> listModel = new DefaultListModel<LibraryRow>();

				// Create a list to compare against for dups
				List<String> libraryNameList = new ArrayList<String>();
				for (File file : AppPreferences.getAssetRoots()) {
					libraryNameList.add(file.getName());
				}
				// Generate the list
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(result.getBytes())));
					String line = null;
					while ((line = reader.readLine()) != null) {
						LibraryRow row = new LibraryRow(line);

						// Don't include if we've already got it
						if (libraryNameList.contains(row.name)) {
							continue;
						}
						listModel.addElement(row);
					}
					model = listModel;
				} catch (Throwable t) {
					log.error("unable to parse library list", t);
					model = new MessageListModel(I18N.getText("dialog.addresource.errorDownloading"));
				}
				return null;
			}

			@Override
			protected void done() {
				getLibraryList().setModel(model);
			}
		}.execute();
	}

	@Override
	public boolean commit() {
		if (!super.commit()) {
			return false;
		}

		// Add the resource
		final List<LibraryRow> rowList = new ArrayList<LibraryRow>();

		switch (model.getTab()) {
		case LOCAL:
			if (StringUtils.isEmpty(model.getLocalDirectory())) {
				TabletopTool.showMessage("dialog.addresource.warn.filenotfound", "Error", JOptionPane.ERROR_MESSAGE, model.getLocalDirectory());
				return false;
			}
			File root = new File(model.getLocalDirectory());
			if (!root.exists()) {
				TabletopTool.showMessage("dialog.addresource.warn.filenotfound", "Error", JOptionPane.ERROR_MESSAGE, model.getLocalDirectory());
				return false;
			}
			if (!root.isDirectory()) {
				TabletopTool.showMessage("dialog.addresource.warn.directoryrequired", "Error", JOptionPane.ERROR_MESSAGE, model.getLocalDirectory());
				return false;
			}
			try {
				AppSetup.installLibrary(FileUtil.getNameWithoutExtension(root), root);
			} catch (MalformedURLException e) {
				log.error("Bad path url: " + root.getPath(), e);
				TabletopTool.showMessage("dialog.addresource.warn.badpath", "Error", JOptionPane.ERROR_MESSAGE, model.getLocalDirectory());
				return false;
			} catch (IOException e) {
				log.error("IOException adding local root: " + root.getPath(), e);
				TabletopTool.showMessage("dialog.addresource.warn.badpath", "Error", JOptionPane.ERROR_MESSAGE, model.getLocalDirectory());
				return false;
			}
			return true;

		case WEB:
			if (StringUtils.isEmpty(model.getUrlName())) {
				TabletopTool.showMessage("dialog.addresource.warn.musthavename", "Error", JOptionPane.ERROR_MESSAGE, model.getLocalDirectory());
				return false;
			}
			// validate the url format so that we don't hit it later
			try {
				new URL(model.getUrl());
			} catch (MalformedURLException e) {
				TabletopTool.showMessage("dialog.addresource.warn.invalidurl", "Error", JOptionPane.ERROR_MESSAGE, model.getUrl());
				return false;
			}
			rowList.add(new LibraryRow(model.getUrlName(), model.getUrl(), -1));
			break;

		case TABLETOPTOOL_SITE:
			List<LibraryRow> selectedRows = getLibraryList().getSelectedValuesList();
			if (selectedRows == null || selectedRows.isEmpty()) {
				TabletopTool.showMessage("dialog.addresource.warn.mustselectone", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			for (LibraryRow row : selectedRows) {

				//validate the url format
				row.path = LIBRARY_URL + "/" + row.path;
				try {
					new URL(row.path);
				} catch (MalformedURLException e) {
					TabletopTool.showMessage("dialog.addresource.warn.invalidurl", "Error", JOptionPane.ERROR_MESSAGE, row.path);
					return false;
				}
				rowList.add(row);
			}
			break;
		}

		new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				for (LibraryRow row : rowList) {
					try {
						RemoteFileDownloader downloader = new RemoteFileDownloader(new URL(row.path));
						File tmpFile = downloader.read();
						AppSetup.installLibrary(row.name, tmpFile.toURL());
						tmpFile.delete();
					} catch (IOException e) {
						log.error("Error downloading library: " + e, e);
						TabletopTool.showInformation("dialog.addresource.warn.couldnotload");
					}
				}
				return null;
			}
		}.execute();
		return true;
	}

	private void close() {
		unbind();
		dialog.closeDialog();
	}

	private static class LibraryRow {
		private final String name;
		private String path;
		private final int size;

		public LibraryRow(String name, String path, int size) {
			this.name = name.trim();
			this.path = path.trim();
			this.size = size;
		}

		public LibraryRow(String row) {
			String[] data = row.split("\\|");

			name = data[0].trim();
			path = data[1].trim();
			size = Integer.parseInt(data[2]);
		}

		@Override
		public String toString() {
			return "<html><b>" + name + "</b> <i>(" + getSizeString() + ")</i>";
		}

		private String getSizeString() {
			NumberFormat format = NumberFormat.getNumberInstance();
			if (size < 1000) {
				return format.format(size) + " bytes";
			}
			if (size < 1000000) {
				return format.format(size / 1000) + " k";
			}
			return format.format(size / 1000000) + " mb";
		}
	}

	public static class Model {
		private String localDirectory;
		private String urlName;
		private String url;
		private Tab tab = Tab.LOCAL;

		public String getLocalDirectory() {
			return localDirectory;
		}

		public void setLocalDirectory(String localDirectory) {
			this.localDirectory = localDirectory;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Tab getTab() {
			return tab;
		}

		public void setTab(Tab tab) {
			this.tab = tab;
		}

		public String getUrlName() {
			return urlName;
		}

		public void setUrlName(String urlName) {
			this.urlName = urlName;
		}
	}

	private class MessageListModel extends AbstractListModel<String> {
		private final String message;

		public MessageListModel(String message) {
			this.message = message;
		}

		@Override
		public String getElementAt(int index) {
			return message;
		}

		@Override
		public int getSize() {
			return 1;
		}
	}
}
