package edu.jhuapl.sbmt.image.gui.model.images;

import java.awt.Color;
import java.io.File;
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
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;

import vtk.vtkPolyData;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImageSearchModelListener;
import edu.jhuapl.sbmt.core.image.ImageSearchResultsListener;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;
import edu.jhuapl.sbmt.image.model.ImageCollection;
import edu.jhuapl.sbmt.image.model.keys.ImageKey;
import edu.jhuapl.sbmt.model.phobos.HierarchicalSearchSpecification.Selection;
import edu.jhuapl.sbmt.query.database.ImageDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import nom.tam.fits.FitsException;

public class ImageSearchModel implements Controller.Model, MetadataManager
{
//    private static final Key<Date> START_DATE_KEY = Key.of("startDate");

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
    final Key<SortedMap<String, Boolean>> isShowingKey = Key.of("imagesShowing");
    final Key<SortedMap<String, Boolean>> isOffLimbShowingKey = Key.of("offLimbShowing");
    final Key<SortedMap<String, Boolean>> isFrustrumShowingKey = Key.of("frustrumShowing");
    final Key<SortedMap<String, Boolean>> isBoundaryShowingKey = Key.of("boundaryShowing");
    final Key<SortedMap<String, Color>> boundaryColorKey = Key.of("boundaryColor");

    private SmallBodyViewConfig smallBodyConfig;
    protected ModelManager modelManager;
    protected IdPair resultIntervalCurrentlyShown = null;
    protected List<List<String>> imageResults = new ArrayList<List<String>>();
    protected ImageCollection imageCollection;
    protected IImagingInstrument instrument;
    protected ImageSource imageSourceOfLastQuery = ImageSource.SPICE;
    private Date startDate = null;
    private Date endDate = null;
    public int currentSlice;
    public int currentBand;
    private Renderer renderer;
    private Vector<ImageSearchResultsListener> resultsListeners;
    private Vector<ImageSearchModelListener> modelListeners;
    protected int[] selectedImageIndices;
    protected List<Integer> camerasSelected;
    protected List<Integer> filtersSelected;
    private double minDistanceQuery;
    private double maxDistanceQuery;
    private double minIncidenceQuery;
    private double maxIncidenceQuery;
    private double minEmissionQuery;
    private double maxEmissionQuery;
    private double minPhaseQuery;
    private double maxPhaseQuery;
    private double minResolutionQuery;
    private double maxResolutionQuery;
    private int selectedLimbIndex;
    private String selectedLimbString;
    private boolean searchByFilename;
    private String searchFilename = null;
    private boolean excludeGaskell;
    private boolean excludeGaskellEnabled;
    private Set<String> selectedFilenames;
    protected int numBoundaries = 10;

    private static final SimpleDateFormat STANDARD_UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    static {
        STANDARD_UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public ImageSearchModel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            Renderer renderer,
            IImagingInstrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.renderer = renderer;
        this.resultsListeners = new Vector<ImageSearchResultsListener>();
        this.modelListeners = new Vector<ImageSearchModelListener>();
        this.instrument = instrument;
        this.imageCollection = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
        this.startDate = smallBodyConfig.imageSearchDefaultStartDate;
        this.endDate = smallBodyConfig.imageSearchDefaultEndDate;
        camerasSelected = new LinkedList<Integer>();
        filtersSelected = new LinkedList<Integer>();
        selectedImageIndices = new int[] {};
    }


