package edu.jhuapl.sbmt.image.model;

import java.util.HashMap;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class RenderableCylindricalImage implements IRenderableImage
{
	private Layer layer;
	private HashMap<String, String> metadata;
	private int imageWidth, imageHeight;
	private LayerMasking masking = new LayerMasking(new int[] {0,0,0,0});
	private double offset;
	private double defaultOffset;
	private CylindricalBounds bounds;
	private IntensityRange intensityRange = new IntensityRange(0, 255);
	private String filename;
	private boolean forceUpdate = false;

	public RenderableCylindricalImage(Layer layer, HashMap<String, String> metadata, CylindricalBounds bounds)
	{
		this.layer = layer;
		this.bounds = bounds;
		this.metadata = metadata;
		this.imageWidth = layer.iSize();
		this.imageHeight = layer.jSize();
	}

	/**
	 * @return the layer
	 */
	@Override
	public Layer getLayer()
	{
		return layer;
	}


	/**
	 * @return the metadata
	 */
	public HashMap<String, String> getMetadata()
	{
		return metadata;
	}


	/**
	 * @return the imageWidth
	 */
	public int getImageWidth()
	{
		return imageWidth;
	}


	/**
	 * @return the imageHeight
	 */
	public int getImageHeight()
	{
		return imageHeight;
	}


	@Override
	public LayerMasking getMasking()
	{
		return masking;
	}


	public void setMasking(LayerMasking masking)
	{
		this.masking = masking;
	}


	public double getOffset()
	{
		return offset;
	}


	public void setOffset(double offset)
	{
		this.offset = offset;
	}


	public double getDefaultOffset()
	{
		return defaultOffset;
	}


	public void setDefaultOffset(double defaultOffset)
	{
		this.defaultOffset = defaultOffset;
	}


	public CylindricalBounds getBounds()
	{
		return bounds;
	}


	public void setBounds(CylindricalBounds bounds)
	{
		this.bounds = bounds;
	}

	/**
	 * @return the isLinearInterpolation
	 */
	@Override
	public boolean isLinearInterpolation()
	{
		return true;
	}

	@Override
	public PointingFileReader getPointing()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the intensityRange
	 */
	public IntensityRange getIntensityRange()
	{
		return intensityRange;
	}

	/**
	 * @param intensityRange the intensityRange to set
	 */
	public void setIntensityRange(IntensityRange intensityRange)
	{
		this.intensityRange = intensityRange;
	}

	public String getFilename()
	{
		return filename;
	}


	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	@Override
	public int getLayerIndex() {
		return 0;
	}

	@Override
	public void setLayerIndex(int layerIndex) {
		
	}

	@Override
	public boolean getForceUpdate()
	{
		return forceUpdate;
	}

	@Override
	public void setForceUpdate(boolean forceUpdate)
	{
		this.forceUpdate = forceUpdate;
	}
}
