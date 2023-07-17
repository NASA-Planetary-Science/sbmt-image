package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class RenderablePointedImageGenerator extends BasePipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage>
{


	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<RenderablePointedImage>();
		for (Triple<Layer, HashMap<String, String>, PointingFileReader> input : inputs)
		{
			outputs.add(new RenderablePointedImage(input.getLeft(),
											input.getMiddle(),
											input.getRight()));
		}
	}
}
