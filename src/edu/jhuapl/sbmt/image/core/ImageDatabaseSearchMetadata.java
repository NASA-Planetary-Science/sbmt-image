package edu.jhuapl.sbmt.image.core;

import java.util.List;
import java.util.TreeSet;

import org.joda.time.DateTime;

import com.google.common.collect.Range;

import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.query.database.DatabaseSearchMetadata;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;

public class ImageDatabaseSearchMetadata extends DatabaseSearchMetadata
{
    public static final Key<Boolean> SUM_OF_PRODUCTS = Key.of("Sum of Products Search");
    public static final Key<List<Integer>> CAMERAS_SELECTED = Key.of("Cameras Selected");
    public static final Key<List<Integer>> FILTERS_SELECTED = Key.of("Filters Selected");
    public static final Key<Double> FROM_RESOLUTION = Key.of("From Resolution");
    public static final Key<Double> TO_RESOLUTION = Key.of("To Resolution");
    public static final Key<TreeSet<Integer>> CUBE_LIST = Key.of("Cube List");
    public static final Key<String> IMAGE_SOURCE = Key.of("Image Source");
    public static final Key<Integer> HAS_LIMB = Key.of("Limb Image");


    protected ImageDatabaseSearchMetadata(FixedMetadata metadata)
    {
        super(metadata);
        // TODO Auto-generated constructor stub
    }

    public static ImageDatabaseSearchMetadata of(String name, DateTime startDate, DateTime stopDate,
            Range<Double> distanceRange, String searchString,
            List<Integer> polygonTypes, Range<Double> incidenceRange,
            Range<Double> emissionRange, Range<Double> phaseRange,
            boolean sumOfProductsSearch, List<Integer> camerasSelected, List<Integer> filtersSelected,
            Range<Double> resolutionRange, TreeSet<Integer> cubeList, ImageSource imageSource, int hasLimb)
    {
        FixedMetadata metadata = FixedMetadata.of(createSettableMetadata(name, startDate, stopDate, distanceRange, searchString,
                                                polygonTypes, incidenceRange, emissionRange, phaseRange, sumOfProductsSearch, camerasSelected,
                                                filtersSelected, resolutionRange, cubeList, imageSource, hasLimb));
        return new ImageDatabaseSearchMetadata(metadata);
    }

    protected static SettableMetadata createSettableMetadata(String name, DateTime startDate, DateTime stopDate,
            Range<Double> distanceRange, String searchString,
            List<Integer> polygonTypes, Range<Double> incidenceRange,
            Range<Double> emissionRange, Range<Double> phaseRange, boolean sumOfProductsSearch,
            List<Integer> camerasSelected, List<Integer> filtersSelected,
            Range<Double> resolutionRange, TreeSet<Integer> cubeList, ImageSource imageSource, int hasLimb)
    {
        SettableMetadata metadata = createSettableMetadata(name, startDate, stopDate, distanceRange, searchString, polygonTypes, incidenceRange, emissionRange, phaseRange);
        metadata.put(SUM_OF_PRODUCTS, sumOfProductsSearch);
        metadata.put(CAMERAS_SELECTED, camerasSelected);
        metadata.put(FILTERS_SELECTED, filtersSelected);
        metadata.put(FROM_RESOLUTION, resolutionRange.lowerEndpoint());
        metadata.put(TO_RESOLUTION, resolutionRange.upperEndpoint());
        metadata.put(CUBE_LIST, cubeList);
        metadata.put(IMAGE_SOURCE, imageSource.name());
        metadata.put(HAS_LIMB, hasLimb);
        return metadata;
    }

}
