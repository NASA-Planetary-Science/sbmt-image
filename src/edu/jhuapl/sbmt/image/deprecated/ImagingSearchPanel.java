/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImagingSearchPanel.java
 *
 * Created on May 5, 2011, 3:15:17 PM
 */
package edu.jhuapl.sbmt.image.deprecated;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.jidesoft.swing.CheckBoxTree;

import vtk.vtkActor;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.core.Image;
import edu.jhuapl.sbmt.image.core.ImageDatabaseSearchMetadata;
import edu.jhuapl.sbmt.image.core.ImagingInstrument;
import edu.jhuapl.sbmt.image.core.keys.ImageKey;
import edu.jhuapl.sbmt.image.gui.color.ColorImagePopupMenu;
import edu.jhuapl.sbmt.image.gui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.gui.images.ImagePopupMenu;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImageCollection;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeCollection;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundary;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification.Selection;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.util.ImageGalleryGenerator;
import edu.jhuapl.sbmt.util.ImageGalleryGenerator.ImageGalleryEntry;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import nom.tam.fits.FitsException;

@Deprecated
public class ImagingSearchPanel extends javax.swing.JPanel implements PropertyChangeListener, TableModelListener, MouseListener, ListSelectionListener
{
    private static final SimpleDateFormat STANDARD_UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    static {
        STANDARD_UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private SmallBodyViewConfig smallBodyConfig;
    protected final ModelManager modelManager;
    private final SbmtInfoWindowManager infoPanelManager;
    private final SbmtSpectrumWindowManager spectrumPanelManager;
    private final PickManager pickManager;
    protected final Renderer renderer;
    private java.util.Date startDate = null;
    private java.util.Date endDate = null;
    protected IdPair resultIntervalCurrentlyShown = null;
    private ImageKey selectedRedKey;
    private ImageKey selectedGreenKey;
    private ImageKey selectedBlueKey;
    private JCheckBox[] filterCheckBoxes;
    private JCheckBox[] userDefinedCheckBoxes;
    protected CheckBoxTree checkBoxTree;

    protected final ImagingInstrument instrument;

    // The source of the images of the most recently executed query
    protected ImageSource sourceOfLastQuery = ImageSource.SPICE;

    protected List<List<String>> imageRawResults = new ArrayList<List<String>>();
    private ImagePopupMenu imagePopupMenu;
    private ColorImagePopupMenu colorImagePopupMenu;
    private ImageCubePopupMenu imageCubePopupMenu;
    private boolean enableGallery;

    public int getCurrentSlice() { return 0; }

    public String getCurrentBand() { return "0"; }

    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,bndrColumnIndex,dateColumnIndex,idColumnIndex,filenameColumnIndex;

    private MetadataManager stateManager;

    protected ModelManager getModelManager()
    {
        return modelManager;
    }

    /** Creates new form ImagingSearchPanel */
    public ImagingSearchPanel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        this.pickManager = pickManager;

        this.instrument = instrument;
        this.stateManager = null;
    }

    public ImagingSearchPanel init()
    {
        pickManager.getDefaultPicker().addPropertyChangeListener(this);

        initComponents();

        initExtraComponents();

        populateMonochromePanel(monochromePanel);

        postInitComponents(instrument);

        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        imagePopupMenu = new ImagePopupMenu(modelManager, images, boundaries, infoPanelManager, spectrumPanelManager, renderer, this);
        boundaries.addPropertyChangeListener(this);
        images.addPropertyChangeListener(this);

        ColorImageCollection colorImages = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
//        colorImagePopupMenu = new ColorImagePopupMenu(colorImages, infoPanelManager, modelManager, this);

        ImageCubeCollection imageCubes = (ImageCubeCollection)modelManager.getModel(getImageCubeCollectionModelName());
        imageCubePopupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, this);

        imageCubes.addPropertyChangeListener(this);

        imageCubesDisplayedList.addListSelectionListener(this);

        // Hide gallery if not APL internal mode
        if (!Configuration.isAPLVersion())
        {
            viewResultsGalleryButton.setVisible(false);
        }
        else
        {
            viewResultsGalleryButton.setEnabled(enableGallery);
        }

        //        // XXX: Mike Z's defaults for testing off-limb plane generation
        //searchByFilenameCheckBox.setSelected(true);
        //        searchByNumberTextField.setText("N46055787522");
        //searchByNumberTextField.setText("W46990353518");

        // Setup hierarchical image search
        initHierarchicalImageSearch();

        final List<List<String>> emptyList = new ArrayList<>();
        setImageResults(emptyList);

        initializeStateManager();

