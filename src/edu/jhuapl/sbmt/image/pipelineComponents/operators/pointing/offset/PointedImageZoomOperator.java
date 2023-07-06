package edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PointedImageZoomOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{
	@Override
	public void processData() throws IOException, Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
		SpacecraftPointingState origState = inputPair.getLeft();

		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
		SpacecraftPointingDelta scDelta = inputPair.getRight();
		double zoomFactor = scDelta.getZoomFactor();
		if (zoomFactor == 1.0)
		{
			outputs.add(Pair.of(scState, scDelta));
			return;
		}
        double zoomRatio = 1.0 / zoomFactor;
        double range = MathUtil.vnorm(origState.getSpacecraftPosition());

//        System.out.println("PointedImageZoomOperator: processData: zoom factor " + zoomFactor + " zoom ratio " + zoomRatio);
//        System.out.println("PointedImageZoomOperator: processData: boresight dir " + new Vector3D(origState.getBoresightDirection()));
//        int nslices = image.getImageDepth();
//        int currentSlice = image.getCurrentSlice();
//        for (int slice = 0; slice < nslices; slice++)
//        {
            double[] surfacePoint = new double[3];

            for (int i = 0; i < 3; i++)
            {
            	surfacePoint[i] = origState.getSpacecraftPosition()[i] + range*origState.getBoresightDirection()[i];
            	scState.getSpacecraftPosition()[i] = surfacePoint[i] - range*origState.getBoresightDirection()[i] * zoomRatio;
            }
//            resetFrustaAndFootprint(slice);
//        }
//        System.out.println("PointedImageZoomOperator: processData: zoomed state " + scState);

		outputs.add(Pair.of(scState, scDelta));
	}
}
