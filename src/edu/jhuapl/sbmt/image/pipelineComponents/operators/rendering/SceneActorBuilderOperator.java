package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering;

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
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;


public class SceneActorBuilderOperator extends BasePipelineOperator<Pair<SmallBodyModel, vtkActor>, vtkActor>
{

	List<SmallBodyModel> smallBodyModels;
	List<vtkActor> actors;

	public SceneActorBuilderOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		smallBodyModels = inputs.stream().map( item -> item.getLeft()).toList();
		actors = inputs.stream().map( item -> item.getRight()).toList();
//		smallBodyModels = inputs.get(0).getLeft();
//		actors = inputs.get(0).getRight();
		processImages();
	}


	private void processImages()
	{
        try
		{
        	outputs.addAll(generateBodyModelActor(smallBodyModels));
        	for (vtkActor actor : actors)
        		outputs.add(actor);
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