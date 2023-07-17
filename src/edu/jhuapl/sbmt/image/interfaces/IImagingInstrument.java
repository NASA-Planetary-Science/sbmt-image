package edu.jhuapl.sbmt.image.interfaces;

import edu.jhuapl.saavtk.util.FillDetector;
import edu.jhuapl.sbmt.core.config.Instrument;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.Image;
import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.SpectralImageMode;
import edu.jhuapl.sbmt.query.v2.IDataQuery;

import crucible.crust.metadata.api.Metadata;

public interface IImagingInstrument
{
    Metadata store();

    /**
     * Deprecated: phase out in order to move away from enum-like
     * mission-specific types to represent image variations. Nothing should be
     * consulting this to know what to do anymore.
     *
     * @return
     */
    @Deprecated
    ImageType getType();

    /**
     * Deprecated: no longer necessary or useful for queries (fixed list v. db
     * etc.) to be determined by the instrument.
     *
     * @return
     */
    @Deprecated
    IDataQuery getSearchQuery();

    PointingSource[] getSearchImageSources();

    SpectralImageMode getSpectralMode();

    Instrument getInstrumentName();

    FillDetector<Float> getFillDetector(Image image);

    int[] getLinearInterpolationDims();

    int[] getMaskValues();

    public double[] getFillValues();

    ImageBinPadding getBinPadding();

    /**
     * Get the {@link Orientation} to use for images from this instrument when
     * projecting them using the pointing type indicated by the argument.
     *
     * @param source the pointing type
     * @return the orientation
     */
    Orientation getOrientation(PointingSource source);

}
