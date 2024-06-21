/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.image.ui.custom.importer;

import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.core.util.VtkENVIReader;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.ImageType;
import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;


public class CustomImageImporterDialog<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JDialog
{
	private JTextField imagePathTextField;
	private JTextField imageNameTextField;
	private JTextField pointingFilenameTextField;
	private JTextField minLatitudeTextField;
	private JTextField maxLatitudeTextField;
	private JTextField minLongitudeTextField;
	private JTextField maxLongitudeTextField;
//	private JComboBox<ImageType> imageTypeComboBox;
	private JComboBox<String> imageFlipComboBox;
	private JComboBox<String> imageRotationComboBox;
	private JComboBox<String> pointingTypeComboBox;
	private JCheckBox flipAboutXCheckBox;
	private boolean isEditMode;
	private boolean isEllipsoid;
//	private BaseItemManager<G1> imageCollection;
	private Optional<G1> existingImage;
	private ImageType imageType = null;

	public CustomImageImporterDialog(Window parent, boolean isEditMode, boolean isEllipsoid,
			/*BaseItemManager<G1> imageCollection,*/ Optional<G1> existingImage)
	{
		 super(parent, isEditMode ? "Edit Image" : "Import New Image", Dialog.ModalityType.APPLICATION_MODAL);
		 this.isEditMode = isEditMode;
		 this.isEllipsoid = isEllipsoid;
//		 this.imageCollection = imageCollection;
		 this.existingImage = existingImage;
		 initGUI();
		 setSize(550, 400);
	}

	private void initGUI()
	{
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(buildImagePathInput());
		getContentPane().add(buildImageNameInput());
//		getContentPane().add(buildImageTypeInput());

		getContentPane().add(buildPointingInput());
		getContentPane().add(buildSubmitCancelPanel());

		existingImage.ifPresent(image -> {

			imagePathTextField.setText(image.getFilename());
			imageNameTextField.setText(image.getName());
			if (image.getImageType() != ImageType.GENERIC_IMAGE)
			{
				if (!image.getPointingSource().isEmpty())
				{
					pointingTypeComboBox.setSelectedIndex(0);
					pointingFilenameTextField.setText(image.getPointingSource());
					imageFlipComboBox.setSelectedItem(image.getFlip());
					imageRotationComboBox.setSelectedItem(""+ (int)(image.getRotation()));
				}
				else
				{
					pointingTypeComboBox.setSelectedIndex(1);
					minLatitudeTextField.setText(""+image.getBounds().minLatitude());
					maxLatitudeTextField.setText(""+image.getBounds().maxLatitude());
					minLongitudeTextField.setText(""+image.getBounds().minLongitude());
					maxLongitudeTextField.setText(""+image.getBounds().maxLongitude());
				}
			}
			else	//cylindrical
			{
				pointingTypeComboBox.setSelectedIndex(1);
				minLatitudeTextField.setText(""+image.getBounds().minLatitude());
				maxLatitudeTextField.setText(""+image.getBounds().maxLatitude());
				minLongitudeTextField.setText(""+image.getBounds().minLongitude());
				maxLongitudeTextField.setText(""+image.getBounds().maxLongitude());
			}

		});
	}

	private JPanel buildImagePathInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Image Path:"));

		imagePathTextField = new JTextField();
		imagePathTextField.setMinimumSize(new Dimension(350, 30));
		imagePathTextField.setPreferredSize(new Dimension(350, 30));
		imagePathTextField.setMaximumSize(new Dimension(350, 30));

		panel.add(Box.createHorizontalStrut(10));
		panel.add(imagePathTextField);

		JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(e -> {
			File file = CustomFileChooser.showOpenDialog(this, "Select Image");
	        if (file == null)
	        {
	            return;
	        }

	        String filename = file.getAbsolutePath();
	        imagePathTextField.setText(filename);
	        String imageFileName = file.getName();
	        String extension = FilenameUtils.getExtension(imageFileName).toLowerCase();
//	        if (extension.equals("fits") || extension.equals("fit"))
//	        {
//	        	imageTypeComboBox.setSelectedItem(instrument.getType());
//	        }
//	        else
//	        {
//	        	imageTypeComboBox.setSelectedItem(ImageType.GENERIC_IMAGE);
//	        	pointingTypeComboBox.setSelectedItem("Simple Cylindrical Projection");
//	        }

	        imageNameTextField.setText(imageFileName);
		});

		panel.add(browseButton);
		return panel;
	}

	private JPanel buildImageNameInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Image Name:"));

		imageNameTextField = new JTextField();
		imageNameTextField.setMinimumSize(new Dimension(350, 30));
		imageNameTextField.setPreferredSize(new Dimension(350, 30));
		imageNameTextField.setMaximumSize(new Dimension(350, 30));

		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageNameTextField);
		panel.add(Box.createHorizontalStrut(100));

		return panel;
	}

