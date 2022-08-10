package edu.jhuapl.sbmt.image.gui.model.custom;


import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.io.Files;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkImageReader2;
import vtk.vtkImageReader2Factory;
import vtk.vtkPNGWriter;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickEvent;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.config.Strings;
import edu.jhuapl.sbmt.core.image.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.core.image.CustomImageKeyInterface;
import edu.jhuapl.sbmt.core.image.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.core.image.IImagingInstrument;
import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImageSource;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.core.image.ProjectionType;
import edu.jhuapl.sbmt.image.gui.model.CustomImageResultsListener;
import edu.jhuapl.sbmt.image.gui.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.image.gui.ui.custom.CustomImageImporterDialog;
import edu.jhuapl.sbmt.image.model.CustomPerspectiveImage;
import edu.jhuapl.sbmt.image.model.CylindricalImage;
import edu.jhuapl.sbmt.image.model.ImageCollection;
import edu.jhuapl.sbmt.util.VtkENVIReader;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;
import nom.tam.fits.FitsException;

public class CustomImagesModel extends ImageSearchModel
{
    private List<CustomImageKeyInterface> customImages;
    private Vector<CustomImageResultsListener> customImageListeners;
    private boolean initialized = false;
//    private int numImagesInCollection = -1;
    final Key<List<CustomImageKeyInterface>> customImagesKey = Key.of("customImages");
    CustomImageKeyInterface revisedKey = null;

