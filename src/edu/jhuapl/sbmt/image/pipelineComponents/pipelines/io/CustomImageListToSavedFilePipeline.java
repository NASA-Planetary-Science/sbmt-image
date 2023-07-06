package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export.SaveCustomImageListToFileOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class CustomImageListToSavedFilePipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{

	public CustomImageListToSavedFilePipeline(List<G1> images) throws IOException, Exception
	{
		Just.of(images)
			.operate(new SaveCustomImageListToFileOperator<>())
			.run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> CustomImageListToSavedFilePipeline<G1> of(List<G1> images) throws IOException, Exception
	{
		return new CustomImageListToSavedFilePipeline<G1>(images);
	}

}
