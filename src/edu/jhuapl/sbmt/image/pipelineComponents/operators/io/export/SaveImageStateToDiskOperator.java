package edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.gson.Serializers;

public class SaveImageStateToDiskOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<G1, File>
{
	@Override
	public void processData() throws IOException, Exception
	{
		G1 image = inputs.get(0);
		File cachedFile = FileCache.getFileFromServer(image.getFilename());
		String fileWithoutExtension = FilenameUtils.removeExtension(cachedFile.getAbsolutePath());
		File outputFile = new File(fileWithoutExtension + ".metadata");
		Metadata metadata = InstanceGetter.defaultInstanceGetter().providesMetadataFromGenericObject(PerspectiveImageMetadata.class).provide(image.getImages().get(0));
		Serializers.serialize("ImageMetadata", metadata, outputFile);
		outputs.add(outputFile);
	}
}
