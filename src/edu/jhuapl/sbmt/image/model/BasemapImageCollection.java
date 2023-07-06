package edu.jhuapl.sbmt.image.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkProp;
import vtk.vtkProperty;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.saavtk.model.SaavtkItemManager;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.ImagePipelineFactory;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages.RenderableCylindricalImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.RenderableImageActorPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.SettableMetadata;

public class BasemapImageCollection<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends SaavtkItemManager<G1> implements PropertyChangeListener
{
	private List<SmallBodyModel> smallBodyModels;
	private HashMap<G1, List<vtkActor>> imageRenderers;
	private HashMap<G1, PerspectiveImageRenderingState<G1>> renderingStates;
	private static ExecutorService executor = Executors.newCachedThreadPool();

	class PerspectiveImageRenderingState<G1>
	{
		boolean isMapped = false;
		boolean isFrustumShowing = false;
		boolean isBoundaryShowing = false;
		boolean isOfflimbShowing = false;
		boolean isOffLimbBoundaryShowing = false;
		Color boundaryColor;
		Color offLimbBoundaryColor = Color.red;
		Color frustumColor = Color.green;
		double offLimbFootprintDepth;
		boolean contrastSynced = false;
		IntensityRange imageContrastRange;
		IntensityRange offLimbContrastRange;
	}

	public BasemapImageCollection(List<SmallBodyModel> smallBodyModels)
	{
		this.imageRenderers = new HashMap<G1, List<vtkActor>>();
		this.renderingStates = new HashMap<G1, PerspectiveImageRenderingState<G1>>();
		this.smallBodyModels = smallBodyModels;
	}

	public void removeUserImage(G1 image)
	{
		setImageMapped(image, false);
	}

	public void hideAllImages()
	{
		List<G1> allImages = getAllItems();
		allImages.forEach(img -> setImageMapped(img, false));
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

    public G1 addImage(BasemapImage image)
    {
    	Optional<G1> existing = getAllItems().stream().filter(existingImage -> existingImage.getName() == image.getImageName()).findFirst();
    	if (existing.isPresent()) return existing.get();
    	List<G1> allImages = Lists.newArrayList();
    	allImages.addAll(getAllItems());
    	String filename = FileCache.getFileFromServer(image.getImageFilename()).getAbsolutePath();
    	filename = image.getImageFilename();
    	ImageType imageType = image.getImageType();
    	PointingSource pointingSource = image.getPointingType();
    	double[] fillValues = new double[] {};
    	ImageRotation rotation = image.getRotation();
    	ImageFlip flip = image.getFlip();
    	FileType fileType = image.getPointingFileType();
    	String pointingFileName = "";
    	if (image.getPointingFileName() != null && !image.getPointingFileName().contains("null"))
    	{
    		pointingFileName = image.getPointingFileName();
    	}

    	PerspectiveImageMetadata perImage = new PerspectiveImageMetadata(filename, imageType, pointingSource, null, fillValues);
    	double minLat = image.getLllat();
		double maxLat = image.getUrlat();
		double minLon = image.getLllon();
		double maxLon = image.getUrlon();
		perImage.setBounds(new CylindricalBounds(minLat, maxLat, minLon, maxLon));
		perImage.setPointingSource(pointingFileName);
		perImage.setRotation(rotation.rotation());
		perImage.setFlip(flip.flip());
		//file type?

    	CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(perImage));
		compImage.setName(image.getImageName());
		allImages.add((G1)compImage);
    	setAllItems(allImages);
		PerspectiveImageRenderingState<G1> state = new PerspectiveImageRenderingState<G1>();
		renderingStates.put((G1)compImage,state);

		return (G1)compImage;
    }

    public G1 getImage(String name)
    {
    	G1 image = null;
    	List<G1> matchedImages = getAllItems().stream().filter(img -> img.getFilename().equals(name)).toList();
    	if (matchedImages.size() == 1) image = matchedImages.get(0);
    	return image;
    }

	public void setImages(List<G1> images)
	{
		setAllItems(images);
		for (G1 image : images)
		{
			if (renderingStates.get(image) != null) continue;
			PerspectiveImageRenderingState<G1> state = new PerspectiveImageRenderingState<G1>();
			renderingStates.put(image,state);
		}
	}

	@Override
	public List<vtkProp> getProps()
	{
		List<vtkProp> props = Lists.newArrayList();
		for (List<vtkActor> actors : imageRenderers.values())
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
	}

	public void updateImage(G1 image)
	{
		Thread thread = getPipelineThread(image, (Void v) -> {
			image.setStatus("Loaded");
			return null;
		});
		thread.start();

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
		if ((actors == null && mapped == true) || reRender)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
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
					SwingUtilities.invokeLater( () -> {
						pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
					});
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
		}
		renderingStates.get(image).isMapped = mapped;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public boolean getImageMapped(G1 image)
	{
		return image.isMapped();
	}

	public String getImageStatus(G1 image)
	{
		return image.getStatus();
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

	public Optional<IPerspectiveImage> getImage(vtkActor actor)
	{
		Optional<IPerspectiveImage> matchingImage = Optional.empty();
		for (IPerspectiveImage image : imageRenderers.keySet())
		{
			List<vtkActor> actors = imageRenderers.get(image);
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
		for (G1 image : getAllItems())
		{
			List<vtkActor> actors = imageRenderers.get(image);
			actors.forEach(actor -> {
				vtkProperty interiorProperty = actor.GetProperty();
				interiorProperty.SetOpacity(opacity);
			});
		}
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

	public List<SmallBodyModel> getSmallBodyModels()
	{
		return smallBodyModels;
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
			System.out.println("PerspectiveImageCollection.PipelineThread: PipelineThread: making pipeline thread ");
			this.image = image;
			this.completionBlock = completionBlock;
		}

		@Override
		public void run()
		{
			RenderableImageActorPipeline pipeline = null;
			try
			{
				if (image.getImageType() != ImageType.GENERIC_IMAGE)
				{
					pipeline = new RenderablePointedImageToScenePipeline(image, smallBodyModels);

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
			updatePipeline(image, pipeline);
			completionBlock.apply(null);
			SwingUtilities.invokeLater( () -> {
				pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
			});
		}
	}

    private void runThreadOnExecutorService(Thread thread)
    {
    	executor.execute(thread);
    }
}