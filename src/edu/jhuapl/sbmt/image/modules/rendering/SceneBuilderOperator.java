package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkFloatArray;
import vtk.vtkImageData;
import vtk.vtkLookupTable;
import vtk.vtkPointData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.image.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.InfoFileReader;


public class SceneBuilderOperator extends BasePipelineOperator<Pair<List<SmallBodyModel>, List<RenderableImage>>, vtkActor>
{

	List<SmallBodyModel> smallBodyModels;
	List<RenderableImage> renderableImages;

	public SceneBuilderOperator()
	{


	}

	@Override
	public void processData() throws IOException, Exception
	{
		smallBodyModels = inputs.get(0).getLeft();
		renderableImages = inputs.get(0).getRight();
//		smallBodyModels = Lists.newArrayList();
//		inputs.get(0).forEach( body -> smallBodyModels.addAll((List<SmallBodyModel>)body));
//		renderableImages = Lists.newArrayList();
//		inputs.get(1).forEach( data -> renderableImages.add((RenderableImage)data));
		processImages();
	}


	private void processImages()
	{
        try
		{
        	outputs.addAll(generateBodyModelActor(smallBodyModels));
			outputs.addAll(generateImageActor(renderableImages));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<vtkActor> generateBodyModelActor(List<SmallBodyModel> smallBodyModels)
	{
		List<vtkActor> smallBodyActors = Lists.newArrayList();
		smallBodyModels.forEach(smallBodyModel -> {
			vtkPolyDataMapper  smallBodyMapper = new vtkPolyDataMapper();
	        smallBodyMapper.SetInputData(smallBodyModel.getSmallBodyPolyData());
	        vtkLookupTable lookupTable = new vtkLookupTable();
	        smallBodyMapper.SetLookupTable(lookupTable);
	        smallBodyMapper.UseLookupTableScalarRangeOn();

			VtkLodActor smallBodyActor = new VtkLodActor(this);
			smallBodyActor.setDefaultMapper(smallBodyMapper);
			smallBodyActor.setLodMapper(LodMode.MaxQuality, smallBodyMapper);
			smallBodyActors.add(smallBodyActor);
		});

		return smallBodyActors;
	}

	private List<vtkActor> generateImageActor(List<RenderableImage> renderableImages) throws IOException, Exception
	{

		List<vtkActor> actors = Lists.newArrayList();
		for (RenderableImage renderableImage: renderableImages)
		{
			InfoFileReader infoReader = renderableImage.getPointing();

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


	        VtkImageRenderer imageRenderer = new VtkImageRenderer();
	        List<vtkImageData> imageData = Lists.newArrayList();
	        Just.of(renderableImage.getLayer())
	        	.operate(imageRenderer)
	        	.operate(new VtkImageContrastOperator(null))
	        	.subscribe(Sink.of(imageData)).run();

	    	for (SmallBodyModel smallBody : smallBodyModels)
	    	{
	    		vtkFloatArray textureCoords = new vtkFloatArray();
	    		vtkPolyData tmp = null;
	    		vtkPolyData footprint = new vtkPolyData();
		        tmp = smallBody.computeFrustumIntersection(spacecraftPositionAdjusted,
		        															frustum1Adjusted,
		        															frustum3Adjusted,
		        															frustum4Adjusted,
		        															frustum2Adjusted);
		        if (tmp == null) return null;

		        // Need to clear out scalar data since if coloring data is being shown,
		        // then the color might mix-in with the image.
		        tmp.GetCellData().SetScalars(null);
		        tmp.GetPointData().SetScalars(null);

		        footprint.DeepCopy(tmp);

		        vtkPointData pointData = footprint.GetPointData();
		        pointData.SetTCoords(textureCoords);
		        PolyDataUtil.generateTextureCoordinates(frustum, renderableImage.getImageWidth(), renderableImage.getImageHeight(), footprint);
		        pointData.Delete();

				vtkTexture imageTexture = new vtkTexture();
		        imageTexture.InterpolateOn();
		        imageTexture.RepeatOff();
		        imageTexture.EdgeClampOn();
		        imageTexture.SetInputData(imageData.get(0));

				vtkPolyDataMapper mapper = new vtkPolyDataMapper();
				mapper.SetInputData(footprint);

				vtkActor actor = new vtkActor();
				actor.SetMapper(mapper);
				actor.SetTexture(imageTexture);
		        vtkProperty footprintProperty = actor.GetProperty();
		        footprintProperty.LightingOff();
		        actors.add(actor);
	    	}
		}
		return actors;
	}
}