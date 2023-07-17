package edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing;

import java.util.List;

import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;
import edu.jhuapl.sbmt.pointing.io.InfoFileReader;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class InfofileReaderPublisher extends BasePipelinePublisher<PointingFileReader>
{
	public InfofileReaderPublisher(String filename)
	{
		InfoFileReader reader = new InfoFileReader(filename);
		reader.read();
		this.outputs = List.of(reader);
	}
}
