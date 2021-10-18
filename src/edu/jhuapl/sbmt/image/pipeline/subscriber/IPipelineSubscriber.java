package edu.jhuapl.sbmt.image.pipeline.subscriber;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image.pipeline.IPipelineComponent;
import edu.jhuapl.sbmt.image.pipeline.publisher.IPipelinePublisher;

public interface IPipelineSubscriber<InputType extends Object> extends IPipelineComponent
{

	public void receive(List<InputType> items) throws IOException, Exception;


	public void setPublisher(IPipelinePublisher<InputType> publisher);
}
