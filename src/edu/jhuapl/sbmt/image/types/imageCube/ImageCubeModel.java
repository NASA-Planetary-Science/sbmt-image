package edu.jhuapl.sbmt.image.types.imageCube;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.core.listeners.ImageCubeResultsListener;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.impl.SettableMetadata;
import nom.tam.fits.FitsException;

public class ImageCubeModel implements Controller.Model, MetadataManager
{
    protected int nbands = 0;
    protected ImageCollection imageCollection;
    protected ImageCubeCollection imageCubeCollection;
    protected Vector<ImageCubeResultsListener> resultsListeners;
    protected ImageSearchModel imageSearchModel;

    final Key<Integer> numberBandsKey = Key.of("numberOfBands");

    public ImageCubeModel()
    {
        resultsListeners = new Vector<ImageCubeResultsListener>();
    }

    public ModelNames getImageCubeCollectionModelName()
    {
        return ModelNames.CUBE_IMAGES;
    }

    public ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_IMAGE_CUBE_BOUNDARIES;
    }

    public int getNbands()
    {
        return nbands;
    }

    public void setNbands(int nbands)
    {
        this.nbands = nbands;
    }

    public void loadImage(ImageCubeKey key) throws FitsException, IOException, NoOverlapException
    {
        if (!imageCubeCollection.containsImage(key))
            imageCubeCollection.addImage(key);
    }

    public void unloadImage(ImageCubeKey key)
    {
        imageCubeCollection.removeImage(key);
    }

    public void setColorImageCollection(ImageCubeCollection images)
    {
        this.imageCubeCollection = images;
    }

    public ImageCubeCollection getImageCubeCollection()
    {
        return imageCubeCollection;
    }

    public ImageCollection getImageCollection()
    {
        return imageCollection;
    }

    public void setImageCollection(ImageCollection imageCollection)
    {
        this.imageCollection = imageCollection;
    }

    public void addResultsChangedListener(ImageCubeResultsListener listener)
    {
        resultsListeners.add(listener);
    }

    public void removeResultsChangedListener(ImageCubeResultsListener listener)
    {
        resultsListeners.remove(listener);
    }

    public void removeAllResultsChangedListeners()
    {
        resultsListeners.removeAllElements();
    }

    protected void fireDeleteListeners(ImageCubeKey key)
    {
        for (ImageCubeResultsListener listener : resultsListeners)
        {
          listener.imageCubeRemoved(key);
        }
    }

    protected void fireErrorMessage(String message)
    {
        for (ImageCubeResultsListener listener : resultsListeners)
        {
          listener.presentErrorMessage(message);
        }
    }

    protected void fireInformationalMessage(String message)
    {
        for (ImageCubeResultsListener listener : resultsListeners)
        {
          listener.presentInformationalMessage(message);
        }
    }

    public ImageSearchModel getImageSearchModel()
    {
        return imageSearchModel;
    }

    public void setImageSearchModel(ImageSearchModel imageSearchModel)
    {
        this.imageSearchModel = imageSearchModel;
    }

    public void generateImageCube(ActionEvent e) //throws edu.jhuapl.sbmt.model.image.ImageCube.NoOverlapException, IOException, FitsException
    {
        ImageKeyInterface firstKey = null;
        boolean multipleFrustumVisible = false;

        List<ImageKeyInterface> selectedKeys = new ArrayList<>();
        for (ImageKeyInterface key : imageSearchModel.getSelectedImageKeys()) { selectedKeys.add(key); }
        for (ImageKeyInterface selectedKey : selectedKeys)
        {
            PerspectiveImage selectedImage = (PerspectiveImage)imageCollection.getImage(selectedKey);
            if(selectedImage == null)
            {
                // We are in here because the image is not mapped, display an error message and exit
                fireErrorMessage("All selected images must be mapped when generating an image cube.");
                return;
            }

            // "first key" is indicated by the first image with a visible frustum
            if (selectedImage.isFrustumShowing())
             {
                if(firstKey == null)
                {
                    firstKey = selectedKey;
                }
                else
                {
                    multipleFrustumVisible = true;
                }
            }
        }

        if(selectedKeys.size() == 0)
        {
            // We are in here because no images were selected by user
            fireErrorMessage("At least one image must be selected when generating an image cube.");
            return;
        }
        else if(firstKey == null)
        {
            // We are in here because no frustum was selected by user
            fireErrorMessage("At least one selected image must have its frustum showing when generating an image cube.");
            return;
        }
        else
        {
            PerspectiveImage firstImage = (PerspectiveImage)imageCollection.getImage(firstKey);
            ImageCubeKey imageCubeKey = new ImageCubeKey(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());

            try
            {
                if (!imageCubeCollection.containsImage(imageCubeKey))
                {
                    imageCubeCollection.addImage(imageCubeKey);
                    for (ImageCubeResultsListener listener : resultsListeners)
                    {
                        listener.imageCubeAdded(imageCubeKey);
                    }

                    if(multipleFrustumVisible)
                    {
                        fireInformationalMessage("More than one selected image has a visible frustum, image cube was generated using the first such frustum in order of appearance in the image list.");
                    }
                }
                else
                {
                    fireInformationalMessage("Image cube consisting of same images already exists, no new image cube was generated.");
                }
            }
            catch (IOException e1)
            {
                fireErrorMessage("There was an error mapping the image.");
                e1.printStackTrace();
            }
            catch (FitsException e1)
            {
                fireErrorMessage("There was an error mapping the image.");
                e1.printStackTrace();
            }
            catch (ImageCube.NoOverlapException e1)
            {
                fireErrorMessage("Cube Generation: The images you selected do not overlap.");
            }
        }
    }

    public void removeImageCube(ImageCubeKey imageCubeKey)
    {
        imageCubeCollection.removeImage(imageCubeKey);
        fireDeleteListeners(imageCubeKey);
    }

    @Override
    public Metadata store()
    {
        SettableMetadata data = (SettableMetadata)imageSearchModel.store();
        data.put(numberBandsKey, nbands);
        return data;
    }

    @Override
    public void retrieve(Metadata source)
    {
        imageSearchModel.retrieve(source);
        nbands = source.get(numberBandsKey);
    }
}
