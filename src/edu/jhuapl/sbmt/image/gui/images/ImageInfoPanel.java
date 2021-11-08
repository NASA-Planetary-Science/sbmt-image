/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageInfoPanel2.java
 *
 * Created on May 30, 2011, 12:24:26 PM
 */
package edu.jhuapl.sbmt.image.gui.images;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.status.LegacyStatusHandler;
import edu.jhuapl.sbmt.image.controllers.images.ContrastSlider;
import edu.jhuapl.sbmt.image.controllers.images.OfflimbControlsController;
import edu.jhuapl.sbmt.image.core.Image;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;


public class ImageInfoPanel extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener //, KeyListener
{
    public static final double VIEWPOINT_DELTA = 1.0;
    public static final double ROTATION_DELTA = 5.0;

    private vtkJoglPanelComponent renWin;
    private Image image;
    private ImageCollection imageCollection;
    private PerspectiveImageBoundaryCollection imageBoundaryCollection;
    private vtkImageSlice actor;
    private vtkImageReslice reslice;
    private vtkPropPicker imagePicker;
    private boolean initialized = false;
    private boolean centerFrustumMode = false;
    private LegacyStatusHandler refStatusHandler;

    /** Creates new form ImageInfoPanel2 */
    public ImageInfoPanel(
            final Image image,
            ImageCollection imageCollection,
            PerspectiveImageBoundaryCollection imageBoundaryCollection,
            LegacyStatusHandler aStatusHandler)
    {
        this.image = image;

        initComponents();

        this.imageCollection = imageCollection;
        this.imageBoundaryCollection = imageBoundaryCollection;
        refStatusHandler = aStatusHandler;

        renWin = new vtkJoglPanelComponent();
        renWin.getComponent().setPreferredSize(new Dimension(550, 550));

        vtkInteractorStyleImage style =
            new vtkInteractorStyleImage();
        renWin.setInteractorStyle(style);

        renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent",this,"levelsChanged");


        vtkImageData displayedImage = image.getTexture().GetInput();

        // Only allow contrast changing for images with exactly 1 channel
//        if (image.getNumberOfComponentsOfOriginalImage() > 1)
//        {
//            slider.setEnabled(false);
//            jLabel1.setEnabled(false);
//        }

//        if (image instanceof PerspectiveImage)
//            applyAdjustmentsButton1.setSelected(((PerspectiveImage)image).getApplyFramedAdjustments());

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

// for testing backplane generation
//        {
//            int band = 10;
//            vtkImageData plane = new vtkImageData();
//            plane.DeepCopy(displayedImage);
//            float[] bp = image.generateBackplanes();
//            double minValue = Double.MAX_VALUE;
//            double maxValue = -Double.MAX_VALUE;
//            int pixelStart = image.index(0, 0, band);
//            int pixelEnd = image.index(0, 0, band+1);
//            for (int i=pixelStart; i<pixelEnd; ++i)
//            {
//                if (bp[i] == Image.PDS_NA) continue;
//                if (bp[i] < minValue) minValue = bp[i];
//                if (bp[i] > maxValue) maxValue = bp[i];
//            }
//            System.out.println("min band " + band + " : " + minValue);
//            System.out.println("max band " + band + " : " + maxValue);
//            for (int i=0; i<image.getImageHeight(); ++i)
//                for (int j=0; j<image.getImageWidth(); ++j)
//                {
//                    double v = bp[image.index(j, i, band)];
//                    if (v == Image.PDS_NA)
//                        v = minValue;
//                    else
//                        v = (v-minValue) * 255.0 / (maxValue - minValue);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 0, v);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 1, v);
//                    plane.SetScalarComponentFromFloat(j, i, 0, 2, v);
//                }
//            actor.SetInput(plane);
//        }

        renWin.getRenderer().AddActor(actor);

        renWin.setSize(550, 550);

        imagePicker = new vtkPropPicker();
        imagePicker.PickFromListOn();
        imagePicker.InitializePickList();
        vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        imagePicker.AddPickList(actor);
        renWin.getComponent().addMouseListener(this);
        renWin.getComponent().addMouseMotionListener(this);
//        renWin.addKeyListener(this);

        // Trying to add a vtksbmtJoglCanvasComponent in the netbeans gui
        // does not seem to work so instead add it here.
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
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

        table.setModel(model);

        createMenus();

        // Finally make the frame visible
        String name = new File(image.getImageName()).getName();
        if (image instanceof PerspectiveImage)
        {
            PerspectiveImage pimage = (PerspectiveImage)image;
            int depth = pimage.getImageDepth();
            if (depth > 1)
            {
                String band = pimage.getCurrentBand();
                name = band + ":" + name;
            }
        }
        setTitle("Image " + name + " Properties");

        pack();
        setVisible(true);

        initialized = true;

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

    int[] previousLevels=null;
	private JPanel offLimbPanel;
	private OfflimbControlsController offlimbController;

    private void levelsChanged()
    {
        // don't do anything for now; this seems to disable the auto-contrast functionality

/*        vtkInteractorStyleImage style=(vtkInteractorStyleImage)renWin.getRenderWindowInteractor().GetInteractorStyle();
        int[] currentLevels=style.GetWindowLevelCurrentPosition();
        if (previousLevels==null)
            previousLevels=currentLevels;
        int dBrightness=currentLevels[1]-previousLevels[1];
        int dContrast=currentLevels[0]-previousLevels[0];
        int sliderChange=(int)((double)(slider.getMaximum()-slider.getMinimum())*(double)dContrast/(double)renWin.getComponent().getWidth());
        System.out.println(dContrast+" "+sliderChange);
        slider.setHighValue(slider.getHighValue()+sliderChange);
        //
        int lowVal = slider.getLowValue();
        int highVal = slider.getHighValue();
        if (image != null)
            image.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
        previousLevels=currentLevels;*/
    }

    private void createMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...", "image.png", "png");
                RenderIoUtil.saveToFile(file, renWin, null);
            }
        });
        fileMenu.add(mi);
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        /**
         * The following is a bit of a hack. We want to reuse the PopupMenu
         * class, but instead of having a right-click popup menu, we want instead to use
         * it as an actual menu in a menu bar. Therefore we simply grab the menu items
         * from that class and put these in our new JMenu.
         */
        ImagePopupMenu imagesPopupMenu =
            new ImagePopupMenu(null, imageCollection, imageBoundaryCollection, null, null, null, this);

        imagesPopupMenu.setCurrentImage(image.getKey());

        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        Component[] components = imagesPopupMenu.getComponents();
        for (Component item : components)
        {
            if (item instanceof JMenuItem)
            {
                // Do not show the "Show Image" option since that creates problems
                // since it's supposed to close this window also.
                if (!(((JMenuItem)item).getAction() instanceof ImagePopupMenu.MapImageAction))
                    menu.add(item);
            }
        }

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    @Override
    public Model getModel()
    {
        return image;
    }

    @Override
    public Model getCollectionModel()
    {
        return imageCollection;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (centerFrustumMode && e.getButton() == 1)
        {
            if (e.isAltDown())
            {
//                System.out.println("Resetting pointing...");
//                ((PerspectiveImage)image).resetSpacecraftState();
            }
            else
            {
                centerFrustumOnPixel(e);

                ((PerspectiveImage)image).loadFootprint();
//                ((PerspectiveImage)image).calculateFrustum();
            }
//            PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(image.getKey());
//            boundary.update();
//            ((PerspectiveImageBoundary)boundary).firePropertyChange();

            ((PerspectiveImage)image).firePropertyChange();
        }

      int pickSucceeded = doPick(e, imagePicker, renWin);
      if (pickSucceeded == 1)
      {
          double[] p = imagePicker.GetPickPosition();

          // Display selected pixel coordinates in console output
          // Note we reverse x and y so that the pixel is in the form the camera
          // position/orientation program expects.
          System.out.println(p[1] + " " + p[0]);

          // Display status bar message upon being picked
          refStatusHandler.setLeftTextSource(image, null, 0, p);
      }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (centerFrustumMode && !e.isAltDown())
        {
            ((PerspectiveImage)image).calculateFrustum();
            ((PerspectiveImage)image).firePropertyChange();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (centerFrustumMode && e.getButton() == 1)
        {
            if (!e.isAltDown())
            {
                centerFrustumOnPixel(e);
                ((PerspectiveImage)image).loadFootprint();
            }

            ((PerspectiveImage)image).firePropertyChange();

        }
        else
            updateSpectrumRegion(e);
    }

    private void updateSpectrumRegion(MouseEvent e)
    {
        int pickSucceeded = doPick(e, imagePicker, renWin);
        if (pickSucceeded == 1)
        {
            double[] p = imagePicker.GetPickPosition();
            double[][] spectrumRegion = { { p[0], p[1] } };
            if (image instanceof PerspectiveImage)
                ((PerspectiveImage)image).setSpectrumRegion(spectrumRegion);
        }
    }

    private void centerFrustumOnPixel(MouseEvent e)
    {
        int pickSucceeded = doPick(e, imagePicker, renWin);
        if (pickSucceeded == 1)
        {
            double[] pickPosition = imagePicker.GetPickPosition();
            // Note we reverse x and y so that the pixel is in the form the camera
            // position/orientation program expects.
            if (image instanceof PerspectiveImage)
            {
                PerspectiveImage pi = (PerspectiveImage)image;
                pi.setTargetPixelCoordinates(pickPosition);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

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
        GridBagConstraints gridBagConstraints;

        factorLabel = new JLabel();
        slider = new ContrastSlider(image, false);
        jLabel1 = new JLabel();
        jLabel7 = new JLabel();
        jScrollPane1 = new JScrollPane();
        table = new JTable();
        jPanel1 = new JPanel();
        leftSpinner = new JSpinner();
        bottomSpinner = new JSpinner();
        jLabel3 = new JLabel();
        rightSpinner = new JSpinner();
        topSpinner = new JSpinner();
        jLabel6 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        pointingPanel = new JPanel();
        leftButton = new JButton();
        rightButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        rotateLeftButton = new JButton();
        zoomOutButton = new JButton();
        zoomInButton = new JButton();
        rotateRightButton = new JButton();
        jPanel3 = new JPanel();
        interpolateCheckBox1 = new JCheckBox();
        resetFrameAdjustmentsButton = new JButton();
        adjustFrameCheckBox3 = new JCheckBox();
        factorLabel1 = new JLabel();
        factorTextField1 = new JTextField();
//        applyAdjustmentsButton1 = new JCheckBox();
        jLabel8 = new JLabel();

        if (image instanceof PerspectiveImage) {
        	PerspectiveImage perspIm = (PerspectiveImage) image;
	        offLimbPanel = new JPanel();
	        offlimbController = new OfflimbControlsController(perspIm, perspIm.getCurrentSlice(), slider);
        }

        factorLabel.setText("Factor");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 500));
        setPreferredSize(new Dimension(775, 900));
        getContentPane().setLayout(new GridBagLayout());

        slider.setMinimum(0);
        slider.setMaximum(255);
        int lowValue = 0;
        int hiValue = 255;
        // get existing contrast and set slider appropriately
        if (image instanceof PerspectiveImage)
        {
           lowValue = ((PerspectiveImage)image).getDisplayedRange().min;
           hiValue  = ((PerspectiveImage)image).getDisplayedRange().max;
        }
        slider.setHighValue(hiValue);
        slider.setLowValue(lowValue);
        slider.addChangeListener(evt -> {
           slider.sliderStateChanged(evt);

           if (!(image instanceof PerspectiveImage)) return;

           PerspectiveImage pimg = (PerspectiveImage)image;
           if (pimg.isContrastSynced()) {
        	   offlimbController.getControlsPanel().getImageContrastSlider().setHighValue(slider.getHighValue());
        	   offlimbController.getControlsPanel().getImageContrastSlider().setLowValue(slider.getLowValue());
           }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 3, 0, 0);
        getContentPane().add(slider, gridBagConstraints);

        jLabel1.setText("Contrast:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(3, 6, 3, 0);
        getContentPane().add(jLabel1, gridBagConstraints);

//        jLabel7.setText("Adjust:");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 3;
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new Insets(3, 6, 3, 0);
//        getContentPane().add(jLabel7, gridBagConstraints);

        jScrollPane1.setMinimumSize(new Dimension(452, 200));
        jScrollPane1.setPreferredSize(new Dimension(452, 200));

        jScrollPane1.setViewportView(table);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanel1.setAlignmentX(0.0F);
        jPanel1.setLayout(new GridBagLayout());

        leftSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        leftSpinner.setPreferredSize(new Dimension(60, 28));
        leftSpinner.addChangeListener(evt ->  { leftSpinnerStateChanged(evt); });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 10);
        jPanel1.add(leftSpinner, gridBagConstraints);

        bottomSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        bottomSpinner.setPreferredSize(new Dimension(60, 28));
        bottomSpinner.addChangeListener(evt ->  { bottomSpinnerStateChanged(evt); });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 10);
        jPanel1.add(bottomSpinner, gridBagConstraints);

        jLabel3.setText("Left");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 2);
        jPanel1.add(jLabel3, gridBagConstraints);

        rightSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        rightSpinner.setPreferredSize(new Dimension(60, 28));
        rightSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                rightSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 10);
        jPanel1.add(rightSpinner, gridBagConstraints);

        topSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        topSpinner.setPreferredSize(new Dimension(60, 28));
        topSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                topSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 0, 10);
        jPanel1.add(topSpinner, gridBagConstraints);

        jLabel6.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel6.setText("Bottom");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 2);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel4.setText("Top");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 2);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("Right");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new Insets(0, 0, 0, 2);
        jPanel1.add(jLabel5, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 8, 0, 0);
        getContentPane().add(jPanel1, gridBagConstraints);

        pointingPanel.setLayout(new GridBagLayout());

        leftButton.setText("<");
        leftButton.setToolTipText("left");
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                leftButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        pointingPanel.add(leftButton, gridBagConstraints);

        rightButton.setText(">");
        rightButton.setToolTipText("right");
        rightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rightButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        pointingPanel.add(rightButton, gridBagConstraints);

        upButton.setText("^");
        upButton.setToolTipText("up");
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        pointingPanel.add(upButton, gridBagConstraints);

        downButton.setText("v");
        downButton.setToolTipText("down");
        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        pointingPanel.add(downButton, gridBagConstraints);

        rotateLeftButton.setText("\\");
        rotateLeftButton.setToolTipText("rotate left");
            rotateLeftButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    rotateLeftButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(rotateLeftButton, gridBagConstraints);

            zoomOutButton.setText("-><-");
            zoomOutButton.setToolTipText("zoom out");
            zoomOutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    zoomOutButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(zoomOutButton, gridBagConstraints);

            zoomInButton.setText("<-->");
            zoomInButton.setToolTipText("zoom in");
            zoomInButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    zoomInButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridx = 5;
            gridBagConstraints.gridy = 1;
            pointingPanel.add(zoomInButton, gridBagConstraints);

            rotateRightButton.setText("/");
            rotateRightButton.setToolTipText("rotate right");
            rotateRightButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    rotateRightButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 7;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(rotateRightButton, gridBagConstraints);

            interpolateCheckBox1.setSelected(true);
            interpolateCheckBox1.setText("Interpolate Pixels");
            interpolateCheckBox1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    interpolateCheckBox1ActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.ipadx = 15;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            pointingPanel.add(interpolateCheckBox1, gridBagConstraints);

            resetFrameAdjustmentsButton.setText("Reset Pointing");
            resetFrameAdjustmentsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    resetFrameAdjustmentsButtonActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 6;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(resetFrameAdjustmentsButton, gridBagConstraints);

            adjustFrameCheckBox3.setText("Select Target");
            adjustFrameCheckBox3.setName(""); // NOI18N
            adjustFrameCheckBox3.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    adjustFrameCheckBox3ActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.ipadx = 15;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(adjustFrameCheckBox3, gridBagConstraints);

            factorLabel1.setText("Factor");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 5;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 15;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(factorLabel1, gridBagConstraints);