    public ModelManager getModelManager()
    {
        return modelManager;
    }

    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.IMAGES;
    }

    public ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES;
    }

    public void loadImage(ImageKeyInterface key, ImageCollection images) throws FitsException, IOException
    {
        images.addImage(key);
    }

    public void loadImage(String name)
    {

        List<ImageKeyInterface> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
        for (ImageKeyInterface key : keys)
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
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }

        }
   }

    public void unloadImage(ImageKeyInterface key, ImageCollection images)
    {
        images.removeImage(key);
    }

    public void unloadImage(String name)
    {

        List<ImageKeyInterface> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
        for (ImageKeyInterface key : keys)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            unloadImage(key, images);
        }
   }

    public void setImageVisibility(String name, boolean visible)
    {
        List<ImageKeyInterface> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
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

    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }


    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
    }


    public List<List<String>> getImageResults()
    {
        return imageResults;
    }


    public void setImageResults(List<List<String>> imageRawResults)
    {
        this.imageResults = imageRawResults;
        fireResultsChanged();
        fireResultsCountChanged(this.imageResults.size());
    }

    public ImageCollection getImageCollection()
    {
        return imageCollection;
    }


    public void setImageCollection(ImageCollection imageCollection)
    {
        this.imageCollection = imageCollection;
    }


    public IImagingInstrument getInstrument()
    {
        return instrument;
    }

    public void setInstrument(IImagingInstrument instrument)
    {
        this.instrument = instrument;
    }

    public ImageSource getImageSourceOfLastQuery()
    {
        return imageSourceOfLastQuery;
    }

    public void setImageSourceOfLastQuery(ImageSource imageSourceOfLastQuery)
    {
        this.imageSourceOfLastQuery = imageSourceOfLastQuery;
    }


    public Date getStartDate()
    {
        return startDate;
    }


    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }


    public Date getEndDate()
    {
        return endDate;
    }


    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }


    public void setModelManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
    }

    public int getCurrentSlice() { return 0; }

    public String getCurrentBand() { return "0"; }

    public SmallBodyViewConfig getSmallBodyConfig()
    {
        return smallBodyConfig;
    }

    public Renderer getRenderer()
    {
        return renderer;
    }

    public void setCurrentSlice(int currentSlice)
    {
        this.currentSlice = currentSlice;
    }


    public void setCurrentBand(int currentBand)
    {
        this.currentBand = currentBand;
    }

    public List<ImageKeyInterface> createImageKeys(String boundaryName, ImageSource sourceOfLastQuery, IImagingInstrument instrument)
    {
        List<ImageKeyInterface> result = new ArrayList<ImageKeyInterface>();
        result.add(createImageKey(boundaryName, sourceOfLastQuery, instrument));
        return result;
    }

    public ImageKeyInterface createImageKey(String imagePathName, ImageSource sourceOfLastQuery, IImagingInstrument instrument)
    {
        int slice = this.getCurrentSlice();
        String band = this.getCurrentBand();
        ImageKeyInterface key = new ImageKey(imagePathName, sourceOfLastQuery, null, null, instrument, band, slice, null);
        return key;
    }

    public int getNumberOfFiltersActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchFilterNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    public int getNumberOfUserDefinedCheckBoxesActuallyUsed()
    {
        String[] names = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    public List<List<String>> processResults(List<List<String>> input)
    {
        imageResults.addAll(input);
        setResultIntervalCurrentlyShown(new IdPair(0, imageResults.size()));
        return imageResults;
    }

    private void fireResultsChanged()
    {
        for (ImageSearchResultsListener listener : resultsListeners)
        {
            listener.resultsChanged(imageResults);
        }
    }

    protected void fireResultsCountChanged(int count)
    {
        for (ImageSearchResultsListener listener : resultsListeners)
        {
            listener.resultsCountChanged(count);
        }
    }

    public void addResultsChangedListener(ImageSearchResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(ImageSearchResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    private void fireModelChanged()
    {
        for (ImageSearchModelListener listener : modelListeners)
        {
            listener.modelUpdated();
        }
    }

    public void addModelChangedListener(ImageSearchModelListener listener)
    {
        modelListeners.add(listener);
    }

    public void removeModelChangedListener(ImageSearchModelListener listener)
    {
        modelListeners.remove(listener);
    }

    public void removeAllModelChangedListeners()
    {
        modelListeners.removeAllElements();
    }

    public ImageKeyInterface[] getSelectedImageKeys()
    {
        int[] indices = selectedImageIndices;
        ImageKeyInterface[] selectedKeys = new ImageKeyInterface[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String image = imageResults.get(index).get(0);
                String name = new File(image).getName();
                image = image.substring(0,image.length()-4);
                ImageKeyInterface selectedKey = (ImageKeyInterface)createImageKey(image, imageSourceOfLastQuery, instrument);
                if (!selectedKey.getBand().equals("0"))
                    name = selectedKey.getBand() + ":" + name;
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }


    public void setSelectedImageIndex(int[] selectedImageIndex)
    {
        this.selectedImageIndices = selectedImageIndex;
    }

    public int[] getSelectedImageIndex()
    {
        return selectedImageIndices;
    }

    public void performSearch()
    {
        imageResults.clear();
//        String searchField = null;
//        if (searchByFilename)
//            searchField = searchFilename;

        GregorianCalendar startDateGreg = new GregorianCalendar();
        GregorianCalendar endDateGreg = new GregorianCalendar();
        startDateGreg.setTime(getStartDate());
        endDateGreg.setTime(getEndDate());
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
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
        SmallBodyModel smallBodyModel = (SmallBodyModel)getModelManager().getModel(ModelNames.SMALL_BODY);
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
            smallBodyModel.setCubeVisibility(cubeList);
            smallBodyModel.calculateCubeSize(false, 0.0);
            smallBodyModel.clearCubes();
        }

        ImageSource imageSource = getImageSourceOfLastQuery(); // ImageSource.valueOf(((Enum)panel.getSourceComboBox().getSelectedItem()).name());

        // Populate camera and filter list differently based on if we are doing sum-of-products or product-of-sums search
        boolean sumOfProductsSearch;
//        List<Integer> camerasSelected;
//        List<Integer> filtersSelected;
        SmallBodyViewConfig smallBodyConfig = getSmallBodyConfig();
        if(smallBodyConfig.hasHierarchicalImageSearch)
        {
            // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
            sumOfProductsSearch = true;

            // Process the user's selections
//            smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections(
//                    panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
//
//            // Get the selected (camera,filter) pairs
//            camerasSelected = smallBodyConfig.hierarchicalImageSearchSpecification.getSelectedCameras();
//            filtersSelected = smallBodyConfig.hierarchicalImageSearchSpecification.getSelectedFilters();

            Selection selection = smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections();
            camerasSelected = selection.getSelectedCameras();
            filtersSelected = selection.getSelectedFilters();
        }
        else
        {
            // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
            sumOfProductsSearch = false;

            // Populate list of selected cameras
//            camerasSelected = new LinkedList<Integer>();
//            int numberOfCameras = getNumberOfUserDefinedCheckBoxesActuallyUsed();
//            for (int i=0; i<numberOfCameras; i++)
//            {
//                if(panel.getUserDefinedCheckBoxes()[i].isSelected())
//                {
//                    camerasSelected.add(i);
//                }
//            }
//
//            // Populate list of selected filters
//            filtersSelected = new LinkedList<Integer>();
//            int numberOfFilters = getNumberOfFiltersActuallyUsed();
//            for (int i=0; i<numberOfFilters; i++)
//            {
//                if(panel.getFilterCheckBoxes()[i].isSelected())
//                {
//                    filtersSelected.add(i);
//                }
//            }
        }
        List<List<String>> results = null;
        if (getInstrument().getSearchQuery() instanceof FixedListQuery)
        {
            FixedListQuery query = (FixedListQuery) getInstrument().getSearchQuery();
            results = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource, searchFilename)).getResultlist();
        }
        else
        {
            // Run queries based on user specifications
            ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                    Range.closed(minDistanceQuery, maxDistanceQuery),
                    searchFilename, null,
                    Range.closed(minIncidenceQuery, maxIncidenceQuery),
                    Range.closed(minEmissionQuery, maxEmissionQuery),
                    Range.closed(minPhaseQuery, maxPhaseQuery),
                    sumOfProductsSearch, camerasSelected, filtersSelected,
                    Range.closed(minResolutionQuery, maxResolutionQuery),
                    cubeList, imageSource, selectedLimbIndex);
            results = getInstrument().getSearchQuery().runQuery(searchMetadata).getResultlist();
       }

        // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
        // then remove from the list images which are contained in the other list by doing
        // an additional search.
        if (imageSource == ImageSource.SPICE && excludeGaskell)
        {
            List<List<String>> resultsOtherSource = null;
            if (getInstrument().getSearchQuery() instanceof FixedListQuery)
            {
                FixedListQuery query = (FixedListQuery)getInstrument().getSearchQuery();
//                FileInfo info = FileCache.getFileInfoFromServer(query.getRootPath() + "/" /*+ dataListPrefix + "/"*/ + imageListName);
//                if (!info.isExistsOnServer().equals(YesOrNo.YES))
//                {
//                    System.out.println("Could not find " + imageListName + ". Using imagelist.txt instead");
//                    imageListName = "imagelist.txt";
//                }
                resultsOtherSource = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist" /*imageListName*/, "images", query.getRootPath(), imageSource)).getResultlist();
            }
            else
            {

                ImageDatabaseSearchMetadata searchMetadataOther = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                        Range.closed(minDistanceQuery, maxDistanceQuery),
                        searchFilename, null,
                        Range.closed(minIncidenceQuery, maxIncidenceQuery),
                        Range.closed(minEmissionQuery, maxEmissionQuery),
                        Range.closed(minPhaseQuery, maxPhaseQuery),
                        sumOfProductsSearch, camerasSelected, filtersSelected,
                        Range.closed(minResolutionQuery, maxResolutionQuery),
                        cubeList, imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE, selectedLimbIndex);

                    resultsOtherSource = getInstrument().getSearchQuery().runQuery(searchMetadataOther).getResultlist();

            }

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
        setImageSourceOfLastQuery(imageSource);

        setImageResults(processResults(results));

    }

    public List<List<String>> getFixedList(ImageSource imageSource)
    {
    	String rootPath = getInstrument().getSearchQuery().getRootPath();
    	String dataPath = getInstrument().getSearchQuery().getDataPath();
    	String galleryPath = getInstrument().getSearchQuery().getGalleryPath();
    	String imageListName = "imagelist-info.txt";
        if (imageSource.equals(ImageSource.GASKELL))
        {
        	imageListName = "imagelist-sum.txt";
        	if (!FileCache.instance().isAccessible(rootPath + "/" + imageListName))
        		imageListName = "imagelist.txt";
        }
        return getInstrument().getSearchQuery().getResultsFromFileListOnServer(rootPath + "/" + imageListName, dataPath, galleryPath, false);
    }


    public List<Integer> getCamerasSelected()
    {
        return camerasSelected;
    }


    public List<Integer> getFiltersSelected()
    {
        return filtersSelected;
    }


    public double getMinDistanceQuery()
    {
        return minDistanceQuery;
    }


    public void setMinDistanceQuery(double minDistanceQuery)
    {
        this.minDistanceQuery = minDistanceQuery;
    }


    public double getMaxDistanceQuery()
    {
        return maxDistanceQuery;
    }


    public void setMaxDistanceQuery(double maxDistanceQuery)
    {
        this.maxDistanceQuery = maxDistanceQuery;
    }


    public double getMinIncidenceQuery()
    {
        return minIncidenceQuery;
    }


    public void setMinIncidenceQuery(double minIncidenceQuery)
    {
        this.minIncidenceQuery = minIncidenceQuery;
    }


    public double getMaxIncidenceQuery()
    {
        return maxIncidenceQuery;
    }


    public void setMaxIncidenceQuery(double maxIncidenceQuery)
    {
        this.maxIncidenceQuery = maxIncidenceQuery;
    }


    public double getMinEmissionQuery()
    {
        return minEmissionQuery;
    }


    public void setMinEmissionQuery(double minEmissionQuery)
    {
        this.minEmissionQuery = minEmissionQuery;
    }


    public double getMaxEmissionQuery()
    {
        return maxEmissionQuery;
    }


    public void setMaxEmissionQuery(double maxEmissionQuery)
    {
        this.maxEmissionQuery = maxEmissionQuery;
    }


    public double getMinPhaseQuery()
    {
        return minPhaseQuery;
    }


    public void setMinPhaseQuery(double minPhaseQuery)
    {
        this.minPhaseQuery = minPhaseQuery;
    }


    public double getMaxPhaseQuery()
    {
        return maxPhaseQuery;
    }


    public void setMaxPhaseQuery(double maxPhaseQuery)
    {
        this.maxPhaseQuery = maxPhaseQuery;
    }


    public double getMinResolutionQuery()
    {
        return minResolutionQuery;
    }


    public void setMinResolutionQuery(double minResolutionQuery)
    {
        this.minResolutionQuery = minResolutionQuery;
    }


    public double getMaxResolutionQuery()
    {
        return maxResolutionQuery;
    }


    public void setMaxResolutionQuery(double maxResolutionQuery)
    {
        this.maxResolutionQuery = maxResolutionQuery;
    }


    public int getSelectedLimbIndex()
    {
        return selectedLimbIndex;
    }


    public void setSelectedLimbIndex(int selectedLimbIndex)
    {
        this.selectedLimbIndex = selectedLimbIndex;
    }


    public String getSelectedLimbString()
    {
        return selectedLimbString;
    }


    public void setSelectedLimbString(String selectedLimbString)
    {
        this.selectedLimbString = selectedLimbString;
    }


    public boolean isSearchByFilename()
    {
        return searchByFilename;
    }


    public void setSearchByFilename(boolean searchByFilename)
    {
        this.searchByFilename = searchByFilename;
    }


    public String getSearchFilename()
    {
        return searchFilename;
    }


    public void setSearchFilename(String searchFilename)
    {
        this.searchFilename = searchFilename;
    }


    public boolean isExcludeGaskell()
    {
        return excludeGaskell;
    }


    public void setExcludeGaskell(boolean excludeGaskell)
    {
        this.excludeGaskell = excludeGaskell;
    }


    public boolean isExcludeGaskellEnabled()
    {
        return excludeGaskellEnabled;
    }


    public void setExcludeGaskellEnabled(boolean excludeGaskellEnabled)
    {
        this.excludeGaskellEnabled = excludeGaskellEnabled;
    }


