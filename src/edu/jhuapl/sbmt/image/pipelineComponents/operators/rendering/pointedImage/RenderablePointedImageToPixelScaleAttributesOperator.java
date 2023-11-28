package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkPoints;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class RenderablePointedImageToPixelScaleAttributesOperator extends BasePipelineOperator<Pair<vtkPolyData, RenderablePointedImage>, ImagePixelScale>
{
	ImagePixelScale pixelScale;
	PointingFileReader pointing;
	vtkPolyData footprint;
	double imageHeight, imageWidth;

	@Override
	public void processData() throws IOException, Exception
	{
		pixelScale = new ImagePixelScale();
		footprint = inputs.get(0).getLeft();
		pointing = inputs.get(0).getRight().getPointing();
		imageWidth = inputs.get(0).getRight().getImageWidth();
		imageHeight = inputs.get(0).getRight().getImageHeight();
		computePixelScale();
		outputs.add(pixelScale);
	}


	void computePixelScale()
    {
    	double[] spacecraftPosition = pointing.getSpacecraftPosition();
//    	int currentSlice = image.currentSlice;
//    	vtkPolyData currentFootprint = footprint[currentSlice];
//        if (footprint.getFootprintGenerated()[currentSlice] == false)
//            footprint.loadFootprint();

        int numberOfPoints = (int)footprint.GetNumberOfPoints();

        vtkPoints points = footprint.GetPoints();

        pixelScale.minHorizontalPixelScale = Double.MAX_VALUE;
        pixelScale.maxHorizontalPixelScale = -Double.MAX_VALUE;
        pixelScale.meanHorizontalPixelScale = 0.0;
        pixelScale.minVerticalPixelScale = Double.MAX_VALUE;
        pixelScale.maxVerticalPixelScale = -Double.MAX_VALUE;
        pixelScale.meanVerticalPixelScale = 0.0;
        
        double horizScaleFactor = 2.0 * Math.tan(MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum3()) / 2.0) / imageWidth;
        double vertScaleFactor = 2.0 * Math.tan(MathUtil.vsep(pointing.getFrustum1(), pointing.getFrustum2()) / 2.0) / imageHeight;

        double[] vec = new double[3];

        for (int i = 0; i < numberOfPoints; ++i)
        {
            double[] pt = points.GetPoint(i);
            
            vec[0] = pt[0] - spacecraftPosition[0];
            vec[1] = pt[1] - spacecraftPosition[1];
            vec[2] = pt[2] - spacecraftPosition[2];
            double dist = MathUtil.vnorm(vec);

            double horizPixelScale = dist * horizScaleFactor;
            double vertPixelScale = dist * vertScaleFactor;

            if (horizPixelScale < pixelScale.minHorizontalPixelScale)
            	pixelScale.minHorizontalPixelScale = horizPixelScale;
            if (horizPixelScale > pixelScale.maxHorizontalPixelScale)
            	pixelScale.maxHorizontalPixelScale = horizPixelScale;
            if (vertPixelScale < pixelScale.minVerticalPixelScale)
            	pixelScale.minVerticalPixelScale = vertPixelScale;
            if (vertPixelScale > pixelScale.maxVerticalPixelScale)
            	pixelScale.maxVerticalPixelScale = vertPixelScale;

            pixelScale.meanHorizontalPixelScale += horizPixelScale;
            pixelScale.meanVerticalPixelScale += vertPixelScale;
        }

        pixelScale.meanHorizontalPixelScale /= (double) numberOfPoints;
        pixelScale.meanVerticalPixelScale /= (double) numberOfPoints;

        points.Delete();
    }
}
