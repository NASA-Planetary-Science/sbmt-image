package edu.jhuapl.sbmt.image.config;

import java.util.Date;
import java.util.List;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.sbmt.core.body.BodyViewConfig;
import edu.jhuapl.sbmt.core.config.BaseFeatureConfigIO;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Metadata;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public class ImagingInstrumentConfigIO extends BaseFeatureConfigIO // BaseInstrumentConfigIO implements MetadataManager
{
	final Key<List<ImagingInstrument>> imagingInstruments = Key.of("imagingInstruments");
	final Key<Long> imageSearchDefaultStartDate = Key.of("imageSearchDefaultStartDate");
	final Key<Long> imageSearchDefaultEndDate = Key.of("imageSearchDefaultEndDate");
	final Key<String[]> imageSearchFilterNames = Key.of("imageSearchFilterNames");
	final Key<String[]> imageSearchUserDefinedCheckBoxesNames = Key.of("imageSearchUserDefinedCheckBoxesNames");
	final Key<Double> imageSearchDefaultMaxSpacecraftDistance = Key.of("imageSearchDefaultMaxSpacecraftDistance");
	final Key<Double> imageSearchDefaultMaxResolution = Key.of("imageSearchDefaultMaxResolution");
	final Key<Boolean> hasHierarchicalImageSearch = Key.of("hasHierarchicalImageSearch");
	final Key<Metadata> hierarchicalImageSearchSpecification = Key.of("hierarchicalImageSearchSpecification");

	private String metadataVersion = "1.0";

	public ImagingInstrumentConfigIO()
	{

	}

	public ImagingInstrumentConfigIO(String metadataVersion, ViewConfig viewConfig)
	{
		this.metadataVersion = metadataVersion;
		this.viewConfig = viewConfig;
	}

	@Override
	public void retrieve(Metadata configMetadata)
	{
		featureConfig = new ImagingInstrumentConfig((BodyViewConfig)viewConfig);
		ImagingInstrumentConfig c = (ImagingInstrumentConfig) featureConfig;
		if (!configMetadata.hasKey(imagingInstruments)) return;
//		Metadata imagingMetadata = read(imagingInstruments, configMetadata);
//		if (imagingMetadata == null) return;
//		c.imagingInstruments = null; 
		c.imagingInstruments = read(imagingInstruments, configMetadata);

//		ImagingInstrument inst = new ImagingInstrument();
//		inst.retrieve(imagingMetadata);
//		c.imagingInstruments = Lists.newArrayList(inst);

		if (configMetadata.hasKey(imageSearchDefaultStartDate))
		{
			Long imageSearchDefaultStart = read(imageSearchDefaultStartDate, configMetadata);
			c.imageSearchDefaultStartDate = new Date(imageSearchDefaultStart);
		}
		if (configMetadata.hasKey(imageSearchDefaultEndDate))
		{
			Long imageSearchDefaultEnd = read(imageSearchDefaultEndDate, configMetadata);
			c.imageSearchDefaultEndDate = new Date(imageSearchDefaultEnd);

		}
		
		c.imageSearchFilterNames = read(imageSearchFilterNames, configMetadata);
		c.imageSearchUserDefinedCheckBoxesNames = read(imageSearchUserDefinedCheckBoxesNames, configMetadata);
		c.imageSearchDefaultMaxSpacecraftDistance = read(imageSearchDefaultMaxSpacecraftDistance, configMetadata);
		c.imageSearchDefaultMaxResolution = read(imageSearchDefaultMaxResolution, configMetadata);
		if (configMetadata.hasKey(hasHierarchicalImageSearch))
			c.hasHierarchicalImageSearch = read(hasHierarchicalImageSearch, configMetadata);

			// c.hierarchicalImageSearchSpecification.getMetadataManager().retrieve(read(hierarchicalImageSearchSpecification,
			// configMetadata));

		
	}

	@Override
	public Metadata store()
	{

		SettableMetadata result = SettableMetadata.of(Version.of(metadataVersion));
		storeConfig(result);
		return result;

	}

	private SettableMetadata storeConfig(SettableMetadata configMetadata)
	{
		// ImagingInstrumentConfig c = new ImagingInstrumentConfig();
		ImagingInstrumentConfig c = (ImagingInstrumentConfig) featureConfig;
//		SettableMetadata configMetadata = SettableMetadata.of(Version.of(metadataVersion));
		if (c.imagingInstruments == null) return configMetadata;

		
		write(imagingInstruments, c.imagingInstruments, configMetadata);
//		write(imagingInstruments, c.imagingInstruments.get(0).store(), configMetadata);
		writeDate(imageSearchDefaultStartDate, c.imageSearchDefaultStartDate, configMetadata);
		writeDate(imageSearchDefaultEndDate, c.imageSearchDefaultEndDate, configMetadata);
		write(imageSearchFilterNames, c.imageSearchFilterNames, configMetadata);
		write(imageSearchUserDefinedCheckBoxesNames, c.imageSearchUserDefinedCheckBoxesNames, configMetadata);
		write(imageSearchDefaultMaxSpacecraftDistance, c.imageSearchDefaultMaxSpacecraftDistance, configMetadata);
		write(imageSearchDefaultMaxResolution, c.imageSearchDefaultMaxResolution, configMetadata);
		write(hasHierarchicalImageSearch, c.hasHierarchicalImageSearch, configMetadata);
		if (c.hasHierarchicalImageSearch && c.hierarchicalImageSearchSpecification != null)
			write(hierarchicalImageSearchSpecification,
					c.hierarchicalImageSearchSpecification.getMetadataManager().store(), configMetadata);

		return configMetadata;
	}

}
