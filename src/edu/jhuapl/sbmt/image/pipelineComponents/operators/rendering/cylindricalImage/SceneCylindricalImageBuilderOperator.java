package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkLookupTable;
import vtk.vtkPolyDataMapper;

import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.CylindricalImageRenderables;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;


public class SceneCylindricalImageBuilderOperator extends BasePipelineOperator<Pair<List<SmallBodyModel>,IRenderableImage>, Pair<List<vtkActor>, List<ImageRenderable>>>
{

	List<List<SmallBodyModel>> smallBodyModels;
	List<IRenderableImage> renderableImages;

	public SceneCylindricalImageBuilderOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		smallBodyModels = inputs.stream().map( item -> item.getLeft()).toList();
		renderableImages = inputs.stream().map( item -> item.getRight()).toList();
		processImages();
	}


	private void processImages()
	{
//		final List<Future<CylindricalImageRenderables>> resultList;
//		List<Callable<CylindricalImageRenderables>> taskList = new ArrayList<>();
////		for (RenderableCylindricalImage image : renderableImages)
//		for (int i=0; i<2; i++)
//    	{
//			Callable<CylindricalImageRenderables> task = new RenderableCylindricalImageTask(renderableImages.get(i), List.copyOf(smallBodyModels.get(i)));
//			taskList.add(task);
//		}
//		resultList = ThreadService.submitAll(taskList);
//		List<vtkActor> smallBodyActors = generateBodyModelActor(smallBodyModels.get(0));
//		try
//		{
//			for (int i = 0; i < resultList.size(); i++)
//			{
//				outputs.add(Pair.of(smallBodyActors, List.of(resultList.get(i).get())));
//			}
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		try
		{

//        	System.out.println("SceneCylindricalImageBuilderOperator: processImages: generating small body model actors");
        	List<vtkActor> smallBodyActors = generateBodyModelActor(smallBodyModels.get(0));
//        	System.out.println("SceneCylindricalImageBuilderOperator: processImages: ");
        	for (IRenderableImage image : renderableImages)
        	{
        		CylindricalImageRenderables cylImgRenderable = new CylindricalImageRenderables(image, smallBodyModels.get(renderableImages.indexOf(image)));
        		outputs.add(Pair.of(smallBodyActors, List.of(cylImgRenderable)));
        	}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private class RenderableCylindricalImageTask implements Callable<CylindricalImageRenderables>
//	{
//		IRenderableImage image;
//		List<SmallBodyModel> smallBodyModels;
//
//		public RenderableCylindricalImageTask(IRenderableImage image, List<SmallBodyModel> smallBodyModels)
//		{
//			this.image = image;
//			this.smallBodyModels = smallBodyModels;
//		}
//
//		@Override
//		public CylindricalImageRenderables call() throws Exception
//		{
//			return new CylindricalImageRenderables(image, smallBodyModels);
//		}
//	}

	private List<vtkActor> generateBodyModelActor(List<SmallBodyModel> smallBodyModels)
	{
		List<vtkActor> smallBodyActors = Lists.newArrayList();
		smallBodyModels.forEach(smallBodyModel -> {
			vtkPolyDataMapper  smallBodyMapper = new vtkPolyDataMapper();
	        smallBodyMapper.SetInputData(smallBodyModel.getSmallBodyPolyData());
	        vtkLookupTable lookupTable = new vtkLookupTable();
	        smallBodyMapper.SetLookupTable(lookupTable);
	        smallBodyMapper.UseLookupTableScalarRangeOn();

			VtkLodActor smallBodyActor = new VtkLodActor(this);
			smallBodyActor.setDefaultMapper(smallBodyMapper);
			smallBodyActor.setLodMapper(LodMode.MaxQuality, smallBodyMapper);
			smallBodyActors.add(smallBodyActor);
		});

		return smallBodyActors;
	}
}