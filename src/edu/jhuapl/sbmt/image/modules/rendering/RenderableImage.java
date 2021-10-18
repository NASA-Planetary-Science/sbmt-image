package edu.jhuapl.sbmt.image.modules.rendering;

import java.util.HashMap;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class RenderableImage
{
	private InfoFileReader pointing;
	private Layer layer;
	private HashMap<String, String> metadata;
	private int imageWidth, imageHeight;

	public RenderableImage(Layer layer, HashMap<String, String> metadata, InfoFileReader pointing)
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
	public InfoFileReader getPointing()
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

}