    public CustomImagesModel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            Renderer renderer,
            IImagingInstrument instrument)
    {
        super(smallBodyConfig, modelManager, renderer, instrument);
        this.customImages = new Vector<CustomImageKeyInterface>();
        this.customImageListeners = new Vector<CustomImageResultsListener>();

        this.imageCollection = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
    }

    public CustomImagesModel(ImageSearchModel model)
    {
        this(model.getSmallBodyConfig(), model.getModelManager(), model.getRenderer(), model.getInstrument());
    }

    @Override
    public ModelNames getImageCollectionModelName()
    {
        return ModelNames.CUSTOM_IMAGES;
    }

    @Override
    public ModelNames getImageBoundaryCollectionModelName()
    {
        return ModelNames.PERSPECTIVE_CUSTOM_IMAGE_BOUNDARIES;
    }

    public List<CustomImageKeyInterface> getCustomImages()
    {
        return customImages;
    }

    public void setCustomImages(List<CustomImageKeyInterface> customImages)
    {
        this.customImages = customImages;
    }

    public void addResultsChangedListener(CustomImageResultsListener listener)
    {
        customImageListeners.add(listener);
    }

    public void removeResultsChangedListener(CustomImageResultsListener listener)
    {
        customImageListeners.remove(listener);
    }

    private void fireResultsChanged()
    {
        for (CustomImageResultsListener listener : customImageListeners)
        {
            listener.resultsChanged(customImages);
        }
    }

    public void loadImage(CustomImageKeyInterface key, ImageCollection images) throws FitsException, IOException
    {
        images.addImage(key);
    }

    @Override
    public void loadImage(String name)
    {
    	CustomImageKeyInterface key = (CustomImageKeyInterface)createImageKey(name, imageSourceOfLastQuery, instrument);
//        for (ImageKeyInterface key : keys)
//        {
            try
            {
                ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
                if (!images.containsImage(key))
                {
                	key.setImagefilename(getCustomDataFolder() + File.separator + key.getImageFilename());
                    loadImage(key, images);
                }
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                        "There was an error mapping the image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
            }

//        }
   }

    public CustomImageKeyInterface getRevisedKey(CustomImageKeyInterface info)
    {
    	CustomImageKeyInterface revisedKey = null;
		if (info.getProjectionType() == ProjectionType.PERSPECTIVE)
		{
			revisedKey = new CustomPerspectiveImageKey( //
			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), //
			        info.getImageFilename(), info.getSource(), info.getImageType(), //
			        ((CustomPerspectiveImageKey)info).getRotation(), ((CustomPerspectiveImageKey)info).getFlip(), //
			        info.getFileType(), info.getPointingFile(), info.getDate(), info.getOriginalName());
		}
		else
		{
			revisedKey = new CustomCylindricalImageKey( //
			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), //
			        info.getImageFilename(), info.getImageType(), info.getSource(), info.getDate(), info.getOriginalName());
		}
		return revisedKey;
    }

    public void loadImages(CustomImageKeyInterface info)
    {
		CustomImageKeyInterface revisedKey = getRevisedKey(info);
		try
        {
            if (!imageCollection.containsImage(revisedKey))
            {
                loadImage(revisedKey, imageCollection);
            }
        }
        catch (Exception e1) {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                    "There was an error mapping the image.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            e1.printStackTrace();
        }
   }

    public void unloadImage(ImageKeyInterface key, ImageCollection images)
    {
        images.removeImage(key);
    }

    @Override
    public void unloadImage(String name)
    {

    	ImageKeyInterface key = createImageKey(name, imageSourceOfLastQuery, instrument);
//        for (ImageKeyInterface key : keys)
        {
            ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
            unloadImage(key, images);
        }
   }

    public void unloadImages(String name, CustomImageKeyInterface key)
    {
    	unloadImage(key, imageCollection);
    }

    public void removeAllButtonActionPerformed(ActionEvent evt)
    {
    	for (CustomImageKeyInterface key : customImages)
        {
        	Image image = imageCollection.getImage(key);
        	if (image != null) image.setBoundaryVisibility(false);
        }
        setResultIntervalCurrentlyShown(null);
    }

    public void removeAllImagesButtonActionPerformed(ActionEvent evt)
    {
        imageCollection.removeImages(ImageSource.GASKELL);
        imageCollection.removeImages(ImageSource.GASKELL_UPDATED);
        imageCollection.removeImages(ImageSource.SPICE);
        imageCollection.removeImages(ImageSource.CORRECTED_SPICE);
        imageCollection.removeImages(ImageSource.CORRECTED);
        imageCollection.removeImages(ImageSource.LOCAL_CYLINDRICAL);
        imageCollection.removeImages(ImageSource.LOCAL_PERSPECTIVE);
    }

    public void saveImage(int index, CustomImageKeyInterface oldImageInfo, CustomImageKeyInterface newImageInfo) throws IOException
    {
        String uuid = UUID.randomUUID().toString();

        // If newImageInfo.imagefilename is null, that means we are in edit mode
        // and should continue to use the existing image
        if (newImageInfo.getImageFilename() == null)
        {
            newImageInfo.setImagefilename(oldImageInfo.getImageFilename());
        }
        else
        {
            // Check if this image is any of the supported formats
        	String newImageFilename = newImageInfo.getImageFilename();
            if(VtkENVIReader.isENVIFilename(newImageFilename)){
                // We were given an ENVI file (binary or header)
                // Can assume at this point that both binary + header files exist in the same directory
                String extension = FilenameUtils.getExtension(newImageFilename);
                // Get filenames of the binary and header files
                String enviBinaryFilename = VtkENVIReader.getBinaryFilename(newImageFilename);
                String enviHeaderFilename = VtkENVIReader.getHeaderFilename(newImageFilename);
                // Rename newImageInfo as that of the binary file
                newImageInfo.setImagefilename("image-" + uuid /*+ "." + extension*/);

                // Copy over the binary file
                Files.copy(new File(enviBinaryFilename /*+ "." + extension*/),
                        new File(getCustomDataFolder() + File.separator
                        		+ newImageInfo.getImageFilename()));
//                                + FilenameUtils.getBaseName(newImageFilename)));

                // Copy over the header file
                Files.copy(new File(enviHeaderFilename),
                        new File(getCustomDataFolder() + File.separator
                        		+ newImageInfo.getImageFilename() + ".hdr"));
//                                + VtkENVIReader.getHeaderFilename(newImageFilename)));
            }
            else if(newImageFilename.endsWith(".fit") || newImageFilename.endsWith(".fits") ||
            		newImageFilename.endsWith(".FIT") || newImageFilename.endsWith(".FITS"))
            {
                // Copy FIT file to cache
                String newFilename = "image-" + uuid + ".fit";
                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                FileUtil.copyFile(newImageFilename,  newFilepath);
                newImageInfo.setImagefilename(newFilename);
            }
            else
            {

                // Convert native VTK supported image to PNG and save to cache
                vtkImageReader2Factory imageFactory = new vtkImageReader2Factory();
                vtkImageReader2 imageReader = imageFactory.CreateImageReader2(newImageFilename);
                if (imageReader == null)
                {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
                        "The format of the specified file is not supported.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                imageReader.SetFileName(newImageFilename);
                imageReader.Update();

                vtkAlgorithmOutput imageReaderOutput = imageReader.GetOutputPort();
                vtkPNGWriter imageWriter = new vtkPNGWriter();
                imageWriter.SetInputConnection(imageReaderOutput);
                // We save out the image using a new name that makes use of a UUID
                newImageInfo.setImagefilename("image-" + uuid + ".png");
                imageWriter.SetFileName(getCustomDataFolder() + File.separator + newImageInfo.getImageFilename());
                //imageWriter.SetFileTypeToBinary();
                imageWriter.Write();
            }
        }

     // Operations specific for perspective projection type
        if (newImageInfo.getProjectionType() == ProjectionType.PERSPECTIVE)
        {

            // If newImageInfo.sumfilename and infofilename are both null, that means we are in edit mode
            // and should continue to use the existing sumfile
        	if (newImageInfo.getPointingFile() == null)
            {
            	((CustomPerspectiveImageKey)newImageInfo).pointingFilename = oldImageInfo.getPointingFile();
            }
            else
            {
            	String newFilename;
                if (newImageInfo.getFileType() == FileType.SUM)
                {
                    // We save out the sumfile using a new name that makes use of a UUID
                    newFilename = "sumfile-" + uuid + ".SUM";
                }
                else
                {
                    // We save out the infofile using a new name that makes use of a UUID
                    newFilename = "infofile-" + uuid + ".INFO";
                }
                String newFilepath = getCustomDataFolder() + File.separator + newFilename;
                FileUtil.copyFile(newImageInfo.getPointingFile(), newFilepath);
                // Change newImageInfo.infofilename to the new location of the file
                ((CustomPerspectiveImageKey)newImageInfo).pointingFilename = newFilename;
            }
        }
        if (index >= customImages.size())
        {
            customImages.add(newImageInfo);
        }
        else
        {
            customImages.set(index, newImageInfo);
        }

        updateConfigFile();
        fireResultsChanged();
        try
        {
            remapImageToRenderer(index);
        }
        catch (FitsException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void editButtonActionPerformed(ActionEvent evt)
    {
      int selectedItem = getSelectedImageIndex()[0];
      if (selectedItem >= 0)
      {
    	  CustomImageKeyInterface oldImageInfo = customImages.get(selectedItem);

          CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, true, getInstrument());
          dialog.setCurrentImageNames(getCustomImageNames());
          dialog.setImageInfo(oldImageInfo, getModelManager().getPolyhedralModel().isEllipsoid());
          dialog.setLocationRelativeTo(null);
          dialog.setVisible(true);

          // If user clicks okay replace item in list
          if (dialog.getOkayPressed())
          {
        	  CustomImageKeyInterface newImageInfo = dialog.getImageInfo();
              try
              {
                  saveImage(selectedItem, oldImageInfo, newImageInfo);
                  loadImages(newImageInfo);
              }
              catch (IOException e)
              {
                  e.printStackTrace();
              }
          }
      }
    }

    public void deleteButtonActionPerformed(ActionEvent evt)
    {
        if (getSelectedImageIndex().length == 0) return;
        int selectedItem = getSelectedImageIndex()[0];
        if (selectedItem >= 0)
        {
        	CustomImageKeyInterface info = customImages.get(selectedItem);
            unloadImage(getKeyForImageInfo(info), imageCollection);
            customImages.remove(info);
            updateConfigFile();
            fireResultsChanged();
        }
    }

    public String getFilePathForName(String infoName)
    {
    	for (CustomImageKeyInterface info : customImages)
        {
    		if (info.getName().equals(infoName)) return info.getImageFilename();
        }
    	return null;
    }

    public CustomImageKeyInterface getImageInfoForName(String infoName)
    {
    	for (CustomImageKeyInterface info : customImages)
        {
    		if (info.getName().equals(infoName)) return info;
        }
    	return null;
    }

    @Override
    public ImageKeyInterface createImageKey(String imagePathName, ImageSource sourceOfLastQuery, IImagingInstrument instrument)
    {
        for (CustomImageKeyInterface info : customImages)
        {
            if (FilenameUtils.getBaseName(info.getName()).equals(imagePathName))
            {
                return getKeyForImageInfo(info);
            }
        }
        return super.createImageKey(imagePathName, sourceOfLastQuery, instrument);
    }

    private ImageKeyInterface getKeyForImageInfo(CustomImageKeyInterface imageInfo)
    {
    	return imageInfo;
    }

    /**
     * This function unmaps the image from the renderer and maps it again,
     * if it is currently shown.
     * @throws IOException
     * @throws FitsException
     */
    public void remapImageToRenderer(int index) throws FitsException, IOException
    {
    	CustomImageKeyInterface imageKey = customImages.get(index);
        if (imageCollection.containsImage(imageKey))
        {
            Image image = imageCollection.getImage(imageKey);
            boolean visible = image.isVisible();
            if (visible)
                image.setVisible(false);
            imageCollection.removeImage(imageKey);
            imageCollection.addImage(imageKey);
            if (visible)
                image.setVisible(true);
            }
        }

    public CustomImageKeyInterface getImageKeyForIndex(int index)
    {
    	CustomImageKeyInterface imageKey = customImages.get(index);
        return imageKey;
    }

    private boolean migrateConfigFileIfNeeded() throws IOException
    {
        MapUtil configMap = new MapUtil(getConfigFilename());
        if (configMap.getAsArray(Image.IMAGE_NAMES) != null)
        {
            //backup the old config file
            FileUtils.copyFile(new File(getConfigFilename()), new File(getConfigFilename() + ".orig"));

            //migrate it to the new format
            if (configMap.containsKey(CylindricalImage.LOWER_LEFT_LATITUDES) || configMap.containsKey(Image.PROJECTION_TYPES))
            {
                boolean needToUpgradeConfigFile = false;
                String[] imageNames = configMap.getAsArray(Image.IMAGE_NAMES);
                String[] imageFilenames = configMap.getAsArray(Image.IMAGE_FILENAMES);
                String[] projectionTypes = configMap.getAsArray(Image.PROJECTION_TYPES);
                String[] imageTypes = configMap.getAsArray(Image.IMAGE_TYPES);
                String[] imageRotations = configMap.getAsArray(Image.IMAGE_ROTATIONS);
                String[] imageFlips = configMap.getAsArray(Image.IMAGE_FLIPS);
                if (imageFilenames == null)
                {
                    // for backwards compatibility
                    imageFilenames = configMap.getAsArray(Image.IMAGE_MAP_PATHS);
                    imageNames = new String[imageFilenames.length];
                    projectionTypes = new String[imageFilenames.length];
                    imageTypes = new String[imageFilenames.length];
                    imageRotations = new String[imageFilenames.length];
                    imageFlips = new String[imageFilenames.length];
                    for (int i=0; i<imageFilenames.length; ++i)
                    {
                        imageNames[i] = new File(imageFilenames[i]).getName();
                        imageFilenames[i] = "image" + i + ".png";
                        projectionTypes[i] = ProjectionType.CYLINDRICAL.toString();
                        imageTypes[i] = ImageType.GENERIC_IMAGE.toString();
                        imageRotations[i] = Double.toString(0.0);
                        imageFlips[i] = "None";
                    }

                    // Mark that we need to upgrade config file to latest version
                    // which we'll do at end of function.
                    needToUpgradeConfigFile = true;
                }
                double[] lllats = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LATITUDES);
                double[] lllons = configMap.getAsDoubleArray(CylindricalImage.LOWER_LEFT_LONGITUDES);
                double[] urlats = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LATITUDES);
                double[] urlons = configMap.getAsDoubleArray(CylindricalImage.UPPER_RIGHT_LONGITUDES);
                String[] sumfileNames = configMap.getAsArray(CustomPerspectiveImage.SUMFILENAMES);
                String[] infofileNames = configMap.getAsArray(CustomPerspectiveImage.INFOFILENAMES);

                int numImages = lllats != null ? lllats.length : (projectionTypes != null ? projectionTypes.length : 0);

                //convert to the new format
                for (int i=0; i<numImages; ++i)
                {
//                	CustomImageKeyInterface imageInfo = new CustomImageKeyInterface();
                    String name = imageNames[i];
                    String imageFilename = imageFilenames[i];
                    ProjectionType projectionType = ProjectionType.valueOf(projectionTypes[i]);
                    ImageType imageType = imageTypes == null ? ImageType.GENERIC_IMAGE : ImageType.valueOf(imageTypes[i]);
                    double rotation = imageRotations == null ? 0.0 : Double.valueOf(imageRotations[i]);
                    String flip = imageFlips == null ? "None" : imageFlips[i];

                    if (projectionTypes == null || ProjectionType.CYLINDRICAL.toString().equals(projectionTypes[i]))
                    {
                    	CustomCylindricalImageKey imageInfo = new CustomCylindricalImageKey(name, imageFilename, imageType, ImageSource.LOCAL_CYLINDRICAL, new Date(), name);

                        imageInfo.setLllat(lllats[i]);
                        imageInfo.setLllon(lllons[i]);
                        imageInfo.setUrlat(urlats[i]);
                        imageInfo.setUrlon(urlons[i]);

                        customImages.add(imageInfo);

                    }
                    else
                    {
                    	String infoname = infofileNames[i];
                    	String sumname = sumfileNames[i];
                    	String pointingFilename = "";
                    	FileType fileType;
                    	if (infoname.equals(""))
                    	{
                    		pointingFilename = sumname;
                    		fileType = FileType.SUM;
                    	}
                    	else
                    	{
                    		pointingFilename = infoname;
                    		fileType = FileType.INFO;

                    	}
                    	CustomPerspectiveImageKey imageInfo = new CustomPerspectiveImageKey(name, imageFilename, ImageSource.LOCAL_PERSPECTIVE, imageType, rotation, flip, fileType, pointingFilename, new Date(), name);

                        customImages.add(imageInfo);

                    }

                }
            }

            updateConfigFile();
            return true;
        }
        else if (configMap.getAsArray(Strings.SPECTRUM_NAMES.getName()) != null)
        {
            //backup the old config file
        	String dir = new File(getConfigFilename()).getParent();
            FileUtils.copyFile(new File(getConfigFilename()), new File(dir + File.separator + FilenameUtils.getBaseName(getConfigFilename()) + "_spect.txt"));
            FileUtils.deleteQuietly(new File(getConfigFilename()));
            return true;
        }
        else
            return false;

    }

    public void updateConfigFile()
    {
        try
        {
            Serializers.serialize("CustomImages", this, new File(getConfigFilename()));
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void initializeImageList() throws IOException
    {
        if (initialized)
            return;

        boolean updated = migrateConfigFileIfNeeded();
        if (!updated)
        {
            if (!(new File(getConfigFilename()).exists())) return;
            //check to make sure the old plate coloring config file isn't here
            MapUtil configMap = new MapUtil(getConfigFilename());
            //check here if the model uses the very old format, and rename if so
            if (configMap.containsKey("CustomShapeModelFormat"))
            {
            	File configDir = new File(getModelManager().getPolyhedralModel().getPlateConfigFilename()).getParentFile();
            	FileUtils.moveFile(new File(getConfigFilename()), new File(configDir, "shapeConfig.txt"));
				return;
            }

			if (configMap.containsKey(GenericPolyhedralModel.CELL_DATA_FILENAMES))
			{
				FileUtils.moveFile(new File(getConfigFilename()), new File(getModelManager().getPolyhedralModel().getPlateConfigFilename()));
				return;
			}
			try
			{
                FixedMetadata metadata = Serializers.deserialize(new File(getConfigFilename()), "CustomImages");
                retrieve(metadata);
			}
			catch (Exception e)
			{
			    e.printStackTrace();
			}
        }
        for (CustomImageKeyInterface info : customImages)
        {
            fireInfoChangedListeners(info);
        }

        initialized = true;
        fireResultsChanged();
        setResultIntervalCurrentlyShown(null);
    }

    public List<String> getCustomImageNames()
    {
    	ArrayList<String> list = new ArrayList<String>();
    	for (CustomImageKeyInterface info : customImages)
        {
    		list.add(info.getName());
        }
    	return list;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_PICKED.equals(evt.getPropertyName()))
        {
            PickEvent e = (PickEvent)evt.getNewValue();
            Model model = modelManager.getModel(e.getPickedProp());
            if (model instanceof ImageCollection)// || model instanceof PerspectiveImageBoundaryCollection)
            {
                // Get the actual filename of the selected image
                ImageKeyInterface key = ((ImageCollection)model).getImage((vtkActor)e.getPickedProp()).getKey();
                String name = new File(key.getName()).getName();

                int idx = -1;
                int size = customImages.size();
                for (int i=0; i<size; ++i)
                {
                    // We want to compare the actual image filename here, not the displayed name which may not be unique
                	CustomImageKeyInterface imageInfo = customImages.get(i);
                    String imageFilename = imageInfo.getImageFilename();
                    if (name.equals(imageFilename))
                    {
                        idx = i;
                        break;
                    }
                }
            }
        }
        else if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
//            // If an image was added/removed, then
//            ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//            int currImagesInCollection = images.getImages().size();
//
//            if(currImagesInCollection != numImagesInCollection)
//            {
//                // Update count of number of images in collection and update slider
//                numImagesInCollection = currImagesInCollection;
////                valueChanged(null);
//            }
        }
    }

    public void setImageVisibility(CustomImageKeyInterface key, boolean visible)
    {
//        List<ImageKey> keys = createImageKeys(name, imageSourceOfLastQuery, instrument);
//        ImageCollection images = (ImageCollection)modelManager.getModel(getImageCollectionModelName());
//        for (ImageKey key : keys)
//        {
            if (imageCollection.containsImage(key))
            {
                Image image = imageCollection.getImage(key);
                image.setVisible(visible);
            }
            propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
//        }
    }

    @Override
    public CustomImageKeyInterface[] getSelectedImageKeys()
    {
        int[] indices = selectedImageIndices;
        CustomImageKeyInterface[] selectedKeys = new CustomImageKeyInterface[indices.length];
        if (indices.length > 0)
        {
            int i=0;
            for (int index : indices)
            {
                String image = imageResults.get(index).get(0);
                String name = new File(image).getName();
                image = image.substring(0,image.length()-4);
//                ImageKey selectedKey = createImageKey(image, imageSourceOfLastQuery, instrument);
                CustomImageKeyInterface selectedKey = getImageKeyForIndex(index);
//                if (!selectedKey.band.equals("0"))
//                    name = selectedKey.band + ":" + name;
                selectedKeys[i++] = selectedKey;
            }
        }
        return selectedKeys;
    }

    private String getConfigFilename()
    {
        return getModelManager().getPolyhedralModel().getConfigFilename();
    }

    public String getCustomDataFolder()
    {
        return getModelManager().getPolyhedralModel().getCustomDataFolder();
    }

    @Override
    public IdPair getResultIntervalCurrentlyShown()
    {
        return resultIntervalCurrentlyShown;
    }

    @Override
    public void setResultIntervalCurrentlyShown(IdPair resultIntervalCurrentlyShown)
    {
        this.resultIntervalCurrentlyShown = resultIntervalCurrentlyShown;
    }

    @Override
    public Metadata store()
    {
    	SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    	result.put(customImagesKey, customImages);

    	return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
    	try
    	{
    		customImages = source.get(customImagesKey);
    	}
    	catch (ClassCastException cce)
    	{
    		Key<Metadata[]> oldCustomImagesKey = Key.of("customImages");
    		Metadata[] oldCustomImages = source.get(oldCustomImagesKey);
    		List<CustomImageKeyInterface> migratedImages = new ArrayList<CustomImageKeyInterface>();
    		for (Metadata meta : oldCustomImages)
    		{
    			migratedImages.add(CustomImageKeyInterface.retrieveOldFormat(meta));
    		}
    		customImages = migratedImages;
    		updateConfigFile();
    	}
    }

    public void saveImages(List<CustomImageKeyInterface> customImages, String filename)
    {
        SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        Metadata[] infoArray = new Metadata[customImages.size()];
        int i=0;
        final Key<Metadata[]> customImagesKey = Key.of("SavedImages");
        for (CustomImageKeyInterface info : customImages)
        {
            infoArray[i++] = info.store();
        }
        write(customImagesKey, infoArray, configMetadata);
        try
        {
            Serializers.serialize("SavedImages", configMetadata, new File(filename));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadImages(String file)
    {
        FixedMetadata metadata;
        try
        {
            final Key<Metadata[]> customImagesKey = Key.of("SavedImages");
            metadata = Serializers.deserialize(new File(file), "SavedImages");
//            retrieve(metadata);
            Metadata[] metadataArray = read(customImagesKey, metadata);
            for (Metadata meta : metadataArray)
            {
            	CustomImageKeyInterface info = CustomImageKeyInterface.retrieve(meta);
                customImages.add(info);
            }
            updateConfigFile();
            fireResultsChanged();

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected <T> void write(Key<T> key, T value, SettableMetadata configMetadata)
    {
        if (value != null)
        {
            configMetadata.put(key, value);
        }
    }

    protected <T> T read(Key<T> key, Metadata configMetadata)
    {
        T value = configMetadata.get(key);
        if (value != null)
            return value;
        return null;
    }

    public void fireInfoChangedListeners(CustomImageKeyInterface info)
    {
//        for (ImageCreationModelChangedListener listener : listeners)
//        {
//            listener.demInfoListChanged(info);
//        }
    }
}


//@Override
//public void stateChanged(ChangeEvent e)
//{
//    // Custom image slider moved
//    int index = imageList.getSelectedIndex();
//    Object selectedValue = imageList.getSelectedValue();
//    if (selectedValue == null)
//        return;
//
//    // Get the actual filename of the selected image
//    String imagename = ((ImageInfo)selectedValue).imagefilename;
//
//    JSlider source = (JSlider)e.getSource();
//    currentSlice = (int)source.getValue();
//    bandValue.setText(Integer.toString(currentSlice));
//
//    ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//    Set<Image> imageSet = images.getImages();
//    for (Image i : imageSet)
//    {
//        if (i instanceof PerspectiveImage)
//        {
//            // We want to compare the actual image filename here, not the displayed name which may not be unique
//            PerspectiveImage image = (PerspectiveImage)i;
//            ImageKey key = image.getKey();
//            String name = new File(key.name).getName();
//
//            if (name.equals(imagename))
//            {
//                image.setCurrentSlice(currentSlice);
//                image.setDisplayedImageRange(null);
//                if (!source.getValueIsAdjusting())
//                {
//                     image.loadFootprint();
//                     image.firePropertyChange();
//                }
//                return; // twupy1: Only change band for a single image now even if multiple ones are highlighted since differeent cubical images can have different numbers of bands.
//            }
//        }
//    }
//
////        System.out.println("State changed: " + fps);
//}
