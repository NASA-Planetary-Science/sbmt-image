package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageRenderingState;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.LoadCustomImageListFromFileOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

public class LoadFileToCustomImageListPipeline<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	Pair<List<G1>, HashMap<G1, PerspectiveImageRenderingState<G1>>>[] results = new Pair[1];

	private LoadFileToCustomImageListPipeline(IImagingInstrument instrument, String customFolder) throws IOException, Exception
	{
		File file = CustomFileChooser.showOpenDialog(null, "Select File");
		if (file == null) return;
		Just.of(file)
			.operate(new LoadCustomImageListFromFileOperator<G1>(instrument, customFolder))
			.subscribe(PairSink.of(results))
			.run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> LoadFileToCustomImageListPipeline<G1> of(IImagingInstrument instrument, String customFolder) throws IOException, Exception
	{
		return new LoadFileToCustomImageListPipeline<G1>(instrument, customFolder);
	}

	public Pair<List<G1>, HashMap<G1, PerspectiveImageRenderingState<G1>>> getResults()
	{
		return results[0];
	}

}
