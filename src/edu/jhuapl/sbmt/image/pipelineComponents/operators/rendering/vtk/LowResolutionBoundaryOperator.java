package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class LowResolutionBoundaryOperator extends BasePipelineOperator<Pair<PointingFileReader, List<SmallBodyModel>>, vtkActor>
{
	private double offset;

	public LowResolutionBoundaryOperator(double offset)
	{
		this.offset = offset;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkActor actor = new vtkActor();
	    vtkPolyData boundary = new vtkPolyData();
	    vtkPolyDataMapper boundaryMapper = new vtkPolyDataMapper();
	    List<SmallBodyModel> smallBodyModels = inputs.get(0).getRight();

	    synchronized(LowResolutionBoundaryOperator.class) {

	        for (SmallBodyModel smallBodyModel : smallBodyModels)
	    	{
			    vtkPolyData emptyPolyData = new vtkPolyData();
		        boundary.DeepCopy(emptyPolyData);
		    	vtkPoints points = boundary.GetPoints();
		        vtkCellArray verts = boundary.GetVerts();

		        vtkIdList idList = new vtkIdList();
		        idList.SetNumberOfIds(1);
		        PointingFileReader pointing = inputs.get(0).getLeft();
		        double[] spacecraftPosition = pointing.getSpacecraftPosition();
		        double[] frustum1 = pointing.getFrustum1();
		        double[] frustum2 = pointing.getFrustum2();
		        double[] frustum3 = pointing.getFrustum3();



//	        	System.out.println("LowResolutionBoundaryOperator: processData: processing boundary for body " + smallBodyModel.getConfig().getUniqueName());
		        vtksbCellLocator cellLocator = smallBodyModel.getCellLocator();

		        vtkGenericCell cell = new vtkGenericCell();

		        // Note it doesn't matter what image size we use, even
		        // if it's not the same size as the original image. Just
		        // needs to large enough so enough points get drawn.
		        final int IMAGE_WIDTH = 1024;
		        final int IMAGE_HEIGHT = 1024;

		        int count = 0;

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

		        //double horizScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum3) / 2.0 ) / IMAGE_HEIGHT;
		        //double vertScaleFactor = 2.0 * Math.tan( GeometryUtil.vsep(frustum1, frustum2) / 2.0 ) / IMAGE_WIDTH;

		        double scdist = MathUtil.vnorm(spacecraftPosition);

		        for (int i=0; i<IMAGE_HEIGHT; ++i)
		        {
		            // Compute the vector on the left of the row.
		            double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
		            double[] left = {
		                    corner1[0] + fracHeight*vec13[0],
		                    corner1[1] + fracHeight*vec13[1],
		                    corner1[2] + fracHeight*vec13[2]
		            };

		            for (int j=0; j<IMAGE_WIDTH; ++j)
		            {
		                if (j == 1 && i > 0 && i < IMAGE_HEIGHT-1)
		                {
		                    j = IMAGE_WIDTH-2;
		                    continue;
		                }

		                double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
		                double[] vec = {
		                        left[0] + fracWidth*vec12[0],
		                        left[1] + fracWidth*vec12[1],
		                        left[2] + fracWidth*vec12[2]
		                };
		                vec[0] -= spacecraftPosition[0];
		                vec[1] -= spacecraftPosition[1];
		                vec[2] -= spacecraftPosition[2];
		                MathUtil.unorm(vec, vec);

		                double[] lookPt = {
		                        spacecraftPosition[0] + 2.0*scdist*vec[0],
		                        spacecraftPosition[1] + 2.0*scdist*vec[1],
		                        spacecraftPosition[2] + 2.0*scdist*vec[2]
		                };

		                double tol = 1e-6;
		                double[] t = new double[1];
		                double[] x = new double[3];
		                double[] pcoords = new double[3];
		                int[] subId = new int[1];
		                long[] cellId = new long[1];
		                int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);

		                if (result > 0)
		                {
		                    double[] closestPoint = x;

		                    //double horizPixelScale = closestDist * horizScaleFactor;
		                    //double vertPixelScale = closestDist * vertScaleFactor;

		                    points.InsertNextPoint(closestPoint);
		                    idList.SetId(0, count);
		                    verts.InsertNextCell(idList);

		                    ++count;
		                }
		            }
		        }

		        PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(
		                boundary,
		                smallBodyModel.getSmallBodyPolyData(),
		                smallBodyModel.getCellNormals(),
		                smallBodyModel.getCellLocator(),
		                offset);
//		                3.0*smallBodyModel.getMinShiftAmount());

		        boundary.Modified();
		        boundaryMapper.SetInputData(boundary);

		        actor.SetMapper(boundaryMapper);
		        actor.GetProperty().SetColor(1.0, 0.0, 0.0);
		        actor.GetProperty().SetPointSize(2.0f);
		        outputs.add(actor);
	    	}
        }
	}
}

