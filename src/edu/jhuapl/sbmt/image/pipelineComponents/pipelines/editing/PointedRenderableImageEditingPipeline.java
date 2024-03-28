package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.editing;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.PointedImageEditingWindow;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;

public class PointedRenderableImageEditingPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	public PointedRenderableImageEditingPipeline(G1 image, List<SmallBodyModel> smallBodies, boolean isCustom) throws Exception
	{
		RenderablePointedImageToScenePipeline<G1> actorPipeline =
				new RenderablePointedImageToScenePipeline<G1>(image, smallBodies);
		List<vtkActor> actors = actorPipeline.getRenderableImageActors();

		IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects =
				Publishers.formPair(Just.of(smallBodies), Just.of(actors));

		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
		IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		//*******************************
		//Throw them to the preview tool
		//*******************************
		PointedImageEditingWindow<G1> preview = new PointedImageEditingWindow<G1>(image, smallBodies.get(0), isCustom);

		sceneObjects
			.operate(sceneBuilder)
			.subscribe(preview)
			.run();
	}
}
