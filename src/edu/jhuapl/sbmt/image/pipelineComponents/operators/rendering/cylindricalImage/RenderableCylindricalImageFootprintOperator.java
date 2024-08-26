package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadPolydataFromCachePipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import vtk.vtkPolyData;

public class RenderableCylindricalImageFootprintOperator extends BasePipelineOperator<IRenderableImage, vtkPolyData>
{
	private final List<SmallBodyModel> smallBodyModels;

	public RenderableCylindricalImageFootprintOperator(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		IRenderableImage renderableImage = inputs.get(0);
		CylindricalBounds bounds = renderableImage.getBounds();
		double lllat = bounds.minLatitude();
        double lllon = bounds.minLongitude();
        double urlat = bounds.maxLatitude();
        double urlon = bounds.maxLongitude();
		for (SmallBodyModel smallBodyModel : smallBodyModels)
		{
			String imageFilename = getPrerenderingFileNameBase(renderableImage, smallBodyModel) + "_footprintImageData.vtk.gz";
    		vtkPolyData existingFootprint = LoadPolydataFromCachePipeline.of(imageFilename).orNull();
    		if (existingFootprint != null)
    		{
    			outputs.add(existingFootprint);
    			continue;
    		}

			BasePipelineOperator<vtkPolyData, vtkPolyData> latitudeClipOperator = null;
			latitudeClipOperator = new PassthroughOperator<vtkPolyData>();
			if (lllat != -90.0 || lllon != 0.0 || urlat != 90.0 || urlon != 360.0)
	        {
	            if (smallBodyModel.isEllipsoid())
	            	latitudeClipOperator
	            		= new EllipsoidCylindicalClipOperator(smallBodyModel, lllat, urlat);
	            else
	            	latitudeClipOperator
	            		= new GeneralShapeCylindicalClipOperator(lllat, urlat);
	        }

			BasePipelineOperator<vtkPolyData, vtkPolyData> longitudeClipOperator
				= new PartialCylindricalClipOperator(lllat, lllon, urlat, urlon);

			List<vtkPolyData> polyDatas = Lists.newArrayList();
			Just.of(smallBodyModel.getSmallBodyPolyData())
				.operate(latitudeClipOperator)
				.operate(longitudeClipOperator)
				.subscribe(Sink.of(polyDatas))
				.run();

	        // Need to clear out scalar data since if coloring data is being shown,
	        // then the color might mix-in with the image.
	        polyDatas.get(0).GetCellData().SetScalars(null);
	        polyDatas.get(0).GetPointData().SetScalars(null);

	        vtkPolyData shiftedFootprint = new vtkPolyData();
	        shiftedFootprint.DeepCopy(polyDatas.get(0));
	        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, renderableImage.getOffset());
	        outputs.add(shiftedFootprint);

//	        VTKDebug.writePolyDataToFile(shiftedFootprint, "/Users/steelrj1/Desktop/" + FilenameUtils.getBaseName(renderableImage.getFilename()) + "_" + smallBodyModel.getModelResolution() + "_" + smallBodyModel.getModelName().replaceAll(" ", "_")+ "_footprintImageData.vtk.gz");
		}
	}

	private String getPrerenderingFileNameBase(IRenderableImage renderableImage, SmallBodyModel smallBodyModel)
    {
        String imageName = renderableImage.getFilename();
        String topPath = FileCache.instance().getFile(imageName).getParent();
        if (new File(imageName).exists()) topPath = new File(imageName).getParent();
        String result = SafeURLPaths.instance().getString(topPath, "support",
        												  FilenameUtils.getBaseName(imageName) + "_" + smallBodyModel.getModelResolution() + "_" + smallBodyModel.getModelName().replaceAll(" ", "_"));

        return result;
    }
}
