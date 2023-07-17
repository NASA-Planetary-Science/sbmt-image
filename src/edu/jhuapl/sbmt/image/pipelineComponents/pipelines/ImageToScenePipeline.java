package edu.jhuapl.sbmt.image.pipelineComponents.pipelines;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.RenderableImageActorPipeline;

public class ImageToScenePipeline implements RenderableImageActorPipeline
{
	protected Pair<List<vtkActor>, List<ImageRenderable>>[] sceneOutputs;
	protected int activeLayerIndex = 0;

	@Override
	public List<vtkActor> getSmallBodyActors()
	{
		return sceneOutputs[0].getLeft();
	}

	@Override
	public List<vtkActor> getRenderableImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			if (renderable.getFootprints() == null) continue;
			imageActors.addAll(renderable.getFootprints());
		}
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageActors()
	{
		List<vtkActor> imageActors = Lists.newArrayList();
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			if (renderable.getModifiedFootprintActors() == null) continue;
			imageActors.addAll(renderable.getModifiedFootprintActors());
		}
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			if (renderable.getBoundaries() == null) continue;
			imageBoundaryActors.addAll(renderable.getBoundaries());
		}
		return imageBoundaryActors;
	}

	@Override
	public List<vtkActor> getRenderableModifiedImageBoundaryActors()
	{
		List<vtkActor> imageBoundaryActors = Lists.newArrayList();
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			if (renderable.getModifiedBoundaryActors() == null) continue;
			imageBoundaryActors.addAll(renderable.getModifiedBoundaryActors());
		}
		return imageBoundaryActors;
	}

	public List<vtkActor> getRenderableImageFrustumActors()
	{
		List<vtkActor> frustumActors = Lists.newArrayList();
		if (sceneOutputs[activeLayerIndex] == null)  return frustumActors;
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			if (renderable.getFrustum() == null) continue;
			frustumActors.add(renderable.getFrustum());
		}
		return frustumActors;
	}

	public List<vtkActor> getRenderableOfflimbImageActors()
	{
		List<vtkActor> offLimbActors = Lists.newArrayList();
		if (sceneOutputs[activeLayerIndex] == null)  return offLimbActors;
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			vtkActor actor = renderable.getOffLimb();
			if (actor != null)
			{
				actor.SetVisibility(1);
				offLimbActors.add(renderable.getOffLimb());
			}
		}
		return offLimbActors;
	}

	public List<vtkActor> getRenderableModifiedImageFrustumActors()
	{
		List<vtkActor> frustumActors = Lists.newArrayList();
		if (sceneOutputs[activeLayerIndex] == null)  return frustumActors;
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			if (renderable.getModifiedFrustumActor() == null) continue;
			frustumActors.add(renderable.getModifiedFrustumActor());
		}
		return frustumActors;
	}



	@Override
	public List<vtkActor> getRenderableOffLimbBoundaryActors()
	{
		List<vtkActor> offLimbActors = Lists.newArrayList();
		if (sceneOutputs[activeLayerIndex] == null)  return offLimbActors;
		for (ImageRenderable renderable : sceneOutputs[activeLayerIndex].getRight())
		{
			vtkActor actor = renderable.getOffLimbBoundary();
			if (actor != null)
			{
				actor.SetVisibility(1);
				offLimbActors.add(actor);
			}
		}
		return offLimbActors;
	}
}
