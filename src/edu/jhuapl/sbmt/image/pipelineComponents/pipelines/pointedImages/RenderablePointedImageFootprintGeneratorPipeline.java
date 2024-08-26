package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageFootprintOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import vtk.vtkImageData;
import vtk.vtkPolyData;

public class RenderablePointedImageFootprintGeneratorPipeline
{
	Pair<List<vtkImageData>, List<vtkPolyData>>[] outputs = new Pair[1];

	public RenderablePointedImageFootprintGeneratorPipeline(IRenderableImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this(image, smallBodyModels, false);
	}

	public RenderablePointedImageFootprintGeneratorPipeline(IRenderableImage image, List<SmallBodyModel> smallBodyModels, boolean useModifiedPointing) throws IOException, Exception
	{
		Just.of(image)
			.operate(new RenderablePointedImageFootprintOperator(smallBodyModels, useModifiedPointing))
			.subscribe(PairSink.of(outputs))
			.run();
	}

	public List<vtkPolyData> getFootprintPolyData()
	{
		if (outputs[0] == null) return Lists.newArrayList();
		return outputs[0].getRight();
	}

	public List<vtkImageData> getImageData()
	{
		if (outputs[0] == null) return Lists.newArrayList();
		return outputs[0].getLeft();
	}
}
