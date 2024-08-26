package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.offlimb.OfflimbActorOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.offlimb.OfflimbPlaneGeneratorOperators;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkImageData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

public class PointedImageRenderables extends ImageRenderable
{
	private boolean offLimbVisibility = false;
    private boolean offLimbBoundaryVisibility = false;
    private double offLimbFootprintDepth;
    List<vtkImageData> imageData = Lists.newArrayList();
    List<vtkImageData> modifiedImageData = Lists.newArrayList();
    private RenderablePointedImageFootprintGeneratorPipeline pipeline;

	public PointedImageRenderables(RenderablePointedImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this.image = image;
		this.generateOfflimb = image.isOfflimbShowing();
		this.smallBodyModels = smallBodyModels;
		prepareFootprints(image);
		footprintActors = processFootprints(footprintPolyData, imageData, image.isLinearInterpolation());
		frustumActor = processFrustum(image, image.getPointing());
		boundaryActors = processBoundaries();
		if (generateOfflimb) generateOfflimbActors();
		image.getModifiedPointing().ifPresent(pointing -> {
			try
			{
				prepareModifiedFootprints(image);
				modifiedFootprintActors = processFootprints(modifiedFootprintPolyData, modifiedImageData, image.isLinearInterpolation());
				modifiedFrustumActor = processFrustum(image, pointing);
				modifiedBoundaryActors = processBoundaries();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private void prepareFootprints(RenderablePointedImage renderableImage) throws IOException, Exception
	{
		pipeline = new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		footprintPolyData = pipeline.getFootprintPolyData();
		imageData.addAll(pipeline.getImageData());
	}

	private void prepareModifiedFootprints(RenderablePointedImage renderableImage) throws IOException, Exception
	{
		pipeline = new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels, true);
		modifiedFootprintPolyData = pipeline.getFootprintPolyData();
		modifiedImageData.addAll(pipeline.getImageData());
	}

	private vtkActor processFrustum(RenderablePointedImage renderableImage, PointingFileReader pointing)
	{
		vtkActor frustumActor;
		double diagonalLength = smallBodyModels.get(0).getBoundingBoxDiagonalLength();
		double[] scPos = pointing.getSpacecraftPosition();
		offLimbFootprintDepth = new Vector3D(scPos).getNorm();
    	double[] frus1 = pointing.getFrustum1();
    	double[] frus2 = pointing.getFrustum2();
    	double[] frus3 = pointing.getFrustum3();
    	double[] frus4 = pointing.getFrustum4();

		vtkPolyData frustumPolyData = new vtkPolyData();
		frustumActor = new vtkActor();
		vtkPoints points = new vtkPoints();
		vtkCellArray lines = new vtkCellArray();

		vtkIdList idList = new vtkIdList();
		idList.SetNumberOfIds(2);

		double maxFrustumRayLength = MathUtil.vnorm(scPos) + diagonalLength;
		double[] origin = scPos;

		double[] UL = { origin[0] + frus1[0] * maxFrustumRayLength,
				origin[1] + frus1[1] * maxFrustumRayLength,
				origin[2] + frus1[2] * maxFrustumRayLength };
		double[] UR = { origin[0] + frus2[0] * maxFrustumRayLength,
				origin[1] + frus2[1] * maxFrustumRayLength,
				origin[2] + frus2[2] * maxFrustumRayLength };
		double[] LL = { origin[0] + frus3[0] * maxFrustumRayLength,
				origin[1] + frus3[1] * maxFrustumRayLength,
				origin[2] + frus3[2] * maxFrustumRayLength };
		double[] LR = { origin[0] + frus4[0] * maxFrustumRayLength,
				origin[1] + frus4[1] * maxFrustumRayLength,
				origin[2] + frus4[2] * maxFrustumRayLength };

		double minFrustumRayLength = MathUtil.vnorm(scPos) - diagonalLength;
		maxFrustumDepth = maxFrustumRayLength; // reasonable approx for max on frustum depth
		minFrustumDepth = minFrustumRayLength; // reasonable approx for min on frustum depth

		//using this method signature instead of InsertNextPoint(array) because the VTK is manipulating the passed in array!
		points.InsertNextPoint(scPos[0], scPos[1], scPos[2]);
		points.InsertNextPoint(UL[0], UL[1], UL[2]);
		points.InsertNextPoint(UR[0], UR[1], UR[2]);
		points.InsertNextPoint(LL[0], LL[1], LL[2]);
		points.InsertNextPoint(LR[0], LR[1], LR[2]);

		idList.SetId(0, 0);
		idList.SetId(1, 1);
		lines.InsertNextCell(idList);
		idList.SetId(0, 0);
		idList.SetId(1, 2);
		lines.InsertNextCell(idList);
		idList.SetId(0, 0);
		idList.SetId(1, 3);
		lines.InsertNextCell(idList);
		idList.SetId(0, 0);
		idList.SetId(1, 4);
		lines.InsertNextCell(idList);

		frustumPolyData.SetPoints(points);
		frustumPolyData.SetLines(lines);

		vtkPolyDataMapper frusMapper = new vtkPolyDataMapper();
		frusMapper.SetInputData(frustumPolyData);

		frustumActor.SetMapper(frusMapper);
		return frustumActor;
	}

	private Pair<vtkActor, vtkActor> processOfflimb(RenderablePointedImage renderableImage, List<vtkImageData> imageData) throws IOException, Exception
	{
		vtkActor offLimbActor;
		vtkActor offLimbBoundaryActor;
		offLimbFootprintDepth = renderableImage.getOfflimbDepth();
		List<vtkActor> actors = Lists.newArrayList();
		double[] boundingBox = pipeline.getFootprintPolyData().get(0).GetBounds();
		Just.of(renderableImage)
			.operate(new OfflimbPlaneGeneratorOperators(offLimbFootprintDepth, smallBodyModels, boundingBox, (int)pipeline.getFootprintPolyData().get(0).GetNumberOfPoints()))
			.operate(new OfflimbActorOperator(imageData))
			.subscribe(Sink.of(actors))
			.run();
		offLimbActor = actors.get(0);
		offLimbBoundaryActor = actors.get(1);

		offLimbActor.SetVisibility(offLimbVisibility ? 1 : 0);
		offLimbBoundaryActor.SetVisibility(offLimbBoundaryVisibility ? 1 : 0);
		return Pair.of(offLimbActor, offLimbBoundaryActor);
	}

	private void generateOfflimbActors()
	{
		if (generateOfflimb == true && offLimbActor == null)
		{
			Pair<vtkActor, vtkActor> offLimbActors;
			try
			{
				offLimbActors = processOfflimb((RenderablePointedImage)image, imageData);
				offLimbActor = offLimbActors.getLeft();
				offLimbBoundaryActor = offLimbActors.getRight();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generateOfflimb = false;
		}
	}

	@Override
	public vtkActor getOffLimb()
	{
		generateOfflimbActors();
		return offLimbActor;
	}

	@Override
	public vtkActor getOffLimbBoundary()
	{
		generateOfflimbActors();
		return offLimbBoundaryActor;
	}

	/**
	 * @return the modifiedFootprintActors
	 */
	public List<vtkActor> getModifiedFootprintActors()
	{
		return modifiedFootprintActors;
	}

	/**
	 * @return the modifiedFrustumActor
	 */
	public vtkActor getModifiedFrustumActor()
	{
		return modifiedFrustumActor;
	}

	/**
	 * @return the modifiedBoundaryActors
	 */
	public List<vtkActor> getModifiedBoundaryActors()
	{
		return modifiedBoundaryActors;
	}
}
