package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;
import vtk.vtkTexture;

import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class VTKImagePolyDataRendererOperator extends BasePipelineOperator<Pair<vtkImageData, vtkPolyData>, vtkActor>
{
	boolean isLinearInterpolation;

	public VTKImagePolyDataRendererOperator(boolean interpolationState)
	{
		this.isLinearInterpolation = interpolationState;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkPolyData footprint = inputs.get(0).getRight();

		vtkTexture imageTexture = new vtkTexture();
//        imageTexture.InterpolateOn();
		imageTexture.SetInterpolate(isLinearInterpolation ? 1 : 0);
        imageTexture.RepeatOff();
        imageTexture.EdgeClampOn();
        imageTexture.SetInputData(inputs.get(0).getLeft());

		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.ScalarVisibilityOff();
        mapper.SetScalarModeToDefault();
		mapper.SetInputData(footprint);
		mapper.Update();

		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		actor.SetTexture(imageTexture);
        actor.SetForceOpaque(true);
        vtkProperty footprintProperty = actor.GetProperty();
        footprintProperty.LightingOff();

        outputs.add(actor);
	}

}
