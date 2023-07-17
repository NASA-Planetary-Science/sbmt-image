package edu.jhuapl.sbmt.image.model;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;

public class PerspectiveImageMetadata implements IPerspectiveImage
{
	private static final  Key<String> nameKey = Key.of("name");
    private static final  Key<String> imageFileNameKey = Key.of("imagefilename");
    private static final  Key<String> pointingSourceKey = Key.of("pointingSource");
    private static final  Key<String> pointingSourceTypeKey = Key.of("pointingSourceType");
    private static final  Key<Integer[]> intensityRangeKey = Key.of("intensityRange");
    private static final  Key<int[]> maskValuesKey = Key.of("masks");
    private static final  Key<int[]> autoMaskValuesKey = Key.of("autoMasks");
    private static final  Key<Boolean> useAutoMaskKey = Key.of("useAutoMask");
    private static final  Key<int[]> trimValuesKey = Key.of("trims");
    private static final  Key<double[]> fillValuesKey = Key.of("fillValues");
    private static final  Key<Integer> numberOfLayersKey = Key.of("numberOfLayers");
    private static final  Key<String> flipKey = Key.of("flip");
    private static final  Key<Double> rotationKey = Key.of("rotation");
    private static final  Key<String> imageOriginKey = Key.of("imageOrigin");
    private static final  Key<Double> etKey = Key.of("et");
    private static final  Key<Long> longTimeKey = Key.of("longTime");
    private static final  Key<Boolean> simulateLightingKey = Key.of("simulateLighting");
    private static final  Key<Double> offsetKey = Key.of("offset");
    private static final  Key<Double> defaultOffsetKey = Key.of("defaultOffset");
    private static final  Key<String> imageTypeKey = Key.of("imageType");
    private static final  Key<CylindricalBounds> boundsKey = Key.of("cylindricalBounds");
    private static final Key<PerspectiveImageMetadata> PERSPECTIVE_IMAGE_KEY = Key.of("perspectiveImage");
    private static final Key<Boolean> interpolationKey = Key.of("interpolation");

	private String name = "";

	private String filename;

	String pointingSource;
	Optional<String> modifiedPointingSource = Optional.ofNullable(null);

	PointingSource pointingSourceType = PointingSource.SPICE;

	//masking
	private int[] maskValues = new int[] { 0,0,0,0 };
	private int[] autoMaskValues = null;
	private boolean useAutoMask = false;

	//trim
	private int[] trimValues = new int[] {0, 0, 0, 0};

	//Linear interpolation dimensions
	private int[] linearInterpolatorDims = null;

	private boolean isLinearInterpolation = true;

	//default contrast stretch
	private IntensityRange intensityRange = new IntensityRange(0, 255);

	//default offlimb contrast stretch
	private IntensityRange offlimbIntensityRange = new IntensityRange(0, 255);

	private double offlimbDepth = -Double.NaN;
	private double minFrustumLength, maxFrustumLength;

	//number of layers
	private int numberOfLayers = 1;

	private int currentLayer = 0;

	//fill values
	private double[] fillValues = new double[] {};

	//flip and rotation
    private double rotation = 0.0;
    private String flip = "None";

    //source of image
    ImageOrigin imageOrigin = ImageOrigin.LOCAL;

//    int[] padValues, maxSizeValues;
    ImageBinPadding binPadding;
    int binning = 1;

	//needs: adjusted pointing (load and save), rendering, header/metadata

    private int index;
	private double et;
	private Long longTime;
	private boolean simulateLighting = false;
	private double offset = 1e-7;
	private double defaultOffset = 1e-7;
	private ImageType imageType;
	private CylindricalBounds bounds = new CylindricalBounds(0, 0, 0, 0);

