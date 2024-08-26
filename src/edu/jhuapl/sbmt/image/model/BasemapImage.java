package edu.jhuapl.sbmt.image.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;
import edu.jhuapl.ses.jsqrl.impl.gson.Serializers;

public class BasemapImage
{
	//general fields
	private String imageFilename;
	private PointingSource pointingType;
	private String imageDescription;
	private String imageName;
	private ImageType imageType;

	//perspective based fields
	private ImageRotation rotation = ImageRotation.Zero;
	private ImageFlip flip = ImageFlip.NONE;
	private String pointingFileName;
	private FileType pointingFileType = null;


	//cylindrical based fields
	private double lllat, lllon, urlat, urlon;


    private static final Key<String> imageFilenameKey = Key.of("imageFilename" );
    private static final Key<String> pointingTypeKey = Key.of("pointingType");
    private static final Key<String> imageDescriptionKey = Key.of("imageDescription");
    private static final Key<String> imageNameKey = Key.of("imageName");
    private static final Key<String> imageTypeKey = Key.of("imageType");

    private static final Key<Double> imageRotationKey = Key.of("imageRotation");
    private static final Key<String> imageFlipKey = Key.of("imageFlip");
    private static final Key<String> pointingFileNameKey = Key.of("pointingFileName");
    private static final Key<String> pointingFileTypeKey = Key.of("pointingFileType");


    private static final Key<Double> lllatKey = Key.of("lllat");
    private static final Key<Double> lllonKey = Key.of("lllon");
    private static final Key<Double> urlatKey = Key.of("urlat");
    private static final Key<Double> urlonKey = Key.of("urlon");

    private static final Key<BasemapImage> BASEMAP_IMAGE_KEY = Key.of("basemapImage");

	public BasemapImage()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param imageFilename
	 * @param pointingType
	 * @param imageDescription
	 * @param imageName
	 * @param imageType
	 * @param pointingFileType
	 */
	public BasemapImage(String imageFilename, PointingSource pointingType, String imageDescription, String imageName,
			ImageType imageType)
	{
		this.imageFilename = imageFilename;
		this.pointingType = pointingType;
		this.imageDescription = imageDescription;
		this.imageName = imageName;
		this.imageType = imageType;
	}

	public static void initializeSerializationProxy()
	{
		InstanceGetter.defaultInstanceGetter().register(BASEMAP_IMAGE_KEY, (metadata) -> {

	        String imagefilename = metadata.get(imageFilenameKey);
	        PointingSource pointingType = PointingSource.valueFor(metadata.get(pointingTypeKey));
	        String description = metadata.get(imageDescriptionKey);
	        String name = metadata.get(imageNameKey);
	        ImageType imageType = ImageType.valueOf(metadata.get(imageTypeKey));

	        ImageRotation rotation = ImageRotation.of(metadata.get(imageRotationKey));
	        ImageFlip flip = ImageFlip.valueOf(metadata.get(imageFlipKey));
	        String pointingFileName = metadata.get(pointingFileNameKey);
	        String pointingFileTypeValue = metadata.get(pointingFileTypeKey);
	        FileType pointingFileType = pointingFileTypeValue.isEmpty() ? null : FileType.valueOf(pointingFileTypeValue);

	        Double lllat = metadata.get(lllatKey);
	        Double urlat = metadata.get(urlatKey);
	        Double lllon = metadata.get(lllonKey);
	        Double urlon = metadata.get(urlonKey);

	        BasemapImage image = new BasemapImage(imagefilename, pointingType, description, name, imageType);
	        if (pointingType == PointingSource.LOCAL_CYLINDRICAL)
	        {
	        	image.setLllat(lllat);
	    		image.setUrlat(urlat);
	    		image.setLllon(lllon);
	    		image.setUrlon(urlon);
	        }
	        else
	        {
	        	image.setPointingFileType(pointingFileType);
	    		image.setFlip(flip);
	    		image.setRotation(rotation);
	    		image.setPointingFileName(pointingFileName);
	        }

			return image;
		}, BasemapImage.class, key -> {
			SettableMetadata result = SettableMetadata.of(Version.of(1, 1));
			result.put(imageFilenameKey, key.imageFilename);
			result.put(pointingTypeKey, key.pointingType.toString());
			result.put(imageDescriptionKey, key.imageDescription);
			result.put(imageNameKey, key.imageName);
			result.put(imageTypeKey, key.imageType.toString());

			result.put(imageRotationKey, Double.parseDouble(key.rotation.toString()));
			result.put(imageFlipKey, key.flip.toString().toUpperCase());
			result.put(pointingFileNameKey, key.pointingFileName);
			result.put(pointingFileTypeKey, key.pointingFileType == null ? "" : key.pointingFileType.toString());

			result.put(lllatKey, key.lllat);
			result.put(urlatKey, key.urlat);
			result.put(lllonKey, key.lllon);
			result.put(urlonKey, key.urlon);

			return result;
		});
	}

