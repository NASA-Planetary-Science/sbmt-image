package edu.jhuapl.sbmt.image.model.bodies.bennu;

import java.io.IOException;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.sbmt.common.client.SmallBodyModel;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;

import nom.tam.fits.FitsException;

public class SamCamEarthImage extends MapCamImage
{
    public SamCamEarthImage(ImageKeyInterface key,
            SmallBodyModel smallBodyModel,
            boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        // Flip image along X axis. For some reason we need to do
        // this so the image is displayed properly.
        ImageDataUtil.flipImageXAxis(rawImage);
    }
}
