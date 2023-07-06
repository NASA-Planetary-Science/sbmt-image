package edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview;

import java.awt.Container;
import java.io.IOException;
import java.util.List;

import vtk.vtkActor;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.ui.PointedImageEditingPanel;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.subscriber.IPipelineSubscriber;

public class PointedImageEditingWindow<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements IPipelineSubscriber<vtkActor>
{
	private IPipelinePublisher<vtkActor> publisher;
	private SmallBodyModel smallBodyModel;
	private PointedImageEditingPanel<G1> preview;
	private G1 image;

	public PointedImageEditingWindow(G1 image, SmallBodyModel smallBodyModel)
	{
		this.image = image;
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void receive(List<vtkActor> items)
	{
		try
		{
			preview = new PointedImageEditingPanel<G1>(image, smallBodyModel, items);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void receive(vtkActor item)
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
	public PointedImageEditingWindow<G1> run() throws IOException, Exception
	{
		publisher.run();
		return this;
	}
}