package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import vtk.vtkActor;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage.SceneCylindricalImageBuilderOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.ImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin.BuiltInVTKReader;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RenderableCylindricalImageToScenePipeline extends ImageToScenePipeline
{
	List<vtkActor> renderableImageActors = Lists.newArrayList();


	public RenderableCylindricalImageToScenePipeline(IPerspectiveImage image, List<SmallBodyModel> smallBodyModel) throws InvalidGDALFileTypeException, Exception
	{
		activeLayerIndex = image.getCurrentLayer();
		sceneOutputs = new Pair[1];
		List<IRenderableImage> renderableImages = CylindricalImageToRenderableImagePipeline.of(List.of(image)).getRenderableImages();
		for (IRenderableImage renderableImage : renderableImages)
		{
			((RenderableCylindricalImage)renderableImage).setOffset(3.0 * smallBodyModel.get(0).getMinShiftAmount());
			((RenderableCylindricalImage)renderableImage).setDefaultOffset(3.0 * smallBodyModel.get(0).getMinShiftAmount());
			((RenderableCylindricalImage)renderableImage).setIntensityRange(image.getIntensityRange());
			((RenderableCylindricalImage)renderableImage).setMasking(new LayerMasking(image.getMaskValues()));
			((RenderableCylindricalImage)renderableImage).setFilename(image.getFilename());
		}
		//*************************
		//zip the sources together
		//*************************
		List<List<SmallBodyModel>> smallBodyModelCollection = Lists.newArrayList();
		for (int i=0; i<renderableImages.size(); i++)
		{
			smallBodyModelCollection.add(smallBodyModel);
		}
		IPipelinePublisher<Pair<List<SmallBodyModel>, IRenderableImage>> sceneObjects =
				Publishers.formPair(Just.of(smallBodyModelCollection), Just.of(renderableImages));
		//*****************************************************************************************************
		//Pass them into the scene builder to perform intersection calculations, and send actors to List
		//*****************************************************************************************************
		sceneOutputs = new Pair[renderableImages.size()];
		IPipelineOperator<Pair<List<SmallBodyModel>, IRenderableImage>, Pair<List<vtkActor>, List<ImageRenderable>>> sceneBuilder =
				new SceneCylindricalImageBuilderOperator();
		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(PairSink.of(sceneOutputs)).run();
	}

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadAllVtkLibraries();

		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
		List<SmallBodyModel> smallBodyModels = Lists.newArrayList();

		vtkReader.subscribe(Sink.of(smallBodyModels)).run();

//		RenderCylindricalImageToScenePipeline pipeline =
//				new RenderCylindricalImageToScenePipeline("/Users/steelrj1/.sbmt-stage-apl/cache/2/GASKELL/EROS/MSI/images/M0125990473F4_2P_IOF_DBL.FIT", new CylindricalBounds(-25, 30, 142, 200), smallBodyModels); //was -25, 30, -38, 20
//
////		RenderableCylindricalImageActorPipeline pipeline =
////				new RenderableCylindricalImageActorPipeline("/Users/steelrj1/Desktop/image_map.png", new CylindricalBounds(-90, 90, 0, 360), smallBodyModels);
//
//
//		List<vtkActor> actors = pipeline.getRenderableImageActors();
//
//		IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));
//		IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();
//
//		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));
//
//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(preview)		//subscribe to the scene builder with the preview
//			.run();

	}
}