	public PerspectiveImageMetadata(String filename, ImageType imageType, PointingSource pointingSourceType, String pointingSource, double[] fillValues)
	{
		this.filename = filename;
		this.imageType = imageType;
		this.pointingSourceType = pointingSourceType;
		this.pointingSource = pointingSource;
		this.fillValues = fillValues;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public int[] getMaskValues()
	{
		return maskValues;
	}

	public void setMaskValues(int[] maskValues)
	{
		this.maskValues = maskValues;
	}

	/**
	 * @return the useAutoMask
	 */
	public boolean isUseAutoMask()
	{
		return useAutoMask;
	}

	/**
	 * @param useAutoMask the useAutoMask to set
	 */
	public void setUseAutoMask(boolean useAutoMask)
	{
		this.useAutoMask = useAutoMask;
	}

	/**
	 * @return the autoMaskValues
	 */
	public int[] getAutoMaskValues()
	{
		return autoMaskValues;
	}

	/**
	 * @param autoMaskValues the autoMaskValues to set
	 */
	public void setAutoMaskValues(int[] autoMaskValues)
	{
		this.autoMaskValues = autoMaskValues;
	}

	public int[] getTrimValues()
	{
		return trimValues;
	}

	public void setTrimValues(int[] trimValues)
	{
		this.trimValues = trimValues;
	}

	public int[] getLinearInterpolatorDims()
	{
		return linearInterpolatorDims;
	}

	public void setLinearInterpolatorDims(int[] linearInterpolatorDims)
	{
		this.linearInterpolatorDims = linearInterpolatorDims;
	}

	public IntensityRange getIntensityRange()
	{
		return intensityRange;
	}

	public void setIntensityRange(IntensityRange intensityRange)
	{
		this.intensityRange = intensityRange;
	}

	public IntensityRange getOfflimbIntensityRange()
	{
		return offlimbIntensityRange;
	}

	public void setOfflimbIntensityRange(IntensityRange intensityRange)
	{
		this.offlimbIntensityRange = intensityRange;
	}

	public String getFilename()
	{
		return filename;
	}

	public int getNumberOfLayers()
	{
		return numberOfLayers;
	}

	public int getCurrentLayer()
	{
		return currentLayer;
	}

	public double[] getFillValues()
	{
		return fillValues;
	}

	public double getEt()
	{
		return et;
	}

	public void setEt(double et)
	{
		this.et = et;
	}

	public Date getDate()
	{
		return new Date(longTime);
	}

	public Long getLongTime()
	{
		return this.longTime;
	}

	public void setLongTime(Long longTime)
	{
		this.longTime = longTime;
	}

	public double getRotation()
	{
		return rotation;
	}

	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}

	public String getFlip()
	{
		return flip;
	}

	public void setFlip(String flip)
	{
		this.flip = flip;
	}

	public String getImageOrigin()
	{
		return imageOrigin.getFullName();
	}

	public void setImageOrigin(ImageOrigin imageOrigin)
	{
		this.imageOrigin = imageOrigin;
	}

	public boolean isSimulateLighting()
	{
		return simulateLighting;
	}

	public void setSimulateLighting(boolean simulateLighting)
	{
		this.simulateLighting = simulateLighting;
	}

	public String getPointingSource()
	{
		return pointingSource;
	}

	public void setPointingSource(String pointingSource)
	{
		this.pointingSource = pointingSource;
	}

	public PointingSource getPointingSourceType()
	{
		return pointingSourceType;
	}

	public void setPointingSourceType(PointingSource pointingSourceType)
	{
		this.pointingSourceType = pointingSourceType;
	}

	public double getOffset()
	{
		return offset;
	}

	public void setOffset(double offset)
	{
		this.offset = offset;
	}

	public double getDefaultOffset()
	{
		return defaultOffset;
	}

	public void setDefaultOffset(double defaultOffset)
	{
		this.defaultOffset = defaultOffset;
	}

