package edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PointedImageTranslateInImagePlaneOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{
	@Override
	public void processData() throws IOException, Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
		SpacecraftPointingState origState = inputPair.getLeft();
		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
		SpacecraftPointingDelta scDelta = inputPair.getRight();
		double sampleDelta = scDelta.getSampleOffset();
		double lineDelta = scDelta.getLineOffset();
		double[] sampleAxis = new double[] { 0.0, 0.0, 0.0 };
    	MathUtil.vsub(origState.getFrustum1(), origState.getFrustum2(), sampleAxis);
    	MathUtil.unorm(sampleAxis, sampleAxis);
    	double[] lineAxis = new double[] { 0.0, 0.0, 0.0 };
    	MathUtil.vsub(origState.getFrustum1(), origState.getFrustum3(), lineAxis);
    	MathUtil.unorm(lineAxis, lineAxis);
    	MathUtil.vscl(sampleDelta, sampleAxis, sampleAxis);
    	MathUtil.vadd(origState.getSpacecraftPosition(), sampleAxis, scState.getSpacecraftPosition());
    	MathUtil.vscl(lineDelta, lineAxis, lineAxis);
    	MathUtil.vadd(scState.getSpacecraftPosition(), lineAxis, scState.getSpacecraftPosition());

		outputs.add(Pair.of(scState, scDelta));
	}
}
