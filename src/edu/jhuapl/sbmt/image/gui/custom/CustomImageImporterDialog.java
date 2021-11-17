/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ShapeModelImporterDialog.java
 *
 * Created on Jul 21, 2011, 9:00:24 PM
 */
package edu.jhuapl.sbmt.image.gui.custom;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.image.common.IImagingInstrument;
import edu.jhuapl.sbmt.image.common.ImageType;
import edu.jhuapl.sbmt.image.types.customImage.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.image.types.customImage.CustomImageKeyInterface;
import edu.jhuapl.sbmt.image.types.customImage.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.util.VtkENVIReader;


public class CustomImageImporterDialog extends javax.swing.JDialog
{
    private boolean okayPressed = false;
    private boolean isEllipsoid;
    private boolean isEditMode;
    private IImagingInstrument instrument;
    private static final String LEAVE_UNMODIFIED = "<cannot be changed>";
    private List<String> currentNames;
    private String originalName;

    public enum ProjectionType
    {
        CYLINDRICAL,
        PERSPECTIVE
    }

    /** Creates new form ShapeModelImporterDialog */
    public CustomImageImporterDialog(Window parent, boolean isEditMode, IImagingInstrument instrument)
    {
        super(parent, isEditMode ? "Edit Image" : "Import New Image", Dialog.ModalityType.APPLICATION_MODAL);
        initComponents();
        this.isEditMode = isEditMode;
        this.instrument = instrument;

        if (isEditMode)
        {
            browseImageButton.setEnabled(false);
            browseSumfileButton.setEnabled(false);
            browseInfofileButton.setEnabled(false);
            imagePathTextField.setEnabled(false);
            infofilePathTextField.setEnabled(false);
            sumfilePathTextField.setEnabled(false);
        }
        updateEnabledItems();
    }