//    //Metadata based methods
//    public void initializeStateManager()
//    {
//        if (stateManager == null) {
//            stateManager = new MetadataManager() {
//                final Key<String> pointingKey = Key.of("pointing");
//                final Key<Boolean> excludeSPCKey = Key.of("excludeSPC");
//                final Key<Date> startDateKey = Key.of("startDate");
//                final Key<Date> endDateKey = Key.of("endDate");
//                final Key<String> limbSelectedKey = Key.of("limb");
//                final Key<Double> fromDistanceKey = Key.of("fromDistance");
//                final Key<Double> toDistanceKey = Key.of("toDistance");
//                final Key<Double> fromResolutionKey = Key.of("fromResolution");
//                final Key<Double> toResolutionKey = Key.of("toResolution");
//                final Key<Double> fromIncidenceKey = Key.of("fromIncidence");
//                final Key<Double> toIncidenceKey = Key.of("toIncidence");
//                final Key<Double> fromEmissionKey = Key.of("fromEmission");
//                final Key<Double> toEmissionKey = Key.of("toEmission");
//                final Key<Double> fromPhaseKey = Key.of("fromPhase");
//                final Key<Double> toPhaseKey = Key.of("toPhase");
//                final Key<Boolean> searchByFileNameEnabledKey = Key.of("searchByFileNameEnabled");
//                final Key<String> searchByFileNameKey = Key.of("searchByFileName");
//                final Key<Map<String, Boolean>> filterMapKey = Key.of("filters");
//                final Key<Map<String, Boolean>> userCheckBoxMapKey = Key.of("userCheckBoxes");
//                final Key<Metadata> circleSelectionKey = Key.of("circleSelection");
//                final Key<Metadata> imageTreeFilterKey = Key.of("imageTreeFilters");
//                final Key<List<String[]>> imageListKey = Key.of("imageList");
//                final Key<Set<String>> selectedImagesKey = Key.of("imagesSelected");
//                final Key<Map<String, Boolean>> isShowingKey = Key.of("imagesShowing");
//                final Key<Map<String, Boolean>> isFrustrumShowingKey = Key.of("frustrumShowing");
//                final Key<Map<String, Boolean>> isBoundaryShowingKey = Key.of("boundaryShowing");
//
//                @Override
//                public Metadata store()
//                {
//                    SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
//
//                    ImageSource pointing = getImageSourceOfLastQuery();
//                    result.put(pointingKey, pointing.name());
//
//                    if (excludeGaskellEnabled) {
//                        result.put(excludeSPCKey, excludeGaskell);
//                    }
//
//                    result.put(startDateKey, getStartDate());
//                    result.put(endDateKey, getEndDate());
//                    result.put(limbSelectedKey, selectedLimbString);
//                    result.put(fromDistanceKey, minDistanceQuery);
//                    result.put(toDistanceKey, maxDistanceQuery);
//                    result.put(fromResolutionKey, minResolutionQuery);
//                    result.put(toResolutionKey, maxResolutionQuery);
//                    result.put(fromIncidenceKey, minIncidenceQuery);
//                    result.put(toIncidenceKey, maxIncidenceQuery);
//                    result.put(fromEmissionKey, minEmissionQuery);
//                    result.put(toEmissionKey, maxEmissionQuery);
//                    result.put(fromPhaseKey, minPhaseQuery);
//                    result.put(toPhaseKey, maxPhaseQuery);
//                    result.put(searchByFileNameEnabledKey, searchByFilename);
//                    result.put(searchByFileNameKey, searchFilename);
//
//                    if (smallBodyConfig.hasHierarchicalImageSearch)
//                    {
//                        MetadataManager manager = smallBodyConfig.hierarchicalImageSearchSpecification.getMetadataManager();
//                        result.put(imageTreeFilterKey, manager.store());
//                    }
//                    else
//                    {
//                        int numberFilters = 0;
//
//                        // Regular filters.
//                        numberFilters = getNumberOfFiltersActuallyUsed();
//                        if (numberFilters > 0)
//                        {
//                            ImmutableMap.Builder<String, Boolean> filterBuilder = ImmutableMap.builder();
//                            String[] filterNames = smallBodyConfig.imageSearchFilterNames;
//
//                            for (int index = 0; index < numberFilters; ++index)
//                            {
////                                filterBuilder.put(filterNames[index], filterCheckBoxes[index].isSelected());
//                                filterBuilder.put(filterNames[index], filtersSelected.contains(index));
//                            }
//                            result.put(filterMapKey, filterBuilder.build());
//                        }
//
//                        // User-defined checkboxes.
//                        numberFilters = getNumberOfUserDefinedCheckBoxesActuallyUsed();
//                        if (numberFilters > 0)
//                        {
//                            ImmutableMap.Builder<String, Boolean> filterBuilder = ImmutableMap.builder();
//                            String[] filterNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
//
//                            for (int index = 0; index < numberFilters; ++index)
//                            {
////                                filterBuilder.put(filterNames[index], userDefinedCheckBoxes[index].isSelected());
//                                filterBuilder.put(filterNames[index], camerasSelected.contains(index));
//                            }
//                            result.put(userCheckBoxMapKey, filterBuilder.build());
//                        }
//                    }
//
//                    // Save region selected.
//                    AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
//                    result.put(circleSelectionKey, selectionModel.getMetadataManager().store());
//
//                    // Save list of images.
//                    result.put(imageListKey, listToOutputFormat(imageResults));
//
//                    // Save selected images.
//                    ImmutableSortedSet.Builder<String> selected = ImmutableSortedSet.naturalOrder();
//                    int[] selectedIndices = selectedImageIndices;
//                    for (int selectedIndex : selectedIndices)
//                    {
//                        String image = new File(imageResults.get(selectedIndex).get(0)).getName();
//                        selected.add(image);
//                    }
//                    result.put(selectedImagesKey, selected.build());
//
//                    // Save boundary info.
//                    PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
//                    ImmutableSortedMap.Builder<String, Boolean> bndr = ImmutableSortedMap.naturalOrder();
//                    for (ImageKey key : boundaries.getImageKeys())
//                    {
//                        if (instrument.equals(key.instrument) && pointing.equals(key.source))
//                        {
//                            PerspectiveImageBoundary boundary = boundaries.getBoundary(key);
//                            String fullName = key.name;
//                            String name = new File(fullName).getName();
//                            bndr.put(name, boundary.isVisible());
//                        }
//                    }
//                    result.put(isBoundaryShowingKey, bndr.build());
//
//                    // Save mapped image information.
//                    ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
//                    ImmutableSortedMap.Builder<String, Boolean> showing = ImmutableSortedMap.naturalOrder();
//                    ImmutableSortedMap.Builder<String, Boolean> frus = ImmutableSortedMap.naturalOrder();
//
//                    for (Image image : imageCollection.getImages())
//                    {
//                        String name = image.getImageName();
//                        showing.put(name, image.isVisible());
//                        if (image instanceof PerspectiveImage)
//                        {
//                            PerspectiveImage perspectiveImage = (PerspectiveImage) image;
//                            frus.put(name, perspectiveImage.isFrustumShowing());
//                        }
//                        ImageKey key = image.getKey();
//                    }
//                    result.put(isShowingKey, showing.build());
//                    result.put(isFrustrumShowingKey, frus.build());
//
//                    return result;
//                }
//
//                @Override
//                public void retrieve(Metadata source)
//                {
////                    ImageSearchModel model = ImageSearchModel.this;
////                    ImageSource pointing = ImageSource.valueOf(source.get(pointingKey));
////                    model.setImageSourceOfLastQuery(pointing);
////
////                    if (source.hasKey(excludeSPCKey))
////                    {
////                        model.excludeGaskell = source.get(excludeSPCKey);
////                    }
////
////                    model.setStartDate(source.get(startDateKey));
////                    model.setEndDate(source.get(endDateKey));
////                    model.setSelectedLimbString(source.get(limbSelectedKey));
////                    model.setMinDistanceQuery(source.get(fromDistanceKey));
////                    model.setMaxDistanceQuery(source.get(toDistanceKey));
////                    model.setMinResolutionQuery(source.get(fromResolutionKey));
////                    model.setMaxResolutionQuery(source.get(toResolutionKey));
////                    model.setMinIncidenceQuery(source.get(fromIncidenceKey));
////                    model.setMaxIncidenceQuery(source.get(toIncidenceKey));
////                    model.setMinEmissionQuery(source.get(fromEmissionKey));
////                    model.setMaxEmissionQuery(source.get(toEmissionKey));
////                    model.setMinPhaseQuery(source.get(fromPhaseKey));
////                    model.setMaxPhaseQuery(source.get(toPhaseKey));
////                    model.setSearchByFilename(source.get(searchByFileNameEnabledKey));
////                    model.setSearchFilename(source.get(searchByFileNameKey));
////
//////                    sourceComboBox.setSelectedItem(pointing);
//////                    sourceOfLastQuery = pointing;
//////
//////                    if (source.hasKey(excludeSPCKey)) {
//////                        excludeGaskellCheckBox.setSelected(source.get(excludeSPCKey));
//////                    }
//////
//////                    startSpinner.setValue(source.get(startDateKey));
//////                    endSpinner.setValue(source.get(endDateKey));
//////                    hasLimbComboBox.setSelectedItem(source.get(limbSelectedKey));
//////                    fromDistanceTextField.setValue(source.get(fromDistanceKey));
//////                    toDistanceTextField.setValue(source.get(toDistanceKey));
//////                    fromResolutionTextField.setValue(source.get(fromResolutionKey));
//////                    toResolutionTextField.setValue(source.get(toResolutionKey));
//////                    fromIncidenceTextField.setValue(source.get(fromIncidenceKey));
//////                    toIncidenceTextField.setValue(source.get(toIncidenceKey));
//////                    fromEmissionTextField.setValue(source.get(fromEmissionKey));
//////                    toEmissionTextField.setValue(source.get(toEmissionKey));
//////                    fromPhaseTextField.setValue(source.get(fromPhaseKey));
//////                    toPhaseTextField.setValue(source.get(toPhaseKey));
//////                    searchByFilenameCheckBox.setSelected(source.get(searchByFileNameEnabledKey));
//////                    searchByFilenameTextField.setText(source.get(searchByFileNameKey));
////
////                    if (smallBodyConfig.hasHierarchicalImageSearch)
////                    {
////                        MetadataManager manager = smallBodyConfig.hierarchicalImageSearchSpecification.getMetadataManager();
////                        manager.retrieve(source.get(imageTreeFilterKey));
////                    }
////                    else
////                    {
////                        int numberFilters = 0;
////
////                        // Regular filters.
////                        numberFilters = getNumberOfFiltersActuallyUsed();
//////                        if (numberFilters > 0)
//////                        {
//////                            Map<String, Boolean> filterMap = source.get(filterMapKey);
//////                            String[] filterNames = smallBodyConfig.imageSearchFilterNames;
//////                            for (int index = 0; index < numberFilters; ++index)
//////                            {
//////                                Boolean filterSelected = filterMap.get(filterNames[index]);
//////                                if (filterSelected != null)
//////                                {
//////                                    filterCheckBoxes[index].setSelected(filterSelected);
//////                                }
//////                            }
//////                        }
//////
//////                        // User-defined checkboxes.
//////                        numberFilters = getNumberOfUserDefinedCheckBoxesActuallyUsed();
//////                        if (numberFilters > 0)
//////                        {
//////                            Map<String, Boolean> filterMap = source.get(userCheckBoxMapKey);
//////                            String[] filterNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
//////                            for (int index = 0; index < numberFilters; ++index)
//////                            {
//////                                Boolean filterSelected = filterMap.get(filterNames[index]);
//////                                if (filterSelected != null)
//////                                {
//////                                    userDefinedCheckBoxes[index].setSelected(filterSelected);
//////                                }
//////                            }
//////                        }
////                    }
////
////                    // Restore region selected.
////                    AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
////                    PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
////
////                    selectionModel.getMetadataManager().retrieve(source.get(circleSelectionKey));
////
////                    // Restore list of images.
////                    List<List<String>> imageList = inputFormatToList(source.get(imageListKey));
////                    setImageResults(imageList);
////
////                    // Restore image selections.
//////                    Set<String> selected = source.get(selectedImagesKey);
//////                    resultList.clearSelection();
//////                    for (int index = 0; index < resultList.getRowCount(); ++index)
//////                    {
//////                        String image = new File(imageResults.get(index).get(0)).getName();
//////                        if (selected.contains(image)) {
//////                            resultList.addRowSelectionInterval(index, index);
//////                        }
//////                    }
////
////                    // Restore boundaries. First clear any associated with this model.
////                    for (ImageKey key : boundaries.getImageKeys())
////                    {
////                        if (instrument.equals(key.instrument) && pointing.equals(key.source))
////                        {
////                            boundaries.removeBoundary(key);
////                        }
////                    }
////                    Map<String, Boolean> bndr = source.get(isBoundaryShowingKey);
////                    for (Entry<String, Boolean> entry : bndr.entrySet())
////                    {
////                        try
////                        {
////                            String fullName = instrument.searchQuery.getDataPath() + "/" + entry.getKey();
////                            ImageKey imageKey = createImageKey(fullName, pointing, instrument);
////                            boundaries.addBoundary(imageKey);
////                            boundaries.getBoundary(imageKey).setVisible(entry.getValue());
////                        }
////                        catch (Exception e)
////                        {
////                            e.printStackTrace();
////                        }
////                    }
////
////                    // Restore mapped image information.
////                    ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
////                    Map<String, Boolean> showing = source.get(isShowingKey);
////                    Map<String, Boolean> frus = source.get(isFrustrumShowingKey);
////                    for (String name : showing.keySet())
////                    {
////                        String fullName = instrument.searchQuery.getDataPath() + "/" + name;
////                        loadImages(fullName);
////                    }
////
////                    for (Image image : imageCollection.getImages())
////                    {
////                        String name = image.getImageName();
////                        image.setVisible(showing.containsKey(name) ? showing.get(name) : false);
////                        if (image instanceof PerspectiveImage)
////                        {
////                            PerspectiveImage perspectiveImage = (PerspectiveImage) image;
////                            perspectiveImage.setShowFrustum(frus.containsKey(name) ? frus.get(name) : false);
////                        }
////                    }
////
////                    fireModelChanged();
//                }
//            };
//        }
//    }
//
//    public MetadataManager getMetadataManager()
//    {
//        return stateManager;
//    }

    public Set<String> getSelectedFilenames()
    {
        return selectedFilenames;
    }


    public void setSelectedFilenames(Set<String> selectedFilenames)
    {
        this.selectedFilenames = selectedFilenames;
    }


    public void setCamerasSelected(List<Integer> camerasSelected)
    {
        this.camerasSelected = camerasSelected;
    }


    public void setFiltersSelected(List<Integer> filtersSelected)
    {
        this.filtersSelected = filtersSelected;
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
                    list.add(instrument.getSearchQuery().getDataPath() + "/" + inputArray[index]);
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

    @Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

        ImageSource pointing = getImageSourceOfLastQuery();
        result.put(pointingKey, pointing.name());

        if (excludeGaskellEnabled) {
            result.put(excludeSPCKey, excludeGaskell);
        }

        result.put(startDateKey, getStartDate());
        result.put(endDateKey, getEndDate());
        result.put(limbSelectedKey, selectedLimbString);
        result.put(fromDistanceKey, minDistanceQuery);
        result.put(toDistanceKey, maxDistanceQuery);
        result.put(fromResolutionKey, minResolutionQuery);
        result.put(toResolutionKey, maxResolutionQuery);
        result.put(fromIncidenceKey, minIncidenceQuery);
        result.put(toIncidenceKey, maxIncidenceQuery);
        result.put(fromEmissionKey, minEmissionQuery);
        result.put(toEmissionKey, maxEmissionQuery);
        result.put(fromPhaseKey, minPhaseQuery);
        result.put(toPhaseKey, maxPhaseQuery);
        result.put(searchByFileNameEnabledKey, searchByFilename);
        result.put(searchByFileNameKey, searchFilename);

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
//                    filterBuilder.put(filterNames[index], filterCheckBoxes[index].isSelected());
                    filterBuilder.put(filterNames[index], filtersSelected.contains(index));
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
//                    filterBuilder.put(filterNames[index], userDefinedCheckBoxes[index].isSelected());
                    filterBuilder.put(filterNames[index], camerasSelected.contains(index));
                }
                result.put(userCheckBoxMapKey, filterBuilder.build());
            }
        }

        // Save region selected.
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        result.put(circleSelectionKey, selectionModel.store());

        // Save list of images.
        result.put(imageListKey, listToOutputFormat(imageResults));

        // Save selected images.
        ImmutableSortedSet.Builder<String> selected = ImmutableSortedSet.naturalOrder();
        int[] selectedIndices = selectedImageIndices;
        for (int selectedIndex : selectedIndices)
        {
            String image = new File(imageResults.get(selectedIndex).get(0)).getName();
            selected.add(image);
        }
        result.put(selectedImagesKey, selected.build());

        ImmutableSortedMap.Builder<String, Boolean> bndr = ImmutableSortedMap.naturalOrder();
        ImmutableSortedMap.Builder<String, Color> bndrColor = ImmutableSortedMap.naturalOrder();
        for (Image image: imageCollection.getImages())
        {
        	ImageKeyInterface key = image.getKey();
        	String fullName = key.getName();
        	String name = new File(fullName).getName();
        	bndr.put(name, image.isBoundaryVisible());
        	bndrColor.put(name, image.getBoundaryColor());
        }
        result.put(isBoundaryShowingKey, bndr.build());
        result.put(boundaryColorKey, bndrColor.build());

        // Save boundary info.
