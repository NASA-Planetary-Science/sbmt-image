package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering;

import java.util.List;

import vtk.vtkActor;

public interface RenderableImageActorPipeline
{

	List<vtkActor> getRenderableImageActors();

	List<vtkActor> getRenderableModifiedImageActors();

	List<vtkActor> getRenderableImageBoundaryActors();

	List<vtkActor> getRenderableModifiedImageBoundaryActors();

	List<vtkActor> getRenderableImageFrustumActors();

	List<vtkActor> getRenderableModifiedImageFrustumActors();

	List<vtkActor> getRenderableOfflimbImageActors();

	List<vtkActor> getRenderableOffLimbBoundaryActors();

	List<vtkActor> getSmallBodyActors();

}