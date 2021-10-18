package edu.jhuapl.sbmt.image.modules.pointing;

import java.util.List;

import edu.jhuapl.sbmt.image.pipeline.publisher.BasePipelinePublisher;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class InfofileReaderPublisher extends BasePipelinePublisher<InfoFileReader>
{

	public InfofileReaderPublisher(String filename)
	{
		InfoFileReader reader = new InfoFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
