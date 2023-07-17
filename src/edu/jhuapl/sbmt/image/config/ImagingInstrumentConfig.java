package edu.jhuapl.sbmt.image.config;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.config.ExtensibleTypedLookup.Builder;
import edu.jhuapl.sbmt.config.SBMTBodyConfiguration;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.config.SBMTFileLocators;
import edu.jhuapl.sbmt.config.ShapeModelConfiguration;
import edu.jhuapl.sbmt.config.instruments.IInstrumentConfig;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.IFeatureConfig;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.core.search.HierarchicalSearchSpecification;
import edu.jhuapl.sbmt.image.interfaces.ImageKeyInterface;
import edu.jhuapl.sbmt.image.model.BasemapImage;
import edu.jhuapl.sbmt.image.model.BasicImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImagingInstrumentConfiguration;
import edu.jhuapl.sbmt.image.model.SessionConfiguration;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.image.query.ImageDataQuery;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.IDataQuery;

public class ImagingInstrumentConfig implements IFeatureConfig
{
	public Date imageSearchDefaultStartDate;
	public Date imageSearchDefaultEndDate;
	public String[] imageSearchFilterNames = new String[] {};
	public String[] imageSearchUserDefinedCheckBoxesNames = new String[] {};
	public double imageSearchDefaultMaxSpacecraftDistance;
	public double imageSearchDefaultMaxResolution;
	public boolean hasHierarchicalImageSearch = false;
	public List<ImagingInstrument> imagingInstruments = null;
	public HierarchicalSearchSpecification hierarchicalImageSearchSpecification;
	private List<ImageKeyInterface> imageMapKeys = null;
	public boolean hasImageMap = false;
	public String rootDirOnServer = "";
	private BodyViewConfig config;

	// put these in an interface?
//	public Date getDefaultImageSearchStartDate();
//	public Date getDefaultImageSearchEndDate();
//	public String[] getImageSearchFilterNames();
//	public String[] getImageSearchUserDefinedCheckBoxesNames();
//	public boolean hasHierarchicalImageSearch();
//	public HierarchicalSearchSpecification getHierarchicalImageSearchSpecification();
	
	public ImagingInstrumentConfig(BodyViewConfig config)
	{
		this.config = config;
	}
	
	public void setConfig(BodyViewConfig config)
	{
		this.config = config;
	}

	// Imaging instrument helper methods.
	private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig,
			ShapeModelConfiguration modelConfig, Instrument instrument, PointingSource[] pointingSources,
			ImageType imageType)
	{
		SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM",
				".jpeg");
//        QueryBase queryBase = new FixedListQuery(fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""), fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
		DataQuerySourcesMetadata dataSourceMetadata = DataQuerySourcesMetadata.of(
				fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation(""),
				fileLocator.get(SBMTFileLocator.TOP_PATH).getLocation("images"), null,
				fileLocator.get(SBMTFileLocator.GALLERY_FILE).getLocation(""));
		ImageDataQuery dataQuery = new ImageDataQuery(dataSourceMetadata);
		return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, dataQuery, pointingSources,
				imageType);
	}

	private static ImagingInstrument setupImagingInstrument(SBMTBodyConfiguration bodyConfig,
			ShapeModelConfiguration modelConfig, Instrument instrument, IDataQuery queryBase,
			PointingSource[] imageSources, ImageType imageType)
	{
		SBMTFileLocator fileLocator = SBMTFileLocators.of(bodyConfig, modelConfig, instrument, ".fits", ".INFO", ".SUM",
				".jpeg");
		return setupImagingInstrument(fileLocator, bodyConfig, modelConfig, instrument, queryBase, imageSources,
				imageType);
	}

	private static ImagingInstrument setupImagingInstrument(SBMTFileLocator fileLocator,
			SBMTBodyConfiguration bodyConfig, ShapeModelConfiguration modelConfig, Instrument instrument,
			IDataQuery queryBase, PointingSource[] imageSources, ImageType imageType)
	{
		Builder<ImagingInstrumentConfiguration> imagingInstBuilder = ImagingInstrumentConfiguration.builder(instrument,
				SpectralImageMode.MONO, queryBase, imageSources, fileLocator, imageType);

		// Put it all together in a session.
		Builder<SessionConfiguration> builder = SessionConfiguration.builder(bodyConfig, modelConfig, fileLocator);
		builder.put(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG, imagingInstBuilder.build());
		return BasicImagingInstrument.of(builder.build());
	}

//    @Override
	public Date getDefaultImageSearchStartDate()
	{
		return imageSearchDefaultStartDate;
	}

//	@Override
	public Date getDefaultImageSearchEndDate()
	{
		return imageSearchDefaultEndDate;
	}

