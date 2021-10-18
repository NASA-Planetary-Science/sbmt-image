package edu.jhuapl.sbmt.image.pipeline.operator;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image.pipeline.IPipelineComponent;
import edu.jhuapl.sbmt.image.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image.pipeline.subscriber.IPipelineSubscriber;

public interface IPipelineOperator<InputType extends Object, OutputType extends Object> extends IPipelinePublisher<OutputType>, IPipelineSubscriber<InputType>, IPipelineComponent
{
	public void processData() throws IOException, Exception;

	public List<OutputType> getOutputs();

}
