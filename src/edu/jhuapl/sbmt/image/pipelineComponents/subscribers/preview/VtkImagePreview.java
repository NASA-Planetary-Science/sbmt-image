package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import vtk.vtkImageData;

import edu.jhuapl.sbmt.image.ui.ImagePreviewPanel;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class VtkImagePreview implements IPipelineSubscriber<vtkImageData>
{
	private IPipelinePublisher<vtkImageData> publisher;
	private String title;
	private HashMap<String, String> metadata;
	private boolean showContrast = false;

	public VtkImagePreview(String title, HashMap<String, String> metadata, boolean showContrast)
	{
		this.title = title;
		this.metadata = metadata;
	}

	@Override
	public void receive(List<vtkImageData> items)
	{
		try
		{
			new ImagePreviewPanel(title, items.get(0), metadata, showContrast);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive(vtkImageData item) throws IOException, Exception
	{
		receive(List.of(item));
	}

	@Override
	public void setPublisher(IPipelinePublisher<vtkImageData> publisher)
	{
		this.publisher = publisher;
	}

	@Override
	public VtkImagePreview run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}
}
