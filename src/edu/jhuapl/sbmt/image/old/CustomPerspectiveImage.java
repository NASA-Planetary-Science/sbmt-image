package edu.jhuapl.sbmt.image.old;

import java.io.IOException;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.ImageKeyInterface;
import edu.jhuapl.sbmt.image.model.PerspectiveImage;
import nom.tam.fits.FitsException;
import vtk.vtkImageData;

public class CustomPerspectiveImage extends PerspectiveImage
{
    public CustomPerspectiveImage(ImageKeyInterface key, SmallBodyModel smallBodyModel, boolean loadPointingOnly) throws FitsException, IOException
    {
        super(key, smallBodyModel, loadPointingOnly);
    }

    protected void initialize() throws FitsException, IOException
    {

        super.initialize();

//        setUseDefaultFootprint(true);
    }

    @Override
    protected void processRawImage(vtkImageData rawImage)
    {
        ImageKeyInterface key = getKey();
        if (key.getSource() == PointingSource.LOCAL_PERSPECTIVE)
        {
             super.processRawImage(rawImage);
        }
    }

    @Override
    public int[] getMaskSizes()
    {
        return new int[]{0, 0, 0, 0};
    }
}