//            factorTextField1.setColumns(5);
            factorTextField1.setText("1.0");
            factorTextField1.setPreferredSize(new Dimension(14, 28));
            factorTextField1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    factorTextField1ActionPerformed(evt);
                }
            });
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 4;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.ipadx = 50;
            gridBagConstraints.weightx = 1.0;
            pointingPanel.add(factorTextField1, gridBagConstraints);

            TitledBorder pointingBorder = BorderFactory.createTitledBorder("Pointing Adjustments");
            pointingBorder.setTitleJustification(TitledBorder.CENTER);
            pointingPanel.setBorder(pointingBorder);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 4;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 1.0;

            getContentPane().add(pointingPanel, gridBagConstraints);


//            applyAdjustmentsButton1.setSelected(true);
//            applyAdjustmentsButton1.setText("Apply Adjustments");
//            applyAdjustmentsButton1.setName(""); // NOI18N
//            applyAdjustmentsButton1.addActionListener(new event.ActionListener() {
//                public void actionPerformed(event.ActionEvent evt) {
//                    applyAdjustmentsButton1ActionPerformed(evt);
//                }
//            });
//            gridBagConstraints = new GridBagConstraints();
//            gridBagConstraints.gridx = 2;
//            gridBagConstraints.gridy = 0;
//            gridBagConstraints.ipadx = 15;
//            gridBagConstraints.weightx = 1.0;
//            pointingPanel.add(applyAdjustmentsButton1, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 3;
            gridBagConstraints.anchor = GridBagConstraints.LINE_START;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new Insets(0, 2, 0, 0);
            getContentPane().add(jPanel3, gridBagConstraints);

            jLabel8.setText("Crop:");
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.insets = new Insets(3, 6, 3, 0);
            getContentPane().add(jLabel8, gridBagConstraints);


            // set up panel for offlimb settings
            if (image instanceof PerspectiveImage) {
            	gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 5;
                gridBagConstraints.gridwidth = 2;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = GridBagConstraints.LINE_START;
                gridBagConstraints.weightx = 1.0;

                TitledBorder titledBorder = BorderFactory.createTitledBorder("Offlimb Settings");
                titledBorder.setTitleJustification(TitledBorder.CENTER);
                offLimbPanel.setBorder(titledBorder);
                offLimbPanel.add(offlimbController.getControlsPanel());
                getContentPane().add(offLimbPanel, gridBagConstraints);
            }




            pack();
        }// </editor-fold>//GEN-END:initComponents

