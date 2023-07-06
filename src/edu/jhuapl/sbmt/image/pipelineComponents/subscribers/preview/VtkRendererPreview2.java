package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;
//package edu.jhuapl.sbmt.image2.pipeline.preview;
//
//import java.util.List;
//
//import org.apache.commons.lang3.tuple.Pair;
//
//import vtk.vtkActor;
//
//import edu.jhuapl.sbmt.common.client.SmallBodyModel;
//import edu.jhuapl.sbmt.image2.pipeline.rendering.pointedImage.RenderablePointedImage;
//import edu.jhuapl.sbmt.image2.ui.RendererPreviewPanel2;
//import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
//import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;
//import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
//import edu.jhuapl.sbmt.util.TimeUtil;
//
//public class VtkRendererPreview2 implements IPipelineSubscriber<vtkActor>   //BasePipelineSubscriber<vtkActor>
//{
//	private SmallBodyModel smallBodyModel;
//	IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderablePointedImage>>> sceneObjects;
//	RenderableImagesPipeline renderableImagesPipeline;
//	BodyPositionPipeline bodyPositionPipeline;
//	double startTime;
//
//	public VtkRendererPreview2(String[] imageFiles, String[] pointingFiles, String[] bodyFiles, String[] bodyNames, SpiceInfo[] spiceInfos, String mkPath, String centerBodyName, String initialTime, String instFrame) throws Exception
//	{
//		this.startTime = TimeUtil.str2et(initialTime);
//
//		renderableImagesPipeline = new RenderableImagesPipeline(imageFiles, pointingFiles);
//		bodyPositionPipeline = new BodyPositionPipeline(bodyFiles, bodyNames, spiceInfos, mkPath, centerBodyName, initialTime, instFrame);
//		bodyPositionPipeline.run();
//		List<SmallBodyModel> updatedBodies = bodyPositionPipeline.getOutput();
//		smallBodyModel = updatedBodies.get(0);
//	}
//
//	@Override
//	public void receive(List<vtkActor> items)
//	{
//		try
//		{
//			RendererPreviewPanel2 preview = new RendererPreviewPanel2(smallBodyModel, renderableImagesPipeline, bodyPositionPipeline, startTime);
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//}