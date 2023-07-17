package edu.jhuapl.sbmt.image.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import edu.jhuapl.saavtk.util.FillDetector;
import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.query.IQueryBase;
import edu.jhuapl.sbmt.query.QueryBase;
import edu.jhuapl.sbmt.query.database.GenericPhpQuery;
import edu.jhuapl.sbmt.query.fixedlist.FixedListQuery;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class ImagingInstrument implements MetadataManager, IImagingInstrument
{
    public SpectralImageMode spectralMode;
    public QueryBase searchQuery;
    public PointingSource[] searchImageSources;
    private ImageType type;
    public Instrument instrumentName;
    private Set<Float> fillValues;
    private int[] linearInterpolationDims;
    private int[] maskValues = new int[] { 0, 0, 0, 0 };
//    private int[] padValues = new int[] {0, 0};
//    private int[] maxSizeValues = new int[] {0, 0};
    private ImageBinPadding binPadding;

    protected final Map<PointingSource, Orientation> orientationMap;

    public ImagingInstrument()
    {
        this(SpectralImageMode.MONO, null, null, null, null, 0.0, null, null, null, null, true, null);
    }

    public ImagingInstrument(double rotation, String flip)
    {
        this(SpectralImageMode.MONO, null, ImageType.GENERIC_IMAGE, null, null, rotation, flip, null, null, null, true, null);
    }

    // public ImagingInstrument(ImageType type, Instrument instrumentName)
    // {
    // this(SpectralMode.MONO, null, type, null, instrumentName, 0.0, null);
    // }

    // public ImagingInstrument(SpectralMode spectralMode)
    // {
    // this(spectralMode, null, null, null, null, 0.0, null);
    // }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, 0.0, null, null, null, null, true, null);
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, Map<PointingSource, Orientation> orientationMap)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, 0.0, null, null, null, null, true, orientationMap);
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, double rotation, String flip, ImageBinPadding binPadding)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, rotation, flip, null, null, null, true, null);
        this.binPadding = binPadding;
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, double rotation, String flip)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, rotation, flip, null, null, null, true, null);
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, double rotation, String flip, Collection<Float> fillValues)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, rotation, flip, fillValues, null, null, true, null);
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, double rotation, String flip, Collection<Float> fillValues, int[] linearInterpDims, int[] maskValues)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, rotation, flip, fillValues, linearInterpDims, maskValues, true, null);
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, double rotation, String flip, Collection<Float> fillValues, boolean isTranspose)
    {
        this(spectralMode, searchQuery, type, searchImageSources, instrumentName, rotation, flip, fillValues, null, null, isTranspose, null);
    }

    public ImagingInstrument(SpectralImageMode spectralMode, QueryBase searchQuery, ImageType type, PointingSource[] searchImageSources, Instrument instrumentName, double rotation, String flip, Collection<Float> fillValues, int[] linearInterpDims, int[] maskValues, boolean isTranspose, Map<PointingSource, Orientation> orientationMap)
    {
        this.spectralMode = spectralMode;
        this.searchQuery = searchQuery;
        this.type = type;
        this.searchImageSources = searchImageSources;
        this.instrumentName = instrumentName;
        this.fillValues = fillValues != null ? new LinkedHashSet<>(fillValues) : null;
        this.linearInterpolationDims = linearInterpDims != null ? linearInterpDims : new int[] { 0, 0, 0, 0 };
        this.maskValues = maskValues != null ? maskValues : new int[] { 0, 0, 0, 0 };

        this.orientationMap = new LinkedHashMap<>();
        if (orientationMap != null)
        {
            this.orientationMap.putAll(orientationMap);
        }
        else if (searchImageSources != null)
        {
            Orientation orientation = new OrientationFactory().of(flip != null ? ImageFlip.of(flip) : ImageFlip.NONE, rotation, isTranspose);

            for (PointingSource source : searchImageSources)
            {
                this.orientationMap.put(source, orientation);
            }
        }
    }

    public ImagingInstrument clone()
    {
        PointingSource source0 = searchImageSources != null && searchImageSources.length > 0 ? searchImageSources[0] : null;
        Orientation orientation = getOrientation(source0, null, null, null);

        return new ImagingInstrument(spectralMode, searchQuery.copy(), type, searchImageSources != null ? searchImageSources.clone() : null, instrumentName, orientation.getRotation(), orientation.getFlip().flip(), fillValues, linearInterpolationDims, maskValues, orientation.isTranspose(), orientationMap);
    }

    public ImageType getType()
    {
        return type;
    }

    public PointingSource[] getSearchImageSources()
    {
        return searchImageSources;
    }

    public SpectralImageMode getSpectralMode()
    {
        return spectralMode;
    }

    private static final Key<String> spectralModeKey = Key.of("spectralMode");
    private static final Key<String> queryType = Key.of("queryType");
    private static final Key<Metadata> queryKey = Key.of("query");
    // private static final Key<String> rootPathKey = Key.of("rootPath");
    // private static final Key<String> tablePrefixKey = Key.of("tablePrefix");
    // private static final Key<String> galleryPrefixKey =
    // Key.of("galleryPrefix");
    private static final Key<String> imageTypeKey = Key.of("imageType");
    private static final Key<String[]> imageSourcesKey = Key.of("imageSources");
    private static final Key<String> instrumentKey = Key.of("instrument");
    private static final Key<String> flipKey = Key.of("flip");
    private static final Key<Double> rotationKey = Key.of("rotation");
    private static final Key<Set<Float>> fillValuesKey = Key.of("fillValues");
    private static final Key<Boolean> isTransposeKey = Key.of("isTranspose");
    private static final Key<int[]> linearInterpolationDimsKey = Key.of("linearInterpolationDims");
    private static final Key<int[]> maskValuesKey = Key.of("maskValues");
    private static final Key<Map<String, Orientation>> orientationsKey = Key.of("orientations");
    private static final Key<ImageBinPadding> binPaddingValuesKey = Key.of("binPaddingValues");
