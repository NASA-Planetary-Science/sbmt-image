package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

public class ImageIlluminationAtPoint
{
    double incidence = Double.MAX_VALUE;
    double emission = Double.MAX_VALUE;
    double phase = Double.MAX_VALUE;


	public double getIncidence()
	{
		return incidence;
	}

	public double getEmission()
	{
		return emission;
	}

	public double getPhase()
	{
		return phase;
	}

}
