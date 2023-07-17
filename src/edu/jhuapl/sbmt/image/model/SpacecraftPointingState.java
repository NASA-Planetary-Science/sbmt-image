package edu.jhuapl.sbmt.image.model;

import java.util.Arrays;

import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class SpacecraftPointingState
{
	double[] spacecraftPosition = new double[3];
    double[] frustum1 = new double[3];
    double[] frustum2 = new double[3];
    double[] frustum3 = new double[3];
    double[] frustum4 = new double[3];
    double[] boresightDirection = new double[3];
    double[] upVector = new double[3];
    double[] sunPosition = new double[3];

    // apply all frame adjustments if true
    boolean applyFrameAdjustments =  true ;

    // location in pixel coordinates of the target origin for the adjusted frustum
    double[] targetPixelCoordinates = { Double.MAX_VALUE, Double.MAX_VALUE };

    private int imageHeight, imageWidth;

    public SpacecraftPointingState(int imageWidth, int imageHeight)
    {
    	this.imageHeight = imageHeight;
    	this.imageWidth = imageWidth;
    }

    public SpacecraftPointingState(PointingFileReader pointing, int imageWidth, int imageHeight)
    {
    	this.spacecraftPosition = Arrays.copyOf(pointing.getSpacecraftPosition(), 3);
    	this.frustum1 = Arrays.copyOf(pointing.getFrustum1(), 3);
    	this.frustum2 = Arrays.copyOf(pointing.getFrustum2(), 3);
    	this.frustum3 = Arrays.copyOf(pointing.getFrustum3(), 3);
    	this.frustum4 = Arrays.copyOf(pointing.getFrustum4(), 3);
    	this.boresightDirection = Arrays.copyOf(pointing.getBoresightDirection(), 3);
    	this.upVector = Arrays.copyOf(pointing.getUpVector(), 3);
    	this.sunPosition = Arrays.copyOf(pointing.getSunPosition(), 3);
    	this.applyFrameAdjustments = pointing.isApplyFrameAdjustments();
    	this.targetPixelCoordinates = Arrays.copyOf(pointing.getTargetPixelCoordinates(), 2);
    	this.imageHeight = imageHeight;
    	this.imageWidth = imageWidth;
    }

    public SpacecraftPointingState(SpacecraftPointingState state)
    {
    	this.spacecraftPosition = Arrays.copyOf(state.spacecraftPosition, 3);
    	this.frustum1 = Arrays.copyOf(state.frustum1, 3);
    	this.frustum2 = Arrays.copyOf(state.frustum2, 3);
    	this.frustum3 = Arrays.copyOf(state.frustum3, 3);
    	this.frustum4 = Arrays.copyOf(state.frustum4, 3);
    	this.boresightDirection = Arrays.copyOf(state.boresightDirection, 3);
    	this.upVector = Arrays.copyOf(state.upVector, 3);
    	this.sunPosition = Arrays.copyOf(state.sunPosition, 3);
    	this.applyFrameAdjustments = state.applyFrameAdjustments;
    	this.targetPixelCoordinates = Arrays.copyOf(state.targetPixelCoordinates, 2);
    	this.imageHeight = state.imageHeight;
    	this.imageWidth = state.imageWidth;
    }

	/**
	 * @return the spacecraftPosition
	 */
	public double[] getSpacecraftPosition()
	{
		return spacecraftPosition;
	}

	/**
	 * @param spacecraftPosition the spacecraftPosition to set
	 */
	public void setSpacecraftPosition(double[] spacecraftPosition)
	{
		this.spacecraftPosition = spacecraftPosition;
	}

	/**
	 * @return the frustum1
	 */
	public double[] getFrustum1()
	{
		return frustum1;
	}

	/**
	 * @param frustum1 the frustum1 to set
	 */
	public void setFrustum1(double[] frustum1)
	{
		this.frustum1 = frustum1;
	}

	/**
	 * @return the frustum2
	 */
	public double[] getFrustum2()
	{
		return frustum2;
	}

	/**
	 * @param frustum2 the frustum2 to set
	 */
	public void setFrustum2(double[] frustum2)
	{
		this.frustum2 = frustum2;
	}

	/**
	 * @return the frustum3
	 */
	public double[] getFrustum3()
	{
		return frustum3;
	}

	/**
	 * @param frustum3 the frustum3 to set
	 */
	public void setFrustum3(double[] frustum3)
	{
		this.frustum3 = frustum3;
	}

	/**
	 * @return the frustum4
	 */
	public double[] getFrustum4()
	{
		return frustum4;
	}

	/**
	 * @param frustum4 the frustum4 to set
	 */
	public void setFrustum4(double[] frustum4)
	{
		this.frustum4 = frustum4;
	}

	/**
	 * @return the boresightDirection
	 */
	public double[] getBoresightDirection()
	{
		return boresightDirection;
	}

	/**
	 * @param boresightDirection the boresightDirection to set
	 */
	public void setBoresightDirection(double[] boresightDirection)
	{
		this.boresightDirection = boresightDirection;
	}

	/**
	 * @return the upVector
	 */
	public double[] getUpVector()
	{
		return upVector;
	}

	/**
	 * @param upVector the upVector to set
	 */
	public void setUpVector(double[] upVector)
	{
		this.upVector = upVector;
	}

	/**
	 * @return the sunPosition
	 */
	public double[] getSunPosition()
	{
		return sunPosition;
	}

	/**
	 * @param sunPosition the sunPosition to set
	 */
	public void setSunPosition(double[] sunPosition)
	{
		this.sunPosition = sunPosition;
	}

	/**
	 * @return the applyFrameAdjustments
	 */
	public boolean isApplyFrameAdjustments()
	{
		return applyFrameAdjustments;
	}

	/**
	 * @param applyFrameAdjustments the applyFrameAdjustments to set
	 */
	public void setApplyFrameAdjustments(boolean applyFrameAdjustments)
	{
		this.applyFrameAdjustments = applyFrameAdjustments;
	}

	/**
	 * @return the targetPixelCoordinates
	 */
	public double[] getTargetPixelCoordinates()
	{
		return targetPixelCoordinates;
	}

	/**
	 * @param targetPixelCoordinates the targetPixelCoordinates to set
	 */
	public void setTargetPixelCoordinates(double[] targetPixelCoordinates)
	{
		this.targetPixelCoordinates = targetPixelCoordinates;
	}

	/**
	 * @return the imageHeight
	 */
	public int getImageHeight()
	{
		return imageHeight;
	}

	/**
	 * @return the imageWidth
	 */
	public int getImageWidth()
	{
		return imageWidth;
	}

	@Override
	public String toString()
	{
		return String.format(
				"SpacecraftPointingState [spacecraftPosition=%s, frustum1=%s, frustum2=%s, frustum3=%s, frustum4=%s, boresightDirection=%s, upVector=%s, sunPosition=%s, applyFrameAdjustments=%s, targetPixelCoordinates=%s, imageHeight=%s, imageWidth=%s]",
				Arrays.toString(spacecraftPosition), Arrays.toString(frustum1), Arrays.toString(frustum2),
				Arrays.toString(frustum3), Arrays.toString(frustum4), Arrays.toString(boresightDirection),
				Arrays.toString(upVector), Arrays.toString(sunPosition), applyFrameAdjustments,
				Arrays.toString(targetPixelCoordinates), imageHeight, imageWidth);
	}

}
