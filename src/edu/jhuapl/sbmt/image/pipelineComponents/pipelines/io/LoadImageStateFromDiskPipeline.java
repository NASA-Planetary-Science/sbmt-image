package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.LoadImageStateFromDiskOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class LoadImageStateFromDiskPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	private LoadImageStateFromDiskPipeline(File metadataFile) throws IOException, Exception
	{
		Just.of(metadataFile)
			.subscribe(new LoadImageStateFromDiskOperator<G1>())
			.run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> void of(File file) throws IOException, Exception
	{
		LoadImageStateFromDiskPipeline<G1> pipeline = new LoadImageStateFromDiskPipeline<G1>(file);
	}
}
