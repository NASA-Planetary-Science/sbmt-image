package edu.jhuapl.sbmt.image.model;

import edu.jhuapl.saavtk.config.TypedLookup;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.query.v2.IDataQuery;

/**
 * This class was part of a premature attempt to rationalize the configs. Don't expand its usage.
 */
@Deprecated
public class BasicImagingInstrument
{
    public static ImagingInstrument of(TypedLookup bodyConfiguration) {
        ImagingInstrumentConfiguration configuration = bodyConfiguration.get(SessionConfiguration.IMAGING_INSTRUMENT_CONFIG);

        SpectralImageMode spectralMode = configuration.get(ImagingInstrumentConfiguration.SPECTRAL_MODE);
        IDataQuery searchQuery = configuration.get(ImagingInstrumentConfiguration.QUERY_BASE);
        ImageType type = configuration.get(ImagingInstrumentConfiguration.IMAGE_TYPE);
        PointingSource[] searchImageSources = configuration.get(ImagingInstrumentConfiguration.IMAGE_SOURCE);
        Instrument instrument = configuration.get(ImagingInstrumentConfiguration.INSTRUMENT);
        Boolean isTranspose = configuration.get(ImagingInstrumentConfiguration.TRANSPOSE);
        String flip = "None";
        Double rotation = 0.0;
        if (configuration.get(ImagingInstrumentConfiguration.IMAGE_FLIP) != null)
        	flip = configuration.get(ImagingInstrumentConfiguration.IMAGE_FLIP);
        if (configuration.get(ImagingInstrumentConfiguration.IMAGE_ROTATION) != null)
        	rotation = configuration.get(ImagingInstrumentConfiguration.IMAGE_ROTATION);

        return new ImagingInstrument(spectralMode, searchQuery, type, searchImageSources, instrument, rotation, flip, null, isTranspose != null ? isTranspose.booleanValue() : true);
    }

//  protected BasicImagingInstrument(SpectralMode spectralMode, QueryBase searchQuery, ImageType type, ImageSource[] searchImageSources, Instrument instrument)
//  {
//      super(spectralMode, searchQuery, type, searchImageSources, instrument);
//  }

}