        return this;
    }

    // Sets up everything related to hierarchical image searches
    protected void initHierarchicalImageSearch()
    {
        // Show/hide panels depending on whether this body has hierarchical image search capabilities
        if(smallBodyConfig.hasHierarchicalImageSearch)
        {
            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
            filterCheckBoxPanel.setVisible(false);
            userDefinedCheckBoxPanel.setVisible(false);

            // Create the tree
            checkBoxTree = new CheckBoxTree(smallBodyConfig.hierarchicalImageSearchSpecification.getTreeModel());

            // Bind the checkbox-specific tree selection model to the "spec"
            smallBodyConfig.hierarchicalImageSearchSpecification.setSelectionModel(checkBoxTree.getCheckBoxTreeSelectionModel());

            // Place the tree in the panel
            this.hierarchicalSearchScrollPane.setViewportView(checkBoxTree);
        }
        else
        {
            // No hierarchical search capabilities, hide the scroll pane
            hierarchicalSearchScrollPane.setVisible(false);
        }
    }

    protected void initExtraComponents()
    {
        // to be overridden by subclasses
    }

    protected void populateMonochromePanel(JPanel panel)
    {
        // to be overridden by subclasses
    }

    private int getNumberOfFiltersActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchFilterNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    private int getNumberOfUserDefinedCheckBoxesActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    protected ModelNames getImageCollectionModelName()
    {
        return ModelNames.IMAGES;
    }

    protected ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES;
    }

    private ModelNames getColorImageCollectionModelName()
    {
        return ModelNames.COLOR_IMAGES;
    }

    protected ModelNames getImageCubeCollectionModelName()
    {
        return ModelNames.CUBE_IMAGES;
    }

    protected void postInitComponents(ImagingInstrument instrument)
    {
        excludeGaskellCheckBox.setVisible(false);

        String[] columnNames = {
                "Map",
                "Show",
                "Frus",
                "Bndr",
                "Id",
                "Filename",
                "Date"
        };
        mapColumnIndex=0;
        showFootprintColumnIndex=1;
        frusColumnIndex=2;
        bndrColumnIndex=3;
        idColumnIndex=4;
        filenameColumnIndex=5;
        dateColumnIndex=6;


        Object[][] data = new Object[0][7];
        resultList.setModel(new StructuresTableModel(data, columnNames));
        resultList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resultList.setDefaultRenderer(String.class, new StringRenderer());
        resultList.getColumnModel().getColumn(mapColumnIndex).setPreferredWidth(31);
        resultList.getColumnModel().getColumn(showFootprintColumnIndex).setPreferredWidth(35);
        resultList.getColumnModel().getColumn(frusColumnIndex).setPreferredWidth(31);
        resultList.getColumnModel().getColumn(bndrColumnIndex).setPreferredWidth(31);
        resultList.getColumnModel().getColumn(mapColumnIndex).setResizable(true);
        resultList.getColumnModel().getColumn(showFootprintColumnIndex).setResizable(true);
        resultList.getColumnModel().getColumn(frusColumnIndex).setResizable(true);
        resultList.getColumnModel().getColumn(bndrColumnIndex).setResizable(true);
        resultList.addMouseListener(this);
        resultList.getModel().addTableModelListener(this);
        resultList.getSelectionModel().addListSelectionListener(this);

        enableGallery = instrument.searchQuery.getGalleryPath() != null;
        ImageSource imageSources[] = instrument.searchImageSources;
        DefaultComboBoxModel<ImageSource> sourceComboBoxModel = new DefaultComboBoxModel<>(imageSources);
        sourceComboBox.setModel(sourceComboBoxModel);

        boolean showSourceLabelAndComboBox = true; //imageSources.length > 1 ? true : false;
        sourceLabel.setVisible(showSourceLabelAndComboBox);
        sourceComboBox.setVisible(showSourceLabelAndComboBox);

        startDate = smallBodyConfig.imageSearchDefaultStartDate;
        ((SpinnerDateModel)startSpinner.getModel()).setValue(startDate);
        endDate = smallBodyConfig.imageSearchDefaultEndDate;
        ((SpinnerDateModel)endSpinner.getModel()).setValue(endDate);



        filterCheckBoxes = new JCheckBox[]{
                filter1CheckBox,
                filter2CheckBox,
                filter3CheckBox,
                filter4CheckBox,
                filter5CheckBox,
                filter6CheckBox,
                filter7CheckBox,
                filter8CheckBox,
                filter9CheckBox,
                filter10CheckBox,
                filter11CheckBox,
                filter12CheckBox,
                filter13CheckBox,
                filter14CheckBox,
                filter15CheckBox,
                filter16CheckBox,
                filter17CheckBox,
                filter18CheckBox,
                filter19CheckBox,
                filter20CheckBox,
                filter21CheckBox,
                filter22CheckBox
        };

        String[] filterNames = smallBodyConfig.imageSearchFilterNames;
        int numberOfFiltersActuallyUsed = getNumberOfFiltersActuallyUsed();
        for (int i=filterCheckBoxes.length-1; i>=0; --i)
        {
            if (numberOfFiltersActuallyUsed < i+1)
            {
                filterCheckBoxes[i].setSelected(false);
                filterCheckBoxes[i].setVisible(false);
            }
        }

        for (int i=0; i<filterCheckBoxes.length; ++i)
        {
            if (numberOfFiltersActuallyUsed > i)
            {
                if (filterNames[i].startsWith("*"))
                {
                    filterCheckBoxes[i].setText(filterNames[i].substring(1));
                    filterCheckBoxes[i].setSelected(false);
                }
                else
                {
                    filterCheckBoxes[i].setText(filterNames[i]);
                }
            }
        }



        userDefinedCheckBoxes = new JCheckBox[]{
                userDefined1CheckBox,
                userDefined2CheckBox,
                userDefined3CheckBox,
                userDefined4CheckBox,
                userDefined5CheckBox,
                userDefined6CheckBox,
                userDefined7CheckBox,
                userDefined8CheckBox
        };

        String[] userDefinedNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        int numberOfUserDefinedCheckBoxesActuallyUsed = getNumberOfUserDefinedCheckBoxesActuallyUsed();

        for (int i=userDefinedCheckBoxes.length-1; i>=0; --i)
        {
            if (numberOfUserDefinedCheckBoxesActuallyUsed < i+1)
            {
                userDefinedCheckBoxes[i].setSelected(false);
                userDefinedCheckBoxes[i].setVisible(false);
            }
        }

        for (int i=0; i<userDefinedCheckBoxes.length; ++i)
        {
            if (numberOfUserDefinedCheckBoxesActuallyUsed > i)
            {
                if (userDefinedNames[i].startsWith("*"))
                {
                    userDefinedCheckBoxes[i].setText(userDefinedNames[i].substring(1));
                    userDefinedCheckBoxes[i].setSelected(false);
                }
                else
                {
                    userDefinedCheckBoxes[i].setText(userDefinedNames[i]);
                }
            }
        }



        toDistanceTextField.setValue(smallBodyConfig.imageSearchDefaultMaxSpacecraftDistance);
        toResolutionTextField.setValue(smallBodyConfig.imageSearchDefaultMaxResolution);

        colorImagesDisplayedList.setModel(new DefaultListModel());
        imageCubesDisplayedList.setModel(new DefaultListModel());

        redComboBox.setVisible(false);
        greenComboBox.setVisible(false);
        blueComboBox.setVisible(false);

        ComboBoxModel redModel = getRedComboBoxModel();
        ComboBoxModel greenModel = getGreenComboBoxModel();
        ComboBoxModel blueModel = getBlueComboBoxModel();
        if (redModel != null && greenModel != null && blueModel != null)
        {
            redComboBox.setModel(redModel);
            greenComboBox.setModel(greenModel);
            blueComboBox.setModel(blueModel);

            redComboBox.setVisible(true);
            greenComboBox.setVisible(true);
            blueComboBox.setVisible(true);

            redButton.setVisible(false);
            greenButton.setVisible(false);
            blueButton.setVisible(false);
        }
    }

    protected javax.swing.JComboBox getRedComboBox()
    {
        return redComboBox;
    }

    protected javax.swing.JComboBox getGreenComboBox()
    {
        return greenComboBox;
    }

    protected javax.swing.JComboBox getBlueComboBox()
    {
        return blueComboBox;
    }

    protected ComboBoxModel getRedComboBoxModel()
    {
        return null;
    }

    protected ComboBoxModel getGreenComboBoxModel()
    {
        return null;
    }

    protected ComboBoxModel getBlueComboBoxModel()
    {
        return null;
    }

    protected void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = resultList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                // If the item right-clicked on is not selected, then deselect all the
                // other items and select the item right-clicked on.
                if (!resultList.isRowSelected(index))
                {
                    resultList.clearSelection();
                    resultList.setRowSelectionInterval(index, index);
                }

                int[] selectedIndices = resultList.getSelectedRows();
                List<ImageKey> imageKeys = new ArrayList<ImageKey>();
                for (int selectedIndex : selectedIndices)
                {
                    String name = imageRawResults.get(selectedIndex).get(0);
                    //                    ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                    ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                    imageKeys.add(key);
                }
                imagePopupMenu.setCurrentImages(imageKeys);
                imagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    private void colorImagesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = colorImagesDisplayedList.locationToIndex(e.getPoint());

            if (index >= 0 && colorImagesDisplayedList.getCellBounds(index, index).contains(e.getPoint()))
            {
                colorImagesDisplayedList.setSelectedIndex(index);
                ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).get(index);
                colorImagePopupMenu.setCurrentImage(colorKey);
                colorImagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void imageCubesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            int index = imageCubesDisplayedList.locationToIndex(e.getPoint());

            if (index >= 0 && imageCubesDisplayedList.getCellBounds(index, index).contains(e.getPoint()))
            {
                imageCubesDisplayedList.setSelectedIndex(index);
                ImageCubeKey colorKey = (ImageCubeKey)((DefaultListModel)imageCubesDisplayedList.getModel()).get(index);
                imageCubePopupMenu.setCurrentImage(colorKey);
                imageCubePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected void setImageResults(List<List<String>> results)
    {
        resultsLabel.setText(results.size() + " images matched");
        imageRawResults = results;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());

        resultList.getModel().removeTableModelListener(this);
        images.removePropertyChangeListener(this);
        boundaries.removePropertyChangeListener(this);

        try
        {
            int[] widths = new int[resultList.getColumnCount()];
            int[] columnsNeedingARenderer=new int[]{idColumnIndex,filenameColumnIndex,dateColumnIndex};

            // add the results to the list
            ((DefaultTableModel)resultList.getModel()).setRowCount(results.size());
            int i=0;
            for (List<String> str : results)
            {
                Date dt = new Date(Long.parseLong(str.get(1)));

                String name = imageRawResults.get(i).get(0);
                //            ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageKeyInterface key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                if (images.containsImage(key))
                {
                    resultList.setValueAt(true, i, mapColumnIndex);
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    resultList.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
                    resultList.setValueAt(!image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    resultList.setValueAt(false, i, mapColumnIndex);
                    resultList.setValueAt(false, i, showFootprintColumnIndex);
                    resultList.setValueAt(false, i, frusColumnIndex);
                }


                if (boundaries.containsBoundary(key))
                    resultList.setValueAt(true, i, bndrColumnIndex);
                else
                    resultList.setValueAt(false, i, bndrColumnIndex);

                resultList.setValueAt(i+1, i, idColumnIndex);
                resultList.setValueAt(str.get(0).substring(str.get(0).lastIndexOf("/") + 1), i, filenameColumnIndex);
                resultList.setValueAt(sdf.format(dt), i, dateColumnIndex);

                for (int j : columnsNeedingARenderer)
                {
                    TableCellRenderer renderer = resultList.getCellRenderer(i, j);
                    Component comp = resultList.prepareRenderer(renderer, i, j);
                    widths[j] = Math.max (comp.getPreferredSize().width, widths[j]);
                }

                ++i;
            }

            for (int j : columnsNeedingARenderer)
                resultList.getColumnModel().getColumn(j).setPreferredWidth(widths[j] + 5);

            boolean enablePostSearchButtons = resultList.getModel().getRowCount() > 0;
            saveImageListButton.setEnabled(enablePostSearchButtons);
            saveSelectedImageListButton.setEnabled(resultList.getSelectedRowCount() > 0);
            viewResultsGalleryButton.setEnabled(enableGallery && enablePostSearchButtons);
        }
        finally
        {
            resultList.getModel().addTableModelListener(this);
            images.addPropertyChangeListener(this);
            boundaries.addPropertyChangeListener(this);
        }


        // Show the first set of boundaries
        this.resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
        this.showImageBoundaries(resultIntervalCurrentlyShown);

        // Enable or disable the image gallery button
        viewResultsGalleryButton.setEnabled(enableGallery && !results.isEmpty());
    }


    protected List<ImageKey> createImageKeys(String boundaryName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        List<ImageKey> result = new ArrayList<ImageKey>();
        result.add(createImageKey(boundaryName, sourceOfLastQuery, instrument));
        return result;
    }

    protected ImageKey createImageKey(String imagePathName, ImageSource sourceOfLastQuery, ImagingInstrument instrument)
    {
        int slice = this.getCurrentSlice();
        String band = this.getCurrentBand();
        return new ImageKey(imagePathName, sourceOfLastQuery, null, null, instrument, band, slice, null);
    }

    protected void showImageBoundaries(IdPair idPair)
    {
        int startId = idPair.id1;
        int endId = idPair.id2;

        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        model.removeAllBoundaries();

        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= imageRawResults.size())
                break;

            try
            {
                String currentImage = imageRawResults.get(i).get(0);
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_BOUNDARY.VTK";
                //String boundaryName = currentImage.substring(0,currentImage.length()-4) + "_DDR.LBL";
                String boundaryName = currentImage.substring(0,currentImage.length()-4);

                //                ImageKey key = new ImageKey(boundaryName, sourceOfLastQuery, instrument);

                ImageKey key = createImageKey(boundaryName, sourceOfLastQuery, instrument);
                model.addBoundary(key);

                //                List<ImageKey> keys = createImageKeys(boundaryName, sourceOfLastQuery, instrument);
                //                for (ImageKey key : keys)
                //                {
                //                    key.instrument = this.instrument;
                //                    model.addBoundary(key);
                //                }
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the boundary.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
                break;
            }
        }
    }

    protected void generateColorImage(ActionEvent e)
    {
        ColorImageCollection model = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());

        if (selectedRedKey != null && selectedGreenKey != null && selectedBlueKey != null)
        {
            ColorImageKey colorKey = new ColorImageKey(selectedRedKey, selectedGreenKey, selectedBlueKey);
            try
            {
                DefaultListModel listModel = (DefaultListModel)colorImagesDisplayedList.getModel();
                if (!model.containsImage(colorKey))
                {
                    model.addImage(colorKey);

                    listModel.addElement(colorKey);
                    int idx = listModel.size()-1;
                    colorImagesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = colorImagesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        colorImagesDisplayedList.scrollRectToVisible(cellBounds);
                }
                else
                {
                    int idx = listModel.indexOf(colorKey);
                    colorImagesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = colorImagesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        colorImagesDisplayedList.scrollRectToVisible(cellBounds);
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (ColorImage.NoOverlapException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "The images you selected do not overlap.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    protected void removeColorImage(ActionEvent e)
    {
        int index = colorImagesDisplayedList.getSelectedIndex();
        if (index >= 0)
        {
            ColorImageKey colorKey = (ColorImageKey)((DefaultListModel)colorImagesDisplayedList.getModel()).remove(index);
            ColorImageCollection model = (ColorImageCollection)modelManager.getModel(getColorImageCollectionModelName());
            model.removeImage(colorKey);

            // Select the element in its place (unless it's the last one in which case
            // select the previous one)
            if (index >= colorImagesDisplayedList.getModel().getSize())
                --index;
            if (index >= 0)
                colorImagesDisplayedList.setSelectionInterval(index, index);
        }
    }



    protected void generateImageCube(ActionEvent e)
    {
        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        ImageCubeCollection model = (ImageCubeCollection)modelManager.getModel(getImageCubeCollectionModelName());

        ImageKeyInterface firstKey = null;
        boolean multipleFrustumVisible = false;

        List<ImageKeyInterface> selectedKeys = new ArrayList<ImageKeyInterface>();
        int[] selectedIndices = resultList.getSelectedRows();
        //System.out.println(Arrays.toString(selectedIndices));
        for (int selectedIndex : selectedIndices)
        {
            String name = imageRawResults.get(selectedIndex).get(0);
            ImageKeyInterface selectedKey = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            //            System.out.println("Key: " + selectedKey.name);
            selectedKeys.add(selectedKey);
            PerspectiveImage selectedImage = (PerspectiveImage)images.getImage(selectedKey);
            if(selectedImage == null)
            {
                // We are in here because the image is not mapped, display an error message and exit
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "All selected images must be mapped when generating an image cube.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // "first key" is indicated by the first image with a visible frustum
            if (selectedImage.isFrustumShowing())
            {
                if(firstKey == null)
                {
                    firstKey = selectedKey;
                }
                else
                {
                    multipleFrustumVisible = true;
                }
            }

            //            if (!selectedRedKey.band.equals("0"))
            //                imageName = selectedKey.band + ":" + imageName;
        }

        if(selectedKeys.size() == 0)
        {
            // We are in here because no images were selected by user
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "At least one image must be selected when generating an image cube.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else if(firstKey == null)
        {
            // We are in here because no frustum was selected by user
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                    "At least one selected image must have its frustum showing when generating an image cube.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else
        {
            PerspectiveImage firstImage = (PerspectiveImage)images.getImage(firstKey);

            ImageCubeKey imageCubeKey = new ImageCubeKey(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());
            try
            {
                DefaultListModel listModel = (DefaultListModel)imageCubesDisplayedList.getModel();
                if (!model.containsImage(imageCubeKey))
                {
                    model.addImage(imageCubeKey);

                    listModel.addElement(imageCubeKey);
                    int idx = listModel.size()-1;
                    imageCubesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageCubesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageCubesDisplayedList.scrollRectToVisible(cellBounds);

                    if(multipleFrustumVisible)
                    {
                        JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                                "More than one selected image has a visible frustum, image cube was generated using the first such frustum in order of appearance in the image list.",
                                "Notification",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else
                {
                    int idx = listModel.indexOf(imageCubeKey);
                    imageCubesDisplayedList.setSelectionInterval(idx, idx);
                    Rectangle cellBounds = imageCubesDisplayedList.getCellBounds(idx, idx);
                    if (cellBounds != null)
                        imageCubesDisplayedList.scrollRectToVisible(cellBounds);

                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                            "Image cube consisting of same images already exists, no new image cube was generated.",
                            "Notification",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            catch (IOException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            catch (ImageCube.NoOverlapException e1)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "The images you selected do not overlap.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void removeImageCube(ActionEvent e)
    {
        int index = imageCubesDisplayedList.getSelectedIndex();
        if (index >= 0)
        {
            ImageCubeKey imageCubeKey = (ImageCubeKey)((DefaultListModel)imageCubesDisplayedList.getModel()).remove(index);
            ImageCubeCollection model = (ImageCubeCollection)modelManager.getModel(getImageCubeCollectionModelName());
            model.removeImage(imageCubeKey);

            // Select the element in its place (unless it's the last one in which case
            // select the previous one)
            if (index >= imageCubesDisplayedList.getModel().getSize())
                --index;
            if (index >= 0)
                imageCubesDisplayedList.setSelectionInterval(index, index);
        }
    }


    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        if (!e.getValueIsAdjusting())
        {
            viewResultsGalleryButton.setEnabled(enableGallery && resultList.getSelectedRowCount() > 0);
        }
    }


    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof ImageCollection || model instanceof PerspectiveImageBoundaryCollection)
            {
                String name = null;

                if (model instanceof ImageCollection)
                    name = ((ImageCollection)model).getImageName((vtkActor)e.getPickedProp());
                else if (model instanceof PerspectiveImageBoundaryCollection)
                    name = ((PerspectiveImageBoundaryCollection)model).getBoundaryName((vtkActor)e.getPickedProp());

                int idx = -1;
                int size = imageRawResults.size();
                for (int i=0; i<size; ++i)
                {
                    // Ignore extension (The name returned from getImageName or getBoundary
                    // is the same as the first element of each list with the imageRawResults
                    // but without the extension).
                    String imagePath = imageRawResults.get(i).get(0);
                    imagePath = imagePath.substring(0, imagePath.lastIndexOf("."));
                    if (name.equals(imagePath))
                    {
                        idx = i;
                        break;
                    }
                }

                if (idx >= 0)
                {
                    resultList.setRowSelectionInterval(idx, idx);
                    Rectangle cellBounds = resultList.getCellRect(idx, 0, true);
                    if (cellBounds != null)
                        resultList.scrollRectToVisible(cellBounds);
                }
            }
        }
        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            resultList.getModel().removeTableModelListener(this);
            int size = imageRawResults.size();
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
            for (int i=0; i<size; ++i)
            {
                String name = imageRawResults.get(i).get(0);
                //                ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageKeyInterface key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                if (images.containsImage(key))
                {
                    resultList.setValueAt(true, i, mapColumnIndex);
                    PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                    resultList.setValueAt(image.isVisible(), i, showFootprintColumnIndex);
                    resultList.setValueAt(image.isFrustumShowing(), i, frusColumnIndex);
                }
                else
                {
                    resultList.setValueAt(false, i, mapColumnIndex);
                    resultList.setValueAt(false, i, showFootprintColumnIndex);
                    resultList.setValueAt(false, i, frusColumnIndex);
                }
                if (boundaries.containsBoundary(key))
                    resultList.setValueAt(true, i, bndrColumnIndex);
                else
                    resultList.setValueAt(false, i, bndrColumnIndex);
            }
            resultList.getModel().addTableModelListener(this);
            // Repaint the list in case the boundary colors has changed
            resultList.repaint();
        }
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

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        startDateLabel = new javax.swing.JLabel();
        startSpinner = new javax.swing.JSpinner();
        endDateLabel = new javax.swing.JLabel();
        endSpinner = new javax.swing.JSpinner();
        sourceLabel = new javax.swing.JLabel();
        sourceComboBox = new javax.swing.JComboBox<>();
        excludeGaskellCheckBox = new javax.swing.JCheckBox();
        filterCheckBoxPanel = new javax.swing.JPanel();
        filter1CheckBox = new javax.swing.JCheckBox();
        filter3CheckBox = new javax.swing.JCheckBox();
        filter5CheckBox = new javax.swing.JCheckBox();
        filter7CheckBox = new javax.swing.JCheckBox();
        filter2CheckBox = new javax.swing.JCheckBox();
        filter4CheckBox = new javax.swing.JCheckBox();
        filter6CheckBox = new javax.swing.JCheckBox();
        filter8CheckBox = new javax.swing.JCheckBox();
        filter9CheckBox = new javax.swing.JCheckBox();
        filter10CheckBox = new javax.swing.JCheckBox();
        filter11CheckBox = new javax.swing.JCheckBox();
        filter12CheckBox = new javax.swing.JCheckBox();
        filter13CheckBox = new javax.swing.JCheckBox();
        filter15CheckBox = new javax.swing.JCheckBox();
        filter17CheckBox = new javax.swing.JCheckBox();
        filter19CheckBox = new javax.swing.JCheckBox();
        filter14CheckBox = new javax.swing.JCheckBox();
        filter16CheckBox = new javax.swing.JCheckBox();
        filter18CheckBox = new javax.swing.JCheckBox();
        filter20CheckBox = new javax.swing.JCheckBox();
        filter21CheckBox = new javax.swing.JCheckBox();
        filter22CheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        fromDistanceLabel = new javax.swing.JLabel();
        fromDistanceTextField = new javax.swing.JFormattedTextField();
        toDistanceLabel = new javax.swing.JLabel();
        toDistanceTextField = new javax.swing.JFormattedTextField();
        endDistanceLabel = new javax.swing.JLabel();
        fromResolutionLabel = new javax.swing.JLabel();
        fromResolutionTextField = new javax.swing.JFormattedTextField();
        toResolutionLabel = new javax.swing.JLabel();
        toResolutionTextField = new javax.swing.JFormattedTextField();
        endResolutionLabel = new javax.swing.JLabel();
        fromIncidenceLabel = new javax.swing.JLabel();
        fromIncidenceTextField = new javax.swing.JFormattedTextField();
        toIncidenceLabel = new javax.swing.JLabel();
        toIncidenceTextField = new javax.swing.JFormattedTextField();
        endIncidenceLabel = new javax.swing.JLabel();
        fromEmissionLabel = new javax.swing.JLabel();
        fromEmissionTextField = new javax.swing.JFormattedTextField();
        toEmissionLabel = new javax.swing.JLabel();
        toEmissionTextField = new javax.swing.JFormattedTextField();
        endEmissionLabel = new javax.swing.JLabel();
        fromPhaseLabel = new javax.swing.JLabel();
        fromPhaseTextField = new javax.swing.JFormattedTextField();
        toPhaseLabel = new javax.swing.JLabel();
        toPhaseTextField = new javax.swing.JFormattedTextField();
        endPhaseLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        searchByFilenameCheckBox = new javax.swing.JCheckBox();
        searchByFilenameTextField = new javax.swing.JFormattedTextField();
        jPanel5 = new javax.swing.JPanel();
        clearRegionButton = new javax.swing.JButton();
        submitButton = new javax.swing.JButton();
        selectRegionButton = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        resultsLabel = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        resultList = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        numberOfBoundariesComboBox = new javax.swing.JComboBox();
        prevButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel10 = new javax.swing.JPanel();
        redButton = new javax.swing.JButton();
        redLabel = new javax.swing.JLabel();
        greenButton = new javax.swing.JButton();
        blueButton = new javax.swing.JButton();
        greenLabel = new javax.swing.JLabel();
        blueLabel = new javax.swing.JLabel();
        redComboBox = new javax.swing.JComboBox();
        greenComboBox = new javax.swing.JComboBox();
        blueComboBox = new javax.swing.JComboBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        colorImagesDisplayedList = new javax.swing.JList();
        jPanel11 = new javax.swing.JPanel();
        hasLimbComboBox = new javax.swing.JComboBox();
        hasLimbLabel = new javax.swing.JLabel();
        userDefinedCheckBoxPanel = new javax.swing.JPanel();
        userDefined1CheckBox = new javax.swing.JCheckBox();
        userDefined2CheckBox = new javax.swing.JCheckBox();
        userDefined3CheckBox = new javax.swing.JCheckBox();
        userDefined4CheckBox = new javax.swing.JCheckBox();
        userDefined5CheckBox = new javax.swing.JCheckBox();
        userDefined6CheckBox = new javax.swing.JCheckBox();
        userDefined7CheckBox = new javax.swing.JCheckBox();
        userDefined8CheckBox = new javax.swing.JCheckBox();
        jPanel13 = new javax.swing.JPanel();
        removeAllButton = new javax.swing.JButton();
        removeAllImagesButton = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        saveImageListButton = new javax.swing.JButton();
        saveSelectedImageListButton = new javax.swing.JButton();
        loadImageListButton = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        viewResultsGalleryButton = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        removeColorImageButton = new javax.swing.JButton();
        generateColorImageButton = new javax.swing.JButton();
        jPanel20 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel19 = new javax.swing.JPanel();
        removeImageCubeButton = new javax.swing.JButton();
        generateImageCubeButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        imageCubesDisplayedList = new javax.swing.JList();
        monochromePanel = new javax.swing.JPanel();
        hierarchicalSearchScrollPane = new javax.swing.JScrollPane();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        startDateLabel.setText("Start Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(startDateLabel, gridBagConstraints);

        startSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1126411200000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        jPanel1.add(startSpinner, gridBagConstraints);

        endDateLabel.setText("End Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(endDateLabel, gridBagConstraints);

        endSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1132462800000L), null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(162, 22));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel1.add(endSpinner, gridBagConstraints);

        sourceLabel.setText("Pointing:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel1.add(sourceLabel, gridBagConstraints);

        sourceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(sourceComboBox, gridBagConstraints);

        excludeGaskellCheckBox.setText("Exclude SPC-Derived");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(excludeGaskellCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel8.add(jPanel1, gridBagConstraints);

        filterCheckBoxPanel.setLayout(new java.awt.GridBagLayout());

        filter1CheckBox.setSelected(true);
        filter1CheckBox.setText("Filter 1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        filterCheckBoxPanel.add(filter1CheckBox, gridBagConstraints);

        filter3CheckBox.setSelected(true);
        filter3CheckBox.setText("Filter 3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter3CheckBox, gridBagConstraints);

        filter5CheckBox.setSelected(true);
        filter5CheckBox.setText("Filter 5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter5CheckBox, gridBagConstraints);

        filter7CheckBox.setSelected(true);
        filter7CheckBox.setText("Filter 7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter7CheckBox, gridBagConstraints);

        filter2CheckBox.setSelected(true);
        filter2CheckBox.setText("Filter 2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 8, 0, 0);
        filterCheckBoxPanel.add(filter2CheckBox, gridBagConstraints);

        filter4CheckBox.setSelected(true);
        filter4CheckBox.setText("Filter 4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter4CheckBox, gridBagConstraints);

        filter6CheckBox.setSelected(true);
        filter6CheckBox.setText("Filter 6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter6CheckBox, gridBagConstraints);

        filter8CheckBox.setSelected(true);
        filter8CheckBox.setText("Filter 8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter8CheckBox, gridBagConstraints);

        filter9CheckBox.setSelected(true);
        filter9CheckBox.setText("Filter 9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter9CheckBox, gridBagConstraints);

        filter10CheckBox.setSelected(true);
        filter10CheckBox.setText("Filter 10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter10CheckBox, gridBagConstraints);

        filter11CheckBox.setSelected(true);
        filter11CheckBox.setText("Filter 11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter11CheckBox, gridBagConstraints);

        filter12CheckBox.setSelected(true);
        filter12CheckBox.setText("Filter 12");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter12CheckBox, gridBagConstraints);

        filter13CheckBox.setSelected(true);
        filter13CheckBox.setText("Filter 13");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 0);
        filterCheckBoxPanel.add(filter13CheckBox, gridBagConstraints);

        filter15CheckBox.setSelected(true);
        filter15CheckBox.setText("Filter 15");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter15CheckBox, gridBagConstraints);

        filter17CheckBox.setSelected(true);
        filter17CheckBox.setText("Filter 17");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter17CheckBox, gridBagConstraints);

        filter19CheckBox.setSelected(true);
        filter19CheckBox.setText("Filter 19");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter19CheckBox, gridBagConstraints);

        filter14CheckBox.setSelected(true);
        filter14CheckBox.setText("Filter 14");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 8, 0, 0);
        filterCheckBoxPanel.add(filter14CheckBox, gridBagConstraints);

        filter16CheckBox.setSelected(true);
        filter16CheckBox.setText("Filter 16");
        filter16CheckBox.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter16CheckBox, gridBagConstraints);

        filter18CheckBox.setSelected(true);
        filter18CheckBox.setText("Filter 18");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter18CheckBox, gridBagConstraints);

        filter20CheckBox.setSelected(true);
        filter20CheckBox.setText("Filter 20");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter20CheckBox, gridBagConstraints);

        filter21CheckBox.setSelected(true);
        filter21CheckBox.setText("Filter 21");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 0);
        filterCheckBoxPanel.add(filter21CheckBox, gridBagConstraints);

        filter22CheckBox.setSelected(true);
        filter22CheckBox.setText("Filter 22");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 6);
        filterCheckBoxPanel.add(filter22CheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(filterCheckBoxPanel, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        fromDistanceLabel.setText("S/C Distance from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromDistanceLabel, gridBagConstraints);

        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromDistanceTextField, gridBagConstraints);

        toDistanceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toDistanceLabel, gridBagConstraints);

        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("26");
        toDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toDistanceTextField, gridBagConstraints);

        endDistanceLabel.setText("km");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endDistanceLabel, gridBagConstraints);

        fromResolutionLabel.setText("Resolution from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromResolutionLabel, gridBagConstraints);

        fromResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromResolutionTextField.setText("0");
        fromResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromResolutionTextField, gridBagConstraints);

        toResolutionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toResolutionLabel, gridBagConstraints);

        toResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toResolutionTextField.setText("3");
        toResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toResolutionTextField, gridBagConstraints);

        endResolutionLabel.setText("mpp");
        endResolutionLabel.setToolTipText("meters per pixel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endResolutionLabel, gridBagConstraints);

        fromIncidenceLabel.setText("Incidence from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromIncidenceLabel, gridBagConstraints);

        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromIncidenceTextField, gridBagConstraints);

        toIncidenceLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toIncidenceLabel, gridBagConstraints);

        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toIncidenceTextField, gridBagConstraints);

        endIncidenceLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endIncidenceLabel, gridBagConstraints);

        fromEmissionLabel.setText("Emission from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromEmissionLabel, gridBagConstraints);

        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromEmissionTextField, gridBagConstraints);

        toEmissionLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toEmissionLabel, gridBagConstraints);

        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toEmissionTextField, gridBagConstraints);

        endEmissionLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endEmissionLabel, gridBagConstraints);

        fromPhaseLabel.setText("Phase from");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 2);
        jPanel3.add(fromPhaseLabel, gridBagConstraints);

        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(fromPhaseTextField, gridBagConstraints);

        toPhaseLabel.setText("to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(toPhaseLabel, gridBagConstraints);

        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        jPanel3.add(toPhaseTextField, gridBagConstraints);

        endPhaseLabel.setText("deg");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 2, 1, 2);
        jPanel3.add(endPhaseLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel8.add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        searchByFilenameCheckBox.setText("Search by Filename");
        searchByFilenameCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                searchByFilenameCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel4.add(searchByFilenameCheckBox, gridBagConstraints);

        searchByFilenameTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 122;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel4.add(searchByFilenameTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 0, 0);
        jPanel8.add(jPanel4, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        clearRegionButton.setText("Clear Region");
        clearRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel5.add(clearRegionButton, gridBagConstraints);

        submitButton.setText("Search");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel5.add(submitButton, gridBagConstraints);

        selectRegionButton.setText("Select Region");
        selectRegionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        jPanel5.add(selectRegionButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel8.add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        resultsLabel.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(resultsLabel, gridBagConstraints);

        resultList.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "", "Id", "Filename", "Date"
                }
                ) {
            Class[] types = new Class [] {
                    java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                    false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultList.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(resultList);
        resultList.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel6.add(jScrollPane4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel8.add(jPanel6, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        jLabel6.setText("Number Boundaries:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(jLabel6, gridBagConstraints);

        numberOfBoundariesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130", "140", "150", "160", "170", "180", "190", "200", "210", "220", "230", "240", "250", " " }));
        numberOfBoundariesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberOfBoundariesComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(numberOfBoundariesComboBox, gridBagConstraints);

        prevButton.setText("<");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(prevButton, gridBagConstraints);

        nextButton.setText(">");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel7.add(nextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel8.add(jPanel7, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        jLabel20.setText("Color Image Generation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel9.add(jLabel20, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel9.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel8.add(jPanel9, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        redButton.setBackground(new java.awt.Color(255, 0, 0));
        redButton.setText("Red");
        redButton.setToolTipText("Select an image from the list above and then press this button");
        redButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(redButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(redLabel, gridBagConstraints);

        greenButton.setBackground(new java.awt.Color(0, 255, 0));
        greenButton.setText("Green");
        greenButton.setToolTipText("Select an image from the list above and then press this button");
        greenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel10.add(greenButton, gridBagConstraints);

        blueButton.setBackground(new java.awt.Color(0, 0, 255));
        blueButton.setText("Blue");
        blueButton.setToolTipText("Select an image from the list above and then press this button");
        blueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(blueButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(greenLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel10.add(blueLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(redComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(greenComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel10.add(blueComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        jPanel8.add(jPanel10, gridBagConstraints);

        jScrollPane3.setPreferredSize(new java.awt.Dimension(300, 100));

        colorImagesDisplayedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        colorImagesDisplayedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                colorImagesDisplayedListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                colorImagesDisplayedListMouseReleased(evt);
            }
        });
        jScrollPane3.setViewportView(colorImagesDisplayedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jScrollPane3, gridBagConstraints);

        jPanel11.setLayout(new java.awt.GridBagLayout());

        hasLimbComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "with or without", "with only", "without only" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 2, 0);
        jPanel11.add(hasLimbComboBox, gridBagConstraints);

        hasLimbLabel.setText("Limb:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel11.add(hasLimbLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jPanel8.add(jPanel11, gridBagConstraints);

        userDefinedCheckBoxPanel.setLayout(new java.awt.GridBagLayout());

        userDefined1CheckBox.setSelected(true);
        userDefined1CheckBox.setText("userDefined1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined1CheckBox, gridBagConstraints);

        userDefined2CheckBox.setSelected(true);
        userDefined2CheckBox.setText("userDefined2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined2CheckBox, gridBagConstraints);

        userDefined3CheckBox.setSelected(true);
        userDefined3CheckBox.setText("userDefined3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined3CheckBox, gridBagConstraints);

        userDefined4CheckBox.setSelected(true);
        userDefined4CheckBox.setText("userDefined4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined4CheckBox, gridBagConstraints);

        userDefined5CheckBox.setSelected(true);
        userDefined5CheckBox.setText("userDefined5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined5CheckBox, gridBagConstraints);

        userDefined6CheckBox.setSelected(true);
        userDefined6CheckBox.setText("userDefined6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined6CheckBox, gridBagConstraints);

        userDefined7CheckBox.setSelected(true);
        userDefined7CheckBox.setText("userDefined7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined7CheckBox, gridBagConstraints);

        userDefined8CheckBox.setSelected(true);
        userDefined8CheckBox.setText("userDefined8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        userDefinedCheckBoxPanel.add(userDefined8CheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel8.add(userDefinedCheckBoxPanel, gridBagConstraints);

        jPanel13.setLayout(new java.awt.GridBagLayout());

        removeAllButton.setText("Remove All Boundaries");
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        jPanel13.add(removeAllButton, gridBagConstraints);

        removeAllImagesButton.setText("Remove All Images");
        removeAllImagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllImagesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel13.add(removeAllImagesButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        jPanel8.add(jPanel13, gridBagConstraints);

        jPanel14.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        jPanel8.add(jPanel14, gridBagConstraints);

        jPanel15.setLayout(new java.awt.GridBagLayout());

        saveImageListButton.setText("Save List...");
        saveImageListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveImageListButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel15.add(saveImageListButton, gridBagConstraints);

        saveSelectedImageListButton.setText("Save Selected List...");
        saveSelectedImageListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSelectedImageListButtonActionPerformed(evt);
            }
        });
        jPanel15.add(saveSelectedImageListButton, new java.awt.GridBagConstraints());

        loadImageListButton.setText("Load List...");
        loadImageListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImageListButtonActionPerformed(evt);
            }
        });
        jPanel15.add(loadImageListButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        jPanel8.add(jPanel15, gridBagConstraints);

        jPanel16.setLayout(new java.awt.GridBagLayout());

        viewResultsGalleryButton.setText("View Search Results as Image Gallery");
        viewResultsGalleryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewResultsGalleryButtonActionPerformed(evt);
            }
        });
        jPanel16.add(viewResultsGalleryButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        jPanel8.add(jPanel16, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        jPanel8.add(jPanel17, gridBagConstraints);

        removeColorImageButton.setText("Remove Color Image");
        removeColorImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColorImageButtonActionPerformed(evt);
            }
        });
        jPanel18.add(removeColorImageButton);

        generateColorImageButton.setText("Generate Color Image");
        generateColorImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateColorImageButtonActionPerformed(evt);
            }
        });
        jPanel18.add(generateColorImageButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 23;
        jPanel8.add(jPanel18, gridBagConstraints);

        jPanel20.setLayout(new java.awt.GridBagLayout());

        jLabel21.setText("Image Cube Generation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel20.add(jLabel21, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel20.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPanel8.add(jPanel20, gridBagConstraints);

        removeImageCubeButton.setText("Remove Image Cube");
        removeImageCubeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeImageCubeButtonActionPerformed(evt);
            }
        });
        jPanel19.add(removeImageCubeButton);

        generateImageCubeButton.setText("Generate Image Cube");
        generateImageCubeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateImageCubeButtonActionPerformed(evt);
            }
        });
        jPanel19.add(generateImageCubeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        jPanel8.add(jPanel19, gridBagConstraints);

        jScrollPane5.setPreferredSize(new java.awt.Dimension(300, 100));

        imageCubesDisplayedList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        imageCubesDisplayedList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                imageCubesDisplayedListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                imageCubesDisplayedListMouseReleased(evt);
            }
        });
        jScrollPane5.setViewportView(imageCubesDisplayedList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jScrollPane5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel8.add(monochromePanel, gridBagConstraints);

        hierarchicalSearchScrollPane.setPreferredSize(new java.awt.Dimension(300, 200));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel8.add(hierarchicalSearchScrollPane, gridBagConstraints);

        jScrollPane2.setViewportView(jPanel8);

        add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentHidden(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentHidden
    {//GEN-HEADEREND:event_formComponentHidden
        selectRegionButton.setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_formComponentHidden

    private void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_startSpinnerStateChanged
    {//GEN-HEADEREND:event_startSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)startSpinner.getModel()).getDate();
        if (date != null)
            startDate = date;
    }//GEN-LAST:event_startSpinnerStateChanged

    private void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_endSpinnerStateChanged
    {//GEN-HEADEREND:event_endSpinnerStateChanged
        java.util.Date date =
                ((SpinnerDateModel)endSpinner.getModel()).getDate();
        if (date != null)
            endDate = date;

    }//GEN-LAST:event_endSpinnerStateChanged

    private void searchByFilenameCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_searchByFilenameCheckBoxItemStateChanged
    {//GEN-HEADEREND:event_searchByFilenameCheckBoxItemStateChanged
        boolean enable = evt.getStateChange() == ItemEvent.SELECTED;
        searchByFilenameTextField.setEnabled(enable);
        startDateLabel.setEnabled(!enable);
        startSpinner.setEnabled(!enable);
        endDateLabel.setEnabled(!enable);
        endSpinner.setEnabled(!enable);
        filter1CheckBox.setEnabled(!enable);
        filter2CheckBox.setEnabled(!enable);
        filter3CheckBox.setEnabled(!enable);
        filter4CheckBox.setEnabled(!enable);
        filter5CheckBox.setEnabled(!enable);
        filter6CheckBox.setEnabled(!enable);
        filter7CheckBox.setEnabled(!enable);
        filter8CheckBox.setEnabled(!enable);
        filter9CheckBox.setEnabled(!enable);
        filter10CheckBox.setEnabled(!enable);
        filter11CheckBox.setEnabled(!enable);
        filter12CheckBox.setEnabled(!enable);
        filter13CheckBox.setEnabled(!enable);
        filter14CheckBox.setEnabled(!enable);
        filter15CheckBox.setEnabled(!enable);
        filter16CheckBox.setEnabled(!enable);
        filter17CheckBox.setEnabled(!enable);
        filter18CheckBox.setEnabled(!enable);
        filter19CheckBox.setEnabled(!enable);
        filter20CheckBox.setEnabled(!enable);
        filter21CheckBox.setEnabled(!enable);
        filter22CheckBox.setEnabled(!enable);
        userDefined1CheckBox.setEnabled(!enable);
        userDefined2CheckBox.setEnabled(!enable);
        userDefined3CheckBox.setEnabled(!enable);
        userDefined4CheckBox.setEnabled(!enable);
        userDefined5CheckBox.setEnabled(!enable);
        userDefined6CheckBox.setEnabled(!enable);
        userDefined7CheckBox.setEnabled(!enable);
        userDefined8CheckBox.setEnabled(!enable);
        hasLimbLabel.setEnabled(!enable);
        hasLimbComboBox.setEnabled(!enable);
        fromDistanceLabel.setEnabled(!enable);
        fromDistanceTextField.setEnabled(!enable);
        toDistanceLabel.setEnabled(!enable);
        toDistanceTextField.setEnabled(!enable);
        endDistanceLabel.setEnabled(!enable);
        fromResolutionLabel.setEnabled(!enable);
        fromResolutionTextField.setEnabled(!enable);
        toResolutionLabel.setEnabled(!enable);
        toResolutionTextField.setEnabled(!enable);
        endResolutionLabel.setEnabled(!enable);
        fromIncidenceLabel.setEnabled(!enable);
        fromIncidenceTextField.setEnabled(!enable);
        toIncidenceLabel.setEnabled(!enable);
        toIncidenceTextField.setEnabled(!enable);
        endIncidenceLabel.setEnabled(!enable);
        fromEmissionLabel.setEnabled(!enable);
        fromEmissionTextField.setEnabled(!enable);
        toEmissionLabel.setEnabled(!enable);
        toEmissionTextField.setEnabled(!enable);
        endEmissionLabel.setEnabled(!enable);
        fromPhaseLabel.setEnabled(!enable);
        fromPhaseTextField.setEnabled(!enable);
        toPhaseLabel.setEnabled(!enable);
        toPhaseTextField.setEnabled(!enable);
        endPhaseLabel.setEnabled(!enable);
    }//GEN-LAST:event_searchByFilenameCheckBoxItemStateChanged

    private void selectRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectRegionButtonActionPerformed
    {//GEN-HEADEREND:event_selectRegionButtonActionPerformed
        if (selectRegionButton.isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }//GEN-LAST:event_selectRegionButtonActionPerformed

    private void clearRegionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearRegionButtonActionPerformed
    {//GEN-HEADEREND:event_clearRegionButtonActionPerformed
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION).get(0);
        selectionModel.removeAllStructures();
    }//GEN-LAST:event_clearRegionButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prevButtonActionPerformed
    {//GEN-HEADEREND:event_prevButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
                resultIntervalCurrentlyShown.prevBlock(Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }

    }//GEN-LAST:event_prevButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_nextButtonActionPerformed
    {//GEN-HEADEREND:event_nextButtonActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            if (resultIntervalCurrentlyShown.id2 < resultList.getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem()));
            showImageBoundaries(resultIntervalCurrentlyShown);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void redButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_redButtonActionPerformed
    {//GEN-HEADEREND:event_redButtonActionPerformed
        int index = resultList.getSelectedRow();
        if (index >= 0)
        {
            String image = imageRawResults.get(index).get(0);
            String name = new File(image).getName();
            image = image.substring(0,image.length()-4);
            selectedRedKey = createImageKey(image, sourceOfLastQuery, instrument);
            if (!selectedRedKey.band.equals("0"))
                name = selectedRedKey.band + ":" + name;
            redLabel.setText(name);
        }
    }//GEN-LAST:event_redButtonActionPerformed

    private void greenButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_greenButtonActionPerformed
    {//GEN-HEADEREND:event_greenButtonActionPerformed
        int index = resultList.getSelectedRow();
        if (index >= 0)
        {
            String image = imageRawResults.get(index).get(0);
            String name = new File(image).getName();
            image = image.substring(0,image.length()-4);
            greenLabel.setText(name);
            selectedGreenKey = createImageKey(image, sourceOfLastQuery, instrument);
            if (!selectedGreenKey.band.equals("0"))
                name = selectedGreenKey.band + ":" + name;
            greenLabel.setText(name);
        }
    }//GEN-LAST:event_greenButtonActionPerformed

    private void blueButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_blueButtonActionPerformed
    {//GEN-HEADEREND:event_blueButtonActionPerformed
        int index = resultList.getSelectedRow();
        if (index >= 0)
        {
            String image = imageRawResults.get(index).get(0);
            String name = new File(image).getName();
            image = image.substring(0,image.length()-4);
            blueLabel.setText(name);
            selectedBlueKey = createImageKey(image, sourceOfLastQuery, instrument);
            if (!selectedBlueKey.band.equals("0"))
                name = selectedBlueKey.band + ":" + name;
            blueLabel.setText(name);
        }
    }//GEN-LAST:event_blueButtonActionPerformed

    private void generateColorImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateColorImageButtonActionPerformed
    {//GEN-HEADEREND:event_generateColorImageButtonActionPerformed
        generateColorImage(evt);
    }//GEN-LAST:event_generateColorImageButtonActionPerformed

    private void removeColorImageButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeColorImageButtonActionPerformed
    {//GEN-HEADEREND:event_removeColorImageButtonActionPerformed
        removeColorImage(evt);
    }//GEN-LAST:event_removeColorImageButtonActionPerformed

    private void colorImagesDisplayedListMousePressed(java.awt.event.MouseEvent evt)//GEN-FIRST:event_colorImagesDisplayedListMousePressed
    {//GEN-HEADEREND:event_colorImagesDisplayedListMousePressed
        colorImagesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_colorImagesDisplayedListMousePressed

    private void colorImagesDisplayedListMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_colorImagesDisplayedListMouseReleased
    {//GEN-HEADEREND:event_colorImagesDisplayedListMouseReleased
        colorImagesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_colorImagesDisplayedListMouseReleased

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllButtonActionPerformed
        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
        model.removeAllBoundaries();
        resultIntervalCurrentlyShown = null;
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeAllImagesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeAllImagesButtonActionPerformed
    {//GEN-HEADEREND:event_removeAllImagesButtonActionPerformed
        ImageCollection model = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        model.removeImages(ImageSource.GASKELL);
        model.removeImages(ImageSource.GASKELL_UPDATED);
        model.removeImages(ImageSource.SPICE);
        model.removeImages(ImageSource.CORRECTED_SPICE);
        model.removeImages(ImageSource.CORRECTED);
    }//GEN-LAST:event_removeAllImagesButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
    {//GEN-HEADEREND:event_submitButtonActionPerformed
        try
        {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            selectRegionButton.setSelected(false);
            pickManager.setPickMode(PickMode.DEFAULT);

            String searchField = null;
            if (searchByFilenameCheckBox.isSelected())
                searchField = searchByFilenameTextField.getText().trim();

            GregorianCalendar startDateGreg = new GregorianCalendar();
            GregorianCalendar endDateGreg = new GregorianCalendar();
            startDateGreg.setTime(startDate);
            endDateGreg.setTime(endDate);
            DateTime startDateJoda = new DateTime(
                    startDateGreg.get(GregorianCalendar.YEAR),
                    startDateGreg.get(GregorianCalendar.MONTH)+1,
                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    startDateGreg.get(GregorianCalendar.MINUTE),
                    startDateGreg.get(GregorianCalendar.SECOND),
                    startDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);
            DateTime endDateJoda = new DateTime(
                    endDateGreg.get(GregorianCalendar.YEAR),
                    endDateGreg.get(GregorianCalendar.MONTH)+1,
                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                    endDateGreg.get(GregorianCalendar.MINUTE),
                    endDateGreg.get(GregorianCalendar.SECOND),
                    endDateGreg.get(GregorianCalendar.MILLISECOND),
                    DateTimeZone.UTC);

            TreeSet<Integer> cubeList = null;
            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION).get(0);
            SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY).get(0);
            if (selectionModel.getNumItems() > 0)
            {
                int numberOfSides = selectionModel.getNumberOfSides();
                Ellipse region = selectionModel.getItem(0);

                // Always use the lowest resolution model for getting the intersection cubes list.
                // Therefore, if the selection region was created using a higher resolution model,
                // we need to recompute the selection region using the low res model.
                if (smallBodyModel.getModelResolution() > 0)
                {
                    vtkPolyData interiorPoly = new vtkPolyData();
                    smallBodyModel.drawRegularPolygonLowRes(region.getCenter().toArray(), region.getRadius(), numberOfSides, interiorPoly, null);
                    cubeList = smallBodyModel.getIntersectingCubes(interiorPoly);
                }
                else
                {
                    cubeList = smallBodyModel.getIntersectingCubes(selectionModel.getVtkInteriorPolyDataFor(region));
                }
            }

            ImageSource imageSource = (ImageSource) sourceComboBox.getSelectedItem();

            // Populate camera and filter list differently based on if we are doing sum-of-products or product-of-sums search
            boolean sumOfProductsSearch;
            List<Integer> camerasSelected;
            List<Integer> filtersSelected;
            if(smallBodyConfig.hasHierarchicalImageSearch)
            {
                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
                sumOfProductsSearch = true;

                // Process the user's selections
                Selection selection = smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections();

                // Get the selected (camera,filter) pairs
                camerasSelected = selection.getSelectedCameras();
                filtersSelected = selection.getSelectedFilters();
            }
            else
            {
                // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
                sumOfProductsSearch = false;

                // Populate list of selected cameras
                camerasSelected = new LinkedList<Integer>();
                int numberOfCameras = getNumberOfUserDefinedCheckBoxesActuallyUsed();
                for (int i=0; i<numberOfCameras; i++)
                {
                    if(userDefinedCheckBoxes[i].isSelected())
                    {
                        camerasSelected.add(i);
                    }
                }

                // Populate list of selected filters
                filtersSelected = new LinkedList<Integer>();
                int numberOfFilters = getNumberOfFiltersActuallyUsed();
                for (int i=0; i<numberOfFilters; i++)
                {
                    if(filterCheckBoxes[i].isSelected())
                    {
                        filtersSelected.add(i);
                    }
                }
            }

            List<List<String>> results = null;
            //            System.out.println(
            //                    "ImagingSearchPanel: submitButtonActionPerformed: search query type " + instrument.searchQuery.getClass());
            if (instrument.searchQuery instanceof FixedListQuery)
            {
                FixedListQuery query = (FixedListQuery) instrument.searchQuery;
                results = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource)).getResultlist();
            }
            else
            {
                // Run queries based on user specifications
                ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                        Range.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
                        searchField, null,
                        Range.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
                        Range.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
                        Range.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
                        sumOfProductsSearch, camerasSelected, filtersSelected,
                        Range.closed(Double.valueOf(fromResolutionTextField.getText()), Double.valueOf(toResolutionTextField.getText())),
                        cubeList, imageSource, hasLimbComboBox.getSelectedIndex());

                results = instrument.searchQuery.runQuery(searchMetadata).getResultlist();
            }

            //ALL OF THE BRANCHES BELOW CALL IDENTICAL CODE!
            //            if (instrument.spectralMode == SpectralMode.MULTI)
            //            {
            //                ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
            //                        Ranges.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
            //                        searchField, null,
            //                        Ranges.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
            //                        Ranges.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
            //                        Ranges.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
            //                        sumOfProductsSearch, camerasSelected, filtersSelected,
            //                        Ranges.closed(Double.valueOf(fromResolutionTextField.getText()), Double.valueOf(toResolutionTextField.getText())),
            //                        cubeList, imageSource, hasLimbComboBox.getSelectedIndex());
            //
            //                results = instrument.searchQuery.runQuery(searchMetadata).getResultlist();
            //
            //                results = instrument.searchQuery.runQuery(
            //                    "",
            //                    startDateJoda,
            //                    endDateJoda,
            //                    sumOfProductsSearch,
            //                    camerasSelected,
            //                    filtersSelected,
            //                    Double.parseDouble(fromDistanceTextField.getText()),
            //                    Double.parseDouble(toDistanceTextField.getText()),
            //                    Double.parseDouble(fromResolutionTextField.getText()),
            //                    Double.parseDouble(toResolutionTextField.getText()),
            //                    searchField,
            //                    null,
            //                    Double.parseDouble(fromIncidenceTextField.getText()),
            //                    Double.parseDouble(toIncidenceTextField.getText()),
            //                    Double.parseDouble(fromEmissionTextField.getText()),
            //                    Double.parseDouble(toEmissionTextField.getText()),
            //                    Double.parseDouble(fromPhaseTextField.getText()),
            //                    Double.parseDouble(toPhaseTextField.getText()),
            //                    cubeList,
            //                    imageSource,
            //                    hasLimbComboBox.getSelectedIndex());
            //            }
            //            else if (instrument.spectralMode == SpectralMode.HYPER)
            //            {
            //                results = instrument.searchQuery.runQuery(
            //                    "",
            //                    startDateJoda,
            //                    endDateJoda,
            //                    sumOfProductsSearch,
            //                    camerasSelected,
            //                    filtersSelected,
            //                    Double.parseDouble(fromDistanceTextField.getText()),
            //                    Double.parseDouble(toDistanceTextField.getText()),
            //                    Double.parseDouble(fromResolutionTextField.getText()),
            //                    Double.parseDouble(toResolutionTextField.getText()),
            //                    searchField,
            //                    null,
            //                    Double.parseDouble(fromIncidenceTextField.getText()),
            //                    Double.parseDouble(toIncidenceTextField.getText()),
            //                    Double.parseDouble(fromEmissionTextField.getText()),
            //                    Double.parseDouble(toEmissionTextField.getText()),
            //                    Double.parseDouble(fromPhaseTextField.getText()),
            //                    Double.parseDouble(toPhaseTextField.getText()),
            //                    cubeList,
            //                    imageSource,
            //                    hasLimbComboBox.getSelectedIndex());
            //            }
            //            else
            //            {
            //                results = instrument.searchQuery.runQuery(
            //                    "",
            //                    startDateJoda,
            //                    endDateJoda,
            //                    sumOfProductsSearch,
            //                    camerasSelected,
            //                    filtersSelected,
            //                    Double.parseDouble(fromDistanceTextField.getText()),
            //                    Double.parseDouble(toDistanceTextField.getText()),
            //                    Double.parseDouble(fromResolutionTextField.getText()),
            //                    Double.parseDouble(toResolutionTextField.getText()),
            //                    searchField,
            //                    null,
            //                    Double.parseDouble(fromIncidenceTextField.getText()),
            //                    Double.parseDouble(toIncidenceTextField.getText()),
            //                    Double.parseDouble(fromEmissionTextField.getText()),
            //                    Double.parseDouble(toEmissionTextField.getText()),
            //                    Double.parseDouble(fromPhaseTextField.getText()),
            //                    Double.parseDouble(toPhaseTextField.getText()),
            //                    cubeList,
            //                    imageSource,
            //                    hasLimbComboBox.getSelectedIndex());
            //            }

            // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
            // then remove from the list images which are contained in the other list by doing
            // an additional search.
            if (imageSource == ImageSource.SPICE && excludeGaskellCheckBox.isSelected())
            {
                List<List<String>> resultsOtherSource = null;
                if (instrument.searchQuery instanceof FixedListQuery)
                {
                    FixedListQuery query = (FixedListQuery)instrument.searchQuery;
                    //                    FileInfo info = FileCache.getFileInfoFromServer(query.getRootPath() + "/" /*+ dataListPrefix + "/"*/ + imageListName);
                    //                    if (!info.isExistsOnServer().equals(YesOrNo.YES))
                    //                    {
                    //                        System.out.println("Could not find " + imageListName + ". Using imagelist.txt instead");
                    //                        imageListName = "imagelist.txt";
                    //                    }
                    resultsOtherSource = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist" /*imageListName*/, "images", query.getRootPath(), imageSource)).getResultlist();
                }
                else
                {

                    ImageDatabaseSearchMetadata searchMetadataOther = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                            Range.closed(Double.valueOf(fromDistanceTextField.getText()), Double.valueOf(toDistanceTextField.getText())),
                            searchField, null,
                            Range.closed(Double.valueOf(fromIncidenceTextField.getText()), Double.valueOf(toIncidenceTextField.getText())),
                            Range.closed(Double.valueOf(fromEmissionTextField.getText()), Double.valueOf(toEmissionTextField.getText())),
                            Range.closed(Double.valueOf(fromPhaseTextField.getText()), Double.valueOf(toPhaseTextField.getText())),
                            sumOfProductsSearch, camerasSelected, filtersSelected,
                            Range.closed(Double.valueOf(fromResolutionTextField.getText()), Double.valueOf(toResolutionTextField.getText())),
                            cubeList, imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE, hasLimbComboBox.getSelectedIndex());

                    resultsOtherSource = instrument.searchQuery.runQuery(searchMetadataOther).getResultlist();

                }


                //                List<List<String>> resultsOtherSource = instrument.searchQuery.runQuery(
                //                        "",
                //                        startDateJoda,
                //                        endDateJoda,
                //                        sumOfProductsSearch,
                //                        camerasSelected,
                //                        filtersSelected,
                //                        Double.parseDouble(fromDistanceTextField.getText()),
                //                        Double.parseDouble(toDistanceTextField.getText()),
                //                        Double.parseDouble(fromResolutionTextField.getText()),
                //                        Double.parseDouble(toResolutionTextField.getText()),
                //                        searchField,
                //                        null,
                //                        Double.parseDouble(fromIncidenceTextField.getText()),
                //                        Double.parseDouble(toIncidenceTextField.getText()),
                //                        Double.parseDouble(fromEmissionTextField.getText()),
                //                        Double.parseDouble(toEmissionTextField.getText()),
                //                        Double.parseDouble(fromPhaseTextField.getText()),
                //                        Double.parseDouble(toPhaseTextField.getText()),
                //                        cubeList,
                //                        imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE,
                //                        hasLimbComboBox.getSelectedIndex());

                int numOtherResults = resultsOtherSource.size();
                for (int i=0; i<numOtherResults; ++i)
                {
                    String imageName = resultsOtherSource.get(i).get(0);
                    int numResults = results.size();
                    for (int j=0; j<numResults; ++j)
                    {
                        if (results.get(j).get(0).startsWith(imageName))
                        {
                            results.remove(j);
                            break;
                        }
                    }
                }
            }

            sourceOfLastQuery = imageSource;

            setImageResults(processResults(results));
            setCursor(Cursor.getDefaultCursor());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    protected List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }

    private void sourceComboBoxItemStateChanged(java.awt.event.ItemEvent evt)
    {//GEN-FIRST:event_sourceComboBoxItemStateChanged
        ImageSource imageSource = (ImageSource) sourceComboBox.getSelectedItem();
        for (int i=0; i< sourceComboBox.getModel().getSize(); i++)
        {
            ImageSource source = sourceComboBox.getItemAt(i);
            if (source == ImageSource.GASKELL_UPDATED)
            {
                excludeGaskellCheckBox.setVisible(imageSource == ImageSource.SPICE);
            }
        }
    }//GEN-LAST:event_sourceComboBoxItemStateChanged

    private void numberOfBoundariesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberOfBoundariesComboBoxActionPerformed
        if (resultIntervalCurrentlyShown != null)
        {
            // Only update if there's been a change in what is selected
            int newMaxId = resultIntervalCurrentlyShown.id1 + Integer.parseInt((String)this.numberOfBoundariesComboBox.getSelectedItem());
            if (newMaxId != resultIntervalCurrentlyShown.id2)
            {
                resultIntervalCurrentlyShown.id2 = newMaxId;
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
    }//GEN-LAST:event_numberOfBoundariesComboBoxActionPerformed

    private void saveImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveImageListButtonActionPerformed
        File file = CustomFileChooser.showSaveDialog(this, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing"  + nl);
                int size = imageRawResults.size();
                for (int i=0; i<size; ++i)
                {
                    String image = new File(imageRawResults.get(i).get(0)).getName();
                    String dtStr = imageRawResults.get(i).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + STANDARD_UTC_FORMAT.format(dt) + " " + sourceOfLastQuery.toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_saveImageListButtonActionPerformed

    private void loadImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadImageListButtonActionPerformed
        File file = CustomFileChooser.showOpenDialog(this, "Select File");

        if (file != null)
        {
            try
            {
                List<List<String>> results = new ArrayList<List<String>>();
                List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
                for (int i=0; i<lines.size(); ++i)
                {
                    if (lines.get(i).startsWith("#")) continue;
                    String[] words = lines.get(i).trim().split("\\s+");
                    List<String> result = new ArrayList<String>();
                    String name = instrument.searchQuery.getDataPath() + "/" + words[0];
                    result.add(name);
                    Date dt = STANDARD_UTC_FORMAT.parse(words[1]);
                    result.add(String.valueOf(dt.getTime()));
                    results.add(result);
                }

                sourceOfLastQuery = (ImageSource) sourceComboBox.getSelectedItem();

                setImageResults(processResults(results));
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error reading the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }

    }//GEN-LAST:event_loadImageListButtonActionPerformed

    private void saveSelectedImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSelectedImageListButtonActionPerformed
        File file = CustomFileChooser.showSaveDialog(this, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String nl = System.getProperty("line.separator");
                out.write("#Image_Name Image_Time_UTC Pointing"  + nl);
                int[] selectedIndices = resultList.getSelectedRows();
                for (int selectedIndex : selectedIndices)
                {
                    String image = new File(imageRawResults.get(selectedIndex).get(0)).getName();
                    String dtStr = imageRawResults.get(selectedIndex).get(1);
                    Date dt = new Date(Long.parseLong(dtStr));

                    out.write(image + " " + sdf.format(dt) + " " + sourceOfLastQuery.toString().replaceAll(" ", "_") + nl);
                }

                out.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_saveSelectedImageListButtonActionPerformed

    private void removeImageCubeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeImageCubeButtonActionPerformed
        removeImageCube(evt);
    }//GEN-LAST:event_removeImageCubeButtonActionPerformed

    private void generateImageCubeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateImageCubeButtonActionPerformed
        generateImageCube(evt);
    }//GEN-LAST:event_generateImageCubeButtonActionPerformed

    private void imageCubesDisplayedListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageCubesDisplayedListMousePressed
        imageCubesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageCubesDisplayedListMousePressed

    private void imageCubesDisplayedListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageCubesDisplayedListMouseReleased
        imageCubesDisplayedListMaybeShowPopup(evt);
    }//GEN-LAST:event_imageCubesDisplayedListMouseReleased

    private void viewResultsGalleryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewResultsGalleryButtonActionPerformed
        // Each time gallery results are viewed, try to get a gallery generator.
        ImageGalleryGenerator galleryGenerator = ImageGalleryGenerator.of(instrument);

        // Check if image search results are valid and nonempty
        if(imageRawResults != null && galleryGenerator != null)
        {
            // Create list of gallery and preview image names based on results
            List<ImageGalleryEntry> galleryEntries = new LinkedList<ImageGalleryEntry>();
            for(List<String> res : imageRawResults)
            {
                ImageGalleryEntry entry = galleryGenerator.getEntry(res.get(0));
                galleryEntries.add(entry);
            }

            // Don't bother creating a gallery if empty
            if(galleryEntries.isEmpty())
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "Unable to generate gallery.  Gallery images corresponding to search results are not registered.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create preview gallery based on search results
            String galleryURL = galleryGenerator.generateGallery(galleryEntries);

            // Show gallery preview in browser
            try
            {
                java.awt.Desktop.getDesktop().browse(new File(galleryURL).toURI());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_viewResultsGalleryButtonActionPerformed


    //    protected boolean imageVisible(ImageKey key)
    //    {
    //        return true;
    //    }

    protected void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        images.addImage(key);
        //        if (!imageVisible(key))
        //        images.getImage(key).setVisible(false);
    }

    protected void loadImages(String name)
    {

        List<ImageKey> keys = createImageKeys(name, sourceOfLastQuery, instrument);
        for (ImageKey key : keys)
        {
            try
            {
                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
                if (!images.containsImage(key))
                {
                    loadImage(key, images);
                }
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }

        }
    }

    protected void unloadImage(ImageKeyInterface key, ImageCollection images)
    {
        images.removeImage(key);

    }

    protected void unloadImages(String name)
    {

        List<ImageKey> keys = createImageKeys(name, sourceOfLastQuery, instrument);
        for (ImageKeyInterface key : keys)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            unloadImage(key, images);
        }
    }

    public JList getImageCubesDisplayedList()
    {
        return imageCubesDisplayedList;
    }

    protected void setImageVisibility(String name, boolean visible)
    {
        List<ImageKey> keys = createImageKeys(name, sourceOfLastQuery, instrument);
        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        for (ImageKeyInterface key : keys)
        {
            if (images.containsImage(key))
            {
                Image image = images.getImage(key);
                image.setVisible(visible);
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        if (e.getColumn() == mapColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            String namePrefix = name.substring(0, name.length()-4);
            if ((Boolean)resultList.getValueAt(row, mapColumnIndex))
                loadImages(namePrefix);
            else
            {
                unloadImages(namePrefix);
                LightUtil.switchToLightKit(renderer);
            }
        }
        else if (e.getColumn() == showFootprintColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            String namePrefix = name.substring(0, name.length()-4);
            boolean visible = (Boolean)resultList.getValueAt(row, showFootprintColumnIndex);
            setImageVisibility(namePrefix, visible);
        }
        else if (e.getColumn() == frusColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            ImageKeyInterface key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            if (images.containsImage(key))
            {
                PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                image.setShowFrustum(!image.isFrustumShowing());
            }
        }
        else if (e.getColumn() == bndrColumnIndex)
        {
            int row = e.getFirstRow();
            String name = imageRawResults.get(row).get(0);
            ImageKey key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            try
            {
                PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
                if (!boundaries.containsBoundary(key))
                    boundaries.addBoundary(key);
                else
                    boundaries.removeBoundary(key);
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this),
                        "There was an error mapping the boundary.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }
        }
    }

    public class StringRenderer extends DefaultTableCellRenderer
    {
        PerspectiveImageBoundaryCollection model = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());

        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus,
                int row, int column)
        {
            Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String name = imageRawResults.get(row).get(0);
            //            ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            ImageKeyInterface key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
            if (model.containsBoundary(key))
            {
                int[] c = model.getBoundary(key).getBoundaryColor();
                if (isSelected)
                {
                    co.setForeground(new Color(c[0], c[1], c[2]));
                    co.setBackground(table.getSelectionBackground());
                }
                else
                {
                    co.setForeground(new Color(c[0], c[1], c[2]));
                    co.setBackground(table.getBackground());
                }
            }
            else
            {
                if (isSelected)
                {
                    co.setForeground(table.getSelectionForeground());
                    co.setBackground(table.getSelectionBackground());
                }
                else
                {
                    co.setForeground(table.getForeground());
                    co.setBackground(table.getBackground());
                }
            }

            return co;
        }
    }

    public class StructuresTableModel extends DefaultTableModel
    {
        public StructuresTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == showFootprintColumnIndex || column == frusColumnIndex)
            {
                String name = imageRawResults.get(row).get(0);
                //                ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageKeyInterface key = createImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
                return images.containsImage(key);
            }
            else
            {
                return column == mapColumnIndex || column == bndrColumnIndex;
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= bndrColumnIndex)
                return Boolean.class;
            else
                return String.class;
        }
    }

    //    class TableMouseHandler extends MouseAdapter
    //    {
    public void mousePressed(MouseEvent e)
    {
        resultsListMaybeShowPopup(e);
        saveSelectedImageListButton.setEnabled(resultList.getSelectedRowCount() > 0);
    }

    public void mouseReleased(MouseEvent e)
    {
        resultsListMaybeShowPopup(e);
        saveSelectedImageListButton.setEnabled(resultList.getSelectedRowCount() > 0);
    }


    //    }

    @Override
    public void mouseClicked(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent arg0)
    {
        // TODO Auto-generated method stub

    }

    protected JComboBox getNumberOfBoundariesComboBox()
    {
        return numberOfBoundariesComboBox;
    }

    protected JTable getResultList()
    {
        return resultList;
    }

    public void initializeStateManager()
    {
        if (stateManager == null) {
            stateManager = new MetadataManager() {
                final Key<String> pointingKey = Key.of("pointing");
                final Key<Boolean> excludeSPCKey = Key.of("excludeSPC");
                final Key<Date> startDateKey = Key.of("startDate");
                final Key<Date> endDateKey = Key.of("endDate");
                final Key<String> limbSelectedKey = Key.of("limb");
                final Key<Double> fromDistanceKey = Key.of("fromDistance");
                final Key<Double> toDistanceKey = Key.of("toDistance");
                final Key<Double> fromResolutionKey = Key.of("fromResolution");
                final Key<Double> toResolutionKey = Key.of("toResolution");
                final Key<Double> fromIncidenceKey = Key.of("fromIncidence");
                final Key<Double> toIncidenceKey = Key.of("toIncidence");
                final Key<Double> fromEmissionKey = Key.of("fromEmission");
                final Key<Double> toEmissionKey = Key.of("toEmission");
                final Key<Double> fromPhaseKey = Key.of("fromPhase");
                final Key<Double> toPhaseKey = Key.of("toPhase");
                final Key<Boolean> searchByFileNameEnabledKey = Key.of("searchByFileNameEnabled");
                final Key<String> searchByFileNameKey = Key.of("searchByFileName");
                final Key<Map<String, Boolean>> filterMapKey = Key.of("filters");
                final Key<Map<String, Boolean>> userCheckBoxMapKey = Key.of("userCheckBoxes");
                final Key<Metadata> circleSelectionKey = Key.of("circleSelection");
                final Key<Metadata> imageTreeFilterKey = Key.of("imageTreeFilters");
                final Key<List<String[]>> imageListKey = Key.of("imageList");
                final Key<Set<String>> selectedImagesKey = Key.of("imagesSelected");
                final Key<Map<String, Boolean>> isShowingKey = Key.of("imagesShowing");
                final Key<Map<String, Boolean>> isFrustrumShowingKey = Key.of("frustrumShowing");
                final Key<Map<String, Boolean>> isBoundaryShowingKey = Key.of("boundaryShowing");

                @Override
                public Metadata store()
                {
                    SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

                    ImageSource pointing = (ImageSource) sourceComboBox.getSelectedItem();
                    result.put(pointingKey, pointing.name());

                    if (excludeGaskellCheckBox.isEnabled()) {
                        result.put(excludeSPCKey, excludeGaskellCheckBox.isSelected());
                    }

                    result.put(startDateKey, (Date) startSpinner.getValue());
                    result.put(endDateKey, (Date) endSpinner.getValue());
                    result.put(limbSelectedKey, (String) hasLimbComboBox.getSelectedItem());
                    result.put(fromDistanceKey, Double.valueOf(fromDistanceTextField.getText()));
                    result.put(toDistanceKey, Double.valueOf(toDistanceTextField.getText()));
                    result.put(fromResolutionKey, Double.valueOf(fromResolutionTextField.getText()));
                    result.put(toResolutionKey, Double.valueOf(toResolutionTextField.getText()));
                    result.put(fromIncidenceKey, Double.valueOf(fromIncidenceTextField.getText()));
                    result.put(toIncidenceKey, Double.valueOf(toIncidenceTextField.getText()));
                    result.put(fromEmissionKey, Double.valueOf(fromEmissionTextField.getText()));
                    result.put(toEmissionKey, Double.valueOf(toEmissionTextField.getText()));
                    result.put(fromPhaseKey, Double.valueOf(fromPhaseTextField.getText()));
                    result.put(toPhaseKey, Double.valueOf(toPhaseTextField.getText()));
                    result.put(searchByFileNameEnabledKey, searchByFilenameCheckBox.isSelected());
                    result.put(searchByFileNameKey, searchByFilenameTextField.getText());

                    if (smallBodyConfig.hasHierarchicalImageSearch)
                    {
                        MetadataManager manager = smallBodyConfig.hierarchicalImageSearchSpecification.getMetadataManager();
                        result.put(imageTreeFilterKey, manager.store());
                    }
                    else
                    {
                        int numberFilters = 0;

                        // Regular filters.
                        numberFilters = getNumberOfFiltersActuallyUsed();
                        if (numberFilters > 0)
                        {
                            ImmutableMap.Builder<String, Boolean> filterBuilder = ImmutableMap.builder();
                            String[] filterNames = smallBodyConfig.imageSearchFilterNames;

                            for (int index = 0; index < numberFilters; ++index)
                            {
                                filterBuilder.put(filterNames[index], filterCheckBoxes[index].isSelected());
                            }
                            result.put(filterMapKey, filterBuilder.build());
                        }

                        // User-defined checkboxes.
                        numberFilters = getNumberOfUserDefinedCheckBoxesActuallyUsed();
                        if (numberFilters > 0)
                        {
                            ImmutableMap.Builder<String, Boolean> filterBuilder = ImmutableMap.builder();
                            String[] filterNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;

                            for (int index = 0; index < numberFilters; ++index)
                            {
                                filterBuilder.put(filterNames[index], userDefinedCheckBoxes[index].isSelected());
                            }
                            result.put(userCheckBoxMapKey, filterBuilder.build());
                        }
                    }

                    // Save region selected.
                    AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION).get(0);
                    result.put(circleSelectionKey, selectionModel.store());

                    // Save list of images.
                    result.put(imageListKey, listToOutputFormat(imageRawResults));

                    // Save selected images.
                    ImmutableSortedSet.Builder<String> selected = ImmutableSortedSet.naturalOrder();
                    int[] selectedIndices = resultList.getSelectedRows();
                    for (int selectedIndex : selectedIndices)
                    {
                        String image = new File(imageRawResults.get(selectedIndex).get(0)).getName();
                        selected.add(image);
                    }
                    result.put(selectedImagesKey, selected.build());

                    // Save boundary info.
                    PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
                    ImmutableSortedMap.Builder<String, Boolean> bndr = ImmutableSortedMap.naturalOrder();
                    for (ImageKeyInterface key : boundaries.getImageKeys())
                    {
                        if (instrument.equals(key.getInstrument()) && pointing.equals(key.getSource()))
                        {
                            PerspectiveImageBoundary boundary = boundaries.getBoundary(key);
                            String fullName = key.getName();
                            String name = new File(fullName).getName();
                            bndr.put(name, boundary.isVisible());
                        }
                    }
                    result.put(isBoundaryShowingKey, bndr.build());

                    // Save mapped image information.
                    ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
                    ImmutableSortedMap.Builder<String, Boolean> showing = ImmutableSortedMap.naturalOrder();
                    ImmutableSortedMap.Builder<String, Boolean> frus = ImmutableSortedMap.naturalOrder();

                    for (Image image : imageCollection.getImages())
                    {
                        String name = image.getImageName();
                        showing.put(name, image.isVisible());
                        if (image instanceof PerspectiveImage)
                        {
                            PerspectiveImage perspectiveImage = (PerspectiveImage) image;
                            frus.put(name, perspectiveImage.isFrustumShowing());
                        }
                        ImageKeyInterface key = image.getKey();
                    }
                    result.put(isShowingKey, showing.build());
                    result.put(isFrustrumShowingKey, frus.build());

                    return result;
                }

                @Override
                public void retrieve(Metadata source)
                {
                    ImageSource pointing = ImageSource.valueOf(source.get(pointingKey));

                    sourceComboBox.setSelectedItem(pointing);
                    sourceOfLastQuery = pointing;

                    if (source.hasKey(excludeSPCKey)) {
                        excludeGaskellCheckBox.setSelected(source.get(excludeSPCKey));
                    }

                    startSpinner.setValue(source.get(startDateKey));
                    endSpinner.setValue(source.get(endDateKey));
                    hasLimbComboBox.setSelectedItem(source.get(limbSelectedKey));
                    fromDistanceTextField.setValue(source.get(fromDistanceKey));
                    toDistanceTextField.setValue(source.get(toDistanceKey));
                    fromResolutionTextField.setValue(source.get(fromResolutionKey));
                    toResolutionTextField.setValue(source.get(toResolutionKey));
                    fromIncidenceTextField.setValue(source.get(fromIncidenceKey));
                    toIncidenceTextField.setValue(source.get(toIncidenceKey));
                    fromEmissionTextField.setValue(source.get(fromEmissionKey));
                    toEmissionTextField.setValue(source.get(toEmissionKey));
                    fromPhaseTextField.setValue(source.get(fromPhaseKey));
                    toPhaseTextField.setValue(source.get(toPhaseKey));
                    searchByFilenameCheckBox.setSelected(source.get(searchByFileNameEnabledKey));
                    searchByFilenameTextField.setText(source.get(searchByFileNameKey));

                    if (smallBodyConfig.hasHierarchicalImageSearch)
                    {
                        MetadataManager manager = smallBodyConfig.hierarchicalImageSearchSpecification.getMetadataManager();
                        manager.retrieve(source.get(imageTreeFilterKey));
                    }
                    else
                    {
                        int numberFilters = 0;

                        // Regular filters.
                        numberFilters = getNumberOfFiltersActuallyUsed();
                        if (numberFilters > 0)
                        {
                            Map<String, Boolean> filterMap = source.get(filterMapKey);
                            String[] filterNames = smallBodyConfig.imageSearchFilterNames;
                            for (int index = 0; index < numberFilters; ++index)
                            {
                                Boolean filterSelected = filterMap.get(filterNames[index]);
                                if (filterSelected != null)
                                {
                                    filterCheckBoxes[index].setSelected(filterSelected);
                                }
                            }
                        }

                        // User-defined checkboxes.
                        numberFilters = getNumberOfUserDefinedCheckBoxesActuallyUsed();
                        if (numberFilters > 0)
                        {
                            Map<String, Boolean> filterMap = source.get(userCheckBoxMapKey);
                            String[] filterNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
                            for (int index = 0; index < numberFilters; ++index)
                            {
                                Boolean filterSelected = filterMap.get(filterNames[index]);
                                if (filterSelected != null)
                                {
                                    userDefinedCheckBoxes[index].setSelected(filterSelected);
                                }
                            }
                        }
                    }

                    // Restore region selected.
                    AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION).get(0);
                    PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());

                    selectionModel.retrieve(source.get(circleSelectionKey));

                    // Restore list of images.
                    List<List<String>> imageList = inputFormatToList(source.get(imageListKey));
                    setImageResults(imageList);

                    // Restore image selections.
                    Set<String> selected = source.get(selectedImagesKey);
                    resultList.clearSelection();
                    for (int index = 0; index < resultList.getRowCount(); ++index)
                    {
                        String image = new File(imageRawResults.get(index).get(0)).getName();
                        if (selected.contains(image)) {
                            resultList.addRowSelectionInterval(index, index);
                        }
                    }

                    // Restore boundaries. First clear any associated with this model.
                    for (ImageKeyInterface key : boundaries.getImageKeys())
                    {
                        if (instrument.equals(key.getInstrument()) && pointing.equals(key.getSource()))
                        {
                            boundaries.removeBoundary(key);
                        }
                    }
                    Map<String, Boolean> bndr = source.get(isBoundaryShowingKey);
                    for (Entry<String, Boolean> entry : bndr.entrySet())
                    {
                        try
                        {
                            String fullName = instrument.searchQuery.getDataPath() + "/" + entry.getKey();
                            ImageKey imageKey = createImageKey(fullName, pointing, instrument);
                            boundaries.addBoundary(imageKey);
                            boundaries.getBoundary(imageKey).setVisible(entry.getValue());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    // Restore mapped image information.
                    ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
                    Map<String, Boolean> showing = source.get(isShowingKey);
                    Map<String, Boolean> frus = source.get(isFrustrumShowingKey);
                    for (String name : showing.keySet())
                    {
                        String fullName = instrument.searchQuery.getDataPath() + "/" + name;
                        loadImages(fullName);
                    }

                    for (Image image : imageCollection.getImages())
                    {
                        String name = image.getImageName();
                        image.setVisible(showing.containsKey(name) ? showing.get(name) : false);
                        if (image instanceof PerspectiveImage)
                        {
                            PerspectiveImage perspectiveImage = (PerspectiveImage) image;
                            perspectiveImage.setShowFrustum(frus.containsKey(name) ? frus.get(name) : false);
                        }
                    }
                }
            };
        }
    }

    public MetadataManager getMetadataManager()
    {
        return stateManager;
    }

    private List<String[]> listToOutputFormat(List<List<String>> inputListList)
    {
        // In case there is an exception when reading the time from the input file.
        Date now = new Date();

        List<String[]> outputArrayList = new ArrayList<>(inputListList.size());
        for (List<String> inputList : inputListList)
        {
            String[] array = new String[inputList.size()];
            for (int index = 0; index < inputList.size(); ++index)
            {
                if (index == 0)
                {
                    array[index] = new File(inputList.get(index)).getName();
                }
                else if (index == 1)
                {
                    try
                    {
                        Date date = new Date(Long.parseLong(inputList.get(index)));
                        array[index] = STANDARD_UTC_FORMAT.format(date);
                    }
                    catch (NumberFormatException e)
                    {
                        array[index] = STANDARD_UTC_FORMAT.format(now);
                    }
                }
                else
                {
                    array[index] = inputList.get(index);
                }
            }
            outputArrayList.add(array);
        }
        return outputArrayList;
    }

    private List<List<String>> inputFormatToList(List<String[]> inputArrayList)
    {
        // In case there is an exception when reading the time from the input file.
        String now = String.valueOf(new Date().getTime());

        List<List<String>> outputListList = new ArrayList<>(inputArrayList.size());
        for (String[] inputArray : inputArrayList)
        {
            List<String> list = new ArrayList<>();
            for (int index = 0; index < inputArray.length; ++index)
            {
                if (index == 0)
                {
                    list.add(instrument.searchQuery.getDataPath() + "/" + inputArray[index]);
                }
                else if (index == 1)
                {
                    try
                    {
                        Date date = STANDARD_UTC_FORMAT.parse(inputArray[index]);
                        list.add(String.valueOf(date.getTime()));
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                        list.add(now);
                    }
                }
                else
                {
                    list.add(inputArray[index]);
                }
            }
            outputListList.add(list);
        }
        return outputListList;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton blueButton;
    private javax.swing.JComboBox blueComboBox;
    private javax.swing.JLabel blueLabel;
    private javax.swing.JButton clearRegionButton;
    private javax.swing.JList colorImagesDisplayedList;
    private javax.swing.JLabel endDateLabel;
    private javax.swing.JLabel endDistanceLabel;
    private javax.swing.JLabel endEmissionLabel;
    private javax.swing.JLabel endIncidenceLabel;
    private javax.swing.JLabel endPhaseLabel;
    private javax.swing.JLabel endResolutionLabel;
    private javax.swing.JSpinner endSpinner;
    private javax.swing.JCheckBox excludeGaskellCheckBox;
    private javax.swing.JCheckBox filter10CheckBox;
    private javax.swing.JCheckBox filter11CheckBox;
    private javax.swing.JCheckBox filter12CheckBox;
    private javax.swing.JCheckBox filter13CheckBox;
    private javax.swing.JCheckBox filter14CheckBox;
    private javax.swing.JCheckBox filter15CheckBox;
    private javax.swing.JCheckBox filter16CheckBox;
    private javax.swing.JCheckBox filter17CheckBox;
    private javax.swing.JCheckBox filter18CheckBox;
    private javax.swing.JCheckBox filter19CheckBox;
    private javax.swing.JCheckBox filter1CheckBox;
    private javax.swing.JCheckBox filter20CheckBox;
    private javax.swing.JCheckBox filter21CheckBox;
    private javax.swing.JCheckBox filter22CheckBox;
    private javax.swing.JCheckBox filter2CheckBox;
    private javax.swing.JCheckBox filter3CheckBox;
    private javax.swing.JCheckBox filter4CheckBox;
    private javax.swing.JCheckBox filter5CheckBox;
    private javax.swing.JCheckBox filter6CheckBox;
    private javax.swing.JCheckBox filter7CheckBox;
    private javax.swing.JCheckBox filter8CheckBox;
    private javax.swing.JCheckBox filter9CheckBox;
    private javax.swing.JPanel filterCheckBoxPanel;
    private javax.swing.JLabel fromDistanceLabel;
    private javax.swing.JFormattedTextField fromDistanceTextField;
    private javax.swing.JLabel fromEmissionLabel;
    private javax.swing.JFormattedTextField fromEmissionTextField;
    private javax.swing.JLabel fromIncidenceLabel;
    private javax.swing.JFormattedTextField fromIncidenceTextField;
    private javax.swing.JLabel fromPhaseLabel;
    private javax.swing.JFormattedTextField fromPhaseTextField;
    private javax.swing.JLabel fromResolutionLabel;
    private javax.swing.JFormattedTextField fromResolutionTextField;
    private javax.swing.JButton generateColorImageButton;
    private javax.swing.JButton generateImageCubeButton;
    private javax.swing.JButton greenButton;
    private javax.swing.JComboBox greenComboBox;
    private javax.swing.JLabel greenLabel;
    private javax.swing.JComboBox<String> hasLimbComboBox;
    private javax.swing.JLabel hasLimbLabel;
    private javax.swing.JScrollPane hierarchicalSearchScrollPane;
    private javax.swing.JList imageCubesDisplayedList;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton loadImageListButton;
    private javax.swing.JPanel monochromePanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JComboBox numberOfBoundariesComboBox;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton redButton;
    private javax.swing.JComboBox redComboBox;
    private javax.swing.JLabel redLabel;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeAllImagesButton;
    private javax.swing.JButton removeColorImageButton;
    private javax.swing.JButton removeImageCubeButton;
    private javax.swing.JTable resultList;
    private javax.swing.JLabel resultsLabel;
    private javax.swing.JButton saveImageListButton;
    private javax.swing.JButton saveSelectedImageListButton;
    private javax.swing.JCheckBox searchByFilenameCheckBox;
    private javax.swing.JFormattedTextField searchByFilenameTextField;
    private javax.swing.JToggleButton selectRegionButton;
    private javax.swing.JComboBox<ImageSource> sourceComboBox;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JLabel startDateLabel;
    private javax.swing.JSpinner startSpinner;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel toDistanceLabel;
    private javax.swing.JFormattedTextField toDistanceTextField;
    private javax.swing.JLabel toEmissionLabel;
    private javax.swing.JFormattedTextField toEmissionTextField;
    private javax.swing.JLabel toIncidenceLabel;
    private javax.swing.JFormattedTextField toIncidenceTextField;
    private javax.swing.JLabel toPhaseLabel;
    private javax.swing.JFormattedTextField toPhaseTextField;
    private javax.swing.JLabel toResolutionLabel;
    private javax.swing.JFormattedTextField toResolutionTextField;
    private javax.swing.JCheckBox userDefined1CheckBox;
    private javax.swing.JCheckBox userDefined2CheckBox;
    private javax.swing.JCheckBox userDefined3CheckBox;
    private javax.swing.JCheckBox userDefined4CheckBox;
    private javax.swing.JCheckBox userDefined5CheckBox;
    private javax.swing.JCheckBox userDefined6CheckBox;
    private javax.swing.JCheckBox userDefined7CheckBox;
    private javax.swing.JCheckBox userDefined8CheckBox;
    private javax.swing.JPanel userDefinedCheckBoxPanel;
    private javax.swing.JButton viewResultsGalleryButton;
    // End of variables declaration//GEN-END:variables
}
