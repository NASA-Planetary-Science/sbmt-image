package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;

import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class CameraOrientationPipeline
{
	private double[] focalPoint = new double[3];
	double[] spacecraftPosition = new double[3];
	double[] boresightDirection = new double[3];
	double[] upVector = new double[3];
	double[] quaternion = new double[4];

	public CameraOrientationPipeline(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels, double[] boundingBox) throws Exception
	{
		PointingFileReader infoReader = renderableImage.getPointing();
		spacecraftPosition = infoReader.getSpacecraftPosition();
    	boresightDirection = infoReader.getBoresightDirection();
    	upVector = infoReader.getUpVector();

		// Normalize the direction vector
		double[] direction = new double[3];
		MathUtil.unorm(boresightDirection, direction);

		int cellId = smallBodyModels.get(0).computeRayIntersection(spacecraftPosition, direction, focalPoint);

		if (cellId < 0)
		{
			if (boundingBox == null)
			{
				RenderablePointedImageFootprintGeneratorPipeline pipeline =
					new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
				boundingBox = pipeline.getFootprintPolyData().get(0).GetBounds();
			}
//			BoundingBox bb = new BoundingBox(pipeline.getFootprintPolyData().get(0).GetBounds());
			BoundingBox bb = new BoundingBox(boundingBox);
			double[] centerPoint = bb.getCenterPoint();
			// double[] centerPoint = footprint[currentSlice].GetPoint(0);
			double distanceToCenter = MathUtil.distanceBetween(spacecraftPosition, centerPoint);


			focalPoint[0] = spacecraftPosition[0] + distanceToCenter * direction[0];
			focalPoint[1] = spacecraftPosition[1] + distanceToCenter * direction[1];
			focalPoint[2] = spacecraftPosition[2] + distanceToCenter * direction[2];
		}

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

	}

	public static CameraOrientationPipeline of(RenderablePointedImage renderableImage, List<SmallBodyModel> smallBodyModels, double[] boundingBox) throws Exception
	{
		return new CameraOrientationPipeline(renderableImage, smallBodyModels, boundingBox);
	}

	public double[] getCameraOrientation()
	{
		return focalPoint;
	}

	public double[] getFocalPoint()
	{
		return focalPoint;
	}

	public double[] getSpacecraftPosition()
	{
		return spacecraftPosition;
	}

	public double[] getBoresightDirection()
	{
		return boresightDirection;
	}

	public double[] getUpVector()
	{
		return upVector;
	}

	public double[] getQuaternion()
	{
		return quaternion;
	}

}
