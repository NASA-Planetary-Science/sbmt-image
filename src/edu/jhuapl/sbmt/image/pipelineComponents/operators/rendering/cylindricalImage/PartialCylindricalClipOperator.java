package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage;

import java.io.IOException;

import vtk.vtkAppendPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkFloatArray;
import vtk.vtkPlane;
import vtk.vtkPoints;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class PartialCylindricalClipOperator extends BasePipelineOperator<vtkPolyData, vtkPolyData>
{
	private double lllat;
	private double urlat;
	private double lllon;
	private double urlon;

	public PartialCylindricalClipOperator(double lllat, double lllon, double urlat, double urlon)
	{
		this.lllon = lllon;
		this.urlon = urlon;
		this.lllat = lllat;
		this.urlat = urlat;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkPolyData smallBodyPolyData = inputs.get(0);
		// Divide along the zero longitude line and compute
        // texture coordinates for each half separately, then combine the two.
        // This avoids having a "seam" at the zero longitude line.
        final double[] origin = {0.0, 0.0, 0.0};
        final double[] zaxis = {0.0, 0.0, 1.0};

        // Make sure longitudes are in the interval [0, 360) by converting to
        // rectangular and back to longitude (which puts longitude in interval
        // [-180, 180]) and then adding 360 if longitude is less than zero.
        lllon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, lllon*Math.PI/180.0, 1.0))).lon*180.0/Math.PI;
        urlon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, urlon*Math.PI/180.0, 1.0))).lon*180.0/Math.PI;
        if (lllon < 0.0)
            lllon += 360.0;
        if (urlon < 0.0)
            urlon += 360.0;
        if (lllon >= 360.0)
            lllon = 0.0;
        if (urlon >= 360.0)
            urlon = 0.0;

        vtkPlane planeZeroLon = new vtkPlane();
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, 0.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            planeZeroLon.SetOrigin(origin);
            planeZeroLon.SetNormal(normal);
        }

        // First do the hemisphere from longitude 0 to 180.
        boolean needToGenerateTextures0To180 = true;
        vtkClipPolyData clipPolyData1 = new vtkClipPolyData();
        clipPolyData1.SetClipFunction(planeZeroLon);
        clipPolyData1.SetInputData(smallBodyPolyData);
        clipPolyData1.SetInsideOut(1);
        clipPolyData1.Update();
        vtkPolyData clipPolyData1Output = clipPolyData1.GetOutput();
        vtkPolyData clipPolyData1Hemi = clipPolyData1Output;
        if (lllon > 0.0 && lllon < 180.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, lllon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane1 = new vtkPlane();
            plane1.SetOrigin(origin);
            plane1.SetNormal(normal);

            clipPolyData1 = new vtkClipPolyData();
            clipPolyData1.SetClipFunction(plane1);
            clipPolyData1.SetInputData(clipPolyData1Output);
            clipPolyData1.SetInsideOut(1);
            clipPolyData1.Update();
            clipPolyData1Output = clipPolyData1.GetOutput();
        }
        if (urlon > 0.0 && urlon < 180.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, urlon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane1 = new vtkPlane();
            plane1.SetOrigin(origin);
            plane1.SetNormal(normal);

            // If the following condition is true, that means there are 2 disjoint pieces in the
            // hemisphere, so we'll need to append the two together.
            if (lllon > 0.0 && lllon < 180.0 && urlon <= lllon)
            {
                clipPolyData1 = new vtkClipPolyData();
                clipPolyData1.SetClipFunction(plane1);
                clipPolyData1.SetInputData(clipPolyData1Hemi);
                clipPolyData1.Update();
                vtkPolyData clipOutput = clipPolyData1.GetOutput();

                generateTextureCoordinates(clipPolyData1Output, true, false);
                generateTextureCoordinates(clipOutput, false, true);
                needToGenerateTextures0To180 = false;

                vtkAppendPolyData appendFilter = new vtkAppendPolyData();
                appendFilter.UserManagedInputsOff();
                appendFilter.AddInputData(clipPolyData1Output);
                appendFilter.AddInputData(clipOutput);
                appendFilter.Update();
                clipPolyData1Output = appendFilter.GetOutput();
            }
            else
            {
                clipPolyData1 = new vtkClipPolyData();
                clipPolyData1.SetClipFunction(plane1);
                clipPolyData1.SetInputData(clipPolyData1Output);
                clipPolyData1.Update();
                clipPolyData1Output = clipPolyData1.GetOutput();
            }
        }


        // Next do the hemisphere from longitude 180 to 360.
        boolean needToGenerateTextures180To0 = true;
        vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
        clipPolyData2.SetClipFunction(planeZeroLon);
        clipPolyData2.SetInputData(smallBodyPolyData);
        clipPolyData2.Update();
        vtkPolyData clipPolyData2Output = clipPolyData2.GetOutput();
        vtkPolyData clipPolyData2Hemi = clipPolyData2Output;
        if (lllon > 180.0 && lllon < 360.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, lllon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane2 = new vtkPlane();
            plane2.SetOrigin(origin);
            plane2.SetNormal(normal);

            clipPolyData2 = new vtkClipPolyData();
            clipPolyData2.SetClipFunction(plane2);
            clipPolyData2.SetInputData(clipPolyData2Output);
            clipPolyData2.SetInsideOut(1);
            clipPolyData2.Update();
            clipPolyData2Output = clipPolyData2.GetOutput();
        }
        if (urlon > 180.0 && urlon < 360.0)
        {
            double[] vec = MathUtil.latrec(new LatLon(0.0, urlon*Math.PI/180.0, 1.0));
            double[] normal = new double[3];
            MathUtil.vcrss(vec, zaxis, normal);
            vtkPlane plane2 = new vtkPlane();
            plane2.SetOrigin(origin);
            plane2.SetNormal(normal);

            // If the following condition is true, that means there are 2 disjoint pieces in the
            // hemisphere, so we'll need to append the two together.
            if (lllon > 180.0 && lllon < 360.0 && urlon <= lllon)
            {
                clipPolyData2 = new vtkClipPolyData();
                clipPolyData2.SetClipFunction(plane2);
                clipPolyData2.SetInputData(clipPolyData2Hemi);
                clipPolyData2.Update();
                vtkPolyData clipOutput = clipPolyData2.GetOutput();

                generateTextureCoordinates(clipPolyData2Output, true, false);
                generateTextureCoordinates(clipOutput, false, true);
                needToGenerateTextures180To0 = false;

                vtkAppendPolyData appendFilter = new vtkAppendPolyData();
                appendFilter.UserManagedInputsOff();
                appendFilter.AddInputData(clipPolyData2Output);
                appendFilter.AddInputData(clipOutput);
                appendFilter.Update();
                clipPolyData2Output = appendFilter.GetOutput();
            }
            else
            {
                clipPolyData2 = new vtkClipPolyData();
                clipPolyData2.SetClipFunction(plane2);
                clipPolyData2.SetInputData(clipPolyData2Output);
                clipPolyData2.Update();
                clipPolyData2Output = clipPolyData2.GetOutput();
            }
        }

        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        appendFilter.UserManagedInputsOff();
        // We may not need to include both hemispheres. Test to see
        // if the texture is contained in each hemisphere.
        if (doLongitudeIntervalsIntersect(0.0, 180.0, lllon, urlon))
        {
            if (needToGenerateTextures0To180)
            {
                boolean isOnLeftSide = false;
                boolean isOnRightSide = false;
                if (lllon >= 0.0 && lllon < 180.0)
                    isOnLeftSide = true;
                if (urlon > 0.0 && urlon <= 180.0)
                    isOnRightSide = true;
                generateTextureCoordinates(clipPolyData1Output, isOnLeftSide, isOnRightSide);
            }
            appendFilter.AddInputData(clipPolyData1Output);
        }
        if (doLongitudeIntervalsIntersect(180.0, 0.0, lllon, urlon))
        {
            if (needToGenerateTextures180To0)
            {
                boolean isOnLeftSide = false;
                boolean isOnRightSide = false;
                if (lllon >= 180.0)
                    isOnLeftSide = true;
                if (urlon > 180.0 || urlon == 0.0)
                    isOnRightSide = true;
                generateTextureCoordinates(clipPolyData2Output, isOnLeftSide, isOnRightSide);
            }
            appendFilter.AddInputData(clipPolyData2Output);
        }
        appendFilter.Update();
        outputs.add(appendFilter.GetOutput());
	}

	/**
     * Generates the cylindrical projection texture coordinates for the polydata.
     * If isOnLeftSide is true, that means the polydata borders the left side (the side of lllon) of the image.
     * If isOnRightSide is true, that means the polydata borders the right side (the side of urlon) of the image.
     * @param polydata
     * @param isOnLeftSide
     * @param isOnRightSide
     */
    protected void generateTextureCoordinates(
            vtkPolyData polydata,
            boolean isOnLeftSide,
            boolean isOnRightSide)
    {
        double lllat = this.lllat * (Math.PI / 180.0);
        double lllon = this.lllon * (Math.PI / 180.0);
        double urlat = this.urlat * (Math.PI / 180.0);
        double urlon = this.urlon * (Math.PI / 180.0);

        // Make sure longitudes are in the interval [0, 2*PI) by converting to
        // rectangular and back to longitude (which puts longitude in interval
        // [-PI, PI]) and then adding 2*PI if longitude is less than zero.
        lllon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, lllon, 1.0))).lon;
        urlon = MathUtil.reclat(MathUtil.latrec(new LatLon(0.0, urlon, 1.0))).lon;
        if (lllon < 0.0)
            lllon += 2.0*Math.PI;
        if (urlon < 0.0)
            urlon += 2.0*Math.PI;
        if (lllon >= 2.0*Math.PI)
            lllon = 0.0;
        if (urlon >= 2.0*Math.PI)
            urlon = 0.0;

        vtkFloatArray textureCoords = new vtkFloatArray();

        int numberOfPoints = polydata.GetNumberOfPoints();

        textureCoords.SetNumberOfComponents(2);
        textureCoords.SetNumberOfTuples(numberOfPoints);

        vtkPoints points = polydata.GetPoints();

        double xsize = getDistanceBetweenLongitudes(lllon, urlon);
        // If lower left and upper right longitudes are the same, that
        // means the image extends 360 degrees around the shape model.
        if (xsize == 0.0)
            xsize = 2.0*Math.PI;
        double ysize = urlat - lllat;
        for (int i=0; i<numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);

            LatLon ll = MathUtil.reclat(pt);

    		double latitude = ll.lat;
    		double longitude = ll.lon;

            if (longitude < 0.0)
               longitude += (2.0 * Math.PI);
            if (longitude >= 2.0 * Math.PI)
                longitude = 0.0;

            double dist = getDistanceBetweenLongitudes(lllon, longitude);
            if (isOnLeftSide)
            {
                if (Math.abs(2.0*Math.PI - dist) < 1.0e-2)
                    dist = 0.0;
            }
            else if (isOnRightSide)
            {
                if (Math.abs(dist) < 1.0e-2)
                    dist = xsize;
            }

            double u = dist / xsize;
            double v = (latitude - lllat) / ysize;

            if (u < 0.0) u = 0.0;
            else if (u > 1.0) u = 1.0;
            if (v < 0.0) v = 0.0;
            else if (v > 1.0) v = 1.0;
            textureCoords.SetTuple2(i, u, v);
        }

        polydata.GetPointData().SetTCoords(textureCoords);
    }

	 /**
     * Returns if the two longitudinal intervals intersect at all. If they intersect at
     * a point, (e.g. one interval goes from 1 to 2 and the second goes from 2 to 3), false
     * is returned.
     * @param lower1
     * @param upper1
     * @param lower2
     * @param upper2
     * @return
     */
    private boolean doLongitudeIntervalsIntersect(double lower1, double upper1, double lower2, double upper2)
    {
        if (lower1 == lower2 || upper1 == upper2 || lower1 == upper1 || lower2 == upper2)
            return true;

        // First test if lower2 or upper2 is contained in the first interval
        double dist1 = getDistanceBetweenLongitudesDegrees(lower1, upper1);
        double d = getDistanceBetweenLongitudesDegrees(lower1, lower2);
        if (d > 0.0 && d < dist1)
            return true;
        d = getDistanceBetweenLongitudesDegrees(lower1, upper2);
        if (d > 0.0 && d < dist1)
            return true;

        // Then test if lower1 or upper1 is contained in the second interval
        double dist2 = getDistanceBetweenLongitudesDegrees(lower2, upper2);
        d = getDistanceBetweenLongitudesDegrees(lower2, lower1);
        if (d > 0.0 && d < dist2)
            return true;
        d = getDistanceBetweenLongitudesDegrees(lower2, upper1);
        if (d > 0.0 && d < dist2)
            return true;

        return false;
    }

    /**
     * Assuming leftLon and rightLon are within interval [0, 2*PI], return
     * the distance between the two assuming leftLon is at a lower lon
     * than rightLon. Thus the returned result is always positive within
     * interval [0, 2*PI].
     * @param leftLon
     * @param rightLon
     * @return distance in radians
     */
    private double getDistanceBetweenLongitudes(double leftLon, double rightLon)
    {
        double dist = rightLon - leftLon;
        if (dist >= 0.0)
            return dist;
        else
            return dist + 2.0 * Math.PI;
    }

    /**
     * Same as previous but returns distance in degrees
     */
    private double getDistanceBetweenLongitudesDegrees(double leftLon, double rightLon)
    {
        double dist = rightLon - leftLon;
        if (dist >= 0.0)
            return dist;
        else
            return dist + 360.0;
    }
}
