package edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.InstrumentPointing;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import picante.math.vectorspace.RotationMatrixIJK;
import picante.mechanics.Coverage;
import picante.mechanics.EphemerisID;
import picante.mechanics.FrameID;
import picante.mechanics.FrameTransformFunction;
import picante.mechanics.utilities.SimpleEphemerisID;
import picante.mechanics.utilities.SimpleFrameID;
import vtk.vtkMatrix4x4;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;

public class SpiceBodyOperator extends BasePipelineOperator<Pair<SmallBodyModel, SpicePointingProvider>, SmallBodyModel>
{
	private String centerBodyName;
	private double time;
	private List<SmallBodyModel> smallBodyModels;
	private List<SpicePointingProvider> pointingProviders;


	public SpiceBodyOperator(String centerBodyName, double time)
	{
		this.centerBodyName = centerBodyName;
		this.time = time;
		smallBodyModels = Lists.newArrayList();
		pointingProviders = Lists.newArrayList();
	}

	public void setTime(double time)
	{
		this.time = time;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs.clear();
		smallBodyModels.clear();
		pointingProviders.clear();
		for (int i=0; i< inputs.size(); i++)
		{
			smallBodyModels.add(inputs.get(i).getLeft());
			pointingProviders.add(inputs.get(i).getRight());
		}
		for (SmallBodyModel smallBodyModel : smallBodyModels)
		{
			vtkTransform transform = new vtkTransform();

			if (smallBodyModel.getModelName().equals(centerBodyName))
			{
//				RotationMatrixIJK rotationTransform = getBodyOrientation(smallBodyModel.getModelName());
//				vtkMatrix4x4 fullMatrix = getTransformationMatrix(rotationTransform, new double[] { 0, 0, 0 } );
//				transform.SetMatrix(fullMatrix);
//				transform.Update();
//
//				vtkTransformFilter transformFilter=new vtkTransformFilter();
//				transformFilter.SetInputData(smallBodyModel.getSmallBodyPolyData());
//				transformFilter.SetTransform(transform);
//				transformFilter.Update();
//				smallBodyModel.transformBody(transformFilter);
				outputs.add(smallBodyModel);
				continue;
			}

			//shift the body to the proper location at this time
			double[] bodyPos = getBodyPosition(smallBodyModel.getModelName());

			RotationMatrixIJK rotationTransform = getBodyOrientation(smallBodyModel.getModelName());
			vtkMatrix4x4 fullMatrix = getTransformationMatrix(rotationTransform, bodyPos);
			transform.SetMatrix(fullMatrix);
			transform.Update();

			vtkTransformFilter transformFilter=new vtkTransformFilter();
			transformFilter.SetInputData(smallBodyModel.getSmallBodyPolyData());
			transformFilter.SetTransform(transform);
			transformFilter.Update();
			smallBodyModel.transformBody(transform);
			outputs.add(smallBodyModel);
		}
	}

	private double[] getBodyPosition(String bodyName)
	{
		SpicePointingProvider pointingProvider  = pointingProviders.get(0);
		Preconditions.checkNotNull(time);
		Preconditions.checkNotNull(pointingProvider);
		InstrumentPointing pointing = pointingProvider.provide(time);
		EphemerisID body = new SimpleEphemerisID(bodyName.toUpperCase());
		return new double[] { pointing.getPosition(body).getI(),
				pointing.getPosition(body).getJ(),
				pointing.getPosition(body).getK()

		};
	}

	private RotationMatrixIJK getBodyOrientation(String bodyName)
	{
		SpicePointingProvider pointingProvider  = pointingProviders.get(0);
		Preconditions.checkNotNull(time);
		Preconditions.checkNotNull(pointingProvider);
		FrameID body = new SimpleFrameID("120065803_FIXED");
		FrameTransformFunction frameTransformFunction = pointingProvider.getEphemerisProvider().createFrameTransformFunction(new SimpleFrameID("920065803_FIXED"), body, Coverage.ALL_TIME);
		RotationMatrixIJK transform = frameTransformFunction.getTransform(time);
		return transform;
	}

	private vtkMatrix4x4 getTransformationMatrix(RotationMatrixIJK rotationTransform, double[] bodyPos)
	{
		vtkMatrix4x4 fullMatrix = new vtkMatrix4x4();
		fullMatrix.Identity();
		fullMatrix.SetElement(0, 0, rotationTransform.get(0, 0));
		fullMatrix.SetElement(1, 0, rotationTransform.get(0, 1));
		fullMatrix.SetElement(2, 0, rotationTransform.get(0, 2));
		fullMatrix.SetElement(3, 0, 0);
		fullMatrix.SetElement(0, 1, rotationTransform.get(1, 0));
		fullMatrix.SetElement(1, 1, rotationTransform.get(1, 1));
		fullMatrix.SetElement(2, 1, rotationTransform.get(1, 2));
		fullMatrix.SetElement(3, 1, 0);
		fullMatrix.SetElement(0, 2, rotationTransform.get(2, 0));
		fullMatrix.SetElement(1, 2, rotationTransform.get(2, 1));
		fullMatrix.SetElement(2, 2, rotationTransform.get(2, 2));
		fullMatrix.SetElement(3, 2, 0);
		fullMatrix.SetElement(0, 3, bodyPos[0]);
		fullMatrix.SetElement(1, 3, bodyPos[1]);
		fullMatrix.SetElement(2, 3, bodyPos[2]);
		fullMatrix.SetElement(3, 3, 1);
		return fullMatrix;
	}
}