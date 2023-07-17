package edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PointedImageRotateTargetPixelDirToLocalOriginOperator extends BasePipelineOperator<Pair<SpacecraftPointingState, SpacecraftPointingDelta>, Pair<SpacecraftPointingState, SpacecraftPointingDelta>>
{


	@Override
	public void processData() throws IOException, Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputPair = inputs.get(0);
		SpacecraftPointingState origState = inputPair.getLeft();
		SpacecraftPointingState scState = new SpacecraftPointingState(origState);
		SpacecraftPointingDelta scDelta = inputPair.getRight();

		if (origState.getTargetPixelCoordinates()[0] != Double.MAX_VALUE || origState.getTargetPixelCoordinates()[1] != Double.MAX_VALUE)
		{
			outputs.add(Pair.of(scState, scDelta));
			return;
		}

		int height = origState.getImageHeight();
        double line = height - 1 - origState.getTargetPixelCoordinates()[0];
        double sample = origState.getTargetPixelCoordinates()[1];

        double[] newTargetPixelDirection = getPixelDirection(origState, sample, line);

		Vector3D directionVector = new Vector3D(newTargetPixelDirection);
        Vector3D spacecraftPositionVector = new Vector3D(origState.getSpacecraftPosition());
        Vector3D spacecraftToOriginVector = spacecraftPositionVector.scalarMultiply(-1.0);
        Vector3D originPointingVector = spacecraftToOriginVector.normalize();

        Rotation rotation = new Rotation(directionVector, originPointingVector);

        // int slice = getCurrentSlice();
//        int nslices = image.getImageDepth();
//        for (int slice = 0; slice < nslices; slice++)
//        {
        	MathUtil.rotateVector(origState.getFrustum1(), rotation, scState.getFrustum1());
            MathUtil.rotateVector(origState.getFrustum2(), rotation, scState.getFrustum2());
            MathUtil.rotateVector(origState.getFrustum3(), rotation, scState.getFrustum3());
            MathUtil.rotateVector(origState.getFrustum4(), rotation, scState.getFrustum4());
            MathUtil.rotateVector(origState.getBoresightDirection(), rotation, scState.getBoresightDirection());

//            image.getRendererHelper().resetFrustaAndFootprint(slice);
//        }



		outputs.add(Pair.of(scState, scDelta));
	}

	/**
     * Get the direction from the spacecraft of pixel with specified sample and
     * line. Note that sample is along image width and line is along image height.
     */
    public double[] getPixelDirection(SpacecraftPointingState origState, double sample, double line)
    {
    	double[] spacecraftPosition = origState.getSpacecraftPosition();
    	double[] frustum1 = origState.getFrustum1();
    	double[] frustum2 = origState.getFrustum2();
    	double[] frustum3 = origState.getFrustum3();

        double[] corner1 = {
        		spacecraftPosition[0] + frustum1[0],
        		spacecraftPosition[1] + frustum1[1],
        		spacecraftPosition[2] + frustum1[2]
        };
        double[] corner2 = {
        		spacecraftPosition[0] + frustum2[0],
        		spacecraftPosition[1] + frustum2[1],
        		spacecraftPosition[2] + frustum2[2]
        };
        double[] corner3 = {
        		spacecraftPosition[0] + frustum3[0],
        		spacecraftPosition[1] + frustum3[1],
        		spacecraftPosition[2] + frustum3[2]
        };
        double[] vec12 = {
                corner2[0] - corner1[0],
                corner2[1] - corner1[1],
                corner2[2] - corner1[2]
        };
        double[] vec13 = {
                corner3[0] - corner1[0],
                corner3[1] - corner1[1],
                corner3[2] - corner1[2]
        };

        // Compute the vector on the left of the row.
        double fracHeight = ((double) line / (double) (origState.getImageHeight() - 1));
        double[] left = {
                corner1[0] + fracHeight * vec13[0],
                corner1[1] + fracHeight * vec13[1],
                corner1[2] + fracHeight * vec13[2]
        };

        double fracWidth = ((double) sample / (double) (origState.getImageWidth() - 1));
        double[] dir = {
                left[0] + fracWidth * vec12[0],
                left[1] + fracWidth * vec12[1],
                left[2] + fracWidth * vec12[2]
        };
        dir[0] -= spacecraftPosition[0];
        dir[1] -= spacecraftPosition[1];
        dir[2] -= spacecraftPosition[2];
        MathUtil.unorm(dir, dir);

        return dir;
    }
}
