package edu.jhuapl.sbmt.image.gui.model;

import edu.jhuapl.sbmt.image.model.ColorImage.ColorImageKey;

public interface ColorImageResultsListener
{
    public void colorImageAdded(ColorImageKey colorImageKey);

    public void colorImageRemoved(ColorImageKey image);
}