	public void setRotation(ImageRotation rotation)
	{
		this.rotation = rotation;
	}

	public void setFlip(ImageFlip flip)
	{
		this.flip = flip;
	}

	public void setPointingFileName(String pointingFileName)
	{
		this.pointingFileName = pointingFileName;
	}

	public void setLllat(double lllat)
	{
		this.lllat = lllat;
	}

	public void setLllon(double lllon)
	{
		this.lllon = lllon;
	}

	public void setUrlat(double urlat)
	{
		this.urlat = urlat;
	}

	public void setUrlon(double urlon)
	{
		this.urlon = urlon;
	}

	public void setPointingFileType(FileType pointingFileType)
	{
		this.pointingFileType = pointingFileType;
	}

	/**
	 * @param imageFilename the imageFilename to set
	 */
	public void setImageFilename(String imageFilename)
	{
		this.imageFilename = imageFilename;
	}

	/**
	 * @return the imageFilename
	 */
	public String getImageFilename()
	{
		return imageFilename;
	}

	/**
	 * @return the pointingType
	 */
	public PointingSource getPointingType()
	{
		return pointingType;
	}

	/**
	 * @return the imageDescription
	 */
	public String getImageDescription()
	{
		return imageDescription;
	}

	/**
	 * @return the imageName
	 */
	public String getImageName()
	{
		return imageName;
	}

	/**
	 * @return the imageType
	 */
	public ImageType getImageType()
	{
		return imageType;
	}

	/**
	 * @return the rotation
	 */
	public ImageRotation getRotation()
	{
		return rotation;
	}

	/**
	 * @return the flip
	 */
	public ImageFlip getFlip()
	{
		return flip;
	}

	/**
	 * @return the pointingFileName
	 */
	public String getPointingFileName()
	{
		return pointingFileName;
	}

	/**
	 * @return the pointingFileType
	 */
	public FileType getPointingFileType()
	{
		return pointingFileType;
	}

	/**
	 * @return the lllat
	 */
	public double getLllat()
	{
		return lllat;
	}

	/**
	 * @return the lllon
	 */
	public double getLllon()
	{
		return lllon;
	}

	/**
	 * @return the urlat
	 */
	public double getUrlat()
	{
		return urlat;
	}

	/**
	 * @return the urlon
	 */
	public double getUrlon()
	{
		return urlon;
	}

	/**
	 * @return the basemapImageKey
	 */
	public static Key<BasemapImage> getBasemapImageKey()
	{
		return BASEMAP_IMAGE_KEY;
	}

	@Override
	public String toString()
	{
		return "BasemapImage [imageFilename=" + imageFilename + ", pointingType=" + pointingType + ", imageDescription="
				+ imageDescription + ", imageName=" + imageName + ", imageType=" + imageType + ", rotation=" + rotation
				+ ", flip=" + flip + ", pointingFileName=" + pointingFileName + ", pointingFileType=" + pointingFileType
				+ ", lllat=" + lllat + ", lllon=" + lllon + ", urlat=" + urlat + ", urlon=" + urlon + "]";
	}

	public static void main(String[] args) throws IOException
	{
		BasemapImage.initializeSerializationProxy();
		BasemapImage image1 = new BasemapImage("phobos_cyl_dlr_control.jpg", PointingSource.LOCAL_CYLINDRICAL, "Stooke. P., Stooke Small Bodies Maps V3.0. MULTI-SA-MULTI-6-STOOKEMAPS-V3.0. NASA Planetary Data System, 2015." ,
				"Global Basemap (Stooke 2015) [TBR]", ImageType.GENERIC_IMAGE);
		image1.setLllat(-90);
		image1.setUrlat(90);
		image1.setLllon(-180);
		image1.setUrlon(180);


		BasemapImage image2 = new BasemapImage("PSP_007769_9010_IRB.png", PointingSource.LOCAL_PERSPECTIVE, "PSP_007769_9010_IRB (NASA/JPL-Caltech/UArizona)" ,
				"HiRISE Color Composite [TBR]", ImageType.GENERIC_IMAGE);
		image2.setPointingFileType(FileType.INFO);
		image2.setFlip(ImageFlip.Y);
		image2.setRotation(ImageRotation.of(180));
		image2.setPointingFileName("PSP_007769_9010_IRB.INFO");

		List<BasemapImage> list = Lists.newArrayList();
		list.add(image1);
		list.add(image2);

		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put( Key.of("basemapCollection"), list);

    	File file = new File("/Users/steelrj1/Desktop/basemap_config.txt");
		Serializers.serialize("Basemaps", result, file);



		FixedMetadata readInMetadata = Serializers.deserialize(file, "Basemaps");
		List<BasemapImage> basemapImages = readInMetadata.get(Key.of("basemapCollection"));
		for (BasemapImage image : basemapImages)
		{
			System.out.println("BasemapImage: main: image " + image);
		}
	}

}
