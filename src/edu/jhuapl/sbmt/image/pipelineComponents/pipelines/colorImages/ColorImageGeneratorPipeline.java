package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.colorImages;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.gdal.gdal.gdal;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkFeatureEdges;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.SceneActorBuilderOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color.ColorImageFootprintGeneratorOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color.ColorImageGeneratorOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VTKImagePolyDataRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.ImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin.BuiltInVTKReader;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkRendererPreview;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class ColorImageGeneratorPipeline extends ImageToScenePipeline //implements RenderableImageActorPipeline
{
	List<vtkActor> imageActors = Lists.newArrayList();
	List<vtkImageData> imageDatas = Lists.newArrayList();
	Pair<vtkImageData, vtkPolyData>[] imageAndPolyData = new Pair[1];
	List<SmallBodyModel> smallBodyModels;
	
	public ColorImageGeneratorPipeline(List<IPerspectiveImage> images, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		this(images, smallBodyModels, false);
	}
	
	public ColorImageGeneratorPipeline(List<IPerspectiveImage> images, List<SmallBodyModel> smallBodyModels, boolean forceUpdate) throws Exception
	{
		sceneOutputs = new Pair[1];
		this.smallBodyModels = smallBodyModels;
		Just.of(images)
			.operate(new ColorImageGeneratorOperator(forceUpdate))
			.operate(new ColorImageFootprintGeneratorOperator(smallBodyModels))
			.subscribe(PairSink.of(imageAndPolyData))
			.run();

		Just.of(imageAndPolyData[0])
			.operate(new VTKImagePolyDataRendererOperator(images.get(0).getInterpolateState()))
			.subscribe(Sink.of(imageActors))
			.run();
	}

	public List<vtkActor> getImageActors()
	{
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableImageActors()
	{
		return imageActors;
	}

	@Override
	public List<vtkActor> getRenderableImageBoundaryActors()
	{
		vtkPolyData polyData = imageAndPolyData[0].getRight();
		vtkFeatureEdges edgeExtracter = new vtkFeatureEdges();
		vtkActor boundaryActor = new vtkActor();
		vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
		List<vtkActor> boundaryActors = Lists.newArrayList();
		edgeExtracter.SetInputData(polyData);
		edgeExtracter.BoundaryEdgesOn();
		edgeExtracter.FeatureEdgesOff();
		edgeExtracter.NonManifoldEdgesOff();
		edgeExtracter.ManifoldEdgesOff();
		edgeExtracter.ColoringOff();
		edgeExtracter.Update();

		for (SmallBodyModel smallBody : smallBodyModels)
    	{
			vtkPolyData edgeExtracterOutput = edgeExtracter.GetOutput();
			if (boundaryMapper != null)
			{
		        boundaryMapper.SetInputData(edgeExtracterOutput);
		        boundaryMapper.Update();
		        boundaryActor.SetMapper(boundaryMapper);
		        boundaryActors.add(boundaryActor);
			}
    	}
		return boundaryActors;
	}

	public static void main(String[] args) throws Exception
	{
		NativeLibraryLoader.loadVtkLibraries();
		gdal.AllRegister();

		PerspectiveImageMetadata image1 = new PerspectiveImageMetadata("/Users/steelrj1/Desktop/2356-test-files/1270/M0125990473F4_2P_IOF_DBL.FIT", ImageType.MSI_IMAGE, PointingSource.SPICE, "/Users/steelrj1/Desktop/2356-test-files/1270/M0125990473F4_2P_IOF_DBL.INFO", new double[] {});
		image1.setLinearInterpolatorDims(new int[] { 537, 412 });
		image1.setMaskValues(new int[] {2, 14, 2, 14});

		PerspectiveImageMetadata image2 = new PerspectiveImageMetadata("/Users/steelrj1/Desktop/2356-test-files/1270/M0125990619F4_2P_IOF_DBL.FIT", ImageType.MSI_IMAGE, PointingSource.SPICE, "/Users/steelrj1/Desktop/2356-test-files/1270/M0125990619F4_2P_IOF_DBL.INFO", new double[] {});
		image2.setLinearInterpolatorDims(new int[] { 537, 412 });
		image2.setMaskValues(new int[] {2, 14, 2, 14});

		PerspectiveImageMetadata image3 = new PerspectiveImageMetadata("/Users/steelrj1/Desktop/2356-test-files/1270/M0126023535F4_2P_IOF_DBL.FIT", ImageType.MSI_IMAGE, PointingSource.SPICE, "/Users/steelrj1/Desktop/2356-test-files/1270/M0126023535F4_2P_IOF_DBL.INFO", new double[] {});
		image3.setLinearInterpolatorDims(new int[] { 537, 412 });
		image3.setMaskValues(new int[] {2, 14, 2, 14});

		List<IPerspectiveImage> images = List.of(new CompositePerspectiveImage(List.of(image1)), new CompositePerspectiveImage(List.of(image2)), new CompositePerspectiveImage(List.of(image3)));

		IPipelinePublisher<SmallBodyModel> vtkReader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
		List<SmallBodyModel> smallBodyModels = Lists.newArrayList();

		vtkReader.subscribe(Sink.of(smallBodyModels)).run();

		ColorImageGeneratorPipeline pipeline = new ColorImageGeneratorPipeline(images, smallBodyModels);

		List<vtkActor> actors = pipeline.getImageActors();

		IPipelinePublisher<Pair<SmallBodyModel, vtkActor>> sceneObjects = Publishers.formPair(Just.of(vtkReader.getOutputs()), Just.of(actors));
		IPipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor> sceneBuilder = new SceneActorBuilderOperator();

		VtkRendererPreview preview = new VtkRendererPreview(vtkReader.getOutputs().get(0));

		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(preview)		//subscribe to the scene builder with the preview
			.run();

	}
}
