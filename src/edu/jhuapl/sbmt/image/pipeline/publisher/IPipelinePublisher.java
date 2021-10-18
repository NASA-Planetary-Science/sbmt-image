package edu.jhuapl.sbmt.image.pipeline.publisher;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image.pipeline.IPipelineComponent;
import edu.jhuapl.sbmt.image.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image.pipeline.subscriber.IPipelineSubscriber;

public interface IPipelinePublisher<OutputType extends Object> extends IPipelineComponent
{
	public void publish() throws IOException, Exception;

	public IPipelinePublisher<OutputType> subscribe(IPipelineSubscriber<OutputType> subscriber);

	public <T extends Object> IPipelineOperator<OutputType, T> operate(IPipelineOperator<OutputType, T> operator);

	public List<OutputType> getOutputs();
}
