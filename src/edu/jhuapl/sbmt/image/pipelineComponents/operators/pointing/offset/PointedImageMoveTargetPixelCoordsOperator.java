package edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PointedImageMoveTargetPixelCoordsOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{


	@Override
	public void processData() throws IOException, Exception
	{
//		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
//		SpacecraftPointingState origState = inputPair.getLeft();
//		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
//		SpacecraftPointingDelta scDelta = inputPair.getRight();
//
//
//		double height = (double) image.getImageHeight();
//        if (targetPixelCoordinates[0] == Double.MAX_VALUE || targetPixelCoordinates[1] == Double.MAX_VALUE)
//        {
//            targetPixelCoordinates = image.getPixelFromPoint(image.bodyOrigin);
//            targetPixelCoordinates[0] = height - 1 - targetPixelCoordinates[0];
//        }
//
//        double line = this.targetPixelCoordinates[0] + pixelDelta[0];
//        double sample = targetPixelCoordinates[1] + pixelDelta[1];
//        double[] newFrustumCenterPixel = { line, sample };
//        scState.targetPixelCoordinates = newFrustumCenterPixel;
//
//		outputs.add(Pair.of(scState, scDelta));
	}
}
