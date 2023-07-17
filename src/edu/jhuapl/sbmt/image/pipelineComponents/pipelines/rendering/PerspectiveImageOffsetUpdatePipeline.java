package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

public class PerspectiveImageOffsetUpdatePipeline<G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable>
{
	PerspectiveImageOffsetUpdatePipeline(PerspectiveImageCollection<G1> collection, G1 image, double offset)
	{
		image.setOffset(offset);
		collection.updateImage(image);
	}

	public static <G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable> void of(PerspectiveImageCollection<G1> collection, G1 image, double offset)
	{
		new PerspectiveImageOffsetUpdatePipeline<G1>(collection, image, offset);
	}
}
