/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.image.ui.custom.editing;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.core.util.VtkENVIReader;
import edu.jhuapl.sbmt.image.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageFillValuesController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageMaskController;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;

public class CustomImageEditingDialog<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
		extends JDialog
{
	private JTextField imagePathTextField;
	private JTextField imageNameTextField;
	private JTextField pointingFilenameTextField;
	private JTextField minLatitudeTextField;
	private JTextField maxLatitudeTextField;
	private JTextField minLongitudeTextField;
	private JTextField maxLongitudeTextField;
	private JComboBox<String> imageFlipComboBox;
	private JComboBox<String> imageRotationComboBox;
	private JComboBox<String> pointingTypeComboBox;
	private JCheckBox flipAboutXCheckBox;
	private JButton browseButton;
	private JButton okButton;
	private JButton imagePathBrowseButton;

	ImageMaskController maskController;
	ImageContrastController contrastController;
	ImageFillValuesController fillValuesController;

	private JPanel layerPanel;
	private JPanel controlsPanel;
	private JSplitPane splitPane;
	private boolean isPerspective;
	private JPanel appearancePanel;

	public CustomImageEditingDialog(Window parent, G1 existingImage, boolean isPerspective, Runnable completionBlock, ImageMaskController maskController,
	ImageContrastController contrastController, ImageFillValuesController fillController)
	{
		super(parent, "Edit Image", Dialog.ModalityType.APPLICATION_MODAL);

		this.isPerspective = isPerspective;
		this.contrastController = contrastController;
		this.fillValuesController = fillController;
		this.maskController = maskController;
		this.layerPanel = new JPanel();
		this.layerPanel.setLayout(new GridBagLayout());
		this.controlsPanel = new JPanel();
		this.controlsPanel.setLayout(new GridBagLayout());
		this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlsPanel, layerPanel);
		splitPane.setMinimumSize(new Dimension(750, 550));

		initGUI();

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(splitPane, gridBagConstraints);
		pack();
	}

	private void initGUI()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(1150, 550));
		setPreferredSize(new Dimension(1150, 550));
		getContentPane().setLayout(new GridBagLayout());

		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));

		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		aboutPanel.setBorder(BorderFactory.createTitledBorder("About this image"));

		aboutPanel.add(buildImagePathInput());
		aboutPanel.add(buildImageNameInput());

		controlsPanel.add(aboutPanel);
		// getContentPane().add(buildImageTypeInput());

		JPanel projectionPanel = new JPanel();
		projectionPanel.setBorder(BorderFactory.createTitledBorder("Image Projection"));
		projectionPanel.add(buildPointingInput());

		controlsPanel.add(projectionPanel);


		appearancePanel = new JPanel();
		appearancePanel.setEnabled(false);
		appearancePanel.setLayout(new BoxLayout(appearancePanel, BoxLayout.Y_AXIS));
		appearancePanel.setBorder(BorderFactory.createTitledBorder("Image Appearance"));
		appearancePanel.add(buildContrastController());
		appearancePanel.add(buildTrimController());
		appearancePanel.add(buildFillValuesPanel());
		appearancePanel.add(Box.createVerticalGlue());

		controlsPanel.add(appearancePanel);

		controlsPanel.add(buildSubmitCancelPanel());
	}

	private JPanel buildImagePathInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(30));
		panel.add(new JLabel("Path:"));

		imagePathTextField = new JTextField();
		imagePathTextField.setMinimumSize(new Dimension(350, 30));
		imagePathTextField.setPreferredSize(new Dimension(350, 30));
		imagePathTextField.setMaximumSize(new Dimension(350, 30));

		panel.add(Box.createHorizontalStrut(20));
		panel.add(imagePathTextField);

		imagePathBrowseButton = new JButton("Browse");

		panel.add(imagePathBrowseButton);
		panel.add(Box.createGlue());
		return panel;
	}

	private JPanel buildImageNameInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(30));
		panel.add(new JLabel("Name:"));

		imageNameTextField = new JTextField();
		imageNameTextField.setMinimumSize(new Dimension(350, 30));
		imageNameTextField.setPreferredSize(new Dimension(350, 30));
		imageNameTextField.setMaximumSize(new Dimension(350, 30));

		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageNameTextField);
		panel.add(Box.createGlue());
