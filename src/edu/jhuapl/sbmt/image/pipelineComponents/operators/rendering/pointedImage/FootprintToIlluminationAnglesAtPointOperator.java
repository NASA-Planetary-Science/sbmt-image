package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class FootprintToIlluminationAnglesAtPointOperator extends BasePipelineOperator<Triple<PointingFileReader, double[], double[]>, ImageIlluminationAtPoint>
{
	ImageIlluminationAtPoint illumination;
	PointingFileReader pointing;
    double[] pt;
    double[] normal;

    public FootprintToIlluminationAnglesAtPointOperator()
	{
    	this.pointing = inputs.get(0).getLeft();
		this.pt = inputs.get(0).getMiddle();
		this.normal = inputs.get(0).getRight();
	}

	@Override
	public void processData() throws IOException, Exception
	{
		illumination = new ImageIlluminationAtPoint();
		double[] angles = computeIlluminationAnglesAtPoint(pt, normal);
		illumination.incidence = angles[0];
		illumination.emission = angles[1];
		illumination.phase = angles[2];
		outputs.add(illumination);
	}

	// Computes the incidence, emission, and phase at a point on the footprint with
    // a given normal.
    // (I.e. the normal of the plate which the point is lying on).
    // The output is a 3-vector with the first component equal to the incidence,
    // the second component equal to the emission and the third component equal to
    // the phase.
    double[] computeIlluminationAnglesAtPoint(double[] pt, double[] normal)
    {
    	double[] spacecraftPositionAdjusted = pointing.getSpacecraftPosition();
    	double[] scvec = {
        		spacecraftPositionAdjusted[0] - pt[0],
        		spacecraftPositionAdjusted[1] - pt[1],
        		spacecraftPositionAdjusted[2] - pt[2] };

        double[] sunVector = new double[3];
        MathUtil.vhat(pointing.getSunPosition(), sunVector);
        double incidence = MathUtil.vsep(normal, sunVector) * 180.0 / Math.PI;
        double emission = MathUtil.vsep(normal, scvec) * 180.0 / Math.PI;
        double phase = MathUtil.vsep(sunVector, scvec) * 180.0 / Math.PI;

        double[] angles = { incidence, emission, phase };

        return angles;
    }
}
