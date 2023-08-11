package edu.jhuapl.sbmt.image.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color.RGBALayerMergeOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk.VtkImageContrastPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkTransform;

public class LayerPreviewModel<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

	private vtkImageSlice actor = new vtkImageSlice();
	private vtkImageReslice reslice;

	private G1 image;
	private List<Layer> layers;
	private Layer layer;
	private int displayedLayerIndex = 0;
	private int[] currentMaskValues;
	private double[] currentFillValues;
	private IntensityRange intensityRange;
	private List<HashMap<String, String>> metadata;
	private List<List<HashMap<String, String>>> metadatas;
	private boolean invertY = false;
	private vtkImageData displayedImage;
	
	private HashMap<Layer, vtkImageData> layerImageData = new HashMap<Layer, vtkImageData>();

	public LayerPreviewModel(G1 image, final List<Layer> layers, int currentLayerIndex, IntensityRange intensityRange, int[] currentMaskValues,
				double[] currentFillValues, List<List<HashMap<String, String>>> metadatas, boolean invertY)
	{
		this.image = image;
		this.invertY = invertY;
		this.layers = layers;
		this.intensityRange = intensityRange;
		this.currentMaskValues = currentMaskValues;
		this.currentFillValues = currentFillValues;
		this.displayedLayerIndex = currentLayerIndex;
		this.layer = layers.get(currentLayerIndex);
		this.metadatas = metadatas;
		this.metadata = metadatas.get(currentLayerIndex);
		try
		{
			renderLayer();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getDisplayedLayerIndex()
	{
		return displayedLayerIndex;
	}

	public void setDisplayedLayerIndex(int displayedLayerIndex)
	{
		this.displayedLayerIndex = displayedLayerIndex;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(G1 image)
	{
		this.image = image;
	}

	public G1 getImage()
	{
		return image;
	}

	public vtkImageData getDisplayedImage()
	{
		return displayedImage;
	}

	public void setDisplayedImage(vtkImageData displayedImage)
	{
		this.displayedImage = displayedImage;
	}

	public int[] getCurrentMaskValues()
	{
		return currentMaskValues;
	}

	public void setCurrentMaskValues(int[] currentMaskValues) throws IOException, Exception
	{
		if (Arrays.equals(this.currentMaskValues, currentMaskValues)) return;
		this.currentMaskValues = currentMaskValues;
		if (image != null) image.setMaskValues(currentMaskValues);
		renderLayer();

	}

	public double[] getCurrentFillValues()
	{
		return currentFillValues;
	}

	public void setCurrentFillValues(double[] currentFillValues) throws IOException, Exception
	{
		if (Arrays.equals(this.currentFillValues, currentFillValues)) return;
		this.currentFillValues = currentFillValues;
		if (image != null) image.setFillValues(currentFillValues);
		renderLayer();
	}

	public IntensityRange getIntensityRange()
	{
		return intensityRange;
	}

	public void setIntensityRange(IntensityRange intensityRange) throws IOException, Exception
	{
		this.intensityRange = intensityRange;
		image.setIntensityRange(intensityRange);
		renderLayer();
	}

	public void setIntensity(IntensityRange range) throws IOException, Exception
	{
		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(getDisplayedImage(), range);
		setDisplayedImage(pipeline.getUpdatedData().get(0));
 		updateImage(getDisplayedImage());
	}

	private void generateVtkImageDataAndSetLayer(Layer layer) throws IOException, Exception
	{
		List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
		IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
		reader.
			operate(new VtkImageRendererOperator(isInvertY())).
			subscribe(new Sink<vtkImageData>(displayedImages)).run();
		setDisplayedImage(displayedImages.get(0));
//		contrastController.setImageData(getDisplayedImage());
//		if (getDisplayedImage().GetNumberOfScalarComponents() != 1)
//			contrastController.getView().setVisible(false);
		setLayer(layer);
	}

	private void generateVtkImageData(Layer layer) throws IOException, Exception
	{
		vtkImageData existingImageData = layerImageData.get(layer);
		if (existingImageData != null)
		{
			setDisplayedImage(existingImageData);
			return;
		}
		if (layer.dataSizes().get(0) == 1)
		{
			List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
			IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
			reader.
				operate(new VtkImageRendererOperator(isInvertY())).
				subscribe(new Sink<vtkImageData>(displayedImages)).run();
			setDisplayedImage(displayedImages.get(0));
			layerImageData.put(layer, displayedImages.get(0));
		}
		else if (layer.dataSizes().get(0) == 3)
		{
			List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
			IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
			reader.
				operate(new RGBALayerMergeOperator()).
				subscribe(new Sink<vtkImageData>(displayedImages)).run();
			setDisplayedImage(displayedImages.get(0));
			layerImageData.put(layer, displayedImages.get(0));
		}
	}

	public void renderLayer(Layer layer) throws IOException, Exception
	{
		generateVtkImageDataAndSetLayer(layer);
		updateImage(getDisplayedImage());
	}

	private void renderLayer() throws IOException, Exception
	{
		if (getImage() == null) return;
		regenerateLayerFromImage();
		if (getLayer() == null) return;
		renderLayer(getLayer());
		setIntensity(getIntensityRange());
	}

	private void regenerateLayerFromImage() throws InvalidGDALFileTypeException, IOException, Exception
	{
		IPerspectiveImageToLayerAndMetadataPipeline pipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(getImage());

		setLayers(pipeline.getLayers());
		setLayer(pipeline.getLayers().get(getDisplayedLayerIndex()));
	}

	private void updateImage(vtkImageData displayedImage)
	{
		double[] center = displayedImage.GetCenter();
		int[] dims = displayedImage.GetDimensions();
		// Rotate image by 90 degrees so it appears the same way as when you
		// use the Center in Image option.
		vtkTransform imageTransform = new vtkTransform();
		imageTransform.Translate(center[0], center[1], 0.0);
		imageTransform.RotateZ(0.0);
		imageTransform.Translate(-center[1], -center[0], 0.0);

		reslice = new vtkImageReslice();
		reslice.SetInputData(displayedImage);
//		reslice.SetResliceTransform(imageTransform);
		reslice.SetInterpolationModeToNearestNeighbor();
		reslice.SetOutputSpacing(1.0, 1.0, 1.0);
		reslice.SetOutputOrigin(0.0, 0.0, 0.0);
		reslice.SetOutputExtent(0, dims[0] - 1, 0, dims[1] - 1, 0, dims[2]);
		reslice.Update();

		vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
		imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
		imageSliceMapper.Update();

		actor.SetMapper(imageSliceMapper);
		actor.GetProperty().SetInterpolationTypeToLinear();
		actor.Modified();
	}

	public boolean isInvertY()
	{
		return invertY;
	}

	public void setInvertY(boolean invertY)
	{
		this.invertY = invertY;
	}

	public List<Layer> getLayers()
	{
		return layers;
	}

	public void setLayers(List<Layer> layers)
	{
		this.layers = layers;
	}

	public Layer getLayer()
	{
		return layer;
	}

	public void setLayer(Layer layer) throws Exception
	{
		this.layer = layer;
		generateVtkImageData(layer);
		updateImage(getDisplayedImage());
		setIntensity(getIntensityRange());
	}

	public vtkImageSlice getActor()
	{
		return actor;
	}

	public List<HashMap<String, String>> getMetadata()
	{
		return metadata;
	}

	public void setMetadata(List<HashMap<String, String>> metadata)
	{
		this.metadata = metadata;
	}

	public List<List<HashMap<String, String>>> getMetadatas()
	{
		return metadatas;
	}

	public void setMetadatas(List<List<HashMap<String, String>>> metadatas)
	{
		this.metadatas = metadatas;
	}

}
