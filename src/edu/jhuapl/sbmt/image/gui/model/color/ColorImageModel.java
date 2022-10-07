package edu.jhuapl.sbmt.image.gui.model.color;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Vector;

import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.NoOverlapException;
import edu.jhuapl.sbmt.image.gui.model.ColorImageResultsListener;
import edu.jhuapl.sbmt.image.model.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.image.model.ColorImageCollection;

import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import nom.tam.fits.FitsException;

public class ColorImageModel implements Controller.Model, MetadataManager
{
    protected ImageKeyInterface selectedRedKey;
    protected ImageKeyInterface selectedGreenKey;
    protected ImageKeyInterface selectedBlueKey;
    protected ColorImageCollection imageCollection;
    protected Vector<ColorImageResultsListener> resultsListeners;

    public ColorImageModel()
    {
        resultsListeners = new Vector<ColorImageResultsListener>();
    }

    public ColorImageModel(ColorImageCollection collection)
    {
        this.imageCollection = collection;
    }

    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.COLOR_IMAGES;
    }

    public ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_COLOR_IMAGE_BOUNDARIES;
    }

    public ImageKeyInterface getSelectedRedKey()
    {
        return selectedRedKey;
    }

    public ImageKeyInterface getSelectedGreenKey()
    {
        return selectedGreenKey;
    }

    public ImageKeyInterface getSelectedBlueKey()
    {
        return selectedBlueKey;
    }

    public void setSelectedRedKey(ImageKeyInterface selectedRedKey)
    {
        this.selectedRedKey = selectedRedKey;
    }

    public void setSelectedGreenKey(ImageKeyInterface selectedGreenKey)
    {
        this.selectedGreenKey = selectedGreenKey;
    }

    public void setSelectedBlueKey(ImageKeyInterface selectedBlueKey)
    {
        this.selectedBlueKey = selectedBlueKey;
    }

    public void loadImage(ColorImageKey key) throws FitsException, IOException, NoOverlapException
    {
        imageCollection.addImage(key);
    }

    public void unloadImage(ColorImageKey key)
    {
        imageCollection.removeImage(key);
    }

    public void setImages(ColorImageCollection images)
    {
        this.imageCollection = images;
    }

//    private void fireResultsChanged()
//    {
//        for (ColorImageResultsListener listener : resultsListeners)
//        {
//            listener.resultsChanged(imageResults);
//        }
//    }

    public void addResultsChangedListener(ColorImageResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(ColorImageResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
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

    public void removeColorImage(ColorImageKey colorKey)
    {
        imageCollection.removeImage(colorKey);
        fireDeleteListeners(colorKey);
    }

    protected void fireDeleteListeners(ColorImageKey key)
    {
        for (ColorImageResultsListener listener : resultsListeners)
        {
          listener.colorImageRemoved(key);
        }
    }

    @Override
    public Metadata store()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void retrieve(Metadata source)
    {
        // TODO Auto-generated method stub

    }
}
