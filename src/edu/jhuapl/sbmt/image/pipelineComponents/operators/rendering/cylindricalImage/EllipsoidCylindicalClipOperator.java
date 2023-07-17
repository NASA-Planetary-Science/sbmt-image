package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage;

import java.io.IOException;

import vtk.vtkAlgorithmOutput;
import vtk.vtkClipPolyData;
import vtk.vtkPlane;
import vtk.vtkPolyData;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;


public class EllipsoidCylindicalClipOperator extends BasePipelineOperator<vtkPolyData, vtkPolyData>
{
	private double lowerLeftLat;
	private double upperRightLat;
	private SmallBodyModel smallBodyModel;

	public EllipsoidCylindicalClipOperator(SmallBodyModel smallBodyModel, double lowerLeftLat, double upperRightLat)
	{
		this.lowerLeftLat = lowerLeftLat;
		this.upperRightLat = upperRightLat;
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkPolyData smallBodyPolyData = inputs.get(0);
        double[] zaxis = {0.0, 0.0, 1.0};
        double lllat = lowerLeftLat * (Math.PI / 180.0);
        double urlat = upperRightLat * (Math.PI / 180.0);

        double[] intersectPoint = new double[3];
        smallBodyModel.getPointAndCellIdFromLatLon(lllat, 0.0, intersectPoint);
        double[] vec = new double[]{0.0, 0.0, intersectPoint[2]};


        vtkPlane plane3 = new vtkPlane();
        plane3.SetOrigin(vec);
        plane3.SetNormal(zaxis);

        vtkClipPolyData clipPolyData2 = new vtkClipPolyData();
        clipPolyData2.SetClipFunction(plane3);
        clipPolyData2.SetInputData(smallBodyPolyData);
        clipPolyData2.Update();
        vtkAlgorithmOutput clipPolyData2Output = clipPolyData2.GetOutputPort();


        smallBodyModel.getPointAndCellIdFromLatLon(urlat, 0.0, intersectPoint);
        vec = new double[]{0.0, 0.0, intersectPoint[2]};

        vtkPlane plane4 = new vtkPlane();
        plane4.SetOrigin(vec);
        plane4.SetNormal(zaxis);

        vtkClipPolyData clipPolyData3 = new vtkClipPolyData();
        clipPolyData3.SetClipFunction(plane4);
        clipPolyData3.SetInputConnection(clipPolyData2Output);
        clipPolyData3.SetInsideOut(1);
        clipPolyData3.Update();
        outputs.add(clipPolyData3.GetOutput());
	}
}
