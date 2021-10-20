package edu.jhuapl.sbmt.image.modules.pointing;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;

import vtk.vtkTransform;
import vtk.vtkTransformFilter;

import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.InstrumentPointing;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;

import crucible.core.mechanics.EphemerisID;
import crucible.core.mechanics.utilities.SimpleEphemerisID;

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
		for (int i=0; i< inputs.size(); i++)
		{
			smallBodyModels.add(inputs.get(i).getLeft());
			pointingProviders.add(inputs.get(i).getRight());
			for (SmallBodyModel smallBodyModel : smallBodyModels)
			{
				if (smallBodyModel.getModelName().equals(centerBodyName))
				{
					outputs.add(smallBodyModel);
					continue;
				}

				//shift the body to the proper location at this time
				vtkTransform transform = new vtkTransform();
				double[] bodyPos = getBodyPosition(smallBodyModel.getModelName());
				transform.Translate(bodyPos);
				vtkTransformFilter transformFilter=new vtkTransformFilter();
				transformFilter.SetInputData(smallBodyModel.getSmallBodyPolyData());
				transformFilter.SetTransform(transform);
				transformFilter.Update();
				smallBodyModel.transformBody(transformFilter);
				outputs.add(smallBodyModel);
			}
		}
	}

	private double[] getBodyPosition(String bodyName)
	{
		SpicePointingProvider pointingProvider  = pointingProviders.get(0);
		Preconditions.checkNotNull(time);
		Preconditions.checkNotNull(pointingProvider);
		String currentInstrumentFrameName = pointingProvider.getInstrumentNames()[0];
		InstrumentPointing pointing = pointingProvider.provide(currentInstrumentFrameName, time);
		EphemerisID body = new SimpleEphemerisID(bodyName.toUpperCase());
		return new double[] { pointing.getPosition(body).getI(),
				pointing.getPosition(body).getJ(),
				pointing.getPosition(body).getK()

		};
	}
}