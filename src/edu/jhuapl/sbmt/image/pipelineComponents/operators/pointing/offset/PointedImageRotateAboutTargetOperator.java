package edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PointedImageRotateAboutTargetOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{
	@Override
	public void processData() throws IOException, Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
		SpacecraftPointingState origState = inputPair.getLeft();
		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
		SpacecraftPointingDelta scDelta = inputPair.getRight();
		double angleDegrees = scDelta.getRotationOffset();

        Vector3D axis = new Vector3D(origState.getBoresightDirection());
        Rotation rotation = new Rotation(axis, Math.toRadians(angleDegrees), RotationConvention.VECTOR_OPERATOR);

    	MathUtil.rotateVector(origState.getFrustum1(), rotation, scState.getFrustum1());
        MathUtil.rotateVector(origState.getFrustum2(), rotation, scState.getFrustum2());
        MathUtil.rotateVector(origState.getFrustum3(), rotation, scState.getFrustum3());
        MathUtil.rotateVector(origState.getFrustum4(), rotation, scState.getFrustum4());
        MathUtil.rotateVector(origState.getBoresightDirection(), rotation, scState.getBoresightDirection());

		outputs.add(Pair.of(scState, scDelta));
	}
}