//	private JPanel buildImageTypeInput()
//	{
//		JPanel panel = new JPanel();
//		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//		panel.add(new JLabel("Image Type:"));
//
//		imageTypeComboBox = new JComboBox<ImageType>(new ImageType[] {instrument.getType(), ImageType.GENERIC_IMAGE});
//		imageTypeComboBox.setMaximumSize(new Dimension(350, 30));
//		panel.add(Box.createHorizontalStrut(10));
//		panel.add(imageTypeComboBox);
//		panel.add(Box.createHorizontalStrut(100));
//		return panel;
//	}

	private JPanel buildFlipAboutXCheckBoxInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//		panel.add(new JLabel("Flip "));
		flipAboutXCheckBox = new JCheckBox("Flip about X Axis");
		if (existingImage.isPresent())
			flipAboutXCheckBox.setSelected(existingImage.get().getFlip().equals("X"));
		panel.add(flipAboutXCheckBox);
		return panel;
	}

	private JPanel buildImageRotationInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Image Rotation:"));

		imageRotationComboBox = new JComboBox<String>(new String[] { "0", "90", "180", "270" });
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
		panel.add(new JLabel("Image Flip:"));
		panel.add(Box.createHorizontalStrut(30));
		imageFlipComboBox = new JComboBox<String>(new String[] { "None", "X", "Y" });
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
		fileInputPanel.add(new JLabel("Pointing File:"));
		fileInputPanel.add(Box.createHorizontalStrut(20));
		pointingFilenameTextField = new JTextField();
		pointingFilenameTextField.setMinimumSize(new Dimension(275, 30));
		pointingFilenameTextField.setPreferredSize(new Dimension(275, 30));
		pointingFilenameTextField.setMaximumSize(new Dimension(275, 30));

		fileInputPanel.add(Box.createHorizontalStrut(10));
		fileInputPanel.add(pointingFilenameTextField);
		fileInputPanel.add(Box.createHorizontalStrut(10));
		JButton browseButton = new JButton("Browse");

		browseButton.addActionListener(e -> {
			File file = CustomFileChooser.showOpenDialog(this, "Select Pointing File...");
	        if (file == null)
	        {
	            return;
	        }

	        String filename = file.getAbsolutePath();
	        pointingFilenameTextField.setText(filename);
		});

		fileInputPanel.add(browseButton);
		fileInputPanel.add(Box.createHorizontalStrut(100));


		perspectivePanel.add(fileInputPanel);

		perspectivePanel.add(buildImageRotationInput());
		perspectivePanel.add(buildImageFlipInput());


		/////////////////
		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.X_AXIS));
		optionPanel.add(new JLabel("Pointing Type:"));
