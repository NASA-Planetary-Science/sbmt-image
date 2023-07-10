package edu.jhuapl.sbmt.image.config;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.IFeatureConfig;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.ImageKeyInterface;
import edu.jhuapl.sbmt.image.keys.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.image.keys.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.image.model.BasemapImage;
import edu.jhuapl.sbmt.image.model.ImageFlip;
import edu.jhuapl.sbmt.image.model.ImageRotation;
import edu.jhuapl.sbmt.image.model.ImageType;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.gson.Serializers;

public class BasemapImageConfig implements IFeatureConfig
{
	private List<ImageKeyInterface> imageMapKeys;
	private List<BasemapImage> basemapImages = Lists.newArrayList();
	private String baseMapConfigName = "config.txt";
	private String baseMapConfigNamev2 = "basemap_config.txt";
	private BodyViewConfig config;

//	static {
//		FeatureConfigIOFactory.registerFeatureConfigIO(BasemapImageConfig.class.getSimpleName(), new BasemapImageConfigIO());
//	}

	public BasemapImageConfig(BodyViewConfig config)
	{
		this.config = config;
		generateBasemapImages();
		generateImageMapKeys();
	}

	public BasemapImageConfig(List<ImageKeyInterface> imageMapKeys, List<BasemapImage> basemapImages)
	{
		this.imageMapKeys = imageMapKeys;
		this.basemapImages = basemapImages;
		if (imageMapKeys == null) this.imageMapKeys = Lists.newArrayList();
		for (ImageKeyInterface key : this.imageMapKeys)
		{
			this.basemapImages.add(convertImageKeyToBaseMap(key));
		}
	}

	public void setConfig(BodyViewConfig config)
	{
		this.config = config;
	}

	public List<BasemapImage> getBasemapImages()
	{
		return basemapImages;
	}

	public List<ImageKeyInterface> getImageMapKeys()
	{
		return imageMapKeys;
	}

	@Deprecated
	public void generateImageMapKeys()
	{
//		if (!hasImageMap)
//		{
//			return ImmutableList.of();
//		}
		if (imageMapKeys == null)
		{
			List<ImageKeyInterface> imageMapKeys = ImmutableList.of();

			// Newest/best way to specify maps is with metadata, if this model
			// has it.
			System.out.println("BasemapImageConfig: generateImageMapKeys: config " + config.getUniqueName() + " " + config.serverPath("basemap"));
			String metadataFileName = SafeURLPaths.instance().getString(config.serverPath("basemap"), baseMapConfigName);
			System.out.println("BasemapImageConfig: generateImageMapKeys: metadata file name " + metadataFileName);
			File metadataFile;
			try
			{
				metadataFile = FileCache.getFileFromServer(metadataFileName);
			}
			catch (Exception ignored)
			{
				ignored.printStackTrace();
				// This file is optional.
				metadataFile = null;
//				return ImmutableList.of();
			}
			if (metadataFile != null && metadataFile.isFile())
			{
				// Proceed using metadata.
				try
				{
					Metadata metadata = Serializers.deserialize(metadataFile, "CustomImages");
					imageMapKeys = metadata.get(Key.of("customImages"));
				}
				catch (Exception e)
				{
					// This ought to have worked so report this exception.
					e.printStackTrace();
				}
			}
			else
			{
				// Final option (legacy behavior). The key is hardwired. The
				// file could be in
				// either of two places.
				if (FileCache.isFileGettable(config.serverPath("image_map.png")))
				{
					imageMapKeys = ImmutableList.of(new CustomCylindricalImageKey("image_map", "image_map.png",
							ImageType.GENERIC_IMAGE, PointingSource.IMAGE_MAP, new Date(), "image_map"));
				}
				else if (FileCache.isFileGettable(config.serverPath("basemap/image_map.png")))
				{
					imageMapKeys = ImmutableList.of(new CustomCylindricalImageKey("image_map", "basemap/image_map.png",
							ImageType.GENERIC_IMAGE, PointingSource.IMAGE_MAP, new Date(), "image_map"));
				}
			}

			this.imageMapKeys = correctMapKeys(imageMapKeys/*, metadataFile.getParent()*/);
		}
		for (ImageKeyInterface key : imageMapKeys)
		{
			this.basemapImages.add(convertImageKeyToBaseMap(key));
		}

		return; //imageMapKeys;
	}

