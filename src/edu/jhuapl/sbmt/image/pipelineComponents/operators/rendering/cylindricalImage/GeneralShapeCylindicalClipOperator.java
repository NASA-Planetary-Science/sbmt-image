package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage;

import java.io.IOException;

import vtk.vtkAlgorithmOutput;
import vtk.vtkAppendPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkCone;
import vtk.vtkPlane;
import vtk.vtkPolyData;
import vtk.vtkTransform;

import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class GeneralShapeCylindicalClipOperator extends BasePipelineOperator<vtkPolyData, vtkPolyData>
{
	private double lowerLeftLat;
	private double upperRightLat;

	public GeneralShapeCylindicalClipOperator(double lowerLeftLat, double upperRightLat)
	{
		this.lowerLeftLat = lowerLeftLat;
		this.upperRightLat = upperRightLat;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkPolyData smallBodyPolyData = inputs.get(0);
        double[] origin = {0.0, 0.0, 0.0};
        double[] zaxis = {0.0, 0.0, 1.0};
        double lllat = lowerLeftLat * (Math.PI / 180.0);
        double urlat = upperRightLat * (Math.PI / 180.0);

        // For clipping latitude, first split the shape model in half, do the clipping
        // on each half, and then combine the 2 halves.
        vtkPlane planeZeroLat = new vtkPlane();
        planeZeroLat.SetOrigin(origin);
        planeZeroLat.SetNormal(zaxis);

        double[] yaxis = {0.0, 1.0, 0.0};
        vtkTransform transform = new vtkTransform();
        transform.Identity();
        transform.RotateWXYZ(90.0, yaxis);

        // Do northern hemisphere first
        vtkClipPolyData clipPolyDataNorth = new vtkClipPolyData();
        clipPolyDataNorth.SetClipFunction(planeZeroLat);
        clipPolyDataNorth.SetInputData(smallBodyPolyData);
        clipPolyDataNorth.Update();
        vtkAlgorithmOutput clipNorthOutput = clipPolyDataNorth.GetOutputPort();
        if (lllat > 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 - lowerLeftLat);

            clipPolyDataNorth = new vtkClipPolyData();
            clipPolyDataNorth.SetClipFunction(cone);
            clipPolyDataNorth.SetInputConnection(clipNorthOutput);
            clipPolyDataNorth.SetInsideOut(1);
            clipPolyDataNorth.Update();
            clipNorthOutput = clipPolyDataNorth.GetOutputPort();
        }
        if (urlat > 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 - upperRightLat);

            clipPolyDataNorth = new vtkClipPolyData();
            clipPolyDataNorth.SetClipFunction(cone);
            clipPolyDataNorth.SetInputConnection(clipNorthOutput);
            clipPolyDataNorth.Update();
            clipNorthOutput = clipPolyDataNorth.GetOutputPort();
        }

        // Now do southern hemisphere
        vtkClipPolyData clipPolyDataSouth = new vtkClipPolyData();
        clipPolyDataSouth.SetClipFunction(planeZeroLat);
        clipPolyDataSouth.SetInputData(smallBodyPolyData);
        clipPolyDataSouth.SetInsideOut(1);
        clipPolyDataSouth.Update();
        vtkAlgorithmOutput clipSouthOutput = clipPolyDataSouth.GetOutputPort();
        if (lllat < 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 + lowerLeftLat);

            clipPolyDataSouth = new vtkClipPolyData();
            clipPolyDataSouth.SetClipFunction(cone);
            clipPolyDataSouth.SetInputConnection(clipSouthOutput);
            clipPolyDataSouth.Update();
            clipSouthOutput = clipPolyDataSouth.GetOutputPort();
        }
        if (urlat < 0.0)
        {
            vtkCone cone = new vtkCone();
            cone.SetTransform(transform);
            cone.SetAngle(90.0 + upperRightLat);

            clipPolyDataSouth = new vtkClipPolyData();
            clipPolyDataSouth.SetClipFunction(cone);
            clipPolyDataSouth.SetInputConnection(clipSouthOutput);
            clipPolyDataSouth.SetInsideOut(1);
            clipPolyDataSouth.Update();
            clipSouthOutput = clipPolyDataSouth.GetOutputPort();
        }


        vtkAppendPolyData appendFilter = new vtkAppendPolyData();
        if (urlat > 0.0)
            appendFilter.AddInputConnection(clipNorthOutput);
        if (lllat < 0.0)
            appendFilter.AddInputConnection(clipSouthOutput);

        appendFilter.Update();
        outputs.add(appendFilter.GetOutput());
	}
}
