package edu.jhuapl.sbmt.image.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;
import org.jfree.data.Range;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableList;

import crucible.crust.logging.SimpleLogger;
import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;
import crucible.crust.metadata.impl.gson.Serializers;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.interfaces.PerspectiveImageCollectionListener;
import edu.jhuapl.sbmt.image.keys.CustomImageKeyInterface;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.LowResolutionBoundaryOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.ImagePipelineFactory;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.ImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages.RenderableCylindricalImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;
import vtk.vtkActor;
import vtk.vtkImageData;
import vtk.vtkProp;
import vtk.vtkProperty;

public class PerspectiveImageCollection<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends SaavtkItemManager<G1> implements PropertyChangeListener
{
	private ConcurrentHashMap<IImagingInstrument, List<G1>> imagesByInstrument;
	private ConcurrentHashMap<IImagingInstrument, IdPair> currentBoundaryRangeByInstrument;
	private ConcurrentHashMap<IImagingInstrument, Integer> currentBoundaryOffsetByInstrument;
	private List<G1> userImages;
	private List<G1> userImagesModified;
	private List<SmallBodyModel> smallBodyModels;
	private ConcurrentHashMap<G1, List<vtkActor>> imageRenderers;
	private ConcurrentHashMap<G1, List<vtkActor>> boundaryRenderers;
	private ConcurrentHashMap<G1, List<vtkActor>> frustumRenderers;
	private ConcurrentHashMap<G1, List<vtkActor>> offLimbRenderers;
	private ConcurrentHashMap<G1, List<vtkActor>> offLimbBoundaryRenderers;
	private ConcurrentHashMap<G1, PerspectiveImageRenderingState<G1>> renderingStates;
	@SuppressWarnings("unused")
	private SimpleLogger logger = SimpleLogger.getInstance();
	private IImagingInstrument imagingInstrument;
//	private IdPair currentBoundaryRange = new IdPair(0, 9);
//	private int currentBoundaryOffsetAmount = 10;
	
	private boolean firstCustomLoad = true;
	private List<PerspectiveImageCollectionListener> listeners;
	private static ExecutorService executor = Executors.newCachedThreadPool();

