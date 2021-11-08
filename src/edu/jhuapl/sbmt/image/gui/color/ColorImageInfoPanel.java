/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageInfoPanel2.java
 *
 * Created on May 30, 2011, 12:24:26 PM
 */
package edu.jhuapl.sbmt.image.gui.color;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.table.DefaultTableModel;

import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkInteractorStyleImage;
import vtk.vtkPropCollection;
import vtk.vtkPropPicker;
import vtk.vtkTransform;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.status.LegacyStatusHandler;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImageCollection;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.Chromatism;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;


public class ColorImageInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private ColorImage image;
    private ColorImageCollection imageCollection;

    private vtkJoglPanelComponent renWin;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private vtkImageSlice actor;
    private vtkImageReslice reslice;
    private vtkPropPicker imagePicker;
    private boolean initialized = false;
    private LegacyStatusHandler refStatusHandler;

    private class MouseListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            int pickSucceeded = doPick(e, imagePicker, renWin);
            if (pickSucceeded == 1)
            {
                double[] p = imagePicker.GetPickPosition();
                // Note we reverse x and y so that the pixel is in the form the camera
                // position/orientation program expects.
                System.out.println(p[1] + " " + p[0]);

                // Display status bar message upon being picked
                refStatusHandler.setLeftTextSource(image, null, 0, p);
            }
        }
    }
    /** Creates new form ImageInfoPanel2 */
    public ColorImageInfoPanel(
            final ColorImage image,
            ColorImageCollection imageCollection,
            LegacyStatusHandler aStatusHandler)
    {
        initComponents();

        this.image = image;
        this.imageCollection = imageCollection;
        refStatusHandler = aStatusHandler;

        renWin = new vtkJoglPanelComponent();
        renWin.getComponent().setPreferredSize(new Dimension(550, 550));

        vtkInteractorStyleImage style = new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        vtkImageData displayedImage = image.getTexture().GetInput();

        // Only allow contrast changing for images with exactly 1 channel
        if (image.getNumberOfComponentsOfOriginalImage() > 1)
        {
            redSlider.setEnabled(false);
            greenSlider.setEnabled(false);
            blueSlider.setEnabled(false);
            jLabel4.setEnabled(false);
            jLabel5.setEnabled(false);
            jLabel9.setEnabled(false);
        }

        int[] masking = image.getCurrentMask();
        leftSpinner.setValue(masking[0]);
        topSpinner.setValue(masking[1]);
        rightSpinner.setValue(masking[2]);
        bottomSpinner.setValue(masking[3]);


        double[] center = displayedImage.GetCenter();
        int[] dims = displayedImage.GetDimensions();

        // Rotate image by 90 degrees so it appears the same way as when you
        // use the Center in Image option.
        vtkTransform imageTransform = new vtkTransform();
        imageTransform.Translate(center[0], center[1], 0.0);
        imageTransform.RotateZ(-90.0);
        imageTransform.Translate(-center[1], -center[0], 0.0);

        reslice = new vtkImageReslice();
        reslice.SetInputData(displayedImage);
        reslice.SetResliceTransform(imageTransform);
        reslice.SetInterpolationModeToNearestNeighbor();
        reslice.SetOutputSpacing(1.0, 1.0, 1.0);
        reslice.SetOutputOrigin(0.0, 0.0, 0.0);
        reslice.SetOutputExtent(0, dims[1]-1, 0, dims[0]-1, 0, 0);
        reslice.Update();

        vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
        imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
        imageSliceMapper.Update();

        actor = new vtkImageSlice();
        actor.SetMapper(imageSliceMapper);
        actor.GetProperty().SetInterpolationTypeToLinear();

        renWin.getRenderer().AddActor(actor);

        renWin.setSize(550, 550);

        imagePicker = new vtkPropPicker();
        imagePicker.PickFromListOn();
        imagePicker.InitializePickList();
        vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        imagePicker.AddPickList(actor);
        renWin.getComponent().addMouseListener(new ColorImageInfoPanel.MouseListener());

        // Trying to add a vtkEnhancedRenderWindowPanel in the netbeans gui
        // does not seem to work so instead add it here.
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(renWin.getComponent(), gridBagConstraints);

        // Add a text box for showing information about the image
        String[] columnNames = {"Property", "Value"};

        LinkedHashMap<String, String> properties = null;
        Object[][] data = { {"", ""} };
        try
        {
            properties = image.getProperties();
            int size = properties.size();
            data = new Object[size][2];

            int i=0;
            for (String key : properties.keySet())
            {
                data[i][0] = key;
                data[i][1] = properties.get(key);

                ++i;
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }


        DefaultTableModel model = new DefaultTableModel(data, columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        table1.setModel(model);

//        createMenus();

        // Finally make the frame visible
        String name = new File(image.getImageName()).getName();
        setTitle("Color Image " + name + " Properties");

        pack();
        setVisible(true);

        initialized = true;

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                renWin.resetCamera();
                renWin.Render();
            }
        });
    }


