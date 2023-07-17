package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkImageData;
import vtk.vtkPolyData;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.HighResolutionBoundaryOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.LowResolutionBoundaryOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VTKImagePolyDataRendererOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class ImageRenderable
{
	protected IRenderableImage image;
	protected List<SmallBodyModel> smallBodyModels;
	protected List<vtkActor> footprintActors = Lists.newArrayList();
	protected List<vtkPolyData> footprintPolyData = Lists.newArrayList();
	protected vtkActor frustumActor;
	protected vtkActor offLimbActor;
	protected vtkActor offLimbBoundaryActor;
	protected List<vtkActor> boundaryActors = Lists.newArrayList();
	protected double maxFrustumDepth;
	protected double minFrustumDepth;
	protected List<vtkActor> modifiedFootprintActors = Lists.newArrayList();
	protected List<vtkPolyData> modifiedFootprintPolyData = Lists.newArrayList();
	protected vtkActor modifiedFrustumActor;
	protected List<vtkActor> modifiedBoundaryActors = Lists.newArrayList();
	protected boolean generateOfflimb = false;
	public static boolean USE_PRECISE_BOUNDARY = false;

	protected List<vtkActor> processFootprints(List<vtkPolyData> footprints, List<vtkImageData> imageData, boolean linearInterpolation) throws IOException, Exception
	{
		int i=0;
		if (footprints.size() == 0) return footprintActors;
    	for (SmallBodyModel smallBody : smallBodyModels)
    	{
    		vtkPolyData footprint = footprints.get(i++);
	        footprintPolyData.add(footprint);
	        List<vtkActor> actors = Lists.newArrayList();
	        Just.of(Pair.of(imageData.get(0), footprint))
	        	.operate(new VTKImagePolyDataRendererOperator(linearInterpolation))
	        	.subscribe(Sink.of(actors))
	        	.run();
	        footprintActors.addAll(actors);
    	}
    	return footprintActors;
	}

	protected List<vtkActor> processBoundaries() throws IOException, Exception
	{
		if (USE_PRECISE_BOUNDARY || image == null)
		{
			Just.of(Pair.of(footprintPolyData, smallBodyModels))
				.operate(new HighResolutionBoundaryOperator(smallBodyModels.get(0).getOffset()))
				.subscribe(Sink.of(boundaryActors))
				.run();
		}
		else
		{
			Just.of(Pair.of(image.getPointing(), smallBodyModels))
				.operate(new LowResolutionBoundaryOperator(image.getOffset()))
				.subscribe(Sink.of(boundaryActors))
				.run();
		}

    	return boundaryActors;
	}


	public List<vtkActor> getFootprints()
	{
		return footprintActors;
	}

	/**
	 * @return the footprintPolyData
	 */
	public List<vtkPolyData> getFootprintPolyData()
	{
		return footprintPolyData;
	}

	public vtkActor getFrustum()
	{
		return frustumActor;
	}

	public vtkActor getOffLimb()
	{
		return offLimbActor;
	}

	public vtkActor getOffLimbBoundary()
	{
		return offLimbBoundaryActor;
	}

	public List<vtkActor> getBoundaries()
	{
		return boundaryActors;
	}

	public List<vtkActor> getModifiedFootprintActors()
	{
		return modifiedFootprintActors;
	}

	/**
	 * @return the modifiedFrustumActor
	 */
	public vtkActor getModifiedFrustumActor()
	{
		return modifiedFrustumActor;
	}

	/**
	 * @return the modifiedBoundaryActors
	 */
	public List<vtkActor> getModifiedBoundaryActors()
	{
		return modifiedBoundaryActors;
	}

	public boolean isOfflimbGenerated()
	{
		System.out.println("ImageRenderable: isOfflimbGenerated: return " + generateOfflimb);
		return generateOfflimb;
	}

	public void setOfflimbGenerated(boolean offlimbGenerated)
	{
		this.generateOfflimb = offlimbGenerated;
	}
}