//	@Override
	public HierarchicalSearchSpecification getHierarchicalImageSearchSpecification()
	{
		return hierarchicalImageSearchSpecification;
	}

	public String[] getImageSearchFilterNames()
	{
		return imageSearchFilterNames;
	}

	public String[] getImageSearchUserDefinedCheckBoxesNames()
	{
		return imageSearchUserDefinedCheckBoxesNames;
	}

	public List<ImagingInstrument> getImagingInstruments()
	{
		return imagingInstruments;
	}

	public boolean hasHierarchicalImageSearch()
	{
		return hasHierarchicalImageSearch;
	}

    public List<BasemapImage> getBasemapImages()
    {
    	return Lists.newArrayList();
    }

//	public List<ImageKeyInterface> getImageMapKeys()
//	{
//		if (!hasImageMap)
//		{
//			return ImmutableList.of();
//		}
//
//		if (imageMapKeys == null)
//		{
//			List<CustomCylindricalImageKey> imageMapKeys = ImmutableList.of();
//
//			// Newest/best way to specify maps is with metadata, if this model has it.
//			String metadataFileName = SafeURLPaths.instance().getString(serverPath("basemap"), "config.txt");
//			File metadataFile;
//			try
//			{
//				metadataFile = FileCache.getFileFromServer(metadataFileName);
//			}
//			catch (Exception ignored)
//			{
//				// This file is optional.
//				metadataFile = null;
//			}
//
//			if (metadataFile != null && metadataFile.isFile())
//			{
//				// Proceed using metadata.
//				try
//				{
//					Metadata metadata = Serializers.deserialize(metadataFile, "CustomImages");
//					imageMapKeys = metadata.get(Key.of("customImages"));
//				}
//				catch (Exception e)
//				{
//					// This ought to have worked so report this exception.
//					e.printStackTrace();
//				}
//			}
//			else
//			{
//				// Final option (legacy behavior). The key is hardwired. The file could be in
//				// either of two places.
//				if (FileCache.isFileGettable(serverPath("image_map.png")))
//				{
//					imageMapKeys = ImmutableList.of(new CustomCylindricalImageKey("image_map", "image_map.png",
//							ImageType.GENERIC_IMAGE, PointingSource.IMAGE_MAP, new Date(), "image_map"));
//				}
//				else if (FileCache.isFileGettable(serverPath("basemap/image_map.png")))
//				{
//					imageMapKeys = ImmutableList.of(new CustomCylindricalImageKey("image_map", "basemap/image_map.png",
//							ImageType.GENERIC_IMAGE, PointingSource.IMAGE_MAP, new Date(), "image_map"));
//				}
//			}
//
//			this.imageMapKeys = correctMapKeys(imageMapKeys);
//		}
//
//		return imageMapKeys;
//	}
//
//	/**
//	 * This converts keys with short names, file names, and original names to
//	 * full-fledged keys that image creators can handle. The short form is more
//	 * convenient and idiomatic for storage and for configuration purposes, but the
//	 * longer form can actually be used to create a cylindrical image object.
//	 *
//	 * If/when image key classes are revamped, the shorter form would actually be
//	 * preferable. The name is actually supposed to be the display name, and the
//	 * original name is most likely intended to hold the "original file name" in
//	 * cases where a file is imported into the custom area.
//	 *
//	 * @param keys the input (shorter) keys
//	 * @return the output (full-fledged) keys
//	 */
//	private List<ImageKeyInterface> correctMapKeys(List<CustomCylindricalImageKey> keys)
//	{
//		ImmutableList.Builder<ImageKeyInterface> builder = ImmutableList.builder();
//		for (CustomCylindricalImageKey key : keys)
//		{
//			String fileName = serverPath(key.getImageFilename());
//
//			CustomCylindricalImageKey correctedKey = new CustomCylindricalImageKey(fileName, fileName,
//					ImageType.GENERIC_IMAGE, PointingSource.IMAGE_MAP, new Date(), key.getOriginalName());
//
//			correctedKey.setLllat(key.getLllat());
//			correctedKey.setLllon(key.getLllon());
//			correctedKey.setUrlat(key.getUrlat());
//			correctedKey.setUrlon(key.getUrlon());
//
//			builder.add(correctedKey);
//		}
//
//		return builder.build();
//	}
//
//	public String serverPath(String fileName)
//    {
//        return serverPath(rootDirOnServer, fileName);
//    }
//
//    public String serverPath(String fileName, Instrument instrument)
//    {
//        return serverPath(rootDirOnServer, instrument.toString().toLowerCase(), fileName);
//    }
//
//    public String serverImagePath(String fileName, Instrument instrument)
//    {
//        return serverPath(fileName, instrument, "images");
//    }
//
//    public String serverPath(String fileName, Instrument instrument, String subdir)
//    {
//        return serverPath(rootDirOnServer, instrument.toString().toLowerCase(), subdir, fileName);
//    }
//
//	private static String serverPath(String firstSegment, String... segments)
//    {
//        // Prevent trailing delimiters coming from empty segments at the end.
//        int length = segments.length;
//        while (length > 0)
//        {
//            if (segments[length - 1].isEmpty())
//            {
//                --length;
//            }
//            else
//            {
//                break;
//            }
//        }
//        if (length < segments.length)
//        {
//            segments = Arrays.copyOfRange(segments, 0, length);
//        }
//        return SafeURLPaths.instance().getString(firstSegment, segments);
//    }

	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		ImagingInstrumentConfig c = (ImagingInstrumentConfig) super.clone();

		// deep clone imaging instruments
		if (this.imagingInstruments != null)
		{
//            int length = this.imagingInstruments.length;
//            c.imagingInstruments = new ImagingInstrument[length];
//            for (int i = 0; i < length; i++)
			//TODO fix this
//			c.imagingInstruments = this.imagingInstruments.clone();
		}

		if (this.imagingInstruments != null)
		{
			//TODO fix this
//			c.imagingInstruments = this.imagingInstruments.clone();
			c.imageSearchDefaultStartDate = (Date) this.imageSearchDefaultStartDate.clone();
			c.imageSearchDefaultEndDate = (Date) this.imageSearchDefaultEndDate.clone();
			c.imageSearchFilterNames = this.imageSearchFilterNames.clone();
			c.imageSearchUserDefinedCheckBoxesNames = this.imageSearchUserDefinedCheckBoxesNames.clone();
			c.imageSearchDefaultMaxSpacecraftDistance = this.imageSearchDefaultMaxSpacecraftDistance;
			c.imageSearchDefaultMaxResolution = this.imageSearchDefaultMaxResolution;
			c.hasHierarchicalImageSearch = this.hasHierarchicalImageSearch;
			if (this.hierarchicalImageSearchSpecification != null)
			{
				c.hierarchicalImageSearchSpecification = this.hierarchicalImageSearchSpecification.clone();
			}
			else
			{
				c.hierarchicalImageSearchSpecification = null;
			}
		}
		return c;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		result = prime * result + (hasHierarchicalImageSearch ? 1231 : 1237);
		result = prime * result + ((imageSearchDefaultEndDate == null) ? 0 : imageSearchDefaultEndDate.hashCode());
		temp = Double.doubleToLongBits(imageSearchDefaultMaxResolution);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(imageSearchDefaultMaxSpacecraftDistance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((imageSearchDefaultStartDate == null) ? 0 : imageSearchDefaultStartDate.hashCode());
		result = prime * result + Arrays.hashCode(imageSearchFilterNames);
		result = prime * result + Arrays.hashCode(imageSearchUserDefinedCheckBoxesNames);
		result = prime * result + imagingInstruments.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
		{
			return false;
		}

		ImagingInstrumentConfig other = (ImagingInstrumentConfig) obj;

		if (hasHierarchicalImageSearch != other.hasHierarchicalImageSearch)
		{
			return false;
		}
		if (imageSearchDefaultEndDate == null)
		{
			if (other.imageSearchDefaultEndDate != null)
			{
				return false;
			}
		}
		else if (!imageSearchDefaultEndDate.equals(other.imageSearchDefaultEndDate))
		{
			return false;
		}
		if (Double.doubleToLongBits(imageSearchDefaultMaxResolution) != Double
				.doubleToLongBits(other.imageSearchDefaultMaxResolution))
		{
			return false;
		}
		if (Double.doubleToLongBits(imageSearchDefaultMaxSpacecraftDistance) != Double
				.doubleToLongBits(other.imageSearchDefaultMaxSpacecraftDistance))
		{
			return false;
		}
		if (imageSearchDefaultStartDate == null)
		{
			if (other.imageSearchDefaultStartDate != null)
			{
				return false;
			}
		}
		else if (!imageSearchDefaultStartDate.equals(other.imageSearchDefaultStartDate))
		{
			return false;
		}
		if (!Arrays.equals(imageSearchFilterNames, other.imageSearchFilterNames))
		{
//			System.err.println("BodyViewConfig: equals: image search filter names don't match");
			return false;
		}
		if (!Arrays.equals(imageSearchUserDefinedCheckBoxesNames, other.imageSearchUserDefinedCheckBoxesNames))
		{
//			System.err.println("BodyViewConfig: equals: image search user defined check box names don't match");
			return false;
		}
		if (imagingInstruments != other.imagingInstruments)
		{
//			System.err.println("BodyViewConfig: equals: imaging instruments don't match");
			return false;
		}

		return true;
	}

}
