package edu.jhuapl.sbmt.image.config;

import java.util.List;

import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.image.interfaces.ImageKeyInterface;
import edu.jhuapl.sbmt.image.model.BasemapImage;

import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

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
