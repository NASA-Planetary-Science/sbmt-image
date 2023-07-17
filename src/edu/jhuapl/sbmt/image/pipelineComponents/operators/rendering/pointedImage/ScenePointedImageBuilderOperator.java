package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

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
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;


public class ScenePointedImageBuilderOperator extends BasePipelineOperator<Pair<SmallBodyModel, RenderablePointedImage>, Pair<List<vtkActor>, List<ImageRenderable>>>
{

	List<SmallBodyModel> smallBodyModels;
	List<RenderablePointedImage> renderableImages;

	public ScenePointedImageBuilderOperator()
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
        try
		{
        	outputs.add(Pair.of(Lists.newArrayList(), Lists.newArrayList()));
        	outputs.get(0).getLeft().addAll(generateBodyModelActor(smallBodyModels));
        	List<PointedImageRenderables> renderables = Lists.newArrayList();
        	for (RenderablePointedImage image : renderableImages)
        	{
        		PointedImageRenderables pointedImageRenderable = new PointedImageRenderables(image, smallBodyModels);
        		renderables.add(pointedImageRenderable);
        	}
        	outputs.get(0).getRight().addAll(renderables);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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