//    private void sliderStateChanged(event.ChangeEvent evt)//GEN-FIRST:event_sliderStateChanged
//    {//GEN-HEADEREND:event_sliderStateChanged
//        if (slider.getValueIsAdjusting())
//            return;
//
//        int lowVal = slider.getLowValue();
//        int highVal = slider.getHighValue();
//        if (image != null)
//            image.setDisplayedImageRange(new IntensityRange(lowVal, highVal));
//    }//GEN-LAST:event_sliderStateChanged

    private void leftSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void topSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_topSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_topSpinnerStateChanged

    private void rightSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void bottomSpinnerStateChanged(ChangeEvent evt) {//GEN-FIRST:event_bottomSpinnerStateChanged
        croppingChanged();
    }//GEN-LAST:event_bottomSpinnerStateChanged

    private void zoomInButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_zoomInButtonActionPerformed
//        System.out.println("Zoom In");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveZoomFactorBy(Math.pow(1.1, -getAdjustFactor()));
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_zoomInButtonActionPerformed

    private void leftButtonActionPerformed(ActionEvent evt){//GEN-FIRST:event_leftButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { getAdjustFactor(), 0.0 };
//            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//            ((PerspectiveImage)image).moveYawAngleBy(getAdjustFactor());
            ((PerspectiveImage)image).moveLineOffsetBy(-getAdjustFactor()/1000.0);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_leftButtonActionPerformed

    private void rightButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rightButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { -getAdjustFactor(), 0.0 };
//            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//            ((PerspectiveImage)image).moveYawAngleBy(-getAdjustFactor());
            ((PerspectiveImage)image).moveLineOffsetBy(getAdjustFactor()/1000.0);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_rightButtonActionPerformed

    private void upButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { 0.0, -getAdjustFactor() };
//            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//            ((PerspectiveImage)image).movePitchAngleBy(getAdjustFactor());
            ((PerspectiveImage)image).moveSampleOffsetBy(-getAdjustFactor()/1000.0);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        if (image instanceof PerspectiveImage)
        {
            double[] delta = { 0.0, getAdjustFactor() };
//            ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//            ((PerspectiveImage)image).movePitchAngleBy(-getAdjustFactor());
            ((PerspectiveImage)image).moveSampleOffsetBy(getAdjustFactor()/1000.0);
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void rotateLeftButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rotateLeftButtonActionPerformed
//        System.out.println("Rotate Left");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveRotationAngleBy(-getAdjustFactor());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_rotateLeftButtonActionPerformed

    private void rotateRightButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_rotateRightButtonActionPerformed
//        System.out.println("Rotate Right");
        if (image instanceof PerspectiveImage)
        {
            ((PerspectiveImage)image).moveRotationAngleBy(getAdjustFactor());
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_rotateRightButtonActionPerformed

    private void interpolateCheckBox1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_interpolateCheckBox1ActionPerformed
        if (image instanceof PerspectiveImage)
        {
            boolean interpolate = interpolateCheckBox1.isSelected();
            ((PerspectiveImage)image).setInterpolate(interpolate);
            if (interpolate)
                actor.GetProperty().SetInterpolationTypeToLinear();
            else
                actor.GetProperty().SetInterpolationTypeToNearest();
            ((PerspectiveImage)image).firePropertyChange();
        }
    }//GEN-LAST:event_interpolateCheckBox1ActionPerformed

    private void resetFrameAdjustmentsButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetFrameAdjustmentsButtonActionPerformed
//        System.out.println("Reset Frame Adjustments");
        ((PerspectiveImage)image).resetSpacecraftState();
        ((PerspectiveImage)image).firePropertyChange();
    }//GEN-LAST:event_resetFrameAdjustmentsButtonActionPerformed

    private void adjustFrameCheckBox3ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_adjustFrameCheckBox3ActionPerformed
//        System.out.println("Adjust frame...");
        centerFrustumMode = adjustFrameCheckBox3.isSelected();
    }//GEN-LAST:event_adjustFrameCheckBox3ActionPerformed

        private void zoomOutButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_zoomOutButtonActionPerformed
