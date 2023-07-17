package edu.jhuapl.sbmt.image.config;

import java.util.List;

import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.image.interfaces.ImageKeyInterface;
import edu.jhuapl.sbmt.image.model.BasemapImage;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class BasemapImageConfigIO extends BaseFeatureConfigIO
{
	final Key<List<ImageKeyInterface>> imageMapKey = Key.of("imageMaps");
	final Key<List<BasemapImage>> basemapKey = Key.of("basemaps");
	private String metadataVersion = "1.0";
//	public BasemapImageConfig config;

	public BasemapImageConfigIO()
	{
//		this.metadataVersion = metadataVersion;
	}

	@Override
	public void retrieve(Metadata source)
	{
		List<BasemapImage> basemaps = read(basemapKey, source);
		List<ImageKeyInterface> imageMaps = read(imageMapKey, source);

		featureConfig = new BasemapImageConfig(imageMaps, basemaps);

	}

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(metadataVersion));
		BasemapImageConfig config = (BasemapImageConfig)featureConfig;
		write(basemapKey, config.getBasemapImages(), result);
		if (config.getImageMapKeys() != null && !config.getImageMapKeys().isEmpty())
			write(imageMapKey, config.getImageMapKeys(), result);


		return result;
	}

}
