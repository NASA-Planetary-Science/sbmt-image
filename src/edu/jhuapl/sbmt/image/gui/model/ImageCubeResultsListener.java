package edu.jhuapl.sbmt.image.gui.model;

import edu.jhuapl.sbmt.image.model.ImageCube.ImageCubeKey;

public interface ImageCubeResultsListener
{
    public void imageCubeAdded(ImageCubeKey image);

    public void imageCubeRemoved(ImageCubeKey image);

    public void presentErrorMessage(String message);

    public void presentInformationalMessage(String message);
}
