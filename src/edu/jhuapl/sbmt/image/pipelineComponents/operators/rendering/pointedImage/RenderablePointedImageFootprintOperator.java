package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.BinTranslations;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.PadImageOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color.RGBALayerMergeOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadCachedSupportFilesPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadImageDataFromCachePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadPolydataFromCachePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.SaveImageDataToCachePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.SavePolydataToCachePipeline;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkPointData;
import vtk.vtkPolyData;

public class RenderablePointedImageFootprintOperator extends BasePipelineOperator<IRenderableImage, Pair<List<vtkImageData>, List<vtkPolyData>>>
{
	List<SmallBodyModel> smallBodyModels;
	private boolean useModifiedPointing = false;
	private static HashMap<String, vtkImageData> layerImageData = new HashMap<String, vtkImageData>();


	public RenderablePointedImageFootprintOperator(List<SmallBodyModel> smallBodyModels)
	{
		this(smallBodyModels, false);
	}

	public RenderablePointedImageFootprintOperator(List<SmallBodyModel> smallBodyModels, boolean useModifiedPointing)
	{
		this.smallBodyModels = smallBodyModels;
		this.useModifiedPointing = useModifiedPointing;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		RenderablePointedImage renderableImage = (RenderablePointedImage)inputs.get(0);
		PointingFileReader infoReader = useModifiedPointing ? renderableImage.getModifiedPointing().get() : renderableImage.getPointing();
		double[] spacecraftPositionAdjusted = infoReader.getSpacecraftPosition();
    	double[] frustum1Adjusted = infoReader.getFrustum1();
    	double[] frustum2Adjusted = infoReader.getFrustum2();
    	double[] frustum3Adjusted = infoReader.getFrustum3();
    	double[] frustum4Adjusted = infoReader.getFrustum4();
    	Frustum frustum = new Frustum(spacecraftPositionAdjusted,
						    			frustum1Adjusted,
						    			frustum3Adjusted,
						    			frustum4Adjusted,
						    			frustum2Adjusted);

    	IPipelineOperator<vtkImageData, vtkImageData> padOperator = new PassthroughOperator<>();
    	ImageBinPadding binPadding = renderableImage.getImageBinPadding();
    	//AMICA only, need generic way to handle this
    	if (renderableImage.getStartH() != null)
    	{
    		int xTranslation = 0, yTranslation = 0;
    		int binning = renderableImage.getBinning();
    		if (binning == 1)
    		{
	            xTranslation = renderableImage.getStartH();
	    		yTranslation = 1023 - renderableImage.getLastV();
    		}
    		else if (binning == 2)
    		{
    			xTranslation = renderableImage.getStartH()/2;
    			int lastV = ((renderableImage.getLastV() + 1) / 2) - 1;
	    		yTranslation = 511 - lastV;
    		}
    		binPadding.binTranslations.put(1, new BinTranslations(xTranslation, yTranslation));
    	}
    	if (renderableImage.getImageBinPadding() != null) padOperator = new PadImageOperator(renderableImage.getImageBinPadding(), renderableImage.getBinning());

     	 List<vtkImageData> imageData = Lists.newArrayList();
    	 List<vtkPolyData> footprints = Lists.newArrayList();
    	 
    	 int layerIndex = renderableImage.getLayerIndex();
    	 
    	 synchronized(RenderablePointedImageFootprintOperator.class)
         {
 	    	for (SmallBodyModel smallBody : smallBodyModels)
 	    	{
 	    		String prefix = getPrerenderingFileNameBase(renderableImage, smallBody) + "_" + layerIndex;
	    		String imageDataFilename = prefix + "_footprintImageData.vtk.gz";
 	    		LoadCachedSupportFilesPipeline cachedPipeline = LoadCachedSupportFilesPipeline.of(prefix);
 	    		vtkImageData existingImageData = cachedPipeline.getImageData();
				if (existingImageData != null)
				{
					//this restretches things to the proper contrast 
					Just.of(existingImageData)
						.operate(new VtkImageContrastOperator(renderableImage.getIntensityRange()))
						.subscribe(Sink.of(imageData))
						.run();					
				}
				else
				{
			        IPipelineOperator<Layer, vtkImageData> imageRenderer = new VtkImageRendererOperator();
			        if (renderableImage.getLayer().dataSizes().get(0) == 1)
					{
			        	imageRenderer = new VtkImageRendererOperator();
					}
			        else if (renderableImage.getLayer().dataSizes().get(0) == 3)
			        {
			        	imageRenderer = new RGBALayerMergeOperator();
			        }
			        
			        Just.of(renderableImage.getLayer())
			        	.operate(imageRenderer)
			        	.operate(padOperator)
			        	.operate(new VtkImageContrastOperator(renderableImage.getIntensityRange()))
			        	.operate(new VtkImageVtkMaskingOperator(renderableImage.getMasking().getMask()))
			        	.subscribe(Sink.of(imageData)).run();
			        SaveImageDataToCachePipeline.of(imageData.get(0), imageDataFilename);
				}
				
				
				//Footprints
				String imageFootprintFilename = prefix + "_footprintData.vtk.gz";
				vtkPolyData existingFootprint = cachedPipeline.getFootprintPolyData();
	    		if (existingFootprint != null)
	    		{
	    			footprints.add(existingFootprint);
	    			PolyDataUtil.shiftPolyDataInNormalDirection(existingFootprint, renderableImage.getOffset());
	    			continue;
	    		}
	    		else 
	    		{
		    		vtkFloatArray textureCoords = new vtkFloatArray();
		    		vtkPolyData tmp = null;
		    		vtkPolyData footprint = new vtkPolyData();
			        tmp = smallBody.computeFrustumIntersection(spacecraftPositionAdjusted,
			        															frustum1Adjusted,
			        															frustum3Adjusted,
			        															frustum4Adjusted,
			        															frustum2Adjusted);
	
			        if (tmp == null) continue;
	
			        // Need to clear out scalar data since if coloring data is being shown,
			        // then the color might mix-in with the image.
			        tmp.GetCellData().SetScalars(null);
			        tmp.GetPointData().SetScalars(null);
	
			        footprint.DeepCopy(tmp);
			        vtkPointData pointData = footprint.GetPointData();
			        pointData.SetTCoords(textureCoords);
			        PolyDataUtil.generateTextureCoordinates(frustum, renderableImage.getImageWidth(), renderableImage.getImageHeight(), footprint);
			        pointData.Delete();
			        PolyDataUtil.shiftPolyDataInNormalDirection(footprint, renderableImage.getOffset());
					SavePolydataToCachePipeline.of(footprint, imageFootprintFilename);
					footprints.add(footprint);
	    		}
	    	}
        }
    	outputs.add(Pair.of(imageData, footprints));
	}

    private String getPrerenderingFileNameBase(RenderablePointedImage renderableImage, SmallBodyModel smallBodyModel)
    {
        String imageName = renderableImage.getFilename();
        String topPath = FileCache.instance().getFile(imageName).getParent();
        String result = SafeURLPaths.instance().getString(topPath, "support",
        												  renderableImage.getImageSource().name(),
        												  FilenameUtils.getBaseName(imageName) + "_" + smallBodyModel.getModelResolution() + "_" + smallBodyModel.getModelName()/* + "_" + renderableImage.getPointing().hashCode()*/);

        return result;
    }
}
