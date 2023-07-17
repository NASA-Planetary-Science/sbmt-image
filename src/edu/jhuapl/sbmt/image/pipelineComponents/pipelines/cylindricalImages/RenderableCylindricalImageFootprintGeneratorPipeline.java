package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage.RenderableCylindricalImageFootprintOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RenderableCylindricalImageFootprintGeneratorPipeline
{
	List<vtkPolyData> footprints = Lists.newArrayList();

	public RenderableCylindricalImageFootprintGeneratorPipeline(IRenderableImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		Just.of(image)
			.operate(new RenderableCylindricalImageFootprintOperator(smallBodyModels))
			.subscribe(Sink.of(footprints))
			.run();
	}

	public List<vtkPolyData> getFootprintPolyData()
	{
		return footprints;
	}
}
