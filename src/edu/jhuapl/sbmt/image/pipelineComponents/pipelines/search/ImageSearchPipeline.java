package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.search;

import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.search.CreateImageFromSearchResultOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.search.ImageSearchOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class ImageSearchPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	List<G1> images = Lists.newArrayList();

	public ImageSearchPipeline(ImagingInstrumentConfig config, ModelManager modelManager, PickManager aPickManager, ImageSearchParametersModel searchParamatersModel) throws Exception
	{
		Just.of(searchParamatersModel)
			.operate(new ImageSearchOperator(config, modelManager, aPickManager))
			.operate(new CreateImageFromSearchResultOperator<G1>())
			.subscribe(Sink.of(images))
			.run();
	}

	public List<G1> getImages()
	{
		return images;
	}
}