package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.model.image.InfoFileReader;

public class RenderableImageGenerator extends BasePipelineOperator<Triple<Layer, HashMap<String, String>, InfoFileReader>, RenderableImage>
{


	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<RenderableImage>();
		for (Triple<Layer, HashMap<String, String>, InfoFileReader> input : inputs)
		{
			outputs.add(new RenderableImage(input.getLeft(),
											input.getMiddle(),
											input.getRight()));
		}
	}
}
