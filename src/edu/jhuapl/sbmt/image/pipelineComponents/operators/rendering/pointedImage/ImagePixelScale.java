package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

public class ImagePixelScale
{
    double minHorizontalPixelScale = Double.MAX_VALUE;
    double maxHorizontalPixelScale = -Double.MAX_VALUE;
    double meanHorizontalPixelScale = 0.0;
    double minVerticalPixelScale = Double.MAX_VALUE;
    double maxVerticalPixelScale = -Double.MAX_VALUE;
    double meanVerticalPixelScale = 0.0;

	public double getMinHorizontalPixelScale()
	{
		return minHorizontalPixelScale;
	}
	public double getMaxHorizontalPixelScale()
	{
		return maxHorizontalPixelScale;
	}
	public double getMeanHorizontalPixelScale()
	{
		return meanHorizontalPixelScale;
	}
	public double getMinVerticalPixelScale()
	{
		return minVerticalPixelScale;
	}
	public double getMaxVerticalPixelScale()
	{
		return maxVerticalPixelScale;
	}
	public double getMeanVerticalPixelScale()
	{
		return meanVerticalPixelScale;
	}
}
