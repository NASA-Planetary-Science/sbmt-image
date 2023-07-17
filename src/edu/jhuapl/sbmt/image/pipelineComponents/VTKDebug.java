package edu.jhuapl.sbmt.image.pipelineComponents;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkActor;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkActorPreview;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkImagePreview;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkLayerPreview;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkLayerPreviewOld;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

public class VTKDebug
{
	public static void writePolyDataToFile(vtkPolyData polyData, String filename)
	{
        vtkPolyDataWriter imageWriter = new vtkPolyDataWriter();
        imageWriter.SetInputData(polyData);
        imageWriter.SetFileName(filename);
        imageWriter.SetFileTypeToBinary();
        imageWriter.Write();
	}

	public static void previewLayer(Layer layer, String title) throws Exception
	{
		Pair<Layer, List<HashMap<String, String>>> inputs = Pair.of(layer, List.of(new HashMap<String, String>()));
		Just.of(inputs)
			.subscribe(new VtkLayerPreview(title, 0, new IntensityRange(0, 255), new int[] {0,0,0,0}, new double[] {}, false,  new Runnable()
			{

				@Override
				public void run()
				{
					// TODO Auto-generated method stub

				}
			}))
			.run();
	}

	public static void previewLayerOld(Layer layer, String title) throws Exception
	{
		Pair<Layer, List<HashMap<String, String>>> inputs = Pair.of(layer, List.of(new HashMap<String, String>()));
		Just.of(inputs)
			.subscribe(new VtkLayerPreviewOld(title, 0, new IntensityRange(0, 255), new int[] {0,0,0,0}, new double[] {}, new Runnable()
			{

				@Override
				public void run()
				{
					// TODO Auto-generated method stub

				}
			}))
			.run();
	}

	public static void previewVtkImageData(vtkImageData imageData, String title) throws Exception
	{
		Just.of(imageData)
			.subscribe(new VtkImagePreview(title, new HashMap<String, String>(), false))
			.run();
	}

	public static void previewVtkActor(vtkActor actor, String title) throws Exception
	{
		Just.of(actor)
			.subscribe(new VtkActorPreview(title, new HashMap<String, String>(), false))
			.run();
	}
}