//		panel.add(Box.createHorizontalStrut(100));

		return panel;
	}

	private JPanel buildFlipAboutXCheckBoxInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		// panel.add(new JLabel("Flip "));
		flipAboutXCheckBox = new JCheckBox("Flip about X Axis");


		panel.add(flipAboutXCheckBox);
		return panel;
	}

	private JPanel buildImageRotationInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(0));
		panel.add(new JLabel("Image Rotation:"));

		imageRotationComboBox = new JComboBox<String>(new String[]
		{ "0", "90", "180", "270" });
		imageRotationComboBox.setMaximumSize(new Dimension(350, 30));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageRotationComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}

	private JPanel buildImageFlipInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalStrut(0));
		panel.add(new JLabel("Image Flip:"));
		panel.add(Box.createHorizontalStrut(30));
		imageFlipComboBox = new JComboBox<String>(new String[]
		{ "None", "X", "Y" });
		imageFlipComboBox.setMaximumSize(new Dimension(350, 30));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageFlipComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}

	private JPanel buildPointingInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel cardPanel = new JPanel();
		CardLayout cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		cardPanel.setMinimumSize(new Dimension(500, 100));
		cardPanel.setPreferredSize(new Dimension(500, 100));
		cardPanel.setMaximumSize(new Dimension(500, 100));

		////////////////
		JPanel cylindricalPanel = new JPanel();
		cylindricalPanel.setLayout(new BoxLayout(cylindricalPanel, BoxLayout.Y_AXIS));

		JPanel latitudePanel = new JPanel();
		latitudePanel.setLayout(new BoxLayout(latitudePanel, BoxLayout.X_AXIS));
		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(new JLabel("Latitude Range (deg):"));
		minLatitudeTextField = new JTextField("-90.0");
		minLatitudeTextField.setMinimumSize(new Dimension(100, 30));
		minLatitudeTextField.setPreferredSize(new Dimension(100, 30));
		minLatitudeTextField.setMaximumSize(new Dimension(100, 30));

		maxLatitudeTextField = new JTextField("90.0");
		maxLatitudeTextField.setMinimumSize(new Dimension(100, 30));
		maxLatitudeTextField.setPreferredSize(new Dimension(100, 30));
		maxLatitudeTextField.setMaximumSize(new Dimension(100, 30));

		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(minLatitudeTextField);
		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(new JLabel(" to "));
		latitudePanel.add(Box.createHorizontalStrut(10));

		latitudePanel.add(Box.createHorizontalStrut(10));
		latitudePanel.add(maxLatitudeTextField);
		latitudePanel.add(Box.createHorizontalStrut(100));

		JPanel longitudePanel = new JPanel();
		longitudePanel.setLayout(new BoxLayout(longitudePanel, BoxLayout.X_AXIS));
		longitudePanel.add(new JLabel("Longitude Range (deg east):"));
		minLongitudeTextField = new JTextField("0.0");
		minLongitudeTextField.setMinimumSize(new Dimension(100, 30));
		minLongitudeTextField.setPreferredSize(new Dimension(100, 30));
		minLongitudeTextField.setMaximumSize(new Dimension(100, 30));

		maxLongitudeTextField = new JTextField("360.0");
		maxLongitudeTextField.setMinimumSize(new Dimension(100, 30));
		maxLongitudeTextField.setPreferredSize(new Dimension(100, 30));
		maxLongitudeTextField.setMaximumSize(new Dimension(100, 30));

		longitudePanel.add(Box.createHorizontalStrut(10));
		longitudePanel.add(minLongitudeTextField);
		longitudePanel.add(Box.createHorizontalStrut(10));
		longitudePanel.add(new JLabel(" to "));
		longitudePanel.add(Box.createHorizontalStrut(10));

		longitudePanel.add(Box.createHorizontalStrut(10));
		longitudePanel.add(maxLongitudeTextField);
		longitudePanel.add(Box.createHorizontalStrut(100));

		cylindricalPanel.add(latitudePanel);
		cylindricalPanel.add(longitudePanel);
		cylindricalPanel.add(buildFlipAboutXCheckBoxInput());

		////////////////
		JPanel perspectivePanel = new JPanel();
		perspectivePanel.setLayout(new BoxLayout(perspectivePanel, BoxLayout.Y_AXIS));

		JPanel fileInputPanel = new JPanel();
		fileInputPanel.setLayout(new BoxLayout(fileInputPanel, BoxLayout.X_AXIS));
		fileInputPanel.add(Box.createHorizontalStrut(0));
		fileInputPanel.add(new JLabel("Pointing File:"));
		fileInputPanel.add(Box.createHorizontalStrut(20));
		pointingFilenameTextField = new JTextField("FILE NOT FOUND");
		pointingFilenameTextField.setMinimumSize(new Dimension(275, 30));
		pointingFilenameTextField.setPreferredSize(new Dimension(275, 30));
		pointingFilenameTextField.setMaximumSize(new Dimension(275, 30));

		fileInputPanel.add(Box.createHorizontalStrut(10));
		fileInputPanel.add(pointingFilenameTextField);
		fileInputPanel.add(Box.createHorizontalStrut(10));
		browseButton = new JButton("Browse");

		fileInputPanel.add(browseButton);
		fileInputPanel.add(Box.createHorizontalStrut(100));

		perspectivePanel.add(fileInputPanel);

		perspectivePanel.add(buildImageRotationInput());
		perspectivePanel.add(buildImageFlipInput());

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));

		if (isPerspective)
		{
			CardLayout cl = (CardLayout) (cardPanel.getLayout());
			cl.show(cardPanel, "Perspective Projection");
		}
		else
		{
			CardLayout cl = (CardLayout) (cardPanel.getLayout());
			cl.show(cardPanel, "Simple Cylindrical Projection");
		}
		pointingTypeComboBox = new JComboBox<String>(new String[]
		{ "Perspective Projection", "Simple Cylindrical Projection" });
		pointingTypeComboBox.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				CardLayout cl = (CardLayout) (cardPanel.getLayout());
				cl.show(cardPanel, arg0.getItem().toString());
			}
		});
