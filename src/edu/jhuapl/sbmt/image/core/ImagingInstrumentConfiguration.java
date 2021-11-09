package edu.jhuapl.sbmt.image.core;

import edu.jhuapl.saavtk.config.Configurable;
import edu.jhuapl.saavtk.config.ExtensibleTypedLookup;
import edu.jhuapl.saavtk.config.FixedTypedLookup;
import edu.jhuapl.saavtk.config.Key;
import edu.jhuapl.sbmt.config.SBMTFileLocator;
import edu.jhuapl.sbmt.image.common.ImageType;
import edu.jhuapl.sbmt.image.common.SpectralImageMode;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.query.QueryBase;

public class ImagingInstrumentConfiguration extends ExtensibleTypedLookup implements Configurable
{
    // Required keys.
    public static final Key<Instrument> INSTRUMENT = Key.of("Instrument");
    public static final Key<SpectralImageMode> SPECTRAL_MODE = Key.of("Spectral Mode");
    public static final Key<QueryBase> QUERY_BASE = Key.of("Search query");
    public static final Key<ImageSource[]> IMAGE_SOURCE = Key.of("Image source for searches");
    public static final Key<SBMTFileLocator> FILE_LOCATOR = Key.of("Image file locator");

    // Optional keys.
    // This one is used only on the sbmt2 branch.
//    public static final Key<ImageDataFilter> DATA_FILTER = Key.of("Image data filter");
    // This one is used only on the sbmt1dev branch.
    public static final Key<ImageType> IMAGE_TYPE = Key.of("Image type");
    public static final Key<String> GALLERY_PATH = Key.of("Gallery path"); // If there is a gallery. Relative to image directory.
    public static final Key<String> DISPLAY_NAME = Key.of("Display name"); // If different from instrument.toString().
    public static final Key<Boolean> TRANSPOSE = Key.of("Transpose image");

    // Use this one for sbmt2 branch.
    public static Builder<ImagingInstrumentConfiguration> builder(
            Instrument instrument,
            SpectralImageMode spectralMode,
            QueryBase queryBase,
            ImageSource[] imageSource,
            SBMTFileLocator imageFileLocator)
    {
        FixedTypedLookup.Builder fixedBuilder = FixedTypedLookup.builder();
        fixedBuilder.put(INSTRUMENT, instrument);
        fixedBuilder.put(SPECTRAL_MODE, spectralMode);
        fixedBuilder.put(QUERY_BASE, queryBase);
        fixedBuilder.put(IMAGE_SOURCE, imageSource);
        fixedBuilder.put(FILE_LOCATOR, imageFileLocator);
        return new Builder<ImagingInstrumentConfiguration>(fixedBuilder) {
            @Override
            public ImagingInstrumentConfiguration doBuild()
            {
                return new ImagingInstrumentConfiguration(getFixedBuilder());
            }
        };
    }

    // Use this one for sbmt1dev branch.
    public static Builder<ImagingInstrumentConfiguration> builder(
            Instrument instrument,
            SpectralImageMode spectralMode,
            QueryBase queryBase,
            ImageSource[] imageSource,
            SBMTFileLocator imageFileLocator,
            ImageType type)
    {
        Builder<ImagingInstrumentConfiguration> builder = builder(instrument, spectralMode, queryBase, imageSource, imageFileLocator);
        builder.put(IMAGE_TYPE, type);
        return builder;
    }

    protected ImagingInstrumentConfiguration(FixedTypedLookup.Builder builder)
    {
        super(builder);
    }

}
