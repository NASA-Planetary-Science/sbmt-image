package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.awt.Container;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image.ui.LayerRendererPanel;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkLayerRenderer implements IPipelineSubscriber<Layer>
{
	private IPipelinePublisher<Layer> publisher;
	private LayerRendererPanel preview;
	private boolean invertY;

	public VtkLayerRenderer(boolean invertY)
	{
		this.invertY = invertY;
	}

	@Override
	public void receive(List<Layer> items)
	{
		try
		{
			preview = new LayerRendererPanel(items.get(0), invertY);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(Layer item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	public Container getPanel()
	{
		return preview.getContentPane();
	}

	@Override
	public void setPublisher(IPipelinePublisher<Layer> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkLayerRenderer run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}

//	@Override
//	public VtkLayerRenderer run(Runnable completion) throws IOException, Exception
//	{
//		publisher.run();
//		completion.run();
//		return this;
//	}
}