//		optionPanel.add(pointingTypeComboBox);
		optionPanel.add(Box.createHorizontalStrut(100));

		panel.add(optionPanel);
		cardPanel.add(perspectivePanel, "Perspective Projection");
		cardPanel.add(cylindricalPanel, "Simple Cylindrical Projection");
		panel.add(cardPanel);
		return panel;
	}

	private JPanel buildContrastController()
	{
		contrastController.getView().setBackground(Color.red);
		return contrastController.getView();
	}

	private JPanel buildTrimController()
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 6, 3, 0);
		maskController.getView().setMaximumSize(new Dimension(550, 60));
		return maskController.getView();
	}

	private JPanel buildSubmitCancelPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(e -> setVisible(false));

		panel.add(okButton);
		panel.add(cancelButton);
		return panel;
	}

	private JPanel buildFillValuesPanel()
	{
		fillValuesController.getView().setMaximumSize(new Dimension(550, 100));
		return fillValuesController.getView();
	}

	private String checkForEnviFile(String imagePath, boolean isEnviSupported)
	{
		if (VtkENVIReader.isENVIFilename(imagePath))
		{
			// Both header and binary files must exist
			if (VtkENVIReader.checkFilesExist(imagePath))
			{
				// SBMT supports ENVI
				isEnviSupported = true;
			}
			else
			{
				// Error message
				return "Was not able to locate a corresponding .hdr file for ENVI image binary";
			}
		}
		return "";
	}

	/**
	 * @return the imagePathTextField
	 */
	public JTextField getImagePathTextField()
	{
		return imagePathTextField;
	}

	/**
	 * @return the imageNameTextField
	 */
	public JTextField getImageNameTextField()
	{
		return imageNameTextField;
	}

	/**
	 * @return the pointingFilenameTextField
	 */
	public JTextField getPointingFilenameTextField()
	{
		return pointingFilenameTextField;
	}

	/**
	 * @return the minLatitudeTextField
	 */
	public JTextField getMinLatitudeTextField()
	{
		return minLatitudeTextField;
	}

	/**
	 * @return the maxLatitudeTextField
	 */
	public JTextField getMaxLatitudeTextField()
	{
		return maxLatitudeTextField;
	}

	/**
	 * @return the minLongitudeTextField
	 */
	public JTextField getMinLongitudeTextField()
	{
		return minLongitudeTextField;
	}

	/**
	 * @return the maxLongitudeTextField
	 */
	public JTextField getMaxLongitudeTextField()
	{
		return maxLongitudeTextField;
	}

	/**
	 * @return the imageFlipComboBox
	 */
	public JComboBox<String> getImageFlipComboBox()
	{
		return imageFlipComboBox;
	}

	/**
	 * @return the imageRotationComboBox
	 */
	public JComboBox<String> getImageRotationComboBox()
	{
		return imageRotationComboBox;
	}

	/**
	 * @return the pointingTypeComboBox
	 */
	public JComboBox<String> getPointingTypeComboBox()
	{
		return pointingTypeComboBox;
	}

	/**
	 * @return the trimController
	 */
	public ImageMaskController getMaskController()
	{
		return maskController;
	}

	/**
	 * @return the contrastController
	 */
	public ImageContrastController getContrastController()
	{
		return contrastController;
	}

	/**
	 * @return the flipAboutXCheckBox
	 */
	public JCheckBox getFlipAboutXCheckBox()
	{
		return flipAboutXCheckBox;
	}

	/**
	 * @return the browseButton
	 */
	public JButton getBrowseButton()
	{
		return browseButton;
	}

	public JButton getImagePathBrowseButton()
	{
		return imagePathBrowseButton;
	}
	
	/**
	 * @return the okButton
	 */
	public JButton getOkButton()
	{
		return okButton;
	}

	/**
	 * @return the layerPanel
	 */
	public JPanel getLayerPanel()
	{
		return layerPanel;
	}

	/**
	 * @return the appearancePanel
	 */
	public JPanel getAppearancePanel()
	{
		return appearancePanel;
	}
}
