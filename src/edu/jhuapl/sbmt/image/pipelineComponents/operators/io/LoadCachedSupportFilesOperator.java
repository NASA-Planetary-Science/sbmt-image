package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadImageDataFromCachePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadPolydataFromCachePipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import vtk.vtkImageData;
import vtk.vtkPolyData;

public class LoadCachedSupportFilesOperator extends BasePipelineOperator<String, Pair<List<vtkImageData>, List<vtkPolyData>>>
{
	@Override
	public void processData() throws IOException, Exception
	{
		//input is getPrerenderingFileNameBase(renderableImage, smallBody) + "_" + layerIndex
		String imageDataFilename = inputs.get(0) + "_footprintImageData.vtk.gz";

		vtkImageData existingImageData = LoadImageDataFromCachePipeline.of(imageDataFilename).orNull();

		String imageFootprintFilename = inputs.get(0) + "_footprintData.vtk.gz";
		
		vtkPolyData existingFootprint = LoadPolydataFromCachePipeline.of(imageFootprintFilename).orNull();
		
		List<vtkImageData> imageDatas = Lists.newArrayList();
		List<vtkPolyData> polyDatas = Lists.newArrayList();
		
		if (existingImageData != null) imageDatas.add(existingImageData);
		if (existingFootprint != null) polyDatas.add(existingFootprint);
		
		outputs.add(Pair.of(imageDatas, polyDatas));
	}
}
