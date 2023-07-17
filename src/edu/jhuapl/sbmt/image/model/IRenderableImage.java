package edu.jhuapl.sbmt.image.model;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public interface IRenderableImage
{

	/**
	 * @return the layer
	 */
	Layer getLayer();

	LayerMasking getMasking();

	IntensityRange getIntensityRange();

	/**
	 * @return the isLinearInterpolation
	 */
	boolean isLinearInterpolation();

	public CylindricalBounds getBounds();

	public double getOffset();

	public PointingFileReader getPointing();

	public String getFilename();

	public void setFilename(String filename);

}