	private BasemapImage convertImageKeyToBaseMap(ImageKeyInterface key)
	{

        BasemapImage image = null;
        if (key instanceof CustomCylindricalImageKey)
        {
        	CustomCylindricalImageKey cylKey = (CustomCylindricalImageKey)key;
        	image = new BasemapImage(key.getImageFilename(), PointingSource.LOCAL_CYLINDRICAL, "Basemap Image", key.getName(), ImageType.GENERIC_IMAGE);
        	image.setLllat(cylKey.getLllat());
    		image.setUrlat(cylKey.getUrlat());
    		image.setLllon(cylKey.getLllon());
    		image.setUrlon(cylKey.getUrlon());
    		image.setPointingFileName("null");
        }
        else
        {
        	CustomPerspectiveImageKey perKey = (CustomPerspectiveImageKey)key;
        	image = new BasemapImage(key.getImageFilename(), perKey.getSource(), "Basemap Image", key.getName(), key.getImageType());

        	image.setPointingFileType(perKey.fileType);
    		image.setFlip(ImageFlip.of(perKey.getFlip()));
    		image.setRotation(ImageRotation.of(perKey.getRotation()));
    		image.setPointingFileName(perKey.getPointingFile());
        }
        return image;
	}

	/**
     * This converts keys with short names, file names, and original names to
     * full-fledged keys that image creators can handle. The short form is more
     * convenient and idiomatic for storage and for configuration purposes, but the
     * longer form can actually be used to create a cylindrical image object.
     *
     * If/when image key classes are revamped, the shorter form would actually be
     * preferable. The name is actually supposed to be the display name, and the
     * original name is most likely intended to hold the "original file name" in
     * cases where a file is imported into the custom area.
     *
     * @param keys the input (shorter) keys
     * @return the output (full-fledged) keys
     */
    private List<ImageKeyInterface> correctMapKeys(List<ImageKeyInterface> keys/*, String metadataDir*/)
    {
        ImmutableList.Builder<ImageKeyInterface> builder = ImmutableList.builder();
        for (ImageKeyInterface k : keys)
        {
        	if (k instanceof CustomCylindricalImageKey)
        	{
        		CustomCylindricalImageKey key = (CustomCylindricalImageKey)k;
	            String fileName = config.serverPath(key.getImageFilename());

	            CustomCylindricalImageKey correctedKey = new CustomCylindricalImageKey(fileName, fileName, ImageType.GENERIC_IMAGE, PointingSource.IMAGE_MAP, new Date(), key.getOriginalName());

	            correctedKey.setLllat(key.getLllat());
	            correctedKey.setLllon(key.getLllon());
	            correctedKey.setUrlat(key.getUrlat());
	            correctedKey.setUrlon(key.getUrlon());

	            builder.add(correctedKey);
	        }
//        	else
//        	{
//        		CustomPerspectiveImageKey key = (CustomPerspectiveImageKey)k;
//        		String imageFileName = SafeURLPaths.instance().getString(config.serverPath("basemap"), key.getImageFilename());
//        		String infoFileName = SafeURLPaths.instance().getString(config.serverPath("basemap"), key.getPointingFile());
//        		FileCache.getFileFromServer(imageFileName);
//        		FileCache.getFileFromServer(infoFileName);
//        		String fileName = SafeURLPaths.instance().getUrl(metadataDir + File.separator + key.getImageFilename());
//        		CustomPerspectiveImageKey correctedKey = new CustomPerspectiveImageKey(fileName, fileName, key.getSource(), key.getImageType(), key.getRotation(), key.getFlip(), key.getFileType(), infoFileName,
//        				key.getDate(), key.getOriginalName());
//
//        		builder.add(correctedKey);
//        	}
        }

        return builder.build();
    }

	public void generateBasemapImages()
	{
		List<BasemapImage> basemapImages = Lists.newArrayList();
		String metadataFileName = SafeURLPaths.instance().getString(config.serverPath("basemap"), baseMapConfigNamev2);
		File metadataFile;
		try
		{
			metadataFile = FileCache.getFileFromServer(metadataFileName);
			FixedMetadata readInMetadata = Serializers.deserialize(metadataFile, "Basemaps");
			basemapImages = readInMetadata.get(Key.of("basemapCollection"));
			for (BasemapImage image : basemapImages)
			{
				image.setPointingFileName(
						SafeURLPaths.instance().getString(config.serverPath("basemap"), image.getPointingFileName()));
				image.setImageFilename(
						SafeURLPaths.instance().getString(config.serverPath("basemap"), image.getImageFilename()));
			}
		}
		catch (Exception ignored)
		{
			return;
		}

		this.basemapImages.addAll(basemapImages);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		BasemapImageConfig c = (BasemapImageConfig) super.clone();
		c.basemapImages = List.copyOf(this.basemapImages);
		c.imageMapKeys = List.copyOf(this.imageMapKeys);
		return c;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((basemapImages == null) ? 0 : basemapImages.hashCode());
		result = prime * result + ((imageMapKeys == null) ? 0 : imageMapKeys.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasemapImageConfig other = (BasemapImageConfig) obj;
		if (basemapImages == null)
		{
			if (other.basemapImages != null)
				return false;
		}
		else if (!basemapImages.equals(other.basemapImages))
			return false;
		if (imageMapKeys == null)
		{
			if (other.imageMapKeys != null)
				return false;
		}
		else if (!imageMapKeys.equals(other.imageMapKeys))
			return false;
		return true;
	}

}
