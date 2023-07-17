package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.gson.Serializers;

public class LoadImageStateFromDiskOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<File, G1>
{
	@Override
	public void processData() throws IOException, Exception
	{
		File metadataFile = inputs.get(0);
		FixedMetadata metadata = Serializers.deserialize(metadataFile, "ImageMetadata");
		Key<G1> PERSPECTIVE_IMAGE_KEY = Key.of("ImageMetadata");
		G1 perspectiveImage = InstanceGetter.defaultInstanceGetter().providesGenericObjectFromMetadata(PERSPECTIVE_IMAGE_KEY).provide(metadata);
		outputs.add(perspectiveImage);
	}
}
