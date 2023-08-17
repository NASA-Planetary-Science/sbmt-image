package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.awt.Container;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.controllers.preview.LayerPreviewController;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.LayerPreviewModel;
import edu.jhuapl.sbmt.image.ui.LayerPreviewPanel;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkLayerPreview<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements IPipelineSubscriber<Pair<Layer, List<HashMap<String, String>>>>
{
	private IPipelinePublisher<Pair<Layer, List<HashMap<String, String>>>> publisher;
	private LayerPreviewPanel<G1> preview;
	LayerPreviewController<G1> previewController;
	LayerPreviewModel<G1> previewModel;
	private String title;
	private Runnable completionBlock;
	private int currentLayerIndex;
	private IntensityRange currentIntensityRange;
	private int[] currentMaskValues;
	private double[] currentFillValues = new double[] {};
	private G1 image;
	private boolean invertY = false;
	private SmallBodyModel smallBodyModel;

	public VtkLayerPreview(String title, G1 image, boolean invertY)
	{
		this(image, title, image.getCurrentLayer(), image.getIntensityRange(), image.getMaskValues(), image.getFillValues(), invertY);
		this.image = image;
	}

	public VtkLayerPreview(G1 image, String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] maskValues, double[] fillValues, boolean invertY)
	{
		this.title = title;
		this.currentLayerIndex = currentLayerIndex;
		this.currentIntensityRange = currentIntensityRange;
		this.currentMaskValues = maskValues;
		this.currentFillValues = fillValues;
		this.invertY = invertY;
		this.image = image;
	}

	public VtkLayerPreview(G1 image, String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] currentMaskValues, double[] fillValues, boolean invertY, Runnable completionBlock)
	{
		this(image, title, currentLayerIndex, currentIntensityRange, currentMaskValues, fillValues, invertY);
		this.completionBlock = completionBlock;
	}

	public VtkLayerPreview(String title, int currentLayerIndex, IntensityRange currentIntensityRange, int[] currentMaskValues, double[] fillValues, boolean invertY, Runnable completionBlock)
	{
		this(null, title, currentLayerIndex, currentIntensityRange, currentMaskValues, fillValues, invertY);
		this.completionBlock = completionBlock;
	}

	@Override
	public void receive(List<Pair<Layer, List<HashMap<String, String>>>> items)
	{
		try
		{
			List<Layer> layers = items.stream().map( item -> item.getLeft()).toList();
			List<List<HashMap<String, String>>> metadata = items.stream().map( item -> item.getRight()).toList();
			preview = new LayerPreviewPanel<G1>(title);
			previewModel = new LayerPreviewModel<G1>(image, layers, currentLayerIndex, currentIntensityRange, currentMaskValues, currentFillValues, metadata, invertY, smallBodyModel);
			previewController = new LayerPreviewController<>(preview, previewModel, completionBlock);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
	public VtkLayerPreview<G1> run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

	public IntensityRange getIntensityRange()
	{
		if (previewModel == null) return currentIntensityRange;
		return previewModel.getIntensityRange();
	}

	public int[] getMaskValues()
	{
		if (previewModel == null) return currentMaskValues;
		return previewModel.getCurrentMaskValues();
	}

	public double[] getFillValues()
	{
		if (previewModel == null) return currentFillValues;
		if (previewModel.getCurrentFillValues() == null) return new double[] {};
		return previewModel.getCurrentFillValues();
	}

	public int getDisplayedLayerIndex()
	{
		if (previewModel== null) return currentLayerIndex;
		return previewModel.getDisplayedLayerIndex();
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(G1 image)
	{
		this.image = image;
		if (previewModel == null) return;
		previewModel.setImage(image);
	}
	
	public void setSmallBodyModel(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
	}
}