    public void setImageInfo(CustomImageKeyInterface info, boolean isEllipsoid)
    {
    	String keyName = info == null ? "" : info.getName();
    	String keyImageFilename = info == null ? "" : info.getImageFilename();
    	ProjectionType projection = info == null ? ProjectionType.CYLINDRICAL : info.getProjectionType();
    	ImageType currentImageType = info == null ? ImageType.GENERIC_IMAGE : info.getImageType();
    	originalName = info == null ? "" : info.getOriginalName();

        this.isEllipsoid = isEllipsoid;

        if (isEditMode)
            imagePathTextField.setText(LEAVE_UNMODIFIED);
        else
            imagePathTextField.setText(keyImageFilename);

        imageNameTextField.setText(keyName);

        if (projection == ProjectionType.CYLINDRICAL)
        {
            cylindricalProjectionRadioButton.setSelected(true);

            double lllat = info == null ?  -90.0 : ((CustomCylindricalImageKey)info).getLllat();
            double lllon = info == null ?  0.0 : ((CustomCylindricalImageKey)info).getLllon();
            double urlat = info == null ?  90.0 : ((CustomCylindricalImageKey)info).getUrlat();
            double urlon = info == null ?  360.0 : ((CustomCylindricalImageKey)info).getUrlon();
            lllatFormattedTextField.setText(String.valueOf(lllat));
            lllonFormattedTextField.setText(String.valueOf(lllon));
            urlatFormattedTextField.setText(String.valueOf(urlat));
            urlonFormattedTextField.setText(String.valueOf(urlon));
        }
        else if (projection == ProjectionType.PERSPECTIVE)
        {
            perspectiveProjectionRadioButton.setSelected(true);

            if (isEditMode)
            {
                sumfilePathTextField.setText(LEAVE_UNMODIFIED);
                infofilePathTextField.setText(LEAVE_UNMODIFIED);
                if (info.getFileType() == FileType.SUM)
                {
                	sumfilePathRB.setSelected(true);
                }
            }
            double rotation = info == null ?  0.0 : info.getRotation();
            String flip = info == null ?  "None" : info.getFlip();
            imageFlipComboBox.setSelectedItem(flip);
            imageRotateComboBox.setSelectedItem(Integer.toString((int)rotation));
        }

        if (keyImageFilename.toUpperCase().endsWith(".FITS") || keyImageFilename.toUpperCase().endsWith(".FIT"))
        {
        	DefaultComboBoxModel model = new DefaultComboBoxModel(ImageType.values());
        	model.insertElementAt("<CHOOSE IMAGE TYPE>", 0);
            imageTypeComboBox.setModel(model);
            imageTypeComboBox.setSelectedItem(currentImageType);
        }
        else
            imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));


        imageTypeComboBox.setSelectedItem(currentImageType);


        updateEnabledItems();
    }

    private ProjectionType getSelectedProjectionType()
    {
        if (cylindricalProjectionRadioButton.isSelected())
            return ProjectionType.CYLINDRICAL;
        else
            return ProjectionType.PERSPECTIVE;
    }

    public CustomImageKeyInterface getImageInfo()
    {
    	String imagefilename = imagePathTextField.getText();
        if (LEAVE_UNMODIFIED.equals(imagefilename) || imagefilename == null || imagefilename.isEmpty())
            imagefilename = null;
        else
            originalName = new File(imagefilename).getName();

        // If name is not provided, set name to filename
        ImageType imageType = (ImageType)imageTypeComboBox.getSelectedItem();
        String name = imageNameTextField.getText();
        if ((name == null || name.isEmpty()) && imagefilename != null)
            name = new File(imagefilename).getName();

        if (cylindricalProjectionRadioButton.isSelected())
        {
            CustomCylindricalImageKey key = new CustomCylindricalImageKey(name, imagefilename, imageType, ImageSource.LOCAL_CYLINDRICAL, new Date(), originalName);
            key.setLllat(Double.parseDouble(lllatFormattedTextField.getText()));
            key.setLllon(Double.parseDouble(lllonFormattedTextField.getText()));
            key.setUrlat(Double.parseDouble(urlatFormattedTextField.getText()));
            key.setUrlon(Double.parseDouble(urlonFormattedTextField.getText()));
            return key;
        }
        else
        {
        	String pointingFilename = null;
            FileType fileType = null;
            ImageSource source = null;
            if (sumfilePathRB.isSelected() == true)
            {
            	fileType = FileType.SUM;
            	pointingFilename = sumfilePathTextField.getText();
            	source = ImageSource.LOCAL_PERSPECTIVE;
            }
            if (infofilePathRB.isSelected() == true)
            {
            	pointingFilename = infofilePathTextField.getText();
            	fileType = FileType.INFO;
            	source = ImageSource.LOCAL_PERSPECTIVE;
            }
            if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
            	pointingFilename = null;
            if (LEAVE_UNMODIFIED.equals(pointingFilename) || pointingFilename == null || pointingFilename.isEmpty())
            	pointingFilename = null;

            double rotation = imageRotateComboBox.getSelectedIndex() * 90.0;
            String flip = imageFlipComboBox.getSelectedItem().toString();

            CustomPerspectiveImageKey key = new CustomPerspectiveImageKey(name, imagefilename, source, imageType, instrument, rotation, flip, fileType, pointingFilename, new Date(), originalName);
            return key;
        }
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
        if (!isEditMode && currentNames.contains(imageName))
        {
        	return "Name for custom image already exists.";
        }

        if (imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"))
        {
        	return "Select an image type.";
        }


        if (cylindricalProjectionRadioButton.isSelected())
        {
            if (!isEditMode) // (!imagePath.isEmpty() && !imagePath.equals(LEAVE_UNMODIFIED))
            {
                // Check first to see if it is a natively supported image
                boolean supportedCustomFormat = false;

                // Check if this image is any of the custom supported formats
                if(VtkENVIReader.isENVIFilename(imagePath))
                {
                    // Both header and binary files must exist
                    if(VtkENVIReader.checkFilesExist(imagePath))
                    {
                        // SBMT supports ENVI
                        supportedCustomFormat = true;
                    }
                    else
                    {
                        // Error message
                        return "Was not able to locate a corresponding .hdr file for ENVI image binary";
                    }
                }

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
                double lllat = Double.parseDouble(lllatFormattedTextField.getText());
                double urlat = Double.parseDouble(urlatFormattedTextField.getText());
                Double.parseDouble(lllonFormattedTextField.getText());
                Double.parseDouble(urlonFormattedTextField.getText());

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
        else
        {
            String sumfilePath = sumfilePathTextField.getText();
            if (sumfilePathRB.isSelected() == false || sumfilePath == null)
                sumfilePath = "";

            String infofilePath = infofilePathTextField.getText();
            if (infofilePathRB.isSelected() == false || infofilePath == null)
                infofilePath = "";

            if (!isEditMode || (!sumfilePath.isEmpty() && !sumfilePath.equals(LEAVE_UNMODIFIED) || (!infofilePath.isEmpty() && !infofilePath.equals(LEAVE_UNMODIFIED))))
            {
                if (infofilePathRB.isSelected() == true && infofilePath.isEmpty() == true)
                    return "Please enter the path to an infofile.";

                if (sumfilePathRB.isSelected() == true && sumfilePath.isEmpty() == true)
                    return "Please enter the path to a sumfile.";

                if (!sumfilePath.isEmpty())
                {
                    File file = new File(sumfilePath);
                    if (!file.exists() || !file.canRead() || !file.isFile())
                        return sumfilePath + " does not exist or is not readable.";

                    if (sumfilePath.contains(","))
                        return "Path may not contain commas.";
                }
                else if (!infofilePath.isEmpty())
                {
                    File file = new File(infofilePath);
                    if (!file.exists() || !file.canRead() || !file.isFile())
                        return infofilePath + " does not exist or is not readable.";

                    if (infofilePath.contains(","))
                        return "Path may not contain commas.";
                }
            }
        }

        return null;
    }

    public boolean getOkayPressed()
    {
        return okayPressed;
    }

    public void setCurrentImageNames(List<String> currentNames)
    {
    	this.currentNames = currentNames;
    }

    private void checkCurrentNames()
    {
    	String inputName = imageNameTextField.getText();
    	boolean exists = currentNames.contains(inputName.trim());
    	if (!isEditMode)
    	{
    		nameExistsLabel.setVisible(exists);
    		okButton.setEnabled(!exists);
    	}
    }

    private void checkImageType()
    {
    	boolean typeSet = !(imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"));
    	selectImageTypeLabel.setVisible(!typeSet);
		okButton.setEnabled(typeSet);
    }

    private void updateEnabledItems()
    {
        boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
        lllatLabel.setEnabled(cylindrical);
        lllatFormattedTextField.setEnabled(cylindrical);
        lllonLabel.setEnabled(cylindrical);
        lllonFormattedTextField.setEnabled(cylindrical);
        urlatLabel.setEnabled(cylindrical);
        urlatFormattedTextField.setEnabled(cylindrical);
        urlonLabel.setEnabled(cylindrical);
        urlonFormattedTextField.setEnabled(cylindrical);
        infofilePathRB.setEnabled(!cylindrical);
        browseInfofileButton.setEnabled(!cylindrical && infofilePathRB.isSelected());
        infofilePathTextField.setEnabled(!cylindrical && !isEditMode && infofilePathRB.isSelected());
        sumfilePathRB.setEnabled(!cylindrical);
        browseSumfileButton.setEnabled(!cylindrical && sumfilePathRB.isSelected());
        sumfilePathTextField.setEnabled(!cylindrical && !isEditMode && sumfilePathRB.isSelected());

        boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
        imageFlipComboBox.setEnabled(!cylindrical);
        imageRotateComboBox.setEnabled(!cylindrical);

        selectImageTypeLabel.setVisible(imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE IMAGE TYPE>"));
    }

    /**
     * Installs a listener to receive notification when the text of any
     * {@code JTextComponent} is changed. Internally, it installs a
     * {@link DocumentListener} on the text component's {@link Document},
     * and a {@link PropertyChangeListener} on the text component to detect
     * if the {@code Document} itself is replaced.
     *
     * @param text any text component, such as a {@link JTextField}
     *        or {@link JTextArea}
     * @param changeListener a listener to receieve {@link ChangeEvent}s
     *        when the text is changed; the source object for the events
     *        will be the text component
     * @throws NullPointerException if either parameter is null
     */
    public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0, lastNotifiedChange = 0;

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document)e.getOldValue();
            Document d2 = (Document)e.getNewValue();
            if (d1 != null) d1.removeDocumentListener(dl);
            if (d2 != null) d2.addDocumentListener(dl);
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) d.addDocumentListener(dl);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectionButtonGroup = new ButtonGroup();
        imagePathLabel = new JLabel();
        imagePathTextField = new JTextField();
        browseImageButton = new JButton();
        lllatLabel = new JLabel();
        lllonLabel = new JLabel();
        urlatLabel = new JLabel();
        urlonLabel = new JLabel();
        jPanel1 = new JPanel();
        cancelButton = new JButton();
        okButton = new JButton();
        lllatFormattedTextField = new JFormattedTextField();
        lllonFormattedTextField = new JFormattedTextField();
        urlatFormattedTextField = new JFormattedTextField();
        urlonFormattedTextField = new JFormattedTextField();
        cylindricalProjectionRadioButton = new JRadioButton();
        perspectiveProjectionRadioButton = new JRadioButton();
        infofilePathRB = new JRadioButton("Infofile Path", true);
        browseInfofileButton = new JButton();
        imageLabel = new JLabel();
        imageNameTextField = new JTextField();
        nameExistsLabel = new JLabel();
        selectImageTypeLabel = new JLabel();
        sumfilePathRB = new JRadioButton();
        infofilePathTextField = new JTextField();
        sumfilePathTextField = new JTextField();
        browseSumfileButton = new JButton();
        imageTypeLabel = new JLabel();
        imageTypeComboBox = new JComboBox();
        imageRotateLabel = new JLabel();
        imageFlipLabel = new JLabel();
        imageRotateComboBox = new JComboBox();
        imageFlipComboBox = new JComboBox();
        ButtonGroup tmpBG = new ButtonGroup();
        tmpBG.add(infofilePathRB);
        tmpBG.add(sumfilePathRB);

        addChangeListener(imageNameTextField, e -> { checkCurrentNames(); checkImageType(); });

        imageTypeComboBox.addActionListener(e -> { checkImageType(); checkCurrentNames(); } );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 167));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        imagePathLabel.setText("Image Path");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        getContentPane().add(imagePathLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
        getContentPane().add(imagePathTextField, gridBagConstraints);

        browseImageButton.setText("Browse...");
        browseImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseImageButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 5);
        getContentPane().add(browseImageButton, gridBagConstraints);

        lllatLabel.setText("Lower Left Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllatLabel, gridBagConstraints);

        lllonLabel.setText("Lower Left Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(lllonLabel, gridBagConstraints);

        urlatLabel.setText("Upper Right Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(urlatLabel, gridBagConstraints);

        urlonLabel.setText("Upper Right Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(urlonLabel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        jPanel1.add(cancelButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(okButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        lllatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        lllatFormattedTextField.setText("-90");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllatFormattedTextField, gridBagConstraints);

        lllonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        lllonFormattedTextField.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(lllonFormattedTextField, gridBagConstraints);

        urlatFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        urlatFormattedTextField.setText("90");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlatFormattedTextField, gridBagConstraints);

        urlonFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.########"))));
        urlonFormattedTextField.setText("360");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 60;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(urlonFormattedTextField, gridBagConstraints);

        projectionButtonGroup.add(cylindricalProjectionRadioButton);
        cylindricalProjectionRadioButton.setSelected(true);
        cylindricalProjectionRadioButton.setText("Simple Cylindrical Projection");
        cylindricalProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cylindricalProjectionRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
        getContentPane().add(cylindricalProjectionRadioButton, gridBagConstraints);

        projectionButtonGroup.add(perspectiveProjectionRadioButton);
        perspectiveProjectionRadioButton.setText("Perspective Projection");
        perspectiveProjectionRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                perspectiveProjectionRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 4, 0);
        getContentPane().add(perspectiveProjectionRadioButton, gridBagConstraints);

        infofilePathRB.setText("Infofile Path");
        infofilePathRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEnabledItems();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(infofilePathRB, gridBagConstraints);

        browseInfofileButton.setText("Browse...");
        browseInfofileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseInfofileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseInfofileButton, gridBagConstraints);

        imageLabel.setText("Name");
        imageLabel.setToolTipText("A name describing the image that will be displayed in the image list.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageLabel, gridBagConstraints);

        imageNameTextField.setToolTipText("A name describing the image that will be displayed in the image list.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 400;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 5, 4, 0);
        getContentPane().add(imageNameTextField, gridBagConstraints);

        nameExistsLabel.setText("Already Exists");
        nameExistsLabel.setForeground(Color.red);
        nameExistsLabel.setVisible(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(nameExistsLabel, gridBagConstraints);


        sumfilePathRB.setText("Sumfile Path");
        sumfilePathRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateEnabledItems();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        getContentPane().add(sumfilePathRB, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(infofilePathTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 0);
        getContentPane().add(sumfilePathTextField, gridBagConstraints);

        browseSumfileButton.setText("Browse...");
        browseSumfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseSumfileButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 4, 5);
        getContentPane().add(browseSumfileButton, gridBagConstraints);

        imageTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        imageTypeLabel.setText("Image Type");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageTypeLabel, gridBagConstraints);

        imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
        imageTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageTypeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageTypeComboBox, gridBagConstraints);

        selectImageTypeLabel.setText("Select an Image Type");
        selectImageTypeLabel.setForeground(Color.red);
        selectImageTypeLabel.setVisible(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        getContentPane().add(selectImageTypeLabel, gridBagConstraints);

        imageRotateLabel.setText("Image Rotate");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageRotateLabel, gridBagConstraints);

        imageFlipLabel.setText("Image Flip");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageFlipLabel, gridBagConstraints);

        imageRotateComboBox.setModel(new DefaultComboBoxModel(new String[] { "0", "90", "180", "270" }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageRotateComboBox, gridBagConstraints);

        imageFlipComboBox.setModel(new DefaultComboBoxModel(new String[] { "None", "X", "Y" }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        getContentPane().add(imageFlipComboBox, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browseImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseImageButtonActionPerformed
    {//GEN-HEADEREND:event_browseImageButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Image");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        imagePathTextField.setText(filename);

        String imageFileName = file.getName();
        if (imageFileName.toUpperCase().endsWith(".FITS") || imageFileName.toUpperCase().endsWith(".FIT"))
        {
            ImageType[] allImageTypes = ImageType.values();
            ImageType currentImageType = instrument != null ? instrument.getType() : ImageType.GENERIC_IMAGE;
            DefaultComboBoxModel model = new DefaultComboBoxModel(allImageTypes);
            model.insertElementAt("<CHOOSE IMAGE TYPE>", 0);
            imageTypeComboBox.setModel(model);
            imageTypeComboBox.setSelectedIndex(0);
//            imageTypeComboBox.setSelectedItem(currentImageType);

            boolean cylindrical = cylindricalProjectionRadioButton.isSelected();
            boolean generic = imageTypeComboBox.getSelectedItem() == ImageType.GENERIC_IMAGE;
            imageFlipComboBox.setEnabled(generic && !cylindrical);
            imageRotateComboBox.setEnabled(generic && !cylindrical);
        }
        else
        {
            imageTypeComboBox.setModel(new DefaultComboBoxModel(new ImageType[] { ImageType.GENERIC_IMAGE }));
        }

        imageNameTextField.setText(imageFileName);

        // set default info file name
//        String tokens[] = imageFileName.split("\\.");
//        int ntokens = tokens.length;
//        String suffix = tokens[ntokens-1];
//        int suffixLength = suffix.length();
//        String imageFileNamePrefix = imageFileName.substring(0, imageFileName.length() - suffixLength);
        String imageFileNamePrefix = FilenameUtils.getBaseName(imageFileName);
        String defaultInfoFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + ".INFO";
        String defaultSumFileName = file.getParent() + System.getProperty("file.separator") + imageFileNamePrefix + ".SUM";
        infofilePathTextField.setText(defaultInfoFileName);
        sumfilePathTextField.setText(defaultSumFileName);

        updateEnabledItems();
    }//GEN-LAST:event_browseImageButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        String errorString = validateInput();
        if (errorString != null)
        {
            JOptionPane.showMessageDialog(this,
                    errorString,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        okayPressed = true;
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cylindricalProjectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cylindricalProjectionRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_cylindricalProjectionRadioButtonActionPerformed

    private void perspectiveProjectionRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_perspectiveProjectionRadioButtonActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_perspectiveProjectionRadioButtonActionPerformed

    private void browseInfofileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseInfofileButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Infofile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        infofilePathTextField.setText(filename);
    }//GEN-LAST:event_browseInfofileButtonActionPerformed

    private void browseSumfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSumfileButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select Sumfile");
        if (file == null)
        {
            return;
        }

        String filename = file.getAbsolutePath();
        sumfilePathTextField.setText(filename);
    }//GEN-LAST:event_browseSumfileButtonActionPerformed

    private void imageTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageTypeComboBoxActionPerformed
        updateEnabledItems();
    }//GEN-LAST:event_imageTypeComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton browseImageButton;
    private JButton browseInfofileButton;
    private JButton browseSumfileButton;
    private JButton cancelButton;
    private JRadioButton cylindricalProjectionRadioButton;
    private JComboBox imageFlipComboBox;
    private JLabel imageFlipLabel;
    private JLabel imageLabel;
    private JTextField imageNameTextField;
    private JLabel imagePathLabel;
    private JTextField imagePathTextField;
    private JComboBox imageRotateComboBox;
    private JLabel imageRotateLabel;
    private JComboBox imageTypeComboBox;
    private JLabel imageTypeLabel;
    private JRadioButton infofilePathRB;
    private JTextField infofilePathTextField;
    private JPanel jPanel1;
    private JFormattedTextField lllatFormattedTextField;
    private JLabel lllatLabel;
    private JFormattedTextField lllonFormattedTextField;
    private JLabel lllonLabel;
    private JButton okButton;
    private JRadioButton perspectiveProjectionRadioButton;
    private ButtonGroup projectionButtonGroup;
    private JRadioButton sumfilePathRB;
    private JTextField sumfilePathTextField;
    private JFormattedTextField urlatFormattedTextField;
    private JLabel urlatLabel;
    private JFormattedTextField urlonFormattedTextField;
    private JLabel urlonLabel;
    private JLabel nameExistsLabel;
    private JLabel selectImageTypeLabel;
    // End of variables declaration//GEN-END:variables
}
