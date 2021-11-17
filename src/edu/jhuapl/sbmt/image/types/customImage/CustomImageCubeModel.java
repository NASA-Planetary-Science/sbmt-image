package edu.jhuapl.sbmt.image.types.customImage;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.image.core.listeners.ImageCubeResultsListener;
import edu.jhuapl.sbmt.image.gui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeModel;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;

import nom.tam.fits.FitsException;

public class CustomImageCubeModel extends ImageCubeModel
{
    int nbands = 0;
    protected CustomImagesModel imageSearchModel;


    public CustomImageCubeModel()
    {
        resultsListeners = new Vector<ImageCubeResultsListener>();
    }

    public CustomImagesModel getImageSearchModel()
    {
        return imageSearchModel;
    }

    public void setImageSearchModel(CustomImagesModel imageSearchModel)
    {
        this.imageSearchModel = imageSearchModel;
    }

    public ModelNames getImageCubeCollectionModelName()
    {
        return ModelNames.CUSTOM_CUBE_IMAGES;
    }

    public String getCustomDataFolder()
    {
        return imageSearchModel.getModelManager().getPolyhedralModel().getCustomDataFolder();
    }

    public void generateImageCube(ActionEvent e)
    {
    	CustomImageKeyInterface firstKey = null;
         boolean multipleFrustumVisible = false;

         List<CustomImageKeyInterface> selectedKeys = new ArrayList<>();
         for (CustomImageKeyInterface info : imageSearchModel.getSelectedImageKeys())
         {
//             ImageKeyInterface newKey = new ImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), key.getSource(), key.getFileType(), key.getImageType(), key.getInstrument(), key.getBand(), key.getSlice(), key.getPointingFile());
            CustomImageKeyInterface newKey = null;
     		if (info.getProjectionType() == ProjectionType.PERSPECTIVE)
     		{
     			newKey = new CustomPerspectiveImageKey( //
     			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), //
     			        info.getImageFilename(), info.getSource(), info.getImageType(), info.getInstrument(), //
     			        ((CustomPerspectiveImageKey)info).getRotation(), ((CustomPerspectiveImageKey)info).getFlip(), //
     			        info.getFileType(), info.getPointingFile(), info.getDate(), info.getOriginalName());
                selectedKeys.add(newKey);
     		}
     		else
     		{
     			newKey = new CustomCylindricalImageKey( //
     			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), //
     			        info.getImageFilename(), info.getImageType(), info.getSource(), info.getDate(), info.getOriginalName());
     		}
         }
         for (CustomImageKeyInterface selectedKey : selectedKeys)
         {
            PerspectiveImage selectedImage = (PerspectiveImage)imageCollection.getImage(selectedKey);
            if(selectedImage == null)
            {
                // We are in here because the image is not mapped, display an error message and exit
                fireErrorMessage("All selected custom images must be mapped when generating an image cube.");
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
            fireErrorMessage("At least one custom image must be selected when generating an image cube.");
            return;
        }
        else if(firstKey == null)
        {
            // We are in here because no frustum was selected by user
            fireErrorMessage("At least one selected custom image must have its frustum showing when generating an image cube.");
            return;
        }
        else
        {
            PerspectiveImage firstImage = (PerspectiveImage)imageCollection.getImage(firstKey);
            ImageCubeKey<CustomImageKeyInterface> imageCubeKey = new ImageCubeKey<CustomImageKeyInterface>(selectedKeys, firstKey, firstImage.getLabelfileFullPath(), firstImage.getInfoFileFullPath(), firstImage.getSumfileFullPath());

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
                        fireInformationalMessage("More than one selected image has a visible frustum, custom image cube was generated using the first such frustum in order of appearance in the image list.");
                    }
                }
                else
                {
                    fireInformationalMessage("Image cube consisting of same images already exists, no new custom image cube was generated.");
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
                fireErrorMessage("Cube Generation: The custom images you selected do not overlap.");
            }
        }
    }
}