//		optionPanel.add(Box.createHorizontalGlue());
		pointingTypeComboBox = new JComboBox<String>(new String[] {"Perspective Projection", "Simple Cylindrical Projection"});
		pointingTypeComboBox.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				CardLayout cl = (CardLayout)(cardPanel.getLayout());
				cl.show(cardPanel, arg0.getItem().toString());
			}
		});
		optionPanel.add(pointingTypeComboBox);
		optionPanel.add(Box.createHorizontalStrut(100));

		panel.add(optionPanel);
		cardPanel.add(perspectivePanel, "Perspective Projection");
		cardPanel.add(cylindricalPanel, "Simple Cylindrical Projection");
		panel.add(cardPanel);
		return panel;
	}

	private JPanel buildSubmitCancelPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> {
			String errorString = validateInput();
	        if (errorString != null)
	        {
	            JOptionPane.showMessageDialog(this,
	                    errorString,
	                    "Error",
	                    JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        storeImage();
	        setVisible(false);
		});

		cancelButton.addActionListener(e -> setVisible(false));

		panel.add(okButton);
		panel.add(cancelButton);
		return panel;
	}

	private void storeImage()
	{
		String filename = imagePathTextField.getText();

		existingImage.ifPresent(image -> {

			imageType = image.getImageType();
			existingImage.get().setName(imageNameTextField.getText());
			if (pointingTypeComboBox.getSelectedItem().equals("Perspective Projection"))
			{
				existingImage.get().setPointingSource(pointingFilenameTextField.getText());
				existingImage.get().setFlip((String)imageFlipComboBox.getSelectedItem());
				existingImage.get().setRotation(Double.parseDouble((String)imageRotationComboBox.getSelectedItem()));
			}
			else
			{
				Double minLat = Double.parseDouble(minLatitudeTextField.getText());
				Double maxLat = Double.parseDouble(maxLatitudeTextField.getText());
				Double minLon = Double.parseDouble(minLongitudeTextField.getText());
				Double maxLon = Double.parseDouble(maxLongitudeTextField.getText());
				existingImage.get().setBounds(new CylindricalBounds(minLat, maxLat, minLon, maxLon));
				if (flipAboutXCheckBox.isSelected()) existingImage.get().setFlip("X");
				else existingImage.get().setFlip("None");
			}
		});
//		ImageType imageType = (ImageType)imageTypeComboBox.getSelectedItem();
//		String pointingSource = pointingFilenameTextField.getText();
//		ImageSource pointingSourceType = ImageSource.LOCAL_CYLINDRICAL;
//
//		if (!pointingSource.isEmpty())
//		{
//			String extension = FilenameUtils.getExtension(pointingSource).toLowerCase();
//			pointingSourceType = extension.equals("sum") ? ImageSource.GASKELL : ImageSource.SPICE;
//		}
//		//TODO FIX THIS
//		double[] fillValues = new double[] {};
//		PerspectiveImage image = new PerspectiveImage(filename, imageType, pointingSourceType, pointingSource, fillValues);
//		image.setName(getName());
//		image.setImageOrigin(ImageOrigin.LOCAL);
//		image.setLongTime(new Date().getTime());
//		if (pointingSourceType == ImageSource.LOCAL_CYLINDRICAL)
//		{
//			Double minLat = Double.parseDouble(minLatitudeTextField.getText());
//			Double maxLat = Double.parseDouble(maxLatitudeTextField.getText());
//			Double minLon = Double.parseDouble(minLongitudeTextField.getText());
//			Double maxLon = Double.parseDouble(maxLongitudeTextField.getText());
//			image.setBounds(new CylindricalBounds(minLat, maxLat, minLon, maxLon));
//		}
//		CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
//		compImage.setName(imageNameTextField.getText());
//		imageCollection.addUserImage(compImage);
//		imageCollection.setImagingInstrument(null);
	}

	private String validateInput()
    {
        String imagePath = imagePathTextField.getText();
        if (imagePath == null)
            imagePath = "";

        if (!isEditMode) // || (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED)))
        {
            if (imagePath.isEmpty())
                return "Please enter the path to an image.";

            File file = new File(imagePath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return imagePath + " does not exist or is not readable.";

            if (imagePath.contains(","))
                return "Image path may not contain commas.";
        }

        String imageName = imageNameTextField.getText();
        if (imageName == null)
            imageName = "";
        if (imageName.trim().isEmpty())
            return "Please enter a name for the image. The name can be any text that describes the image.";
        if (imageName.contains(","))
            return "Name may not contain commas.";

        //TODO fix this
//        if (!isEditMode && currentNames.contains(imageName))
//        {
//        	return "Name for custom image already exists.";
//        }

//        if (imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"))
//        {
//        	return "Select an image type.";
//        }


        if (pointingTypeComboBox.getSelectedItem().equals("Simple Cylindrical Projection"))
        {
            if (!isEditMode) // (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED))
            {
                // Check first to see if it is a natively supported image
                boolean supportedCustomFormat = false;

                // Check if this image is any of the custom supported formats
                String message = checkForEnviFile(imagePath, supportedCustomFormat);
                if (supportedCustomFormat == false && !message.equals("")) return message;

                // Otherwise, try to see if VTK natively supports
                if(!supportedCustomFormat)
                {
                    vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
                    vtkImageReader2 imageReader = imageFactory.CreateImageReader2(imagePath);
                    if (imageReader == null)
                        return "The format of the specified image is not supported.";
                }
            }

            try
            {
                double lllat = Double.parseDouble(minLatitudeTextField.getText());
                double urlat = Double.parseDouble(maxLatitudeTextField.getText());
                Double.parseDouble(minLongitudeTextField.getText());
                Double.parseDouble(maxLongitudeTextField.getText());

                if (lllat < -90.0 || lllat > 90.0 || urlat < -90.0 || urlat > 90.0)
                    return "Latitudes must be between -90 and +90.";
                if (lllat >= urlat)
                    return "Upper right latitude must be greater than lower left latitude.";

                if (!isEllipsoid)
                {
                    if ( (lllat < 1.0 && lllat > 0.0) || (lllat > -1.0 && lllat < 0.0) ||
                            (urlat < 1.0 && urlat > 0.0) || (urlat > -1.0 && urlat < 0.0) )
                        return "For non-ellipsoidal shape models, latitudes must be (in degrees) either 0, greater than +1, or less then -1.";
                }
            }
            catch (NumberFormatException e)
            {
                return "An error occurred parsing one of the required fields.";
            }
        }
        else	//uses pointing file
        {
        	String pointingFileName = pointingFilenameTextField.getText();

            if (!isEditMode || (!pointingFileName.isEmpty()))
            {
                if (pointingFilenameTextField.getText().isEmpty() == true)
                    return "Please enter the path to a pointing file.";

                if (!pointingFileName.isEmpty())
                {
                    File file = new File(pointingFileName);
                    if (!file.exists() || !file.canRead() || !file.isFile())
                        return pointingFileName + " does not exist or is not readable.";

                    if (pointingFileName.contains(","))
                        return "Path may not contain commas.";
                }
            }
        }

        return null;
    }

	private String checkForEnviFile(String imagePath, boolean isEnviSupported)
	{
		if(VtkENVIReader.isENVIFilename(imagePath))
        {
            // Both header and binary files must exist
            if(VtkENVIReader.checkFilesExist(imagePath))
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
}

//public class CustomImageImporterDialog extends JDialog
//{
//    private boolean okayPressed = false;
//    private boolean isEllipsoid;
//    private boolean isEditMode;
//    private IImagingInstrument instrument;
//    private static final String LEAVE_UNMODIFIED = "<cannot be changed>";
//    private List<String> currentNames;
//    private String originalName;
//
//    /** Creates new form ShapeModelImporterDialog */
//    public CustomImageImporterDialog(Window parent, boolean isEditMode, IImagingInstrument instrument)
//    {
//        super(parent, isEditMode ? "Edit Image" : "Import New Image", Dialog.ModalityType.APPLICATION_MODAL);
//        initComponents();
//        this.isEditMode = isEditMode;
//        this.instrument = instrument;
//
//        if (isEditMode)
//        {
//            browseImageButton.setEnabled(false);
//            browseSumfileButton.setEnabled(false);
//            browseInfofileButton.setEnabled(false);
//            imagePathTextField.setEnabled(false);
//            infofilePathTextField.setEnabled(false);
//            sumfilePathTextField.setEnabled(false);
//        }
//        updateEnabledItems();
//    }
//
//    public void setImageInfo(CustomImageKeyInterface info, boolean isEllipsoid)
//    {
//    	String keyName = info == null ? "" : info.getName();
//    	String keyImageFilename = info == null ? "" : info.getImageFilename();
//    	ProjectionType projection = info == null ? ProjectionType.CYLINDRICAL : info.getProjectionType();
//    	ImageType currentImageType = info == null ? ImageType.GENERIC_IMAGE : info.getImageType();
//    	originalName = info == null ? "" : info.getOriginalName();
//
//        this.isEllipsoid = isEllipsoid;
//
//        if (isEditMode)
//            imagePathTextField.setText(LEAVE_UNMODIFIED);
//        else
//            imagePathTextField.setText(keyImageFilename);
//
//        imageNameTextField.setText(keyName);
//
//        if (projection == ProjectionType.CYLINDRICAL)
//        {
//            cylindricalProjectionRadioButton.setSelected(true);
//
//            double lllat = info == null ?  -90.0 : ((CustomCylindricalImageKey)info).getLllat();
//            double lllon = info == null ?  0.0 : ((CustomCylindricalImageKey)info).getLllon();
//            double urlat = info == null ?  90.0 : ((CustomCylindricalImageKey)info).getUrlat();
//            double urlon = info == null ?  360.0 : ((CustomCylindricalImageKey)info).getUrlon();
//            lllatFormattedTextField.setText(String.valueOf(lllat));
//            lllonFormattedTextField.setText(String.valueOf(lllon));
//            urlatFormattedTextField.setText(String.valueOf(urlat));
//            urlonFormattedTextField.setText(String.valueOf(urlon));
//        }
//        else if (projection == ProjectionType.PERSPECTIVE)
//        {
//            perspectiveProjectionRadioButton.setSelected(true);
//
//            if (isEditMode)
//            {
//                sumfilePathTextField.setText(LEAVE_UNMODIFIED);
//                infofilePathTextField.setText(LEAVE_UNMODIFIED);
//                if (info.getFileType() == FileType.SUM)
//                {
//                	sumfilePathRB.setSelected(true);
//                }
//            }
//            double rotation = info == null ?  0.0 : info.getRotation();
//            String flip = info == null ?  "None" : info.getFlip();
//            imageFlipComboBox.setSelectedItem(flip);
//            imageRotateComboBox.setSelectedItem(Integer.toString((int)rotation));
//        }
//
//        if (keyImageFilename.toUpperCase().endsWith(".FITS") || keyImageFilename.toUpperCase().endsWith(".FIT"))
//        {
//        	DefaultComboBoxModel model = new DefaultComboBoxModel(ImageType.values());
//        	model.insertElementAt("<CHOOSE IMAGE TYPE>", 0);
//            imageTypeComboBox.setModel(model);
//            imageTypeComboBox.setSelectedItem(currentImageType);
//        }
//        else
//            imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
//
//
//        imageTypeComboBox.setSelectedItem(currentImageType);
//
//
//        updateEnabledItems();
//    }
//
//    private ProjectionType getSelectedProjectionType()
//    {
//        if (cylindricalProjectionRadioButton.isSelected())
//            return ProjectionType.CYLINDRICAL;
//        else
//            return ProjectionType.PERSPECTIVE;
//    }
//
//    public CustomImageKeyInterface getImageInfo()
//    {
//    	String imagefilename = imagePathTextField.getText();
//        if (LEAVE_UNMODIFIED.equals(imagefilename) || imagefilename == null || imagefilename.isEmpty())
//            imagefilename = null;
//        else
//            originalName = new File(imagefilename).getName();
//
//        // If name is not provided, set name to filename
//        ImageType imageType = (ImageType)imageTypeComboBox.getSelectedItem();
//        String name = imageNameTextField.getText();
//        if ((name == null || name.isEmpty()) && imagefilename != null)
//            name = new File(imagefilename).getName();
//
//        if (cylindricalProjectionRadioButton.isSelected())
//        {
//            CustomCylindricalImageKey key = new CustomCylindricalImageKey(name, imagefilename, imageType, ImageSource.LOCAL_CYLINDRICAL, new Date(), originalName);
//            key.setLllat(Double.parseDouble(lllatFormattedTextField.getText()));
//            key.setLllon(Double.parseDouble(lllonFormattedTextField.getText()));
//            key.setUrlat(Double.parseDouble(urlatFormattedTextField.getText()));
//            key.setUrlon(Double.parseDouble(urlonFormattedTextField.getText()));
//            return key;
//        }
//        else
//        {
//        	String pointingFilename = null;
//            FileType fileType = null;
//            ImageSource source = null;
//            if (sumfilePathRB.isSelected() == true)
//            {
//            	fileType = FileType.SUM;
//            	pointingFilename = sumfilePathTextField.getText();
//            	source = ImageSource.LOCAL_PERSPECTIVE;
//            }
//            if (infofilePathRB.isSelected() == true)
//            {
//            	pointingFilename = infofilePathTextField.getText();
//            	fileType = FileType.INFO;
//            	source = ImageSource.LOCAL_PERSPECTIVE;
//            }
//            if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
//            	pointingFilename = null;
//            if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
//            	pointingFilename = null;
//
//            double rotation = imageRotateComboBox.getSelectedIndex() * 90.0;
//            String flip = imageFlipComboBox.getSelectedItem().toString();
//
//            CustomPerspectiveImageKey key = new CustomPerspectiveImageKey(name, imagefilename, source, imageType, rotation, flip, fileType, pointingFilename, new Date(), originalName);
//            return key;
//        }
//    }
//
//    private String validateInput()
//    {
//        String imagePath = imagePathTextField.getText();
//        if (imagePath == null)
//            imagePath = "";
//
//        if (!isEditMode) // || (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED)))
//        {
//            if (imagePath.isEmpty())
//                return "Please enter the path to an image.";
//
//            File file = new File(imagePath);
//            if (!file.exists() || !file.canRead() || !file.isFile())
//                return imagePath + " does not exist or is not readable.";
//
//            if (imagePath.contains(","))
//                return "Image path may not contain commas.";
//        }
//
//        String imageName = imageNameTextField.getText();
//        if (imageName == null)
//            imageName = "";
//        if (imageName.trim().isEmpty())
//            return "Please enter a name for the image. The name can be any text that describes the image.";
//        if (imageName.contains(","))
//            return "Name may not contain commas.";
//        if (!isEditMode && currentNames.contains(imageName))
//        {
//        	return "Name for custom image already exists.";
//        }
//
//        if (imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"))
//        {
//        	return "Select an image type.";
//        }
//
//
//        if (cylindricalProjectionRadioButton.isSelected())
//        {
//            if (!isEditMode) // (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED))
//            {
//                // Check first to see if it is a natively supported image
//                boolean supportedCustomFormat = false;
//
//                // Check if this image is any of the custom supported formats
//                if(VtkENVIReader.isENVIFilename(imagePath))
//                {
//                    // Both header and binary files must exist
//                    if(VtkENVIReader.checkFilesExist(imagePath))
//                    {
//                        // SBMT supports ENVI
//                        supportedCustomFormat = true;
//                    }
//                    else
//                    {
//                        // Error message
//                        return "Was not able to locate a corresponding .hdr file for ENVI image binary";
//                    }
//                }
//
//                // Otherwise, try to see if VTK natively supports
//                if(!supportedCustomFormat)
//                {
//                    vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
//                    vtkImageReader2 imageReader = imageFactory.CreateImageReader2(imagePath);
//                    if (imageReader == null)
//                        return "The format of the specified image is not supported.";
//                }
//            }
//
//            try
//            {
//                double lllat = Double.parseDouble(lllatFormattedTextField.getText());
//                double urlat = Double.parseDouble(urlatFormattedTextField.getText());
//                Double.parseDouble(lllonFormattedTextField.getText());
//                Double.parseDouble(urlonFormattedTextField.getText());
//
//                if (lllat < -90.0 || lllat > 90.0 || urlat < -90.0 || urlat > 90.0)
//                    return "Latitudes must be between -90 and +90.";
//                if (lllat >= urlat)
//                    return "Upper right latitude must be greater than lower left latitude.";
//
//                if (!isEllipsoid)
//                {
//                    if ( (lllat < 1.0 && lllat > 0.0) || (lllat > -1.0 && lllat < 0.0) ||
//                            (urlat < 1.0 && urlat > 0.0) || (urlat > -1.0 && urlat < 0.0) )
//                        return "For non-ellipsoidal shape models, latitudes must be (in degrees) either 0, greater than +1, or less then -1.";
//                }
//            }
//            catch (NumberFormatException e)
//            {
//                return "An error occurred parsing one of the required fields.";
//            }
//        }
//        else
//        {
//            String sumfilePath = sumfilePathTextField.getText();
//            if (sumfilePathRB.isSelected() == false || sumfilePath == null)
//                sumfilePath = "";
//
//            String infofilePath = infofilePathTextField.getText();
//            if (infofilePathRB.isSelected() == false || infofilePath == null)
//                infofilePath = "";
//
//            if (!isEditMode || (!sumfilePath.isEmpty() && !sumfilePath.equals(LEAVE_UNMODIFIED) || (!infofilePath.isEmpty() && !infofilePath.equals(LEAVE_UNMODIFIED))))
//            {
//                if (infofilePathRB.isSelected() == true && infofilePath.isEmpty() == true)
//                    return "Please enter the path to an infofile.";
//
//                if (sumfilePathRB.isSelected() == true && sumfilePath.isEmpty() == true)
//                    return "Please enter the path to a sumfile.";
//
//                if (!sumfilePath.isEmpty())
//                {
//                    File file = new File(sumfilePath);
//                    if (!file.exists() || !file.canRead() || !file.isFile())
//                        return sumfilePath + " does not exist or is not readable.";
//
//                    if (sumfilePath.contains(","))
//                        return "Path may not contain commas.";
//                }
//                else if (!infofilePath.isEmpty())
//                {
//                    File file = new File(infofilePath);
//                    if (!file.exists() || !file.canRead() || !file.isFile())
//                        return infofilePath + " does not exist or is not readable.";
//
//                    if (infofilePath.contains(","))
//                        return "Path may not contain commas.";
//                }
//            }
//        }
//
//        return null;
//    }
//
//    public boolean getOkayPressed()
//    {
//        return okayPressed;
//    }
//
//    public void setCurrentImageNames(List<String> currentNames)
//    {
//    	this.currentNames = currentNames;
//    }
//
//    private void checkCurrentNames()
//    {
//    	String inputName = imageNameTextField.getText();
//    	boolean exists = currentNames.contains(inputName.trim());
//    	if (!isEditMode)
//    	{
//    		nameExistsLabel.setVisible(exists);
//    		okButton.setEnabled(!exists);
//    	}
//    }
//
//    private void checkImageType()
//    {
//    	boolean typeSet = !(imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"));
//    	selectImageTypeLabel.setVisible(!typeSet);
//		okButton.setEnabled(typeSet);
//    }
//
//    private void updateEnabledItems()
//    {
//        boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
//        lllatLabel.setEnabled(cylindrical);
//        lllatFormattedTextField.setEnabled(cylindrical);
//        lllonLabel.setEnabled(cylindrical);
//        lllonFormattedTextField.setEnabled(cylindrical);
//        urlatLabel.setEnabled(cylindrical);
//        urlatFormattedTextField.setEnabled(cylindrical);
//        urlonLabel.setEnabled(cylindrical);
//        urlonFormattedTextField.setEnabled(cylindrical);
//        infofilePathRB.setEnabled(!cylindrical);
//        browseInfofileButton.setEnabled(!cylindrical && infofilePathRB.isSelected());
//        infofilePathTextField.setEnabled(!cylindrical && !isEditMode && infofilePathRB.isSelected());
//        sumfilePathRB.setEnabled(!cylindrical);
//        browseSumfileButton.setEnabled(!cylindrical && sumfilePathRB.isSelected());
//        sumfilePathTextField.setEnabled(!cylindrical && !isEditMode && sumfilePathRB.isSelected());
//
//        boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
//        imageFlipComboBox.setEnabled(!cylindrical);
//        imageRotateComboBox.setEnabled(!cylindrical);
//
//        selectImageTypeLabel.setVisible(imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"));
//    }
//
//    /**
//     * Installs a listener to receive notification when the text of any
//     * {@code JTextComponent} is changed. Internally, it installs a
//     * {@link DocumentListener} on the text component's {@link Document},
//     * and a {@link PropertyChangeListener} on the text component to detect
//     * if the {@code Document} itself is replaced.
//     *
//     * @param text any text component, such as a {@link JTextField}
//     *        or {@link JTextArea}
//     * @param changeListener a listener to receieve {@link ChangeEvent}s
//     *        when the text is changed; the source object for the events
//     *        will be the text component
//     * @throws NullPointerException if either parameter is null
//     */
//    public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
//        Objects.requireNonNull(text);
//        Objects.requireNonNull(changeListener);
//        DocumentListener dl = new DocumentListener() {
//            private int lastChange = 0, lastNotifiedChange = 0;
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                changedUpdate(e);
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                changedUpdate(e);
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                lastChange++;
//                SwingUtilities.invokeLater(() -> {
//                    if (lastNotifiedChange != lastChange) {
//                        lastNotifiedChange = lastChange;
//                        changeListener.stateChanged(new ChangeEvent(text));
//                    }
//                });
//            }
//        };
//        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
//            Document d1 = (Document)e.getOldValue();
//            Document d2 = (Document)e.getNewValue();
//            if (d1 != null) d1.removeDocumentListener(dl);
//            if (d2 != null) d2.addDocumentListener(dl);
//            dl.changedUpdate(null);
//        });
//        Document d = text.getDocument();
//        if (d != null) d.addDocumentListener(dl);
//    }
//
//    /** This method is called from within the constructor to
//     * initialize the form.
//     * WARNING: Do NOT modify this code. The content of this method is
//     * always regenerated by the Form Editor.
//     */
//    @SuppressWarnings("unchecked")
//    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
//    private void initComponents() {
//        java.awt.GridBagConstraints gridBagConstraints;
//
//        projectionButtonGroup = new ButtonGroup();
//        imagePathLabel = new JLabel();
//        imagePathTextField = new JTextField();
//        browseImageButton = new JButton();
//        lllatLabel = new JLabel();
//        lllonLabel = new JLabel();
//        urlatLabel = new JLabel();
//        urlonLabel = new JLabel();
//        jPanel1 = new JPanel();
//        cancelButton = new JButton();
//        okButton = new JButton();
//        lllatFormattedTextField = new JFormattedTextField();
//        lllonFormattedTextField = new JFormattedTextField();
//        urlatFormattedTextField = new JFormattedTextField();
//        urlonFormattedTextField = new JFormattedTextField();
//        cylindricalProjectionRadioButton = new JRadioButton();
//        perspectiveProjectionRadioButton = new JRadioButton();
//        infofilePathRB = new JRadioButton("Infofile Path", true);
//        browseInfofileButton = new JButton();
//        imageLabel = new JLabel();
//        imageNameTextField = new JTextField();
//        nameExistsLabel = new JLabel();
//        selectImageTypeLabel = new JLabel();
//        sumfilePathRB = new JRadioButton();
//        infofilePathTextField = new JTextField();
//        sumfilePathTextField = new JTextField();
//        browseSumfileButton = new JButton();
//        imageTypeLabel = new JLabel();
//        imageTypeComboBox = new JComboBox();
//        imageRotateLabel = new JLabel();
//        imageFlipLabel = new JLabel();
//        imageRotateComboBox = new JComboBox();
//        imageFlipComboBox = new JComboBox();
//        ButtonGroup tmpBG = new ButtonGroup();
//        tmpBG.add(infofilePathRB);
//        tmpBG.add(sumfilePathRB);
//
//        addChangeListener(imageNameTextField, e -> { checkCurrentNames(); checkImageType(); });
//
//        imageTypeComboBox.addActionListener(e -> { checkImageType(); checkCurrentNames(); } );
//
//        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
//        setMinimumSize(new java.awt.Dimension(600, 167));
//        getContentPane().setLayout(new java.awt.GridBagLayout());
//
//        imagePathLabel.setText("Image Path");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
//        getContentPane().add(imagePathLabel, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 400;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
//        getContentPane().add(imagePathTextField, gridBagConstraints);
//
//        browseImageButton.setText("Browse...");
//        browseImageButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                browseImageButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 5);
//        getContentPane().add(browseImageButton, gridBagConstraints);
//
//        lllatLabel.setText("Lower Left Latitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(lllatLabel, gridBagConstraints);
//
//        lllonLabel.setText("Lower Left Longitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 7;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(lllonLabel, gridBagConstraints);
//
//        urlatLabel.setText("Upper Right Latitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(urlatLabel, gridBagConstraints);
//
//        urlonLabel.setText("Upper Right Longitude");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 9;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(urlonLabel, gridBagConstraints);
//
//        jPanel1.setLayout(new java.awt.GridBagLayout());
//
//        cancelButton.setText("Cancel");
//        cancelButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                cancelButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
//        jPanel1.add(cancelButton, gridBagConstraints);
//
//        okButton.setText("OK");
//        okButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                okButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        jPanel1.add(okButton, gridBagConstraints);
//
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 13;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
//        gridBagConstraints.weighty = 1.0;
//        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
//        getContentPane().add(jPanel1, gridBagConstraints);
//
//        lllatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        lllatFormattedTextField.setText("-90");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 6;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(lllatFormattedTextField, gridBagConstraints);
//
//        lllonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        lllonFormattedTextField.setText("0");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 7;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(lllonFormattedTextField, gridBagConstraints);
//
//        urlatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        urlatFormattedTextField.setText("90");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 8;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(urlatFormattedTextField, gridBagConstraints);
//
//        urlonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
//        urlonFormattedTextField.setText("360");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 9;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 60;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(urlonFormattedTextField, gridBagConstraints);
//
//        projectionButtonGroup.add(cylindricalProjectionRadioButton);
//        cylindricalProjectionRadioButton.setSelected(true);
//        cylindricalProjectionRadioButton.setText("Simple Cylindrical Projection");
//        cylindricalProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                cylindricalProjectionRadioButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 5;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
//        getContentPane().add(cylindricalProjectionRadioButton, gridBagConstraints);
//
//        projectionButtonGroup.add(perspectiveProjectionRadioButton);
//        perspectiveProjectionRadioButton.setText("Perspective Projection");
//        perspectiveProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                perspectiveProjectionRadioButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 10;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
//        getContentPane().add(perspectiveProjectionRadioButton, gridBagConstraints);
//
//        infofilePathRB.setText("Infofile Path");
//        infofilePathRB.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                updateEnabledItems();
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 12;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(infofilePathRB, gridBagConstraints);
//
//        browseInfofileButton.setText("Browse...");
//        browseInfofileButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                browseInfofileButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 12;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
//        getContentPane().add(browseInfofileButton, gridBagConstraints);
//
//        imageLabel.setText("Name");
//        imageLabel.setToolTipText("A name describing the image that will be displayed in the image list.");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageLabel, gridBagConstraints);
//
//        imageNameTextField.setToolTipText("A name describing the image that will be displayed in the image list.");
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.ipadx = 400;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
//        getContentPane().add(imageNameTextField, gridBagConstraints);
//
//        nameExistsLabel.setText("Already Exists");
//        nameExistsLabel.setForeground(Color.red);
//        nameExistsLabel.setVisible(false);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
//        getContentPane().add(nameExistsLabel, gridBagConstraints);
//
//
//        sumfilePathRB.setText("Sumfile Path");
//        sumfilePathRB.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                updateEnabledItems();
//            }
//        });
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 11;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
//        getContentPane().add(sumfilePathRB, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 12;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(infofilePathTextField, gridBagConstraints);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 11;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
//        getContentPane().add(sumfilePathTextField, gridBagConstraints);
//
//        browseSumfileButton.setText("Browse...");
//        browseSumfileButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                browseSumfileButtonActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 11;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
//        getContentPane().add(browseSumfileButton, gridBagConstraints);
//
//        imageTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
//        imageTypeLabel.setText("Image Type");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.ipadx = 2;
//        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageTypeLabel, gridBagConstraints);
//
//        imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
//        imageTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                imageTypeComboBoxActionPerformed(evt);
//            }
//        });
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageTypeComboBox, gridBagConstraints);
//
//        selectImageTypeLabel.setText("Select an Image Type");
//        selectImageTypeLabel.setForeground(Color.red);
//        selectImageTypeLabel.setVisible(false);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 2;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
//        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
//        getContentPane().add(selectImageTypeLabel, gridBagConstraints);
//
//        imageRotateLabel.setText("Image Rotate");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageRotateLabel, gridBagConstraints);
//
//        imageFlipLabel.setText("Image Flip");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 4;
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageFlipLabel, gridBagConstraints);
//
//        imageRotateComboBox.setModel(new DefaultComboBoxModel(new String[] { "0", "90", "180", "270" }));
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageRotateComboBox, gridBagConstraints);
//
//        imageFlipComboBox.setModel(new DefaultComboBoxModel(new String[] { "None", "X", "Y" }));
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 4;
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
//        getContentPane().add(imageFlipComboBox, gridBagConstraints);
//
//        pack();
//    }// </editor-fold>//GEN-END:initComponents
//
//    private void browseImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseImageButtonActionPerformed
//    {//GEN-HEADEREND:event_browseImageButtonActionPerformed
//        File file = CustomFileChooser.showOpenDialog(this, "Select Image");
//        if (file == null)
//        {
//            return;
//        }
//
//        String filename = file.getAbsolutePath();
//        imagePathTextField.setText(filename);
//
//        String imageFileName = file.getName();
//        if (imageFileName.toUpperCase().endsWith(".FITS") || imageFileName.toUpperCase().endsWith(".FIT"))
//        {
//            ImageType[] allImageTypes = ImageType.values();
//            ImageType currentImageType = instrument != null ? instrument.getType() : ImageType.GENERIC_IMAGE;
//            DefaultComboBoxModel model = new DefaultComboBoxModel(allImageTypes);
//            model.insertElementAt("<CHOOSE IMAGE TYPE>", 0);
//            imageTypeComboBox.setModel(model);
//            imageTypeComboBox.setSelectedIndex(0);
////            imageTypeComboBox.setSelectedItem(currentImageType);
//
//            boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
//            boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
//            imageFlipComboBox.setEnabled(generic && !cylindrical);
//            imageRotateComboBox.setEnabled(generic && !cylindrical);
//        }
//        else
//        {
//            imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
//        }
//
//        imageNameTextField.setText(imageFileName);
//
//        // set default info file name
////        String tokens[] = imageFileName.split("\\.");
////        int ntokens = tokens.length;
////        String suffix = tokens[ntokens-1];
////        int suffixLength = suffix.length();
////        String imageFileNamePrefix = imageFileName.substring(0, imageFileName.length() - suffixLength);
//        String imageFileNamePrefix = FilenameUtils.getBaseName(imageFileName);
//        String defaultInfoFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + ".INFO";
//        String defaultSumFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + ".SUM";
//        infofilePathTextField.setText(defaultInfoFileName);
//        sumfilePathTextField.setText(defaultSumFileName);
//
//        updateEnabledItems();
//    }//GEN-LAST:event_browseImageButtonActionPerformed
//
//    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
//    {//GEN-HEADEREND:event_cancelButtonActionPerformed
//        setVisible(false);
//    }//GEN-LAST:event_cancelButtonActionPerformed
//
//    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
//    {//GEN-HEADEREND:event_okButtonActionPerformed
//        String errorString = validateInput();
//        if (errorString != null)
//        {
//            JOptionPane.showMessageDialog(this,
//                    errorString,
//                    "Error",
//                    JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        okayPressed = true;
//        setVisible(false);
//    }//GEN-LAST:event_okButtonActionPerformed
//
//    private void cylindricalProjectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cylindricalProjectionRadioButtonActionPerformed
//        updateEnabledItems();
//    }//GEN-LAST:event_cylindricalProjectionRadioButtonActionPerformed
//
//    private void perspectiveProjectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_perspectiveProjectionRadioButtonActionPerformed
//        updateEnabledItems();
//    }//GEN-LAST:event_perspectiveProjectionRadioButtonActionPerformed
//
//    private void browseInfofileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseInfofileButtonActionPerformed
//        File file = CustomFileChooser.showOpenDialog(this, "Select Infofile");
//        if (file == null)
//        {
//            return;
//        }
//
//        String filename = file.getAbsolutePath();
//        infofilePathTextField.setText(filename);
//    }//GEN-LAST:event_browseInfofileButtonActionPerformed
//
//    private void browseSumfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSumfileButtonActionPerformed
//        File file = CustomFileChooser.showOpenDialog(this, "Select Sumfile");
//        if (file == null)
//        {
//            return;
//        }
//
//        String filename = file.getAbsolutePath();
//        sumfilePathTextField.setText(filename);
//    }//GEN-LAST:event_browseSumfileButtonActionPerformed
//
//    private void imageTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageTypeComboBoxActionPerformed
//        updateEnabledItems();
//    }//GEN-LAST:event_imageTypeComboBoxActionPerformed
//
//    // Variables declaration - do not modify//GEN-BEGIN:variables
//    private JButton browseImageButton;
//    private JButton browseInfofileButton;
//    private JButton browseSumfileButton;
//    private JButton cancelButton;
//    private JRadioButton cylindricalProjectionRadioButton;
//    private JComboBox imageFlipComboBox;
//    private JLabel imageFlipLabel;
//    private JLabel imageLabel;
//    private JTextField imageNameTextField;
//    private JLabel imagePathLabel;
//    private JTextField imagePathTextField;
//    private JComboBox imageRotateComboBox;
//    private JLabel imageRotateLabel;
//    private JComboBox imageTypeComboBox;
//    private JLabel imageTypeLabel;
//    private JRadioButton infofilePathRB;
//    private JTextField infofilePathTextField;
//    private JPanel jPanel1;
//    private JFormattedTextField lllatFormattedTextField;
//    private JLabel lllatLabel;
//    private JFormattedTextField lllonFormattedTextField;
//    private JLabel lllonLabel;
//    private JButton okButton;
//    private JRadioButton perspectiveProjectionRadioButton;
//    private ButtonGroup projectionButtonGroup;
//    private JRadioButton sumfilePathRB;
//    private JTextField sumfilePathTextField;
//    private JFormattedTextField urlatFormattedTextField;
//    private JLabel urlatLabel;
//    private JFormattedTextField urlonFormattedTextField;
//    private JLabel urlonLabel;
//    private JLabel nameExistsLabel;
//    private JLabel selectImageTypeLabel;
//    // End of variables declaration//GEN-END:variables
//}
