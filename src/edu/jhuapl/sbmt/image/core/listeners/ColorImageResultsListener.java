package edu.jhuapl.sbmt.image.core.listeners;

import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.ColorImageKey;

public interface ColorImageResultsListener
{
    public void colorImageAdded(ColorImageKey colorImageKey);

    public void colorImageRemoved(ColorImageKey image);
}
