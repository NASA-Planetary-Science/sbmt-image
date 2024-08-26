package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.editing;

import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.colorImages.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.PointedImageEditingWindow;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import vtk.vtkActor;

public class PointedRenderableImageEditingPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	public PointedRenderableImageEditingPipeline(G1 image, List<SmallBodyModel> smallBodies, boolean isCustom) throws Exception
	{
		List<vtkActor> actors = Lists.newArrayList();
		if (image.getNumberOfLayers() == 1)
		{
			RenderablePointedImageToScenePipeline<G1> actorPipeline =
					new RenderablePointedImageToScenePipeline<G1>(image, smallBodies);
			actors = actorPipeline.getRenderableImageActors();
		}
		else if (image.getNumberOfLayers() == 3 && isCustom)
		{
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null), "Pointing adjustments are not available for color images", "Error", JOptionPane.ERROR_MESSAGE);
            return;
			
		}
		else if (image.getNumberOfLayers() == 3) {
			ColorImageGeneratorPipeline pipeline = new ColorImageGeneratorPipeline(image.getImages(), smallBodies);
			actors = pipeline.getImageActors();
		}
		

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
