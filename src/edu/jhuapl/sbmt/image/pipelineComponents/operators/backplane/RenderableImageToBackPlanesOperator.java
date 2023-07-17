package edu.jhuapl.sbmt.image.pipelineComponents.operators.backplane;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import vtk.vtkDataArray;
import vtk.vtkGenericCell;
import vtk.vtkImageData;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.util.BackplaneInfo;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.FootprintToIlluminationAnglesAtPointOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.ImageIlluminationAtPoint;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class RenderableImageToBackPlanesOperator extends BasePipelineOperator<Pair<RenderablePointedImage, SmallBodyModel>, float[]>
{
	boolean returnNullIfContainsLimb;
	private RenderablePointedImage image;
	public static final float PDS_NA = -ImageDataUtil.FILL_CUTOFF;

	public RenderableImageToBackPlanesOperator(boolean returnNullIfContainsLimb)
	{
		this.returnNullIfContainsLimb = returnNullIfContainsLimb;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		image = inputs.get(0).getLeft();
		SmallBodyModel smallBodyModel = inputs.get(0).getRight();
		PointingFileReader pointing = image.getPointing();
		//TODO fix this
		int numberOfBackplanes = 1;

		double[] spacecraftPositionAdjusted = pointing.getSpacecraftPosition();
    	double[] frustum1Adjusted = pointing.getFrustum1();
    	double[] frustum2Adjusted = pointing.getFrustum2();
    	double[] frustum3Adjusted = pointing.getFrustum3();
//    	int currentSlice = image.currentSlice;
        // We need to use cell normals not point normals for the calculations
        vtkDataArray normals = null;
        if (!returnNullIfContainsLimb)
            normals = smallBodyModel.getCellNormals();

        float[] data = new float[numberOfBackplanes * image.getImageHeight() * image.getImageWidth()];

        vtksbCellLocator cellLocator = smallBodyModel.getCellLocator();

        // vtkPoints intersectPoints = new vtkPoints();
        // vtkIdList intersectCells = new vtkIdList();
        vtkGenericCell cell = new vtkGenericCell();

        // For each pixel in the image we need to compute the vector
        // from the spacecraft pointing in the direction of that pixel.
        // To do this, for each row in the image compute the left and
        // right vectors of the entire row. Then for each pixel in
        // the row use the two vectors from either side to compute
        // the vector of that pixel.
        double[] corner1 = {
        		spacecraftPositionAdjusted[0] + frustum1Adjusted[0],
        		spacecraftPositionAdjusted[1] + frustum1Adjusted[1],
        		spacecraftPositionAdjusted[2] + frustum1Adjusted[2]
        };
        double[] corner2 = {
        		spacecraftPositionAdjusted[0] + frustum2Adjusted[0],
        		spacecraftPositionAdjusted[1] + frustum2Adjusted[1],
                spacecraftPositionAdjusted[2] + frustum2Adjusted[2]
        };
        double[] corner3 = {
        		spacecraftPositionAdjusted[0] + frustum3Adjusted[0],
        		spacecraftPositionAdjusted[1] + frustum3Adjusted[1],
        		spacecraftPositionAdjusted[2] + frustum3Adjusted[2]
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

        double horizScaleFactor = 2.0 * Math.tan(MathUtil.vsep(frustum1Adjusted, frustum3Adjusted) / 2.0) / image.getImageHeight();
        double vertScaleFactor = 2.0 * Math.tan(MathUtil.vsep(frustum1Adjusted, frustum2Adjusted) / 2.0) / image.getImageWidth();

        double scdist = MathUtil.vnorm(spacecraftPositionAdjusted);

        RenderablePointedImageFootprintGeneratorPipeline pipeline =
        		new RenderablePointedImageFootprintGeneratorPipeline(image, List.of(smallBodyModel));
        vtkImageData rawImage = pipeline.getImageData().get(0);

        for (int i = 0; i < image.getImageHeight(); ++i)
        {
            // Compute the vector on the left of the row.
            double fracHeight = ((double) i / (double) (image.getImageHeight() - 1));
            double[] left = {
                    corner1[0] + fracHeight * vec13[0],
                    corner1[1] + fracHeight * vec13[1],
                    corner1[2] + fracHeight * vec13[2]
            };

            for (int j = 0; j < image.getImageWidth(); ++j)
            {
                // If we're just trying to know if there is a limb, we
                // only need to do intersections around the boundary of
                // the backplane, not the interior pixels.
                if (returnNullIfContainsLimb)
                {
                    if (j == 1 && i > 0 && i < image.getImageHeight() - 1)
                    {
                        j = image.getImageWidth() - 2;
                        continue;
                    }
                }

                double fracWidth = ((double) j / (double) (image.getImageWidth() - 1));
                double[] vec = {
                        left[0] + fracWidth * vec12[0],
                        left[1] + fracWidth * vec12[1],
                        left[2] + fracWidth * vec12[2]
                };
                vec[0] -= spacecraftPositionAdjusted[0];
                vec[1] -= spacecraftPositionAdjusted[1];
                vec[2] -= spacecraftPositionAdjusted[2];
                MathUtil.unorm(vec, vec);

                double[] lookPt = {
                		spacecraftPositionAdjusted[0] + 2.0 * scdist * vec[0],
                		spacecraftPositionAdjusted[1] + 2.0 * scdist * vec[1],
                		spacecraftPositionAdjusted[2] + 2.0 * scdist * vec[2]
                };

                // cellLocator.IntersectWithLine(spacecraftPosition, lookPt, intersectPoints,
                // intersectCells);
                double tol = 1e-6;
                double[] t = new double[1];
                double[] x = new double[3];
                double[] pcoords = new double[3];
                int[] subId = new int[1];
                long[] cellId = new long[1];
                int result = cellLocator.IntersectWithLine(spacecraftPositionAdjusted, lookPt, tol, t, x, pcoords, subId, cellId, cell);

                // if (intersectPoints.GetNumberOfPoints() == 0)
                // System.out.println(i + " " + j + " " + intersectPoints.GetNumberOfPoints());

                // int numberOfPoints = intersectPoints.GetNumberOfPoints();

                if (result > 0)
                {
                    // If we're just trying to know if there is a limb, do not
                    // compute the values of the backplane (It will crash since
                    // we don't have normals of the asteroid itself)
                    if (returnNullIfContainsLimb)
                        continue;

                    // double[] closestPoint = intersectPoints.GetPoint(0);
                    // int closestCell = intersectCells.GetId(0);
                    double[] closestPoint = x;
                    int closestCell = (int)cellId[0];
                    double closestDist = MathUtil.distanceBetween(closestPoint, spacecraftPositionAdjusted);

                    /*
                     * // compute the closest point to the spacecraft of all the intersecting
                     * points. if (numberOfPoints > 1) { for (int k=1; k<numberOfPoints; ++k) {
                     * double[] pt = intersectPoints.GetPoint(k); double dist =
                     * GeometryUtil.distanceBetween(pt, spacecraftPosition); if (dist < closestDist)
                     * { closestDist = dist; closestCell = intersectCells.GetId(k); closestPoint =
                     * pt; } } }
                     */

                    LatLon llr = MathUtil.reclat(closestPoint);
                    double lat = llr.lat * 180.0 / Math.PI;
                    double lon = llr.lon * 180.0 / Math.PI;
                    if (lon < 0.0)
                        lon += 360.0;

                    double[] normal = normals.GetTuple3(closestCell);
                    List<ImageIlluminationAtPoint> illumAtts = Lists.newArrayList();
            		Just.of(Triple.of(pointing, closestPoint, normal))
            			.operate(new FootprintToIlluminationAnglesAtPointOperator())
            			.subscribe(Sink.of(illumAtts))
            			.run();

            		double[] illumAngles = new double[] {illumAtts.get(0).getIncidence(), illumAtts.get(0).getEmission(), illumAtts.get(0).getPhase()};
//                    double[] illumAngles = image.computeIlluminationAnglesAtPoint(closestPoint, normal);

                    double horizPixelScale = closestDist * horizScaleFactor;
                    double vertPixelScale = closestDist * vertScaleFactor;

                    double[] coloringValues;
                    try
                    {
                        coloringValues = smallBodyModel.getAllColoringValues(closestPoint);
                    }
                    catch (@SuppressWarnings("unused") IOException e)
                    {
                        coloringValues = new double[] {};
                    }
                    int colorValueSize = coloringValues.length;

                    data[index(j, i, BackplaneInfo.PIXEL.ordinal())] = (float) rawImage.GetScalarComponentAsFloat(j, i, 0, 0);
                    data[index(j, i, BackplaneInfo.X.ordinal())] = (float) closestPoint[0];
                    data[index(j, i, BackplaneInfo.Y.ordinal())] = (float) closestPoint[1];
                    data[index(j, i, BackplaneInfo.Z.ordinal())] = (float) closestPoint[2];
                    data[index(j, i, BackplaneInfo.LAT.ordinal())] = (float) lat;
                    data[index(j, i, BackplaneInfo.LON.ordinal())] = (float) lon;
                    data[index(j, i, BackplaneInfo.DIST.ordinal())] = (float) llr.rad;
                    data[index(j, i, BackplaneInfo.INC.ordinal())] = (float) illumAngles[0];
                    data[index(j, i, BackplaneInfo.EMI.ordinal())] = (float) illumAngles[1];
                    data[index(j, i, BackplaneInfo.PHASE.ordinal())] = (float) illumAngles[2];
                    data[index(j, i, BackplaneInfo.HSCALE.ordinal())] = (float) horizPixelScale;
                    data[index(j, i, BackplaneInfo.VSCALE.ordinal())] = (float) vertPixelScale;
                    data[index(j, i, BackplaneInfo.SLOPE.ordinal())] = colorValueSize > 0 ? (float) coloringValues[0] : 0.0F; // slope
                    data[index(j, i, BackplaneInfo.EL.ordinal())] = colorValueSize > 1 ? (float) coloringValues[1] : 0.0F; // elevation
                    data[index(j, i, BackplaneInfo.GRAVACC.ordinal())] = colorValueSize > 2 ? (float) coloringValues[2] : 0.0F; // grav acc;
                    data[index(j, i, BackplaneInfo.GRAVPOT.ordinal())] = colorValueSize > 3 ? (float) coloringValues[3] : 0.0F; // grav pot
                }
                else
                {
                    if (returnNullIfContainsLimb)
                        return;

                    data[index(j, i, 0)] = (float) rawImage.GetScalarComponentAsFloat(j, i, 0, 0);
                    for (int k = 1; k < numberOfBackplanes; ++k)
                        data[index(j, i, k)] = PDS_NA;
                }
            }
        }

        outputs.add(data);
	}

	int index(int i, int j, int k)
    {
        return ((k * image.getImageHeight() + j) * image.getImageWidth() + i);
    }
}
