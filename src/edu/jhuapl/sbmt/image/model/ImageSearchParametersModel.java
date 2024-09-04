package edu.jhuapl.sbmt.image.model;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.Vector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.ImageSearchModelListener;
import edu.jhuapl.sbmt.image.interfaces.ImageSearchResultsListener;
import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.MetadataManager;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public class ImageSearchParametersModel implements Model, MetadataManager
{
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
    final Key<Metadata> imageTreeFilterKey = Key.of("imageTreeFilters");
    final Key<List<String[]>> imageListKey = Key.of("imageList");
    final Key<Set<String>> selectedImagesKey = Key.of("imagesSelected");
    final Key<SortedMap<String, Boolean>> isShowingKey = Key.of("imagesShowing");
    final Key<SortedMap<String, Boolean>> isOffLimbShowingKey = Key.of("offLimbShowing");
    final Key<SortedMap<String, Boolean>> isFrustrumShowingKey = Key.of("frustrumShowing");
    final Key<SortedMap<String, Boolean>> isBoundaryShowingKey = Key.of("boundaryShowing");

    private ImagingInstrumentConfig config;
    protected ModelManager modelManager;
    protected IdPair resultIntervalCurrentlyShown = null;
    protected List<List<String>> imageResults = new ArrayList<List<String>>();
    protected IImagingInstrument instrument;
    protected PointingSource imageSourceOfLastQuery = PointingSource.SPICE;
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

    public ImageSearchParametersModel(ImagingInstrumentConfig config,
            final ModelManager modelManager,
            Renderer renderer,
            IImagingInstrument instrument)
    {
        this.config = config;
        this.modelManager = modelManager;
        this.renderer = renderer;
        this.resultsListeners = new Vector<ImageSearchResultsListener>();
        this.modelListeners = new Vector<ImageSearchModelListener>();
        this.instrument = instrument;
        this.startDate = config.imageSearchDefaultStartDate;
        this.endDate = config.imageSearchDefaultEndDate;
        camerasSelected = new LinkedList<Integer>();
        filtersSelected = new LinkedList<Integer>();
        selectedImageIndices = new int[] {};
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

    public IImagingInstrument getInstrument()
    {
        return instrument;
    }

    public void setInstrument(IImagingInstrument instrument)
    {
        this.instrument = instrument;
    }

    public PointingSource getImageSourceOfLastQuery()
    {
        return imageSourceOfLastQuery;
    }

    public void setImageSourceOfLastQuery(PointingSource imageSourceOfLastQuery)
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

    public ImagingInstrumentConfig getSmallBodyConfig()
    {
        return config;
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

    public int getNumberOfFiltersActuallyUsed()
    {
        String[] names = config.imageSearchFilterNames;
        if (names == null)
            return 0;
        else
            return names.length;
    }

    public int getNumberOfUserDefinedCheckBoxesActuallyUsed()
    {
        String[] names = config.imageSearchUserDefinedCheckBoxesNames;
        if (names == null)
            return 0;
        else
            return names.length;
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

    public void setSelectedImageIndex(int[] selectedImageIndex)
    {
        this.selectedImageIndices = selectedImageIndex;
    }

    public int[] getSelectedImageIndex()
    {
        return selectedImageIndices;
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

    @SuppressWarnings({ "deprecation", "unused" })
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

        PointingSource pointing = getImageSourceOfLastQuery();
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

        if (config.hasHierarchicalImageSearch)
        {
            MetadataManager manager = config.hierarchicalImageSearchSpecification.getMetadataManager();
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
                String[] filterNames = config.imageSearchFilterNames;

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
                String[] filterNames = config.imageSearchUserDefinedCheckBoxesNames;

                for (int index = 0; index < numberFilters; ++index)
                {
//                    filterBuilder.put(filterNames[index], userDefinedCheckBoxes[index].isSelected());
                    filterBuilder.put(filterNames[index], camerasSelected.contains(index));
                }
                result.put(userCheckBoxMapKey, filterBuilder.build());
            }
        }

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

//        // Save boundary info.
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
//
//        // Save mapped image information.
//        ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
//        ImmutableSortedMap.Builder<String, Boolean> showing = ImmutableSortedMap.naturalOrder();
//        ImmutableSortedMap.Builder<String, Boolean> offLimb = ImmutableSortedMap.naturalOrder();
//        ImmutableSortedMap.Builder<String, Boolean> frus = ImmutableSortedMap.naturalOrder();
//
//        for (Image image : imageCollection.getImages())
//        {
//        	ImageKeyInterface key = image.getKey();
//        	if (instrument.equals(key.getInstrument()) && pointing.equals(key.getSource()))
//        	{
//        		String name = image.getImageName();
//        		showing.put(name, image.isVisible());
//        		if (image instanceof PerspectiveImage)
//        		{
//        			PerspectiveImage perspectiveImage = (PerspectiveImage) image;
//        			offLimb.put(name, perspectiveImage.offLimbFootprintIsVisible());
//        			frus.put(name, perspectiveImage.isFrustumShowing());
//        		}
//        	}
//        }
//        result.put(isShowingKey, showing.build());
//        result.put(isOffLimbShowingKey, offLimb.build());
//        result.put(isFrustrumShowingKey, frus.build());

        return result;
    }


    @Override
    public void retrieve(Metadata source)
    {

        ImageSearchParametersModel model = ImageSearchParametersModel.this;
        PointingSource pointing = PointingSource.valueOf(source.get(pointingKey));
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

        if (config.hasHierarchicalImageSearch)
        {
            MetadataManager manager = config.hierarchicalImageSearchSpecification.getMetadataManager();
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
                String[] filterNames = config.imageSearchFilterNames;
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
                String[] filterNames = config.imageSearchUserDefinedCheckBoxesNames;
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
//        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
//        PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)modelManager.getModel(getImageBoundaryCollectionModelName());
//
//        selectionModel.retrieve(source.get(circleSelectionKey));
//
//        // Restore list of images.
//        List<List<String>> imageList = inputFormatToList(source.get(imageListKey));
//        setImageResults(imageList);
//
//        // Restore image selections.
//        selectedFilenames = source.get(selectedImagesKey);
//
//        // Restore boundaries. First clear any associated with this model.
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
//        ImageCollection imageCollection = (ImageCollection) modelManager.getModel(getImageCollectionModelName());
//        Map<String, Boolean> showing = source.get(isShowingKey);
//        Map<String, Boolean> offLimb = source.hasKey(isOffLimbShowingKey) ? source.get(isOffLimbShowingKey) : ImmutableMap.of();
//        Map<String, Boolean> frus = source.get(isFrustrumShowingKey);
//        for (String name : showing.keySet())
//        {
//            String fullName = instrument.getSearchQuery().getDataPath() + "/" + name;
//            loadImage(fullName);
//        }
//
//        for (Image image : imageCollection.getImages())
//        {
//        	ImageKeyInterface key = image.getKey();
//        	if (instrument.equals(key.getInstrument()) && pointing.equals(key.getSource()))
//        	{
//        		String name = image.getImageName();
//        		image.setVisible(showing.containsKey(name) ? showing.get(name) : false);
//        		if (image instanceof PerspectiveImage)
//        		{
//        			PerspectiveImage perspectiveImage = (PerspectiveImage) image;
//                    perspectiveImage.setOffLimbFootprintVisibility(offLimb.containsKey(name) ? offLimb.get(name) : false);
//        			perspectiveImage.setShowFrustum(frus.containsKey(name) ? frus.get(name) : false);
//        		}
//        	}
//
//        }

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
