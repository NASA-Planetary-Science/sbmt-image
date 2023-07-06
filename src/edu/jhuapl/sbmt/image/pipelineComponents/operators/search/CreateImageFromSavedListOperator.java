package edu.jhuapl.sbmt.image.pipelineComponents.operators.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image.model.ImageOrigin;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class CreateImageFromSavedListOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Triple<List<List<String>>, ImagingInstrument, List<String>>, G1>
{
	@Override
	public void processData() throws IOException, Exception
	{
		List<List<String>> results = inputs.get(0).getLeft();
		IImagingInstrument instrument = inputs.get(0).getMiddle();
		List<String> infoBaseNames = inputs.get(0).getRight();
		outputs = new ArrayList<G1>();

		int i=1;
        for (List<String> imageInfo : results)
        {
        	PointingSource imageSource = PointingSource.valueFor(imageInfo.get(2).replace("_", " "));
        	PerspectiveImageMetadata image = new PerspectiveImageMetadata(imageInfo.get(0), instrument.getType(), imageSource, infoBaseNames.get(results.indexOf(imageInfo)), new double[] {});
        	Orientation orientation = instrument.getOrientation(imageSource);
        	image.setFlip(orientation.getFlip().flip());
        	image.setRotation(orientation.getRotation());
        	image.setImageOrigin(ImageOrigin.SERVER);
        	image.setLinearInterpolatorDims(instrument.getLinearInterpolationDims());
        	image.setMaskValues(instrument.getMaskValues());
        	image.setFillValues(instrument.getFillValues());
        	image.setLongTime(Long.parseLong(imageInfo.get(1)));
        	CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
        	compImage.setIndex(i++);
        	outputs.add((G1)compImage);
        }
	}
}
