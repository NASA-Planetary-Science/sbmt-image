package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkCell;
import vtk.vtkCellData;
import vtk.vtkDataArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataNormals;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class FootprintToIlluminationAttributesOperator extends BasePipelineOperator<Pair<vtkPolyData, PointingFileReader>, ImageIllumination>
{
	ImageIllumination illumination;
	PointingFileReader pointing;
	vtkPolyData footprint;
	private vtkPolyDataNormals normalsFilter;
    private boolean normalsGenerated = false;

	@Override
	public void processData() throws IOException, Exception
	{
		illumination = new ImageIllumination();
		normalsFilter = new vtkPolyDataNormals();
		footprint = inputs.get(0).getLeft();
		pointing = inputs.get(0).getRight();
		computeIlluminationAngles();
//		computeCellNormals();
		illumination.horizFovAngle = getHorizontalFovAngle();
		illumination.verticalFovAngle = getVerticalFovAngle();
		illumination.surfaceArea = PolyDataUtil.getSurfaceArea(footprint);
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

    void computeIlluminationAngles()
    {
//    	int currentSlice = image.getCurrentSlice();
//    	vtkPolyData currentFootprint = footprint.getFootprint()[currentSlice];
//        if (footprint.getFootprintGenerated()[currentSlice] == false)
//            footprint.loadFootprint();
//    	VTKDebug.writePolyDataToFile(footprint, "/Users/steelrj1/Desktop" + File.separator + "model_illum_new.vtk");
        computeCellNormals();

        int numberOfCells = (int)footprint.GetNumberOfCells();

        vtkPoints points = footprint.GetPoints();
        vtkCellData footprintCellData = footprint.GetCellData();
        vtkDataArray normals = footprintCellData.GetNormals();

        illumination.minEmission = Double.MAX_VALUE;
        illumination.maxEmission = -Double.MAX_VALUE;
        illumination.minIncidence = Double.MAX_VALUE;
        illumination.maxIncidence = -Double.MAX_VALUE;
        illumination.minPhase = Double.MAX_VALUE;
        illumination.maxPhase = -Double.MAX_VALUE;

        for (int i = 0; i < numberOfCells; ++i)
        {
            vtkCell cell = footprint.GetCell(i);
            double[] pt0 = points.GetPoint(cell.GetPointId(0));
            double[] pt1 = points.GetPoint(cell.GetPointId(1));
            double[] pt2 = points.GetPoint(cell.GetPointId(2));
            double[] centroid = {
                    (pt0[0] + pt1[0] + pt2[0]) / 3.0,
                    (pt0[1] + pt1[1] + pt2[1]) / 3.0,
                    (pt0[2] + pt1[2] + pt2[2]) / 3.0
            };
            double[] normal = normals.GetTuple3(i);

            double[] angles = computeIlluminationAnglesAtPoint(centroid, normal);
            double incidence = angles[0];
            double emission = angles[1];
            double phase = angles[2];

            if (incidence < illumination.minIncidence)
            	illumination.minIncidence = incidence;
            if (incidence > illumination.maxIncidence)
            	illumination.maxIncidence = incidence;
            if (emission < illumination.minEmission)
            	illumination.minEmission = emission;
            if (emission > illumination.maxEmission)
            	illumination.maxEmission = emission;
            if (phase < illumination.minPhase)
            	illumination.minPhase = phase;
            if (phase > illumination.maxPhase)
            	illumination.maxPhase = phase;
            cell.Delete();
        }

        points.Delete();
        footprintCellData.Delete();
        if (normals != null)
            normals.Delete();
    }

    void computeCellNormals()
    {
        if (normalsGenerated == false)
        {
//        	vtkPolyData[] fprint = footprint.getFootprint();
//        	int currentSlice = image.currentSlice;
            normalsFilter.SetInputData(footprint);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(0);
            // normalsFilter.AutoOrientNormalsOn();
            // normalsFilter.ConsistencyOn();
            normalsFilter.SplittingOff();
            normalsFilter.Update();

            if (footprint != null)
            {
                vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
                footprint.DeepCopy(normalsFilterOutput);
                normalsGenerated = true;
            }
        }
    }

    /**
     * Get the maximum FOV angle in degrees of the image (the max of either the
     * horizontal or vetical FOV). I.e., return the angular separation in degrees
     * between two corners of the frustum where the two corners are both on the
     * longer side.
     *
     * @return
     */
    public double getMaxFovAngle()
    {
        return Math.max(getHorizontalFovAngle(), getVerticalFovAngle());
    }

    public double getHorizontalFovAngle()
    {
        return MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum3()) * 180.0 / Math.PI;

    }

    public double getVerticalFovAngle()
    {
        return MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum2()) * 180.0 / Math.PI;
    }
}
