package edu.jhuapl.sbmt.image.model.bodies.bennu;

import java.io.IOException;

import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;

import nom.tam.fits.FitsException;


public class PolyCamImage extends MapCamImage
{
    public PolyCamImage(ImageKeyInterface key, SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }
}