//    private static final Key<int[]> maxSizeValuesKey = Key.of("maxSizes");

    @Override
    public void retrieve(Metadata source)
    {
        spectralMode = SpectralImageMode.valueOf(read(spectralModeKey, source));
        String searchType = read(queryType, source);
        Metadata queryMetadata = read(queryKey, source);
        // Do not use, e.g., GenericPhpQuery.class.getSimpleName() method
        // because if the class gets renamed this would not be able to read
        // previously-saved metadata.
        searchQuery = searchType.equals("GenericPhpQuery") ? new GenericPhpQuery() : new FixedListQuery<>();
        searchQuery.retrieve(queryMetadata);

        type = ImageType.valueOf(read(imageTypeKey, source));

        searchImageSources = null;
        String[] imageSources = read(imageSourcesKey, source);
        if (imageSources != null)
        {
            searchImageSources = new PointingSource[imageSources.length];
            int i = 0;
            for (String src : imageSources)
            {
                searchImageSources[i++] = PointingSource.valueOf(src);
            }
        }
        instrumentName = Instrument.valueOf(read(instrumentKey, source));

        fillValues = read(fillValuesKey, source);

        linearInterpolationDims = read(linearInterpolationDimsKey, source);
        maskValues = read(maskValuesKey, source);

        if (source.hasKey(binPaddingValuesKey))
        {
        	binPadding = read(binPaddingValuesKey, source);
        }

        Map<String, Orientation> orientationMap = read(orientationsKey, source);
        this.orientationMap.clear();
        if (orientationMap != null)
        {
            for (Entry<String, Orientation> entry : orientationMap.entrySet())
            {
                this.orientationMap.put(PointingSource.valueOf(entry.getKey()), entry.getValue());
            }
        }
        else if (searchImageSources != null)
        {
            Orientation orientation = getOrientation(null, read(flipKey, source), read(rotationKey, source), read(isTransposeKey, source));
            for (PointingSource s : searchImageSources)
            {
                this.orientationMap.put(s, orientation);
            }
        }
    }

    @Override
    public Metadata store()
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 2));
        writeEnum(spectralModeKey, spectralMode, configMetadata);
        // Do not use, e.g., GenericPhpQuery.class.getSimpleName() method
        // because if the class gets renamed this would start writing something
        // different that could not be read by the retrieve method above.
        if (searchQuery.getClass() == GenericPhpQuery.class)
        {
            write(queryType, "GenericPhpQuery", configMetadata);
        }
        else if (searchQuery.getClass() == FixedListQuery.class)
        {
            write(queryType, "FixedListQuery", configMetadata);
        }
        else
        {
            // Writing the metadata is actually not a problem --
            // searchQuery.store() should work for any query. However, throw an
            // exception here in the interest of failing fast/early. Do not
            // write metadata here that cannot be read by the retrieve method
            // above. If adding another query type, first fix the retrieve
            // method to read it, then add support here to write it.
            throw new UnsupportedOperationException("Unable to write metadata for query type " + searchQuery.getClass().getSimpleName());
        }

        PointingSource source0 = searchImageSources != null && searchImageSources.length > 0 ? searchImageSources[0] : null;
        Orientation orientation = getOrientation(source0, null, null, null);

        write(queryKey, searchQuery.store(), configMetadata);
        write(imageTypeKey, type.name(), configMetadata);
        writeEnums(imageSourcesKey, searchImageSources, configMetadata);
        writeEnum(instrumentKey, instrumentName, configMetadata);
        write(flipKey, orientation.getFlip().flip(), configMetadata);
        write(rotationKey, orientation.getRotation(), configMetadata);
        write(fillValuesKey, fillValues, configMetadata);
        write(isTransposeKey, orientation.isTranspose(), configMetadata);
        write(linearInterpolationDimsKey, linearInterpolationDims, configMetadata);
        write(maskValuesKey, maskValues, configMetadata);
        write(binPaddingValuesKey, binPadding, configMetadata);

        LinkedHashMap<String, Orientation> orientationStringMap = new LinkedHashMap<>();
        for (Entry<PointingSource, Orientation> entry : orientationMap.entrySet())
        {
            orientationStringMap.put(entry.getKey().name(), entry.getValue());
        }
        write(orientationsKey, orientationStringMap, configMetadata);

        return configMetadata;
    }

    private <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    private void writeEnum(Key<String> key, Enum<?> value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value.name());
        }
    }

    private void writeEnums(Key<String[]> key, Enum<?>[] values, SettableMetadata configMetadata)
    {
        if (values != null)
        {
            String[] names = new String[values.length];
            int i = 0;
            for (Enum<?> val : values)
            {
                names[i++] = val.name();
            }
            configMetadata.put(key, names);
        }
    }

    private <T> T read(Key<T> key, Metadata configMetadata)
    {
        if (configMetadata.hasKey(key))
        {
            return configMetadata.get(key);
        }

        return null;
    }

    public IQueryBase getSearchQuery()
    {
        return searchQuery;
    }

    public Instrument getInstrumentName()
    {
        return instrumentName;
    }

    @Override
    public FillDetector<Float> getFillDetector(Image image)
    {
        return fillValues == null ? ImageDataUtil.getDefaultFillDetector() : ImageDataUtil.getMultiFillValueDetector(fillValues);
    }

    @Override
    public Orientation getOrientation(PointingSource source)
    {
        Orientation orientation = orientationMap.get(source);

        // Do not use Preconditions to avoid string concatenations every time
        // this is called.
        if (orientation == null)
        {
            throw new IllegalArgumentException("Instrument " + getInstrumentName() + " does not have an orientation for pointing type " + source);
        }

        return orientation;
    }

    @Override
    public int[] getLinearInterpolationDims()
    {
        return linearInterpolationDims;
    }

    @Override
    public int[] getMaskValues()
    {
        return maskValues;
    }

    public double[] getFillValues()
    {
        if (fillValues == null)
            return new double[] {};
        double[] fillValuesArray = new double[fillValues.size()];
        int i = 0;
        for (Float val : fillValues)
        {
            fillValuesArray[i++] = val.doubleValue();
        }
        return fillValuesArray;
    }

    /**
	 * @param fillValues the fillValues to set
	 */
	public void setFillValues(Set<Float> fillValues)
	{
		this.fillValues = fillValues;
	}

	protected Orientation getOrientation(PointingSource imageSource, String imageFlip, Double rotation, Boolean isTranspose)
    {
        Orientation orientation;
        if (imageSource != null && orientationMap.containsKey(imageSource))
        {
            orientation = orientationMap.get(imageSource);
        }
        else
        {
            if (imageFlip == null)
            {
                imageFlip = ImageFlip.NONE.flip();
            }
            if (rotation == null)
            {
                rotation = 0.0;
            }
            if (isTranspose == null)
            {
                isTranspose = Boolean.TRUE;
            }
            orientation = new OrientationFactory().of(ImageFlip.of(imageFlip), rotation, isTranspose);
        }

        return orientation;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(linearInterpolationDims);
        result = prime * result + Arrays.hashCode(maskValues);
        result = prime * result + Arrays.hashCode(searchImageSources);
        result = prime * result + Objects.hash(fillValues, instrumentName, orientationMap, searchQuery, spectralMode, type);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ImagingInstrument))
        {
            return false;
        }
        ImagingInstrument other = (ImagingInstrument) obj;
        return Objects.equals(fillValues, other.fillValues) && instrumentName == other.instrumentName && Arrays.equals(linearInterpolationDims, other.linearInterpolationDims)
                && Arrays.equals(maskValues, other.maskValues) && Objects.equals(orientationMap, other.orientationMap)
                && Arrays.equals(searchImageSources, other.searchImageSources) && Objects.equals(searchQuery, other.searchQuery) && spectralMode == other.spectralMode && Objects.equals(type, other.type);
    }

    @Override
    public String toString()
    {
        return instrumentName + ": spectralMode=" + spectralMode + ", searchQuery=" + searchQuery + ", orientationMap=" + orientationMap + ", fillValues=" + fillValues + ", linearInterpolationDims="
                + Arrays.toString(linearInterpolationDims) + ", maskValues=" + Arrays.toString(maskValues) + "]";
    }

    public ImageBinPadding getBinPadding()
    {
    	return binPadding;
    }
}
