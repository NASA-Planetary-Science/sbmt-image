package edu.jhuapl.sbmt.image.common;

import edu.jhuapl.saavtk.util.FillDetector;
import edu.jhuapl.sbmt.image.core.Image;
import edu.jhuapl.sbmt.model.image.ImageSource;
import edu.jhuapl.sbmt.model.image.Instrument;
import edu.jhuapl.sbmt.query.IQueryBase;

import crucible.crust.metadata.api.Metadata;

public interface IImagingInstrument
{
    Metadata store();

    ImageType getType();

    String getFlip();

    double getRotation();

    IQueryBase getSearchQuery();

    ImageSource[] getSearchImageSources();

    SpectralImageMode getSpectralMode();

    Instrument getInstrumentName();

    FillDetector<Float> getFillDetector(Image image);

}
