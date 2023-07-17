package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

public class ImageIllumination
{
    double minIncidence = Double.MAX_VALUE;
    double maxIncidence = -Double.MAX_VALUE;
    double minEmission = Double.MAX_VALUE;
    double maxEmission = -Double.MAX_VALUE;
    double minPhase = Double.MAX_VALUE;
    double maxPhase = -Double.MAX_VALUE;
    double horizFovAngle, verticalFovAngle;
    double surfaceArea;

	public double getMinIncidence()
	{
		return minIncidence;
	}
	public double getMaxIncidence()
	{
		return maxIncidence;
	}
	public double getMinEmission()
	{
		return minEmission;
	}
	public double getMaxEmission()
	{
		return maxEmission;
	}
	public double getMinPhase()
	{
		return minPhase;
	}
	public double getMaxPhase()
	{
		return maxPhase;
	}

	public double getHorizFovAngle()
	{
		return horizFovAngle;
	}
	public double getVerticalFovAngle()
	{
		return verticalFovAngle;
	}

	public double getSurfaceArea()
	{
		return surfaceArea;
	}

}