//    private void createMenus()
//    {
//        JMenuBar menuBar = new JMenuBar();
//
//        JMenu fileMenu = new JMenu("File");
//        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                File file = ImageFileChooser.showSaveDialog(renWin, "Export to Image...");
//                renWin.saveToFile(file);
//            }
//        });
//        fileMenu.add(mi);
//        fileMenu.setMnemonic('F');
//        menuBar.add(fileMenu);
//
//        /**
//         * The following is a bit of a hack. We want to reuse the PopupMenu
//         * class, but instead of having a right-click popup menu, we want instead to use
//         * it as an actual menu in a menu bar. Therefore we simply grab the menu items
//         * from that class and put these in our new JMenu.
//         */
//        ImagePopupMenu imagesPopupMenu =
//            new ImagePopupMenu(imageCollection, imageBoundaryCollection, null, null, this);
//
//        imagesPopupMenu.setCurrentImage(image.getKey());
//
//        JMenu menu = new JMenu("Options");
//        menu.setMnemonic('O');
//
//        Component[] components = imagesPopupMenu.getComponents();
//        for (Component item : components)
//        {
//            if (item instanceof JMenuItem)
//            {
//                // Do not show the "Show Image" option since that creates problems
//                // since it's supposed to close this window also.
//                if (!(((JMenuItem)item).getAction() instanceof ImagePopupMenu.MapImageAction))
//                    menu.add(item);
//            }
//        }
//
//        menuBar.add(menu);
//
//        setJMenuBar(menuBar);
//    }

    public Model getModel()
    {
        return image;
    }

    public Model getCollectionModel()
    {
        return imageCollection;
    }

    private void croppingChanged()
    {
        if (!initialized)
            return;

        Integer top = (Integer) leftSpinner.getValue();
        Integer right = (Integer) topSpinner.getValue();
        Integer bottom = (Integer) rightSpinner.getValue();
        Integer left = (Integer) bottomSpinner.getValue();

        int[] masking = {top, right, bottom, left};

        image.setCurrentMask(masking);
    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;
        renWin.Render();
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

        jPanel1 = new javax.swing.JPanel();
        blueSlider = new com.jidesoft.swing.RangeSlider();
        redSlider = new com.jidesoft.swing.RangeSlider();
        jLabel6 = new javax.swing.JLabel();
        greenScaleSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        redScaleSlider = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        greenMonoCheckbox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        greenSlider = new com.jidesoft.swing.RangeSlider();
        blueMonoCheckbox = new javax.swing.JCheckBox();
        blueScaleSlider = new javax.swing.JSlider();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        redMonoCheckbox = new javax.swing.JCheckBox();
        interpolateCheckBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        table1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        leftSpinner = new javax.swing.JSpinner();
        bottomSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        rightSpinner = new javax.swing.JSpinner();
        topSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        blueSlider.setMajorTickSpacing(10);
        blueSlider.setMaximum(255);
        blueSlider.setPaintTicks(true);
        blueSlider.setHighValue(255);
        blueSlider.setLowValue(0);
        blueSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blueSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(blueSlider, gridBagConstraints);

        redSlider.setMajorTickSpacing(10);
        redSlider.setMaximum(255);
        redSlider.setPaintTicks(true);
        redSlider.setHighValue(255);
        redSlider.setLowValue(0);
        redSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(redSlider, gridBagConstraints);

        jLabel6.setText("Red:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel1.add(jLabel6, gridBagConstraints);

        greenScaleSlider.setMajorTickSpacing(50);
        greenScaleSlider.setMinorTickSpacing(10);
        greenScaleSlider.setPaintTicks(true);
        greenScaleSlider.setValue(100);
        greenScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                greenScaleSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(greenScaleSlider, gridBagConstraints);

        jLabel3.setText("Blue");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        redScaleSlider.setMajorTickSpacing(50);
        redScaleSlider.setMinorTickSpacing(10);
        redScaleSlider.setPaintTicks(true);
        redScaleSlider.setValue(100);
        redScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redScaleSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(redScaleSlider, gridBagConstraints);

        jLabel4.setText("Mono");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel4, gridBagConstraints);

        greenMonoCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenMonoCheckboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(greenMonoCheckbox, gridBagConstraints);

        jLabel2.setText("Green:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        greenSlider.setMajorTickSpacing(10);
        greenSlider.setMaximum(255);
        greenSlider.setPaintTicks(true);
        greenSlider.setHighValue(255);
        greenSlider.setLowValue(0);
        greenSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                greenSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        jPanel1.add(greenSlider, gridBagConstraints);

        blueMonoCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueMonoCheckboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(blueMonoCheckbox, gridBagConstraints);

        blueScaleSlider.setMajorTickSpacing(50);
        blueScaleSlider.setMinorTickSpacing(10);
        blueScaleSlider.setPaintTicks(true);
        blueScaleSlider.setValue(100);
        blueScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blueScaleSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(blueScaleSlider, gridBagConstraints);

        jLabel9.setText("Contrast");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel9, gridBagConstraints);

        jLabel5.setText("Scale");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabel5, gridBagConstraints);

        redMonoCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redMonoCheckboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(redMonoCheckbox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        interpolateCheckBox.setSelected(true);
        interpolateCheckBox.setText("Interpolate Image");
        interpolateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpolateCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
        getContentPane().add(interpolateCheckBox, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(452, 200));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(452, 200));

        jScrollPane2.setViewportView(table1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jScrollPane2, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        leftSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        leftSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        leftSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                leftSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(leftSpinner, gridBagConstraints);

        bottomSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        bottomSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        bottomSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bottomSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(bottomSpinner, gridBagConstraints);

        jLabel7.setText("Left");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel7, gridBagConstraints);

        rightSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        rightSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        rightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rightSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(rightSpinner, gridBagConstraints);

        topSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        topSpinner.setPreferredSize(new java.awt.Dimension(60, 28));
        topSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                topSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        jPanel2.add(topSpinner, gridBagConstraints);

        jLabel8.setText("Bottom");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel8, gridBagConstraints);

        jLabel10.setText("Top");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel10, gridBagConstraints);

        jLabel11.setText("Right");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel2.add(jLabel11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void redSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {//GEN-FIRST:event_redSliderStateChanged
        if (redSlider.getValueIsAdjusting())
            return;

        adjustContrast();
    }//GEN-LAST:event_redSliderStateChanged

    private void greenSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {//GEN-FIRST:event_greenSliderStateChanged
        if (greenSlider.getValueIsAdjusting())
            return;

        adjustContrast();
    }//GEN-LAST:event_greenSliderStateChanged

    private void blueSliderStateChanged(javax.swing.event.ChangeEvent evt)
    {//GEN-FIRST:event_blueSliderStateChanged
        if (blueSlider.getValueIsAdjusting())
            return;

        adjustContrast();
    }//GEN-LAST:event_blueSliderStateChanged

    private void greenMonoCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_greenMonoCheckboxActionPerformed
        blueMonoCheckbox.setSelected(false);
        redMonoCheckbox.setSelected(false);
        adjustContrast();
    }//GEN-LAST:event_greenMonoCheckboxActionPerformed

    private void blueMonoCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blueMonoCheckboxActionPerformed
        greenMonoCheckbox.setSelected(false);
        redMonoCheckbox.setSelected(false);
        adjustContrast();
    }//GEN-LAST:event_blueMonoCheckboxActionPerformed

    private void redMonoCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redMonoCheckboxActionPerformed
        blueMonoCheckbox.setSelected(false);
        greenMonoCheckbox.setSelected(false);
        adjustContrast();
    }//GEN-LAST:event_redMonoCheckboxActionPerformed

    private void interpolateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpolateCheckBoxActionPerformed
        image.setInterpolate(interpolateCheckBox.isSelected());
        if (interpolateCheckBox.isSelected())
            actor.GetProperty().SetInterpolationTypeToLinear();
        else
            actor.GetProperty().SetInterpolationTypeToNearest();
        renWin.Render();
    }//GEN-LAST:event_interpolateCheckBoxActionPerformed

    private void leftSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void bottomSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bottomSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_bottomSpinnerStateChanged

    private void rightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void topSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_topSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_topSpinnerStateChanged

    private void redScaleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redScaleSliderStateChanged
