package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.config.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.search.CreateImageFromSavedListOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.search.LoadImageListOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.search.SearchResultsToPointingFilesOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class LoadImagesFromSavedFilePipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	List<G1> images = Lists.newArrayList();

	public LoadImagesFromSavedFilePipeline(SmallBodyViewConfig viewConfig, String filename, ImagingInstrument instrument) throws Exception
	{
		Just.of(Pair.of(filename, instrument))
			.operate(new LoadImageListOperator())
			.operate(new SearchResultsToPointingFilesOperator(viewConfig))
			.operate(new CreateImageFromSavedListOperator<G1>())
			.subscribe(Sink.of(images))
			.run();
	}

	public List<G1> getImages()
	{
		return images;
	}
}
