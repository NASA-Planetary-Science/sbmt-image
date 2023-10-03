package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;

public class LayerPreviewPanel<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends ModelInfoWindow implements PropertyChangeListener
{
	private vtkJoglPanelComponent renWin;
	private JComboBox<String> layerComboBox;
	private JSplitPane splitPane;
	private JPanel layerPanel;
	private JPanel controlsPanel;
	private JCheckBox syncCheckBox;
	private JButton applyToBodyButton;
	private JPanel syncApplyPanel;
	private JButton applyAllButton;

	public LayerPreviewPanel(String title) throws IOException, Exception
	{
		this.layerPanel = new JPanel();
		this.layerPanel.setLayout(new GridBagLayout());
		this.controlsPanel = new JPanel();
		this.controlsPanel.setLayout(new GridBagLayout());
		controlsPanel.setBorder(BorderFactory.createTitledBorder("Image Appearance"));
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, layerPanel, controlsPanel);
		syncCheckBox = new JCheckBox("Sync with Body");
		applyToBodyButton = new JButton("Apply to Body");
		applyAllButton = new JButton("Apply to Body");
		initComponents();

		createMenus();
		setTitle(title);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(splitPane, gridBagConstraints);
		pack();
		setVisible(true);
		setSize(700, 700);
		this.splitPane.setDividerLocation(0.9);

		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				renWin.resetCamera();
				renWin.Render();
			}
		});
	}



	@Override
	public Model getModel()
	{
		return null;
	}

	@Override
	public Model getCollectionModel()
	{
		return null; //imageCollection;
	}

	private void initComponents()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 500));
		getContentPane().setLayout(new GridBagLayout());
		buildSyncApplyPanel();
		pack();
	}

	private void buildSyncApplyPanel()
	{
		syncApplyPanel = new JPanel();
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.insets = new Insets(3, 20, 10, 0);
		getControlsPanel().add(syncApplyPanel, gridBagConstraints);
		buildSyncCheckBox();
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		getSyncApplyPanel().add(applyAllButton, gridBagConstraints);
	}

	private void buildSyncCheckBox()
	{
		getSyncCheckBox().setSelected(false);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
//		gridBagConstraints.insets = new Insets(3, 20, 10, 0);
		getSyncApplyPanel().add(getSyncCheckBox(), gridBagConstraints);
	}

	public JPanel getSyncApplyPanel()
	{
		return syncApplyPanel;
	}

	public JCheckBox getSyncCheckBox()
	{
		return syncCheckBox;
	}

	public JComboBox<String> getLayerComboBox()
	{
		return layerComboBox;
	}

	public void setLayerComboBox(JComboBox<String> layerComboBox)
	{
		this.layerComboBox = layerComboBox;
	}

	public JButton getApplyToBodyButton()
	{
		return applyToBodyButton;
	}

	public JButton getApplyAllButton()
	{
		return applyAllButton;
	}



	public void propertyChange(PropertyChangeEvent arg0)
	{
		if (renWin.getRenderWindow().GetNeverRendered() > 0)
			return;
		renWin.Render();
	}

	private void createMenus()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
//		JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
//		{
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...",
//						"image.png", "png");
//				RenderIoUtil.saveToFile(file, renWin, null);
//			}
//		});
//		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	public vtkJoglPanelComponent getRenWin()
	{
		return renWin;
	}

	public void setRenWin(vtkJoglPanelComponent renWin)
	{
		this.renWin = renWin;
	}

	public JPanel getLayerPanel()
	{
		return layerPanel;
	}

	public JPanel getControlsPanel()
	{
		return controlsPanel;
	}
}