//        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
//        ImmutableSortedMap.Builder<String, Boolean> bndr = ImmutableSortedMap.naturalOrder();
//        for (ImageKeyInterface key : boundaries.getImageKeys())
//        {
//            if (instrument.equals(((ImageKey)key).getInstrument()) && pointing.equals(key.getSource()))
//            {
//                PerspectiveImageBoundary boundary = boundaries.getBoundary(key);
//                String fullName = key.getName();
//                String name = new File(fullName).getName();
//                bndr.put(name, boundary.isVisible());
//            }
//        }
//        result.put(isBoundaryShowingKey, bndr.build());

        // Save mapped image information.
        ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
        ImmutableSortedMap.Builder<String, Boolean> showing = ImmutableSortedMap.naturalOrder();
        ImmutableSortedMap.Builder<String, Boolean> offLimb = ImmutableSortedMap.naturalOrder();
        ImmutableSortedMap.Builder<String, Boolean> frus = ImmutableSortedMap.naturalOrder();

        for (Image image : imageCollection.getImages())
        {
        	ImageKeyInterface key = image.getKey();
        	if (instrument.equals(key.getInstrument()) && pointing.equals(key.getSource()))
        	{
        		String name = image.getImageName();
        		showing.put(name, image.isVisible());
        		if (image instanceof PerspectiveImage)
        		{
        			PerspectiveImage perspectiveImage = (PerspectiveImage) image;
        			offLimb.put(name, perspectiveImage.offLimbFootprintIsVisible());
        			frus.put(name, perspectiveImage.isFrustumShowing());
        		}
        	}
        }
        result.put(isShowingKey, showing.build());
        result.put(isOffLimbShowingKey, offLimb.build());
        result.put(isFrustrumShowingKey, frus.build());

        return result;
    }


    @Override
    public void retrieve(Metadata source)
    {

        ImageSearchModel model = ImageSearchModel.this;
        ImageSource pointing = ImageSource.valueOf(source.get(pointingKey));
        model.setImageSourceOfLastQuery(pointing);

        if (source.hasKey(excludeSPCKey))
        {
            model.excludeGaskell = source.get(excludeSPCKey);
        }

        model.setStartDate(source.get(startDateKey));
        model.setEndDate(source.get(endDateKey));
        model.setSelectedLimbString(source.get(limbSelectedKey));
        model.setMinDistanceQuery(source.get(fromDistanceKey));
        model.setMaxDistanceQuery(source.get(toDistanceKey));
        model.setMinResolutionQuery(source.get(fromResolutionKey));
        model.setMaxResolutionQuery(source.get(toResolutionKey));
        model.setMinIncidenceQuery(source.get(fromIncidenceKey));
        model.setMaxIncidenceQuery(source.get(toIncidenceKey));
        model.setMinEmissionQuery(source.get(fromEmissionKey));
        model.setMaxEmissionQuery(source.get(toEmissionKey));
        model.setMinPhaseQuery(source.get(fromPhaseKey));
        model.setMaxPhaseQuery(source.get(toPhaseKey));
        model.setSearchByFilename(source.get(searchByFileNameEnabledKey));
        model.setSearchFilename(source.get(searchByFileNameKey));

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
                        filtersSelected.add(index);
//                        filterCheckBoxes[index].setSelected(filterSelected);
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
                        camerasSelected.add(index);
//                        userDefinedCheckBoxes[index].setSelected(filterSelected);
                    }
                }
            }
        }

        // Restore region selected.
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
//        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());

        selectionModel.retrieve(source.get(circleSelectionKey));

        // Restore list of images.
        List<List<String>> imageList = inputFormatToList(source.get(imageListKey));
        setImageResults(imageList);

        // Restore image selections.
        selectedFilenames = source.get(selectedImagesKey);

        //This is now done in a listener
