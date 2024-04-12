package edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing;

import java.util.List;

import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;
import edu.jhuapl.sbmt.pointing.io.LabelFileReader;

public class LabelfileReaderPublisher extends BasePipelinePublisher<LabelFileReader>
{
	public LabelfileReaderPublisher(String filename)
	{
		LabelFileReader reader = new LabelFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