	public ImageType getImageType()
	{
		return imageType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setNumberOfLayers(int numberOfLayers)
	{
		this.numberOfLayers = numberOfLayers;
	}

	public void setCurrentLayer(int index)
	{
		this.currentLayer = index;
	}

	public void setFillValues(double[] fillValues)
	{
		this.fillValues = fillValues;
	}

	public void setImageType(ImageType imageType)
	{
		this.imageType = imageType;
	}

	public void setImageBinPadding(ImageBinPadding binPadding)
	{
		this.binPadding = binPadding;
	}

	public ImageBinPadding getImageBinPadding()
	{
		return binPadding;
	}

	public void setImageBinning(int binning)
	{
		this.binning = binning;
	}

	public int getImageBinning()
	{
		return binning;
	}

	public CylindricalBounds getBounds()
	{
		return bounds;
	}

	public void setBounds(CylindricalBounds bounds)
	{
		this.bounds = bounds;
	}

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(PERSPECTIVE_IMAGE_KEY, (metadata) -> {

	        String name = metadata.get(nameKey);
	        String imagefilename = metadata.get(imageFileNameKey);
	        ImageType imageType = ImageType.valueOf(metadata.get(imageTypeKey));
	        String pointingSource = metadata.get(pointingSourceKey);
	        PointingSource pointingSourceType = PointingSource.valueFor(metadata.get(pointingSourceTypeKey));
	        IntensityRange intensityRange = new IntensityRange(metadata.get(intensityRangeKey)[0], metadata.get(intensityRangeKey)[1]);
	        int numberOfLayers = metadata.get(numberOfLayersKey);
	        String flip = metadata.get(flipKey);
	        Double rotation = metadata.get(rotationKey);
	        String imageOrigin = metadata.get(imageOriginKey);
	        boolean simulateLighting = metadata.get(simulateLightingKey);
	        Double et = metadata.get(etKey);
	        Long longTime = metadata.get(longTimeKey);
	        Double offset = metadata.get(offsetKey);
	        Double defaultOffset = metadata.get(defaultOffsetKey);
	        boolean interpolate = metadata.get(interpolationKey);

	        double[] fillValues = metadata.hasKey(fillValuesKey) ? metadata.get(fillValuesKey) : new double[] {};
	        PerspectiveImageMetadata result = new PerspectiveImageMetadata(imagefilename, imageType, pointingSourceType, pointingSource, fillValues);
	        result.setName(name);
	        result.setIntensityRange(intensityRange);
	        result.setNumberOfLayers(numberOfLayers);
	        result.setFlip(flip);
	        result.setRotation(rotation);
	        result.setImageOrigin(ImageOrigin.valueFor(imageOrigin));
	        result.setMaskValues(metadata.hasKey(maskValuesKey) ? metadata.get(maskValuesKey) : new int[] {});
	        result.setTrimValues(metadata.hasKey(trimValuesKey) ? metadata.get(trimValuesKey) : new int[] {});
	        result.setSimulateLighting(simulateLighting);
	        result.setInterpolateState(interpolate);
	        result.setEt(et);
	        result.setLongTime(longTime);
	        result.setOffset(offset);
	        result.setDefaultOffset(defaultOffset);
	        if (metadata.hasKey(boundsKey))
	        {
		        CylindricalBounds bounds = metadata.get(boundsKey);
		        result.setBounds(bounds);
	        }
	        if (metadata.hasKey(useAutoMaskKey))
	        {
	        	result.setUseAutoMask(metadata.get(useAutoMaskKey));
	        	result.setAutoMaskValues(metadata.get(autoMaskValuesKey));
	        }
			return result;
		}, PerspectiveImageMetadata.class, image -> {
			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
	        result.put(nameKey, image.getName());
	        result.put(imageFileNameKey, image.getFilename());
	        result.put(imageTypeKey, image.getImageType().toString());
	        result.put(pointingSourceKey, image.getPointingSource());
	        result.put(pointingSourceTypeKey, image.getPointingSourceType().toString());
	        result.put(intensityRangeKey, new Integer[] {image.getIntensityRange().min, image.getIntensityRange().max});
	        result.put(numberOfLayersKey, image.getNumberOfLayers());
	        result.put(flipKey, image.getFlip());
	        result.put(rotationKey, image.getRotation());
	        result.put(imageOriginKey, image.getImageOrigin());
	        result.put(maskValuesKey, image.getMaskValues());
	        result.put(autoMaskValuesKey, image.getAutoMaskValues());
	        result.put(useAutoMaskKey, image.isUseAutoMask());
	        result.put(trimValuesKey, image.getTrimValues());
	        result.put(fillValuesKey, image.getFillValues());
	        result.put(simulateLightingKey, image.isSimulateLighting());
	        result.put(interpolationKey, image.getInterpolateState());
	        result.put(etKey, image.getEt());
	        result.put(longTimeKey, image.getLongTime());
	        result.put(offsetKey, image.getOffset());
	        result.put(defaultOffsetKey, image.getDefaultOffset());
	        if (image.getBounds() != null) result.put(boundsKey, image.getBounds());
	        return result;
		});
	}

	@Override
	public List<IPerspectiveImage> getImages()
	{
		return List.of(this);
	}

	@Override
	public double getOfflimbDepth()
	{
		return offlimbDepth;
	}

	@Override
	public void setOfflimbDepth(double depth)
	{
		this.offlimbDepth = depth;
	}

	public double getMinFrustumLength()
	{
		return minFrustumLength;
	}

	public void setMinFrustumLength(double minFrustumLength)
	{
		this.minFrustumLength = minFrustumLength;
	}

	public double getMaxFrustumLength()
	{
		return maxFrustumLength;
	}

	public void setMaxFrustumLength(double maxFrustumLength)
	{
		this.maxFrustumLength = maxFrustumLength;
	}

	@Override
	public Optional<String> getModifiedPointingSource()
	{
		return modifiedPointingSource;
	}

	@Override
	public void setModifiedPointingSource(Optional<String> modifiedPointingSource)
	{
		this.modifiedPointingSource = modifiedPointingSource;
	}

	@Override
	public boolean getInterpolateState()
	{
		return isLinearInterpolation;
	}

	@Override
	public void setInterpolateState(boolean isLinear)
	{
		this.isLinearInterpolation = isLinear;
	}
}