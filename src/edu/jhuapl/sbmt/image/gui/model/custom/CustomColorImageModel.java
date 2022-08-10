package edu.jhuapl.sbmt.image.gui.model.custom;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.NoOverlapException;
import edu.jhuapl.sbmt.image.gui.model.ColorImageResultsListener;
import edu.jhuapl.sbmt.image.gui.model.color.ColorImageModel;
import edu.jhuapl.sbmt.image.model.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.image.model.ColorImageCollection;
import edu.jhuapl.sbmt.image.model.keys.ImageKey;

import nom.tam.fits.FitsException;

public class CustomColorImageModel extends ColorImageModel
{

    public CustomColorImageModel()
    {
        resultsListeners = new Vector<ColorImageResultsListener>();
    }

    public CustomColorImageModel(ColorImageCollection collection)
    {
        this.imageCollection = collection;
    }

    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.CUSTOM_COLOR_IMAGES;
    }


    public void generateColorImage(ActionEvent e) throws IOException, FitsException, NoOverlapException
    {
        ImageKeyInterface selectedRedKey = getSelectedRedKey();
        ImageKeyInterface selectedGreenKey = getSelectedGreenKey();
        ImageKeyInterface selectedBlueKey = getSelectedBlueKey();


        if (selectedRedKey != null && selectedGreenKey != null && selectedBlueKey != null)
        {
            ColorImageKey colorKey = new ColorImageKey(selectedRedKey, selectedGreenKey, selectedBlueKey);
            imageCollection.addImage(colorKey);
            for (ColorImageResultsListener listener : resultsListeners)
            {
                listener.colorImageAdded(colorKey);
            }

        }
    }

    @Override
    public ImageKeyInterface getSelectedRedKey()
    {
    	ImageKeyInterface newRedKey = new ImageKey(SafeURLPaths.instance().getUrl(imageCollection.getCustomDataFolder() + File.separator + selectedRedKey.getImageFilename()), selectedRedKey.getSource(), selectedRedKey.getFileType(), selectedRedKey.getImageType(), selectedRedKey.getInstrument(), selectedRedKey.getBand(), selectedRedKey.getSlice(), selectedRedKey.getPointingFile());
        return newRedKey;
    }

    @Override
    public ImageKeyInterface getSelectedGreenKey()
    {
    	ImageKeyInterface newGreenKey = new ImageKey(SafeURLPaths.instance().getUrl(imageCollection.getCustomDataFolder() + File.separator + selectedGreenKey.getImageFilename()), selectedGreenKey.getSource(), selectedGreenKey.getFileType(), selectedGreenKey.getImageType(), selectedGreenKey.getInstrument(), selectedGreenKey.getBand(), selectedGreenKey.getSlice(), selectedGreenKey.getPointingFile());
        return newGreenKey;
    }

    @Override
    public ImageKeyInterface getSelectedBlueKey()
    {
    	ImageKeyInterface newBlueKey = new ImageKey(SafeURLPaths.instance().getUrl(imageCollection.getCustomDataFolder() + File.separator + selectedBlueKey.getImageFilename()), selectedBlueKey.getSource(), selectedBlueKey.getFileType(), selectedBlueKey.getImageType(), selectedBlueKey.getInstrument(), selectedBlueKey.getBand(), selectedBlueKey.getSlice(), selectedBlueKey.getPointingFile());
        return newBlueKey;
    }

}