	public PerspectiveImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.listeners = Lists.newArrayList();
		this.imagesByInstrument = new ConcurrentHashMap<IImagingInstrument, List<G1>>();
		this.currentBoundaryRangeByInstrument = new ConcurrentHashMap<IImagingInstrument, IdPair>();
		this.currentBoundaryOffsetByInstrument = new ConcurrentHashMap<IImagingInstrument, Integer>();
		this.userImages = Lists.newArrayList();
		this.userImagesModified = Lists.newArrayList();
		this.imageRenderers = new ConcurrentHashMap<G1, List<vtkActor>>();
		this.boundaryRenderers = new ConcurrentHashMap<G1, List<vtkActor>>();
		this.frustumRenderers = new ConcurrentHashMap<G1, List<vtkActor>>();
		this.offLimbRenderers = new ConcurrentHashMap<G1, List<vtkActor>>();
		this.offLimbBoundaryRenderers = new ConcurrentHashMap<G1, List<vtkActor>>();
		this.renderingStates = new ConcurrentHashMap<G1, PerspectiveImageRenderingState<G1>>();
		this.smallBodyModels = smallBodyModels;
		migrateOldUserList();
		for (SmallBodyModel smallBodyModel : smallBodyModels)
		{
			smallBodyModel.addPropertyChangeListener((evt) -> {

				if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							boolean mappedImages = renderingStates.values().stream().filter(pred -> pred.isMapped).toList().isEmpty();
							if (!mappedImages)
							{
								int response = JOptionPane.showConfirmDialog(
									    null,
									    "Mapped images will not appear properly on the surface of the new model \nuntil they are re-rendered, which may take some time. Re-render mapped images? \n\n (Unmapped, loaded images will need to be re-rendered via right click.)",
									    "Remap Images?",
									    JOptionPane.YES_NO_OPTION);
								if (response == JOptionPane.NO_OPTION) return;
							}

			            	Set<G1> keySet = renderingStates.keySet();
			            	for (G1 image : keySet)
			            	{
			            		if (renderingStates.get(image).isMapped)
			            			setImageMapped(image, true, true);
			            	}
						}
					});
				}

			});
		}
	}
	
	public void addListener(PerspectiveImageCollectionListener picl)
	{
		listeners.add(picl);
	}
	
	private void fireImageMapStartedListeners()
	{
		listeners.forEach(listener -> listener.imageMapStarted());
	}
	
	private void fireImageMapEndedListeners()
	{
		listeners.forEach(listener -> listener.imageMapEnded());
	}

	public void clearSearchedImages()
	{
		List<G1> activeImages = imagesByInstrument.get(imagingInstrument);
		if (activeImages == null) return;
		for (G1 image : activeImages)
		{
			if (renderingStates.get(image).isMapped)
				setImageMapped(image, false);
			if (renderingStates.get(image).isBoundaryShowing)
				setImageBoundaryShowing(image, false);
			if (renderingStates.get(image).isFrustumShowing)
				setImageFrustumVisible(image, false);
			if (renderingStates.get(image).isOfflimbShowing)
				setImageOfflimbShowing(image, false);
			if (renderingStates.get(image).isOffLimbBoundaryShowing)
				setOffLimbBoundaryShowing(image, false);
			renderingStates.remove(image);
		}
		imagesByInstrument.clear();
		currentBoundaryOffsetByInstrument.put(imagingInstrument, 10);
		currentBoundaryRangeByInstrument.put(imagingInstrument, new IdPair(0, currentBoundaryOffsetByInstrument.get(imagingInstrument)-1));
// 		currentBoundaryRange = new IdPair(0, currentBoundaryOffsetAmount-1);
	}

	public void clearUserImages()
	{
		if (userImages.size() == 0) return;
		for (G1 image : userImages)
		{
			if (renderingStates.get(image).isMapped)
				setImageMapped(image, false);
			if (renderingStates.get(image).isBoundaryShowing)
				setImageBoundaryShowing(image, false);
			if (renderingStates.get(image).isFrustumShowing)
				setImageFrustumVisible(image, false);
			if (renderingStates.get(image).isOfflimbShowing)
				setImageOfflimbShowing(image, false);
			if (renderingStates.get(image).isOffLimbBoundaryShowing)
				setOffLimbBoundaryShowing(image, false);
			renderingStates.remove(image);
		}
		userImages.clear();
	}

	public void addUserImage(G1 image)
	{
		userImages.add(image);
		PerspectiveImageRenderingState<G1> state = new PerspectiveImageRenderingState<G1>();
		Color color = ColorUtil.generateColor(userImages.indexOf(image)%100, 100);
		state.boundaryColor = color;
		renderingStates.put(image,state);
		userImagesModified.add(image);
		updateUserList();	//update the user created list, stored in metadata
	}
	
	public void addUserImage(G1 image, PerspectiveImageRenderingState<G1> state)
	{
		userImages.add(image);
		Color color = ColorUtil.generateColor(userImages.indexOf(image)%100, 100);
		state.boundaryColor = color;
		renderingStates.put(image,state);
		userImagesModified.add(image);
		updateUserList();	//update the user created list, stored in metadata
	}
	
	public boolean isUserImage(G1 image)
	{
		return userImages.contains(image);
	}

	private void migrateOldUserList()
	{
		String newUserFilename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages.txt";
		if (new File(newUserFilename).exists()) return;	//conversion has already taken place
		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "config.txt";
		if (!new File(filename).exists()) return;	//there is no previous version file
		FixedMetadata metadata;
        try
        {
        	Key<List<CustomImageKeyInterface>> userImagesKey = Key.of("customImages");
        	metadata = Serializers.deserialize(new File(filename), "CustomImages");
        	List<CustomImageKeyInterface> customImages = metadata.get(userImagesKey);
        	for (CustomImageKeyInterface info : customImages)
            {
        		G1 image = convertCustomImageKeyInterfaceToModern(info);
        		userImages.add(image);

            }
        	updateUserList();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        loadUserList();
	}

	/**
	 * Convert pre-Image reorg custom image metadata files to the new CompositePerspectiveImage format. This includes
	 * fields for items such as masking, fill values and interpolation that were not present before the image reorg.
	 * @param info
	 * @return
	 */
	private G1 convertCustomImageKeyInterfaceToModern(CustomImageKeyInterface info)
	{
		PerspectiveImageMetadata image = null;
		double[] fillValues = new double[] {};
		ImageType imageType = info.getImageType();
		String filePath = smallBodyModels.get(0).getCustomDataFolder() + File.separator + info.getImageFilename();
		String pointingFile = smallBodyModels.get(0).getCustomDataFolder() + File.separator + info.getPointingFile();
		PointingSource pointingSourceType = info.getSource();
		image = new PerspectiveImageMetadata(filePath, imageType, pointingSourceType, pointingFile, fillValues);
		image.setName(info.getName());
		image.setImageOrigin(ImageOrigin.LOCAL);
		image.setLongTime(info.getDate().getTime());
		if (pointingSourceType == PointingSource.LOCAL_CYLINDRICAL)
		{
			image.setBounds(new CylindricalBounds(-90,90,0,360));
		}
		else
		{
			if (imageType != ImageType.GENERIC_IMAGE)
			{

				image.setLinearInterpolatorDims(new int[] {});
				image.setMaskValues(new int[] {});
				image.setFillValues(new double[] {});
				image.setFlip(info.getFlip());
				image.setRotation(info.getRotation());
			}
		}
		CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
		compImage.setName(info.getName());

		return (G1)compImage;
	}

	public void loadUserList()
	{
		String instrumentName = ""; //imagingInstrument == null ? "" : imagingInstrument.getType().toString();
		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + instrumentName + ".txt";
        if (!new File(filename).exists()) return;
		FixedMetadata metadata;
        try
        {
        	if (userImages.size() == 0)
        	{
        		final Key<List<G1>> userImagesKey = Key.of("UserImages");
            	metadata = Serializers.deserialize(new File(filename), "UserImages");
            	userImages = read(userImagesKey, metadata);
        	}
            for (G1 image : userImages)
            {
            	PerspectiveImageRenderingState<G1> state = renderingStates.get(image);
            	if (state == null)
        		{
            		state = new PerspectiveImageRenderingState<G1>();
            		renderingStates.put(image,state);
        		}

            	if (firstCustomLoad == false)
            	{
            		if (image.isMapped()) setImageMapped(image, image.isMapped());
            		if (image.isFrustumShowing()) setImageFrustumVisible(image, image.isFrustumShowing());
            		if (image.isBoundaryShowing()) setImageBoundaryShowing(image, image.isBoundaryShowing());
            		if (image.isOfflimbShowing()) setImageOfflimbShowing(image, image.isOfflimbShowing());
//            		state.isMapped = image.isMapped();
//	            	if (image.isMapped()) image.setStatus("Loaded");
//	            	state.isFrustumShowing = image.isFrustumShowing();
//	            	state.isBoundaryShowing = image.isBoundaryShowing();
//	            	state.isOfflimbShowing = image.isOfflimbShowing();
            	}
            	else
            	{
            		image.setMapped(false);
            	}


        		//TODO leaving this here for now, in case I want to implement something like Nobes does for dtms
//        		updateImage(image);
            }
    		setAllItems(userImages);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	private void updateUserList()
	{
		String instrumentName = ""; //imagingInstrument == null ? "" : imagingInstrument.getType().toString();
		String filename = smallBodyModels.get(0).getCustomDataFolder() + File.separator + "userImages" + instrumentName + ".txt";
		SettableMetadata configMetadata = SettableMetadata.of(Version.of(1, 0));
        final Key<List<G1>> userImagesKey = Key.of("UserImages");
        write(userImagesKey, userImages, configMetadata);
        try
        {
            Serializers.serialize("UserImages", configMetadata, new File(filename));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	public void removeUserImage(G1 image)
	{
		setImageMapped(image, false);
		setImageBoundaryShowing(image, false);
		userImages.remove(image);
		setAllItems(userImages);
		updateUserList();
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

	public void setImages(List<G1> images)
	{
		setAllItems(images);
		this.imagesByInstrument.put(imagingInstrument, images);
		for (G1 image : images)
		{
			if (renderingStates.get(image) != null) continue;
			PerspectiveImageRenderingState<G1> state = new PerspectiveImageRenderingState<G1>();
			renderingStates.put(image,state);
		}
//		currentBoundaryOffsetAmount = 10;
//		currentBoundaryRange = new IdPair(0, currentBoundaryOffsetAmount-1);
		
		currentBoundaryOffsetByInstrument.put(imagingInstrument, 10);
		currentBoundaryRangeByInstrument.put(imagingInstrument, new IdPair(0, currentBoundaryOffsetByInstrument.get(imagingInstrument)-1));
		
		updateActiveBoundaries(null);
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> props = Lists.newArrayList();
		if (imagesByInstrument.isEmpty() && userImages.isEmpty()) return props;
		for (List<vtkActor> actors : imageRenderers.values())
		{
			props.addAll(actors);
		}
		for (List<vtkActor> actors : boundaryRenderers.values())
		{
			props.addAll(actors);
		}
		for (List<vtkActor> actors : frustumRenderers.values())
		{
			props.addAll(actors);
		}
		for (List<vtkActor> actors : offLimbRenderers.values())
		{
			props.addAll(actors);
		}
		for (List<vtkActor> actors : offLimbBoundaryRenderers.values())
		{
			props.addAll(actors);
		}
		return props;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		// TODO Auto-generated method stub
	}

	public static double[] colorToDoubleArray(Color color) {
		return new double[] { (double) color.getRed() / 255., (double) color.getGreen() / 255.,
				(double) color.getBlue() / 255. };
	}

	private void updatePipeline(G1 image, RenderableImageActorPipeline pipeline)
	{
		imageRenderers.put(image, pipeline.getRenderableImageActors());
		imageRenderers.get(image).forEach(actor -> actor.SetVisibility(renderingStates.get(image).isMapped ? 1 : 0));

		frustumRenderers.put(image, pipeline.getRenderableImageFrustumActors());
		frustumRenderers.get(image).forEach(actor -> {
			actor.SetVisibility(renderingStates.get(image).isFrustumShowing ? 1 : 0);
			actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).frustumColor));
		});
		offLimbRenderers.put(image, pipeline.getRenderableOfflimbImageActors());
		offLimbRenderers.get(image).forEach(actor -> {
			if (actor != null)
			{
				actor.SetVisibility(renderingStates.get(image).isOfflimbShowing ? 1 : 0);
			}
		});

//		boundaryRenderers.put(image, pipeline.getRenderableImageBoundaryActors());
//		boundaryRenderers.get(image).forEach(actor -> {
//
//			actor.SetVisibility(renderingStates.get(image).isBoundaryShowing ? 1 : 0);
//			if (renderingStates.get(image).boundaryColor == null)
//			{
//				if (imagesByInstrument.get(imagingInstrument) == null) return;
//				Color color = ColorUtil.generateColor(imagesByInstrument.get(imagingInstrument).indexOf(image)%100, 100);
//				renderingStates.get(image).boundaryColor = color;
//			}
//			actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).boundaryColor));
////			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);
//		});

		offLimbBoundaryRenderers.put(image, pipeline.getRenderableOffLimbBoundaryActors());
		offLimbBoundaryRenderers.get(image).forEach(actor -> {
			if (actor != null)
			{
				actor.SetVisibility(renderingStates.get(image).isOffLimbBoundaryShowing ? 1 : 0);
				if (renderingStates.get(image).offLimbBoundaryColor == null)
				{
					if (imagesByInstrument.get(imagingInstrument) == null) return;
					Color color = ColorUtil.generateColor(imagesByInstrument.get(imagingInstrument).indexOf(image)%100, 100);
					renderingStates.get(image).offLimbBoundaryColor = color;
				}
				actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).offLimbBoundaryColor));
			}
		});
		if (userImages.contains(image)) updateUserList();
	}

	public void updateImage(G1 image)
	{

		if (userImages.contains(image)) updateUserImage(image);
		else
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				image.setStatus("Loaded");
				return null;
			});

			runThreadOnExecutorService(thread);

			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);
		}
	}

	public void updateUserImage(G1 image)
	{
		RenderableImageActorPipeline pipeline = null;
		boolean forceUpdate = true;
		try
		{
			pipeline = ImagePipelineFactory.of(image, smallBodyModels, forceUpdate);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (pipeline == null) return;
		updatePipeline(image, pipeline);
		updateUserList();
		userImagesModified.add(image);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);
	}

	public void setImageMapped(G1 image, boolean mapped)
	{
		setImageMapped(image, mapped, false);
	}

	public void setImageMapped(G1 image, boolean mapped, boolean reRender)
	{
		if (image.isMapped() == mapped && reRender == false) return;
		image.setMapped(mapped);
		List<vtkActor> actors = imageRenderers.get(image);

		if ((actors == null && mapped == true) || reRender || userImagesModified.contains(image))
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					fireImageMapStartedListeners();
					image.setStatus("Loading...");
					RenderableImageActorPipeline pipeline = ImagePipelineFactory.of(image, smallBodyModels);

					if (pipeline == null)
					{
						image.setMapped(false);
						image.setStatus("Invalid");
						SwingUtilities.invokeLater( () -> {
							pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
						});
						return;
					}
					updatePipeline(image, pipeline);
					for (vtkActor actor : imageRenderers.get(image))
					{
						actor.SetVisibility(mapped ? 1 : 0);
					}
					image.setStatus("Loaded");
					if (userImagesModified.contains(image)) userImagesModified.remove(image);
					SwingUtilities.invokeLater( () -> {
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
					fireImageMapEndedListeners();
				}
			});
			runThreadOnExecutorService(thread);
		}
		else
		{
			if (actors != null)
			{
				for (vtkActor actor : actors)
				{
					actor.SetVisibility(mapped ? 1 : 0);
				}
			}
			if (userImages.contains(image)) updateUserList();
		}
		renderingStates.get(image).isMapped = mapped;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getImageMapped(G1 image)
	{
		if (renderingStates.get(image) == null) return false;
		return renderingStates.get(image).isMapped;
	}

	public String getImageStatus(G1 image)
	{
		return image.getStatus();
	}

	public int getImageNumberOfLayers(G1 image)
	{
		return image.getNumberOfLayers();
	}

	public void setImageFrustumVisible(G1 image, boolean visible)
	{
		if (image.isFrustumShowing() == visible) return;
		image.setFrustumShowing(visible);
		List<vtkActor> actors = frustumRenderers.get(image);
		if (actors == null && visible == true)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : frustumRenderers.get(image))
				{
					actor.SetVisibility(visible? 1 : 0);
				}
				return null;
			});
			runThreadOnExecutorService(thread);
		}
		else
		{
			if (actors != null)
			{
				for (vtkActor actor : actors)
				{
					actor.SetVisibility(visible ? 1 : 0);
				}
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
				if (userImages.contains(image)) updateUserList();
			}
		}
		renderingStates.get(image).isFrustumShowing = visible;
	}

	public boolean getFrustumShowing(G1 image)
	{
		return renderingStates.get(image).isFrustumShowing;
//		return image.isFrustumShowing();
	}

	public void setImageOfflimbShowing(G1 image, boolean showing)
	{
		if (image.isOfflimbShowing() == showing) return;
		image.setOfflimbShowing(showing);
		image.setOfflimbBoundaryShowing(showing);
		renderingStates.get(image).isOffLimbBoundaryShowing = showing;
		renderingStates.get(image).isOfflimbShowing = showing;
		List<vtkActor> actors = offLimbRenderers.get(image);
		if ((actors == null || actors.isEmpty() )  && showing == true)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : offLimbRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				for (vtkActor actor : offLimbBoundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				return null;
			});
			runThreadOnExecutorService(thread);
		}
		else
		{
			if (actors != null)
			{
				for (vtkActor actor : actors)
				{
					actor.SetVisibility(showing ? 1 : 0);
				}
			}
			if (offLimbBoundaryRenderers.get(image) != null)
			{
				for (vtkActor actor : offLimbBoundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
			}
			if (userImages.contains(image)) updateUserList();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
		renderingStates.get(image).isOfflimbShowing = showing;
	}

	public boolean getImageOfflimbShowing(G1 image)
	{
		return renderingStates.get(image).isOfflimbShowing;
//		return image.isOfflimbShowing();
	}

	public void setOffLimbBoundaryShowing(G1 image, boolean showing)
	{
		if (image.isOfflimbBoundaryShowing() == showing) return;
		image.setOfflimbBoundaryShowing(showing);
		renderingStates.get(image).isOffLimbBoundaryShowing = showing;
		List<vtkActor> actors = offLimbBoundaryRenderers.get(image);
		if (actors == null)
		{
			Thread thread = getPipelineThread(image, (Void v) -> {
				for (vtkActor actor : offLimbBoundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
				}
				return null;
			});
			runThreadOnExecutorService(thread);
		}
		else
		{
			for (vtkActor actor : actors)
			{
				actor.SetVisibility(showing ? 1 : 0);
			}
			if (userImages.contains(image)) updateUserList();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
		renderingStates.get(image).isOffLimbBoundaryShowing = showing;
	}

	public boolean getOffLimbBoundaryShowing(G1 image)
	{
		return renderingStates.get(image).isOffLimbBoundaryShowing;
//		return image.isOfflimbBoundaryShowing();
	}

	public void setImageBoundaryShowing(G1 image, boolean showing)
	{
		if (image.isBoundaryShowing() == showing) return;
		image.setBoundaryShowing(showing);
		List<vtkActor> actors = boundaryRenderers.get(image);
		if (actors == null && showing == true)
		{
//			Thread thread = getPipelineThread(image, (Void v) -> {
//				for (vtkActor actor : boundaryRenderers.get(image))
//				{
//					actor.SetVisibility(showing? 1 : 0);
//				}
//				return null;
//			});
			Thread thread = getBoundaryCreationThread(image, (Void v) -> {
				for (vtkActor actor : boundaryRenderers.get(image))
				{
					actor.SetVisibility(showing? 1 : 0);
					if (renderingStates.get(image).boundaryColor == null)
					{
						if ((imagingInstrument == null) || imagesByInstrument.get(imagingInstrument) == null)
						{
							renderingStates.get(image).boundaryColor = Color.red;
						}
						else
						{
							Color color = ColorUtil.generateColor(imagesByInstrument.get(imagingInstrument).indexOf(image)%100, 100);
							renderingStates.get(image).boundaryColor = color;
						}
					}
					actor.GetProperty().SetColor(colorToDoubleArray(renderingStates.get(image).boundaryColor));
				}
				SwingUtilities.invokeLater(() -> {pcs.firePropertyChange(Properties.MODEL_CHANGED, null, image);});
				return null;
			});
			runThreadOnExecutorService(thread);
		}
		else
		{
			if (actors != null)
			{
				for (vtkActor actor : actors)
				{
					actor.SetVisibility(showing ? 1 : 0);
				}
				if (userImages.contains(image)) updateUserList();
				this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			}
		}
		renderingStates.get(image).isBoundaryShowing = showing;
	}

	public boolean getImageBoundaryShowing(G1 image)
	{
		return renderingStates.get(image).isBoundaryShowing;
//		return image.isBoundaryShowing();
	}

	public Color getImageBoundaryColor(G1 image)
	{
		return renderingStates.get(image).boundaryColor;
	}

	public void setImageBoundaryColor(G1 image, Color color)
	{
		renderingStates.get(image).boundaryColor = color;
		if (boundaryRenderers.get(image).size() == 0) return;
		for (vtkActor actor : boundaryRenderers.get(image))
		{
			actor.GetProperty().SetColor(color.getRed() / 255., color.getGreen() / 255., color.getBlue() / 255.);
	        actor.Modified();
		}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setImageInterpolationState(G1 image, boolean interpolating)
	{
		image.setInterpolateState(interpolating);
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				image.setStatus("Loading...");
				RenderableImageActorPipeline pipeline = ImagePipelineFactory.of(image, smallBodyModels);
				if (pipeline == null) return;
				updatePipeline(image, pipeline);
				image.setStatus("Loaded");
				SwingUtilities.invokeLater( () -> {
					pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
				});
			}
		});
		runThreadOnExecutorService(thread);
		updateUserList();
	}

	public boolean getImageInterpolationState(G1 image)
	{
		return image.getInterpolateState();
	}


	public void setImageStatus(G1 image, String status)
	{
		image.setStatus(status);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public int size()
	{
		return getAllItems().size();
	}

	public Optional<G1> getImage(vtkActor actor)
	{
		Optional<G1> matchingImage = Optional.empty();
		for (G1 image : imageRenderers.keySet())
		{
			List<vtkActor> actors = imageRenderers.get(image);
			if (actors.contains(actor))
			{
				matchingImage = Optional.of(image);
			}
		}

		return matchingImage;
	}

	public Optional<G1> getImageBoundary(vtkActor actor)
	{
		Optional<G1> matchingImage = Optional.empty();
		for (G1 image : boundaryRenderers.keySet())
		{
			List<vtkActor> actors = boundaryRenderers.get(image);
			if (actors.contains(actor))
			{
				matchingImage = Optional.of(image);
			}
		}

		return matchingImage;
	}

	@Override
	public void setOpacity(double opacity)
	{
		List<vtkActor> actors = imageRenderers.get(getSelectedItems().asList().get(0));
		actors.forEach(actor -> {
			vtkProperty interiorProperty = actor.GetProperty();
			interiorProperty.SetOpacity(opacity);
		});
		super.setOpacity(opacity);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getOpacity()
	{
		List<vtkActor> actors = imageRenderers.get(getSelectedItems().asList().get(0));
		vtkActor actor = actors.get(0);
		vtkProperty interiorProperty = actor.GetProperty();
		return interiorProperty.GetOpacity();
	}

	public void setOfflimbOpacity(G1 image, double opacity)
	{
		List<vtkActor> actors = offLimbRenderers.get(image);
		actors.forEach(actor -> {
			vtkProperty interiorProperty = actor.GetProperty();
			interiorProperty.SetOpacity(opacity);
		});
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getOfflimbOpacity(G1 image)
	{
		List<vtkActor> actors = offLimbRenderers.get(image);
		vtkActor actor = actors.get(0);
		vtkProperty interiorProperty = actor.GetProperty();
		return interiorProperty.GetOpacity();
	}

	public void setOffLimbDepth(G1 image, double depth)
	{
		PerspectiveImageRenderingState<G1> renderingState = renderingStates.get(image);
		renderingState.offLimbFootprintDepth = depth;
		image.setOfflimbDepth(depth);
		Thread thread = getPipelineThread(image, (Void v) -> { return null; });
		runThreadOnExecutorService(thread);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public double getOffLimbDepth(G1 image)
	{
		PerspectiveImageRenderingState<G1> renderingState = renderingStates.get(image);
		return renderingState.offLimbFootprintDepth;
	}

	public Color getOffLimbBoundaryColor(G1 image)
	{
		return renderingStates.get(image).offLimbBoundaryColor;
	}

	public void setOffLimbBoundaryColor(G1 image, Color color)
	{
		renderingStates.get(image).offLimbBoundaryColor = color;
		for (vtkActor actor : offLimbBoundaryRenderers.get(image))
		{
			actor.GetProperty().SetColor(color.getRed() / 255., color.getGreen() / 255., color.getBlue() / 255.);
	        actor.Modified();
		}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void setImageContrastRange(G1 image, IntensityRange intensityRange)
	{
		renderingStates.get(image).imageContrastRange = intensityRange;
		Thread thread = getPipelineThread(image, (Void v) -> {

			for (vtkActor actor : offLimbBoundaryRenderers.get(image))
			{
				actor.SetVisibility(renderingStates.get(image).isOffLimbBoundaryShowing ? 1 : 0);

			}
			return null;
		});
		runThreadOnExecutorService(thread);
	}

	public IntensityRange getImageContrastRange(G1 image)
	{
		return renderingStates.get(image).imageContrastRange;
	}

	public void setOffLimbContrastRange(G1 image, IntensityRange intensityRange)
	{
		renderingStates.get(image).offLimbContrastRange = intensityRange;
		image.setOfflimbIntensityRange(intensityRange);
		if (renderingStates.get(image).contrastSynced)
		{
			renderingStates.get(image).imageContrastRange = intensityRange;
			image.setIntensityRange(intensityRange);
		}
		Thread thread = getPipelineThread(image, (Void v) -> {
			SwingUtilities.invokeLater( () -> {
				pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			});
			return null;
		});
		runThreadOnExecutorService(thread);
	}

	public IntensityRange getOffLimbContrastRange(G1 image)
	{
		return renderingStates.get(image).offLimbContrastRange;
	}

	public void setContrastSynced(G1 image, boolean contrastSynced)
	{
		renderingStates.get(image).contrastSynced = contrastSynced;
	}

	public boolean getContrastSynced(G1 image)
	{
		return renderingStates.get(image).contrastSynced;
	}

	public boolean isSimulateLighting(G1 image)
	{
		return image.isSimulateLighting();
	}

	public void setSimulateLighting(G1 image, boolean simulating)
	{
		image.setSimulateLighting(simulating);
	}

	public List<SmallBodyModel> getSmallBodyModels()
	{
		return smallBodyModels;
	}

	public IImagingInstrument getInstrument()
	{
		return this.imagingInstrument;
	}
	
	public void updateActiveBoundaries(IdPair previousRange)
	{
		int lowBound, highBound;
		IdPair currentBoundaryRange = currentBoundaryRangeByInstrument.get(imagingInstrument);
		if (previousRange == null)
		{
			lowBound = currentBoundaryRange.id1;
			highBound = currentBoundaryRange.id2;
		}
		else
		{
			lowBound = Math.min(previousRange.id1, currentBoundaryRange.id1);
			highBound = Math.max(previousRange.id2, currentBoundaryRange.id2);
		}
		lowBound = Math.max(0, lowBound);
		highBound = Math.min(highBound, getAllItems().size()-1);
		for (int i=lowBound; i<=highBound; i++)
		{
			G1 image = getAllItems().get(i);
			boolean validIndex = new Range(currentBoundaryRange.id1, currentBoundaryRange.id2).contains(i) /*&& renderingStates.get(image).isBoundaryShowing*/;
			setImageBoundaryShowing(image, validIndex);
		}
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void offsetBoundariesRange(int offsetAmount)
	{
		IdPair currentBoundaryRange = currentBoundaryRangeByInstrument.get(imagingInstrument);
		IdPair previousRange = new IdPair(currentBoundaryRange.id1, currentBoundaryRange.id2);
//		currentBoundaryOffsetAmount = offsetAmount;
		currentBoundaryOffsetByInstrument.put(imagingInstrument, offsetAmount);
		currentBoundaryRange.offset(offsetAmount);
		if (currentBoundaryRange.id1 > getAllItems().size()-1)
		{
			currentBoundaryRange = new IdPair(0, offsetAmount-1);
		}
		if (currentBoundaryRange.id2 < 0) //update to show the most complete multiple at the end of the list
		{
			int remainder = (getAllItems().size() - 1)%offsetAmount;
			currentBoundaryRange = new IdPair(getAllItems().size()-1-remainder, getAllItems().size() - 1);
		}
		if (currentBoundaryRange.id1 == 0) currentBoundaryRange.id2 = Math.abs(offsetAmount) - 1;
		if (currentBoundaryRange.id2 < getAllItems().size()-1 && currentBoundaryRange.id2-currentBoundaryRange.id1 != currentBoundaryOffsetByInstrument.get(imagingInstrument))
		{
			currentBoundaryRange.id2 = currentBoundaryRange.id1 + Math.abs(offsetAmount) - 1;
 		}
		currentBoundaryRangeByInstrument.put(imagingInstrument, currentBoundaryRange);
		updateActiveBoundaries(previousRange);
	}

	public void setImagingInstrument(IImagingInstrument imagingInstrument)
	{
		if (imagingInstrument == null && userImages.size() == 0)
		{
			loadUserList();
			firstCustomLoad = false;
		}
		if (this.imagingInstrument == imagingInstrument) return;
		this.imagingInstrument = imagingInstrument;
		if (imagingInstrument == null)
		{
			setAllItems(userImages);
			firstCustomLoad = false;
		}
		else if (imagesByInstrument.get(imagingInstrument) == null)
		{
			setAllItems(Lists.newArrayList());
		}
		else
		{
			ImmutableList<G1> filteredImages = ImmutableList.copyOf(imagesByInstrument.get(imagingInstrument).stream().filter(image -> image.getImageType() == imagingInstrument.getType()).toList());
			setAllItems(filteredImages);
		}
//		loadUserList();
	}

	private Thread getBoundaryCreationThread(G1 image,  Function<Void, Void> completionBlock)
	{
		return new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					List<vtkActor> boundaryActors = Lists.newArrayList();
					//Only draw the close up border for color images
					if (image.getNumberOfLayers() == 3)
					{
						ImageToScenePipeline colorPipeline = ImagePipelineFactory.of(image, smallBodyModels);
						boundaryActors = colorPipeline.getRenderableImageBoundaryActors();
					}
					else
					{
						String pointingFile = image.getPointingSource();

						if (!new File(pointingFile).exists())
						{
							pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
						}

						//generate image pointing (in: filename, out: ImagePointing)
						IPipelinePublisher<PointingFileReader> pointingPublisher = null;
						if (image.getPointingSourceType() == PointingSource.SPICE || image.getPointingSourceType() == PointingSource.CORRECTED_SPICE)
							pointingPublisher = new InfofileReaderPublisher(pointingFile);
						else
							pointingPublisher = new SumfileReaderPublisher(pointingFile);
						Just.of(Pair.of(pointingPublisher.getOutput(), smallBodyModels))
							.operate(new LowResolutionBoundaryOperator(image.getOffset()))
							.subscribe(Sink.of(boundaryActors))
							.run();
					}
					boundaryRenderers.put(image, boundaryActors);
//					setImageBoundaryColor(image, Color.red);
					completionBlock.apply(null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private Thread getPipelineThread(G1 image, Function<Void, Void> completionBlock)
	{
		return new PipelineThread(image, completionBlock);
	}

	class PipelineThread extends Thread
	{
		G1 image;
		Function<Void, Void> completionBlock;

		public PipelineThread(G1 image, Function<Void, Void> completionBlock)
		{
			this.image = image;
			this.completionBlock = completionBlock;
		}

		@Override
		public void run()
		{
			RenderableImageActorPipeline pipeline = null;
			try
			{
				if (/*image.getImageType() != ImageType.GENERIC_IMAGE &&*/ image.getPointingSourceType() != PointingSource.LOCAL_CYLINDRICAL)
				{
					pipeline = new RenderablePointedImageToScenePipeline<G1>(image, smallBodyModels);

				}
				else
				{
					pipeline = new RenderableCylindricalImageToScenePipeline(image, smallBodyModels);

				}
			}
			catch (InvalidGDALFileTypeException e)
			{
				 JOptionPane.showMessageDialog(null,
	                        e.getMessage(),
	                        "Invalid file type encountered",
	                        JOptionPane.ERROR_MESSAGE);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (pipeline == null) return;
			if (renderingStates.get(image) == null)
			{
				PerspectiveImageRenderingState<G1> state = new PerspectiveImageRenderingState<G1>();
	    		renderingStates.put(image,state);
			}
			updatePipeline(image, pipeline);
			completionBlock.apply(null);
			SwingUtilities.invokeLater( () -> {
				pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			});
		}
	}


//	public int getCurrentBoundaryOffsetAmount()
//	{
//		return currentBoundaryOffsetAmount;
//	}

	public void setCurrentBoundaryOffsetAmount(int currentBoundaryOffsetAmount)
	{
		Integer currentBoundaryOffset = currentBoundaryOffsetByInstrument.get(imagingInstrument);
		IdPair currentBoundaryRange = currentBoundaryRangeByInstrument.get(imagingInstrument);
		if (currentBoundaryOffset == currentBoundaryOffsetAmount) return;
		IdPair previousRange = new IdPair(currentBoundaryRange.id1, currentBoundaryRange.id2);
		currentBoundaryOffsetByInstrument.put(imagingInstrument, currentBoundaryOffsetAmount);
//		this.currentBoundaryOffsetAmount = currentBoundaryOffsetAmount;
		currentBoundaryRange.id2 = currentBoundaryRange.id1 + currentBoundaryOffsetAmount - 1;
		updateActiveBoundaries(previousRange);
	}
	
	private File getResolvedPointingFile(String source)
	{
		if (new File(source).exists()) return new File(source);
		else return FileCache.getFileFromServer(source);
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		 // Get default status message
        String status = super.getClickStatusBarText(prop, cellId, pickPosition);
        if (getSelectedItems().size() == 0) return status;
        G1 image = getSelectedItems().asList().get(0);
        List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
        IPerspectiveImageToLayerAndMetadataPipeline layerPipeline = null;
        double[] pixelLocation = new double[] {0,0};
        double[] pickedPixel = new double[] {0,0};
        Layer layer  = null;
        try {
        	layerPipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
			IPipelinePublisher<Layer> reader = new Just<Layer>(layerPipeline.getLayers().get(0));
			reader.
				operate(new VtkImageRendererOperator()).
				subscribe(new Sink<vtkImageData>(displayedImages)).run();

			layer = layerPipeline.getLayers().get(0);

	        IPipelinePublisher<PointingFileReader> pointingPublisher = null;
			if (image.getPointingSourceType() == PointingSource.SPICE || image.getPointingSourceType() == PointingSource.CORRECTED_SPICE)
				pointingPublisher = new InfofileReaderPublisher(getResolvedPointingFile(image.getPointingSource()).getAbsolutePath());
			else
				pointingPublisher = new SumfileReaderPublisher(getResolvedPointingFile(image.getPointingSource()).getAbsolutePath());
			Frustum frustum = new Frustum(pointingPublisher.getOutput().getSpacecraftPosition(), pointingPublisher.getOutput().getFrustum1(), pointingPublisher.getOutput().getFrustum3(), pointingPublisher.getOutput().getFrustum4(), pointingPublisher.getOutput().getFrustum2());
	        pickedPixel = getPixelFromPoint(pickPosition, frustum, layer.iSize(), layer.jSize());
	        pixelLocation = new double[]{layer.iSize()-1-pickedPixel[0], pickedPixel[1]};
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }

        status += "Image " + image.getName();

//        // Number format
        DecimalFormat df = new DecimalFormat("#.0");
        df.setRoundingMode(RoundingMode.HALF_UP);

        // Construct status message
        status += ", Pixel Coordinate = (";
        status += df.format(layer.jSize()-1-pickedPixel[0]);
        status += ", ";
        status += df.format(pixelLocation[1]);
        status += ")";

        // Append raw pixel value information
        status += ", Raw Value = ";
        if (displayedImages.get(0) == null)
        {
            status += "Unavailable";
        }
        else
        {
            int ip0 = (int) Math.round(pixelLocation[0]);
            int ip1 = (int) Math.round(pixelLocation[1]);
            if (!displayedImages.get(0).GetScalarTypeAsString().contains("char"))
            {
                float[] pixelColumn = ImageDataUtil.vtkImageDataToArray1D(displayedImages.get(0),
                		layer.iSize() - 1 - ip0, ip1);
                status += pixelColumn[0];
            }
            else
            {
                status += "N/A";
            }
        }
        return status;
	}

    private double[] getPixelFromPoint(double[] pt, Frustum frustum, int imageWidth, int imageHeight)
    {
        double[] uv = new double[2];
        frustum.computeTextureCoordinatesFromPoint(pt, imageWidth, imageHeight, uv, false);
        double[] pixel = new double[2];
        pixel[0] = uv[0] * imageHeight;
        pixel[1] = uv[1] * imageWidth;

        return pixel;
    }

    public Map<List<vtkActor>, String> getImageRenderedComponents(G1 image)
    {
    	Map<List<vtkActor>, String> actorsToSave = Maps.newHashMap();
		actorsToSave.put(imageRenderers.get(image), "footprint");
		actorsToSave.put(offLimbRenderers.get(image), "offlimb");
		actorsToSave.put(offLimbBoundaryRenderers.get(image), "offlimb_boundary");
		actorsToSave.put(frustumRenderers.get(image), "frustum");
		actorsToSave.put(boundaryRenderers.get(image), "boundary");
		return actorsToSave;
    }

    private void runThreadOnExecutorService(Thread thread)
    {
    	executor.execute(thread);
    }
}