//        System.out.println("redScale: " + redScaleSlider.getValue());
       if (!redScaleSlider.getValueIsAdjusting())
           adjustContrast();
    }//GEN-LAST:event_redScaleSliderStateChanged

    private void greenScaleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_greenScaleSliderStateChanged
       if (!greenScaleSlider.getValueIsAdjusting())
           adjustContrast();
    }//GEN-LAST:event_greenScaleSliderStateChanged

    private void blueScaleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blueScaleSliderStateChanged
       if (!blueScaleSlider.getValueIsAdjusting())
           adjustContrast();
    }//GEN-LAST:event_blueScaleSliderStateChanged

    private void adjustContrast()
    {
        int redLowVal = redSlider.getLowValue();
        int redHighVal = redSlider.getHighValue();
        IntensityRange redRange = new IntensityRange(redLowVal, redHighVal);

        int greenLowVal = greenSlider.getLowValue();
        int greenHighVal = greenSlider.getHighValue();
        IntensityRange greenRange = new IntensityRange(greenLowVal, greenHighVal);

        int blueLowVal = blueSlider.getLowValue();
        int blueHighVal = blueSlider.getHighValue();
        IntensityRange blueRange = new IntensityRange(blueLowVal, blueHighVal);

        double redScale = redScaleSlider.getValue() / 100.0;
        double greenScale = greenScaleSlider.getValue() / 100.0;
        double blueScale = blueScaleSlider.getValue() / 100.0;

        Chromatism chromatism = Chromatism.POLY;
        if (redMonoCheckbox.isSelected())
            chromatism = Chromatism.MONO_RED;
        else if (greenMonoCheckbox.isSelected())
            chromatism = Chromatism.MONO_GREEN;
        else if (blueMonoCheckbox.isSelected())
            chromatism = Chromatism.MONO_BLUE;

        image.setDisplayedImageRange(redScale, redRange, greenScale, greenRange, blueScale, blueRange, chromatism);

        image.updateImageMask();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox blueMonoCheckbox;
    private javax.swing.JSlider blueScaleSlider;
    private com.jidesoft.swing.RangeSlider blueSlider;
    private javax.swing.JSpinner bottomSpinner;
    private javax.swing.JCheckBox greenMonoCheckbox;
    private javax.swing.JSlider greenScaleSlider;
    private com.jidesoft.swing.RangeSlider greenSlider;
    private javax.swing.JCheckBox interpolateCheckBox;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner leftSpinner;
    private javax.swing.JCheckBox redMonoCheckbox;
    private javax.swing.JSlider redScaleSlider;
    private com.jidesoft.swing.RangeSlider redSlider;
    private javax.swing.JSpinner rightSpinner;
    private javax.swing.JTable table1;
    private javax.swing.JSpinner topSpinner;
    // End of variables declaration//GEN-END:variables
}
