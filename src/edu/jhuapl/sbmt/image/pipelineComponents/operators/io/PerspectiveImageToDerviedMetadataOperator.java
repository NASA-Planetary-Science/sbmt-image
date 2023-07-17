package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.ImageIllumination;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.ImagePixelScale;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class PerspectiveImageToDerviedMetadataOperator extends BasePipelineOperator<Triple<RenderablePointedImage, ImagePixelScale, ImageIllumination>, HashMap<String, String>>
{
	HashMap<String, String> properties;
	RenderablePointedImage image;
	ImagePixelScale pixelScale;
	ImageIllumination illumination;
	PointingFileReader pointing;

	@Override
	public void processData() throws IOException, Exception
	{
		image = inputs.get(0).getLeft();
		pointing = image.getPointing();
		pixelScale = inputs.get(0).getMiddle();
		illumination = inputs.get(0).getRight();
		generateMetadata();
		outputs.add(properties);
	}

	private void generateMetadata()
	{
		properties = new LinkedHashMap<String, String>();
		HashMap<String, String> imageMetadata = image.getMetadata();

		DecimalFormat df = new DecimalFormat("#.######");

		properties.put("Name", imageMetadata.get("Name"));
		properties.put("Start Time", imageMetadata.get("Start Time"));
		properties.put("Stop Time", imageMetadata.get("Stop Time"));
		properties.put("Spacecraft Distance", df.format(MathUtil.vnorm(pointing.getSpacecraftPosition())) + " km");
		properties.put("Spacecraft Position",
				df.format(pointing.getSpacecraftPosition()[0]) + ", "
						+ df.format(pointing.getSpacecraftPosition()[1]) + ", "
						+ df.format(pointing.getSpacecraftPosition()[2]) + " km");

		double[] quaternion = getQuaternion(pointing.getUpVector(), pointing.getBoresightDirection());
		properties.put("Spacecraft Orientation (quaternion)", "(" + df.format(quaternion[0]) + ", ["
				+ df.format(quaternion[1]) + ", " + df.format(quaternion[2]) + ", " + df.format(quaternion[3]) + "])");
		double[] sunVectorAdjusted = pointing.getSunPosition();
		properties.put("Sun Vector", df.format(sunVectorAdjusted[0]) + ", " + df.format(sunVectorAdjusted[1]) + ", "
				+ df.format(sunVectorAdjusted[2]));

		//TODO FIX THIS
//		if (getCameraName() != null)
//			properties.put("Camera", getCameraName());
//		if (getFilterName() != null)
//			properties.put("Filter", getFilterName());

		// Note \u00B2 is the unicode superscript 2 symbol
		String ss2 = "\u00B2";
		properties.put("Footprint Surface Area", df.format(illumination.getSurfaceArea()) + " km" + ss2);

		// Note \u00B0 is the unicode degree symbol
		String deg = "\u00B0";
		properties.put("FOV",
				df.format(illumination.getHorizFovAngle()) + deg + " x " + df.format(illumination.getVerticalFovAngle()) + deg);

		properties.put("Minimum Incidence", df.format(illumination.getMinIncidence()) + deg);
		properties.put("Maximum Incidence", df.format(illumination.getMaxIncidence()) + deg);
		properties.put("Minimum Emission", df.format(illumination.getMinEmission()) + deg);
		properties.put("Maximum Emission", df.format(illumination.getMaxEmission()) + deg);
		properties.put("Minimum Phase", df.format(illumination.getMinPhase()) + deg);
		properties.put("Maximum Phase", df.format(illumination.getMaxPhase()) + deg);
		properties.put("Minimum Horizontal Pixel Scale",
				df.format(1000.0 * pixelScale.getMinHorizontalPixelScale()) + " meters/pixel");
		properties.put("Maximum Horizontal Pixel Scale",
				df.format(1000.0 * pixelScale.getMaxHorizontalPixelScale()) + " meters/pixel");
		properties.put("Minimum Vertical Pixel Scale",
				df.format(1000.0 * pixelScale.getMinVerticalPixelScale()) + " meters/pixel");
		properties.put("Maximum Vertical Pixel Scale",
				df.format(1000.0 * pixelScale.getMaxVerticalPixelScale()) + " meters/pixel");

	}

	private double[] getQuaternion(double[] upVector, double[] boresightDirection)
	{
		double[] quaternion = new double[4];
		//quaternion calculation
		double[] cx = upVector;
        double[] cz = new double[3];
        MathUtil.unorm(boresightDirection, cz);

        double[] cy = new double[3];
        MathUtil.vcrss(cz, cx, cy);

        double[][] m = {
                { cx[0], cx[1], cx[2] },
                { cy[0], cy[1], cy[2] },
                { cz[0], cz[1], cz[2] }
        };

        Rotation rotation = new Rotation(m, 1.0e-6);
        quaternion[0] = rotation.getQ0();
        quaternion[1] = rotation.getQ1();
        quaternion[2] = rotation.getQ2();
        quaternion[3] = rotation.getQ3();
        return quaternion;
	}
}