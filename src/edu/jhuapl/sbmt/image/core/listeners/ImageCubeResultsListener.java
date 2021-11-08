package edu.jhuapl.sbmt.image.core.listeners;

import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;

public interface ImageCubeResultsListener
{
    public void imageCubeAdded(ImageCubeKey image);

    public void imageCubeRemoved(ImageCubeKey image);

    public void presentErrorMessage(String message);

    public void presentInformationalMessage(String message);
}
