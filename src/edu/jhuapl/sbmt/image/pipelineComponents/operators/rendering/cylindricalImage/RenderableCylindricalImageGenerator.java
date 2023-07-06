package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class RenderableCylindricalImageGenerator extends BasePipelineOperator<Triple<Layer, HashMap<String, String>, CylindricalBounds>, RenderableCylindricalImage>
{


	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<RenderableCylindricalImage>();
		for (Triple<Layer, HashMap<String, String>, CylindricalBounds> input : inputs)
		{
			outputs.add(new RenderableCylindricalImage(input.getLeft(),
											input.getMiddle(),
											input.getRight()));
		}
	}
}
