package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.util.HashMap;
import java.util.Optional;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class RenderablePointedImage implements IRenderableImage
{
	private PointingFileReader pointing;
	private Optional<PointingFileReader> modifiedPointing = Optional.ofNullable(null);
	private Layer layer;
	private HashMap<String, String> metadata;
	private int imageWidth, imageHeight;
	private LayerMasking masking = new LayerMasking(0, 0, 0, 0);
	private double offset;
	private double defaultOffset;
	private IntensityRange intensityRange;
	private IntensityRange offlimbIntensityRange;
	private double minFrustumLength, maxFrustumLength;
	private double offlimbDepth;
	private boolean isLinearInterpolation = true;
	private String filename;
	private boolean offlimbShowing = false;
	private PointingSource imageSource;
	private ImageBinPadding imageBinPadding;
	private int binning = 1;
	private Integer startH;
	private Integer lastV;
	private int layerIndex;
//	private int[] pad;
//	private int[] fullSize;

	public RenderablePointedImage(Layer layer, HashMap<String, String> metadata, PointingFileReader pointing)
	{
		this.layer = layer;
		this.pointing = pointing;
		this.metadata = metadata;
		this.imageWidth = layer.iSize();
		this.imageHeight = layer.jSize();
	}


	/**
	 * @return the pointing
	 */
	public PointingFileReader getPointing()
	{
		return pointing;
	}


	/**
	 * @return the layer
	 */
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

	//MOVE THESE TO POINTINGFILEREADER?
	public double getMaxFovAngle()
    {
        return Math.max(getHorizontalFovAngle(), getVerticalFovAngle());
    }

    public double getHorizontalFovAngle()
    {
        double fovHoriz = MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum3()) * 180.0 / Math.PI;
        return fovHoriz;
    }

    public double getVerticalFovAngle()
    {
        double fovVert = MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum2()) * 180.0 / Math.PI;
        return fovVert;
    }


	public IntensityRange getIntensityRange()
	{
		return intensityRange;
	}


	public void setIntensityRange(IntensityRange intensityRange)
	{
		this.intensityRange = intensityRange;
	}

	public IntensityRange getOfflimbIntensityRange()
	{
		return offlimbIntensityRange;
	}


	public void setOfflimbIntensityRange(IntensityRange intensityRange)
	{
		this.offlimbIntensityRange = intensityRange;
	}


	public double getMinFrustumLength()
	{
		return minFrustumLength;
	}


	public void setMinFrustumLength(double minFrustumLength)
	{
		this.minFrustumLength = minFrustumLength;
	}


	public double getMaxFrustumLength()
	{
		return maxFrustumLength;
	}


	public void setMaxFrustumLength(double maxFrustumLength)
	{
		this.maxFrustumLength = maxFrustumLength;
	}


	public double getOfflimbDepth()
	{
		return offlimbDepth;
	}


	public void setOfflimbDepth(double offlimbDepth)
	{
		this.offlimbDepth = offlimbDepth;
	}


	/**
	 * @return the modifiedPointing
	 */
	public Optional<PointingFileReader> getModifiedPointing()
	{
		return modifiedPointing;
	}


	/**
	 * @param modifiedPointing the modifiedPointing to set
	 */
	public void setModifiedPointing(Optional<PointingFileReader> modifiedPointing)
	{
		this.modifiedPointing = modifiedPointing;
	}


	/**
	 * @return the isLinearInterpolation
	 */
	public boolean isLinearInterpolation()
	{
		return isLinearInterpolation;
	}


	/**
	 * @param isLinearInterpolation the isLinearInterpolation to set
	 */
	public void setLinearInterpolation(boolean isLinearInterpolation)
	{
		this.isLinearInterpolation = isLinearInterpolation;
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
	public CylindricalBounds getBounds()
	{
		// TODO Auto-generated method stub
		return null;
	}


	public boolean isOfflimbShowing()
	{
		return offlimbShowing;
	}


	public void setOfflimbShowing(boolean offlimbShowing)
	{
		this.offlimbShowing = offlimbShowing;
	}


	public PointingSource getImageSource()
	{
		return imageSource;
	}


	public void setImageSource(PointingSource imageSource)
	{
		this.imageSource = imageSource;
	}


	/**
	 * @return the imageBinPadding
	 */
	public ImageBinPadding getImageBinPadding()
	{
		return imageBinPadding;
	}


	/**
	 * @param imageBinPadding the imageBinPadding to set
	 */
	public void setImageBinPadding(ImageBinPadding imageBinPadding)
	{
		this.imageBinPadding = imageBinPadding;
	}


	/**
	 * @return the binning
	 */
	public int getBinning()
	{
		return binning;
	}


	/**
	 * @param binning the binning to set
	 */
	public void setBinning(int binning)
	{
		this.binning = binning;
	}


	/**
	 * @return the startH
	 */
	public Integer getStartH()
	{
		return startH;
	}


	/**
	 * @param startH the startH to set
	 */
	public void setStartH(int startH)
	{
		this.startH = startH;
	}


	/**
	 * @return the lastV
	 */
	public Integer getLastV()
	{
		return lastV;
	}


	/**
	 * @param lastV the lastV to set
	 */
	public void setLastV(int lastV)
	{
		this.lastV = lastV;
	}


	public int getLayerIndex() {
		return layerIndex;
	}


	public void setLayerIndex(int layerIndex) {
		this.layerIndex = layerIndex;
	}
	
	
}
