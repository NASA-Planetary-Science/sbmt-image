package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.awt.Container;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.ui.LayerPreviewPanelOld;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkLayerPreviewOld<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements IPipelineSubscriber<Pair<Layer, List<HashMap<String, String>>>>
{
	private IPipelinePublisher<Pair<Layer, List<HashMap<String, String>>>> publisher;
	private LayerPreviewPanelOld<G1> preview;
	private String title;
	private Runnable completionBlock;
	private int currentLayerIndex;
	private IntensityRange currentIntensityRange;
	private int[] currentMaskValues;
	private double[] currentFillValues = new double[] {};
	private G1 image;

	public VtkLayerPreviewOld(String title, G1 image)
	{
		this(title, image.getCurrentLayer(), image.getIntensityRange(), image.getMaskValues(), image.getFillValues());
		this.image = image;
	}

	public VtkLayerPreviewOld(String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] maskValues, double[] fillValues)
	{
		this.title = title;
		this.currentLayerIndex = currentLayerIndex;
		this.currentIntensityRange = currentIntensityRange;
		this.currentMaskValues = maskValues;
		this.currentFillValues = fillValues;
	}

	public VtkLayerPreviewOld(String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] currentMaskValues, double[] fillValues, Runnable completionBlock)
	{
		this(title, currentLayerIndex, currentIntensityRange, currentMaskValues, fillValues);
		this.completionBlock = completionBlock;
	}

	@Override
	public void receive(List<Pair<Layer, List<HashMap<String, String>>>> items)
	{
		try
		{
			List<Layer> layers = items.stream().map( item -> item.getLeft()).toList();
			List<List<HashMap<String, String>>> metadata = items.stream().map( item -> item.getRight()).toList();
			preview = new LayerPreviewPanelOld<G1>(title, layers, currentLayerIndex, currentIntensityRange, currentMaskValues, currentFillValues, metadata, completionBlock);
			preview.setImage(image);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public void setLayers(List<Layer> layers)
//	{
//		if (preview == null) return;
//		try
//		{
//			System.out.println("VtkLayerPreview: setLayers: setting layers");
//			preview.setLayers(layers);
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	@Override
	public void receive(Pair<Layer, List<HashMap<String, String>>> item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}

	public Container getPanel()
	{
		return preview.getContentPane();
	}

	@Override
	public void setPublisher(IPipelinePublisher<Pair<Layer, List<HashMap<String, String>>>> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkLayerPreviewOld run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

	public IntensityRange getIntensityRange()
	{
		if (preview == null) return currentIntensityRange;
		return preview.getIntensityRange();
	}

	public int[] getMaskValues()
	{
		if (preview == null) return currentMaskValues;
		return preview.getMaskValues();
	}

	public double[] getFillValues()
	{
		if (preview == null) return currentFillValues;
		if (preview.getFillValues() == null) return new double[] {};
		return preview.getFillValues();
	}

	public int getDisplayedLayerIndex()
	{
		if (preview == null) return currentLayerIndex;
		return preview.getDisplayedLayerIndex();
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(G1 image)
	{
		this.image = image;
		if (preview == null) return;
		preview.setImage(image);
	}
}
