package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.awt.Container;
import java.io.IOException;
import java.util.List;

import vtk.vtkActor;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.ui.RendererPreviewPanel;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkRendererPreview implements IPipelineSubscriber<vtkActor>
{
	private IPipelinePublisher<vtkActor> publisher;
	private SmallBodyModel smallBodyModel;
	private RendererPreviewPanel preview;

	public VtkRendererPreview(SmallBodyModel smallBodyModel)
	{
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void receive(List<vtkActor> items)
	{
		try
		{
			preview = new RendererPreviewPanel(smallBodyModel, items);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void receive(vtkActor item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	public Container getPanel()
	{
		return preview.getContentPane();
	}

	@Override
	public void setPublisher(IPipelinePublisher<vtkActor> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkRendererPreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}
}