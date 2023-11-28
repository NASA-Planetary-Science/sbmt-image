package edu.jhuapl.sbmt.image.pipelineComponents.operators.backplane;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;
import vtk.vtkDataArray;
import vtk.vtkGenericCell;
import vtk.vtkImageData;
import vtk.vtksbCellLocator;

public class RenderableImageLimbDetectionOperator
		extends BasePipelineOperator<Pair<RenderablePointedImage, SmallBodyModel>, Boolean>
{
	private RenderablePointedImage image;
	
	public RenderableImageLimbDetectionOperator()
	{
		
	}
	
	@Override
	public void processData() throws IOException, Exception
	{
		image = inputs.get(0).getLeft();
		SmallBodyModel smallBodyModel = inputs.get(0).getRight();
		PointingFileReader pointing = image.getPointing();

		double[] spacecraftPositionAdjusted = pointing.getSpacecraftPosition();
    	double[] frustum1Adjusted = pointing.getFrustum1();
    	double[] frustum2Adjusted = pointing.getFrustum2();
    	double[] frustum3Adjusted = pointing.getFrustum3();

        vtksbCellLocator cellLocator = smallBodyModel.getCellLocator();
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

        double scdist = MathUtil.vnorm(spacecraftPositionAdjusted);

        RenderablePointedImageFootprintGeneratorPipeline pipeline =
        		new RenderablePointedImageFootprintGeneratorPipeline(image, List.of(smallBodyModel));
        
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
                
                if (j == 1 && i > 0 && i < image.getImageHeight() - 1)
                {
                    j = image.getImageWidth() - 2;
                    continue;
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
            
                if (result == 0)
                {
                	i = image.getImageHeight();
                	j = image.getImageWidth();
                	outputs.add(true);
                }
            }
        }
        if (outputs.isEmpty())
        	outputs.add(false);
	}
}