//          System.out.println("Zoom In");
          if (image instanceof PerspectiveImage)
          {
              ((PerspectiveImage)image).moveZoomFactorBy(Math.pow(1.1, getAdjustFactor()));
              ((PerspectiveImage)image).firePropertyChange();
          }
    }//GEN-LAST:event_zoomOutButtonActionPerformed

    private void factorTextField1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_factorTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_factorTextField1ActionPerformed

//    private void applyAdjustmentsButton1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_applyAdjustmentsButton1ActionPerformed
//        // TODO add your handling code here:
//    }//GEN-LAST:event_applyAdjustmentsButton1ActionPerformed

    private double getAdjustFactor()
    {
        double result = 1.0;
        try {
            double delta = 1.0 * Double.parseDouble(factorTextField1.getText());
            result = delta;
        } catch (Exception e) { }

        return result;
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox adjustFrameCheckBox3;
//    private JCheckBox applyAdjustmentsButton1;
    private JSpinner bottomSpinner;
    private JButton downButton;
    private JLabel factorLabel;
    private JLabel factorLabel1;
    private JTextField factorTextField1;
    private JCheckBox interpolateCheckBox1;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JPanel jPanel1;
    private JPanel pointingPanel;
    private JPanel jPanel3;
    private JScrollPane jScrollPane1;
    private JButton leftButton;
    private JSpinner leftSpinner;
    private JButton resetFrameAdjustmentsButton;
    private JButton rightButton;
    private JSpinner rightSpinner;
    private JButton rotateLeftButton;
    private JButton rotateRightButton;
    protected ContrastSlider slider;
    private JTable table;
    private JSpinner topSpinner;
    private JButton upButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    // End of variables declaration//GEN-END:variables

}