//        resultList.clearSelection();
//        for (int index = 0; index < resultList.getRowCount(); ++index)
//        {
//            String image = new File(imageResults.get(index).get(0)).getName();
//            if (selected.contains(image)) {
//                resultList.addRowSelectionInterval(index, index);
//            }
//        }

        // Restore boundaries. First clear any associated with this model.
        for (Image image : imageCollection.getImages())
        {
        	image.setBoundaryVisibility(false);
        }
        Map<String, Boolean> bndr = source.get(isBoundaryShowingKey);
        for (Entry<String, Boolean> entry : bndr.entrySet())
        {
        	try
            {
                String fullName = instrument.getSearchQuery().getDataPath() + "/" + entry.getKey();
                ImageKeyInterface imageKey = createImageKey(fullName, pointing, instrument);
                Image image = imageCollection.getImage(imageKey);
                if (image != null)
                {
                	image.setBoundaryVisibility(entry.getValue());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        Map<String, Color> bndrColor = source.get(boundaryColorKey);
        for (Entry<String, Color> entry : bndrColor.entrySet())
        {
        	try
            {
                String fullName = instrument.getSearchQuery().getDataPath() + "/" + entry.getKey();
                ImageKeyInterface imageKey = createImageKey(fullName, pointing, instrument);
                Image image = imageCollection.getImage(imageKey);
                if (image != null)
                {
                	image.setBoundaryColor(entry.getValue());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

//        for (ImageKeyInterface key : boundaries.getImageKeys())
//        {
//            if (instrument.equals(((ImageKey)key).instrument) && pointing.equals(key.getSource()))
//            {
//                boundaries.removeBoundary(key);
//            }
//        }
//        Map<String, Boolean> bndr = source.get(isBoundaryShowingKey);
//        for (Entry<String, Boolean> entry : bndr.entrySet())
//        {
//            try
//            {
//                String fullName = instrument.getSearchQuery().getDataPath() + "/" + entry.getKey();
//                ImageKeyInterface imageKey = createImageKey(fullName, pointing, instrument);
//                boundaries.addBoundary(imageKey);
//                boundaries.getBoundary(imageKey).setVisible(entry.getValue());
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }

        // Restore mapped image information.
        ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
        Map<String, Boolean> showing = source.get(isShowingKey);
        Map<String, Boolean> offLimb = source.hasKey(isOffLimbShowingKey) ? source.get(isOffLimbShowingKey) : ImmutableMap.of();
        Map<String, Boolean> frus = source.get(isFrustrumShowingKey);
        for (String name : showing.keySet())
        {
            String fullName = instrument.getSearchQuery().getDataPath() + "/" + name;
            loadImage(fullName);
        }

        for (Image image : imageCollection.getImages())
        {
        	ImageKeyInterface key = image.getKey();
        	if (instrument.equals(key.getInstrument()) && pointing.equals(key.getSource()))
        	{
        		String name = image.getImageName();
        		image.setVisible(showing.containsKey(name) ? showing.get(name) : false);
        		if (image instanceof PerspectiveImage)
        		{
        			PerspectiveImage perspectiveImage = (PerspectiveImage) image;
                    perspectiveImage.setOffLimbFootprintVisibility(offLimb.containsKey(name) ? offLimb.get(name) : false);
        			perspectiveImage.setShowFrustum(frus.containsKey(name) ? frus.get(name) : false);
        		}
        	}

        }

        fireModelChanged();

    }


    public int getNumBoundaries()
    {
        return numBoundaries;
    }


    public void setNumBoundaries(int numBoundaries)
    {
        this.numBoundaries = numBoundaries;
    }

}
