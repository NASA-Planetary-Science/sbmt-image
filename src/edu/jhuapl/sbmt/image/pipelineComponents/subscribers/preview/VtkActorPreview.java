package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import vtk.vtkActor;

import edu.jhuapl.sbmt.image.ui.ActorPreviewPanel;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkActorPreview implements IPipelineSubscriber<vtkActor>
{
	private IPipelinePublisher<vtkActor> publisher;
	private String title;
	private HashMap<String, String> metadata;
	private boolean showContrast = false;

	public VtkActorPreview(String title, HashMap<String, String> metadata, boolean showContrast)
	{
		this.title = title;
		this.metadata = metadata;
	}

	@Override
	public void receive(List<vtkActor> items)
	{
		try
		{
			new ActorPreviewPanel(title, items.get(0), metadata, showContrast);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(vtkActor item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	@Override
	public void setPublisher(IPipelinePublisher<vtkActor> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkActorPreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}
}
