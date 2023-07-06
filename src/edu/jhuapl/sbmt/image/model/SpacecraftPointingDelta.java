package edu.jhuapl.sbmt.image.model;

public class SpacecraftPointingDelta
{
	double zoomFactor =  1.0 ;
    double rotationOffset =  0.0 ;
    double pitchOffset =  0.0 ;
    double yawOffset =  0.0 ;
    double sampleOffset = 0.0;
    double lineOffset = 0.0;


    public SpacecraftPointingDelta()
    {

    }

    /**
	 * @return the zoomFactor
	 */
	public double getZoomFactor()
	{
		return zoomFactor;
	}

	/**
	 * @param zoomFactor the zoomFactor to set
	 */
	public void setZoomFactor(double zoomFactor)
	{
		this.zoomFactor = zoomFactor;
	}

	/**
	 * @return the rotationOffset
	 */
	public double getRotationOffset()
	{
		return rotationOffset;
	}

	/**
	 * @param rotationOffset the rotationOffset to set
	 */
	public void setRotationOffset(double rotationOffset)
	{
		this.rotationOffset = rotationOffset;
	}

	/**
	 * @return the pitchOffset
	 */
	public double getPitchOffset()
	{
		return pitchOffset;
	}

	/**
	 * @param pitchOffset the pitchOffset to set
	 */
	public void setPitchOffset(double pitchOffset)
	{
		this.pitchOffset = pitchOffset;
	}

	/**
	 * @return the yawOffset
	 */
	public double getYawOffset()
	{
		return yawOffset;
	}

	/**
	 * @param yawOffset the yawOffset to set
	 */
	public void setYawOffset(double yawOffset)
	{
		this.yawOffset = yawOffset;
	}

	/**
	 * @return the sampleOffset
	 */
	public double getSampleOffset()
	{
		return sampleOffset;
	}

	/**
	 * @param sampleOffset the sampleOffset to set
	 */
	public void setSampleOffset(double sampleOffset)
	{
		this.sampleOffset = sampleOffset;
	}

	/**
	 * @return the lineOffset
	 */
	public double getLineOffset()
	{
		return lineOffset;
	}

	/**
	 * @param lineOffset the lineOffset to set
	 */
	public void setLineOffset(double lineOffset)
	{
		this.lineOffset = lineOffset;
	}

	@Override
	public String toString()
	{
		return String.format(
				"SpacecraftPointingDelta [zoomFactor=%s, rotationOffset=%s, pitchOffset=%s, yawOffset=%s, sampleOffset=%s, lineOffset=%s]",
				zoomFactor, rotationOffset, pitchOffset, yawOffset, sampleOffset, lineOffset);
	}


}
