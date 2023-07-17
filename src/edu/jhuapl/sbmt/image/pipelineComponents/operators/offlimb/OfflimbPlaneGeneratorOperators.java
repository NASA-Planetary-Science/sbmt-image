package edu.jhuapl.sbmt.image.pipelineComponents.operators.offlimb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkImageCanvasSource2D;
import vtk.vtkImageData;
import vtk.vtkImageToPolyDataFilter;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.ThreadService;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadPolydataFromCachePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.SavePolydataToCachePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.CameraOrientationPipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class OfflimbPlaneGeneratorOperators extends BasePipelineOperator<RenderablePointedImage, Pair<RenderablePointedImage, vtkPolyData>>
{
	double offLimbFootprintDepth;
	private List<SmallBodyModel> smallBodyModels;
	private RenderablePointedImage renderableImage;
	private vtkPolyData imagePolyData;
	private double[] spacecraftPosition;
    private double[] focalPoint;
    private double[] upVector;
    private double minFrustumRayLength;
    private double maxFrustumRayLength;
    private double fovx;
    private double fovy;
    private int szW;
    private int szH;
    private vtkImageData imageData;
    private Rotation lookRot;
    private Rotation upRot;
    private Vector3D scPosVector;
    private double[] boundingBox;
    private int numberPoints = -1;
    private double macroPixelResolution = 3;

	public OfflimbPlaneGeneratorOperators(double offLimbFootprintDepth, List<SmallBodyModel> smallBodyModels, double[] boundingBox, int numberPoints)
	{
//		this.offLimbFootprintDepth = offLimbFootprintDepth;
		this.smallBodyModels = smallBodyModels;
		this.boundingBox = boundingBox;
		this.numberPoints = numberPoints;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		renderableImage = inputs.get(0);
        String offLimbImageDataFileName = getPrerenderingFileNameBase() + "_offLimbImageData.vtk.gz";
		this.offLimbFootprintDepth = renderableImage.getOfflimbDepth();

		//check for an existing footprint first
		imagePolyData = LoadPolydataFromCachePipeline.of(offLimbImageDataFileName).orNull();
//		if (imagePolyData == null)
		{
			imagePolyData=new vtkPolyData();
			processStep1();
			processStep2();
			processStep3();

	        SavePolydataToCachePipeline.of(imagePolyData, offLimbImageDataFileName);
		}

        outputs.add(Pair.of(renderableImage, imagePolyData));
	}

    private String getPrerenderingFileNameBase()
    {
        String imageName = renderableImage.getFilename();
        String topPath = FileCache.instance().getFile(imageName).getParent();
        String result = SafeURLPaths.instance().getString(topPath, "support",
        												  renderableImage.getImageSource().name(),
        												  FilenameUtils.getBaseName(imageName) + "_" + smallBodyModels.get(0).getModelResolution() + "_" + smallBodyModels.get(0).getModelName());

        return result;
    }

	private void processStep1() throws IOException, Exception
	{
		PointingFileReader infoReader = renderableImage.getPointing();
		double[] scPos = infoReader.getSpacecraftPosition();
    	minFrustumRayLength = MathUtil.vnorm(scPos)
				- smallBodyModels.get(0).getBoundingBoxDiagonalLength();
    	maxFrustumRayLength = MathUtil.vnorm(scPos)
				+ smallBodyModels.get(0).getBoundingBoxDiagonalLength();

        // Step (1): Discretize the view frustum into macro-pixels, from which geometry will later be derived
        // (1a) get camera parameters
		spacecraftPosition = new double[3];
        focalPoint = new double[3];
        upVector = new double[3];

		CameraOrientationPipeline pipeline = CameraOrientationPipeline.of(renderableImage, List.of(smallBodyModels.get(0)), boundingBox);
		spacecraftPosition = pipeline.getSpacecraftPosition();
		focalPoint = pipeline.getFocalPoint();
		upVector = pipeline.getUpVector();
		if (Double.isNaN(offLimbFootprintDepth))
			this.offLimbFootprintDepth = new Vector3D(spacecraftPosition).getNorm();
        fovx = renderableImage.getHorizontalFovAngle();
        fovy = renderableImage.getVerticalFovAngle();

        // (1b) guess at a resolution for the macro-pixels; these will be used to create quadrilateral cells (i.e. what will eventually be the off-limb geometry) in the camera view-plane
        int res = (int)(Math.sqrt(numberPoints)*macroPixelResolution);

//        int res=(int)Math.sqrt(renderableImage.getFootprint(renderableImage.getDefaultSlice()).GetNumberOfPoints());    // for now just grab the number of points in the on-body footprint; assuming img is roughly planar we apply sqrt to get an approximate number of points on a "side" of the on-body geometry, within the rectangular frustuma
        int[] resolution = new int[]{res,res};    // cast to int and store s- and t- resolutions; NOTE: s and t can be thought of as respectively "horizontal" and "vertical" when viewing the image in the "Properties..." pane (t-hat cross s-hat = look direction in a righthanded coordinate system)
        // allow for later possibility of unequal macro-pixel resolutions; take the highest resolution
        szW = resolution[0];
        szH = resolution[1];
	}

	private void processStep2() throws Exception
	{
		// Step (2): Shoot rays from the camera position toward each macro-pixel & record which ones don't hit the body (these will comprise the off-limb geometry)
        // (2a) determine ray-cast depth; currently implemented as camera-to-origin distance plus body bounding-box diagonal length -- that way rays will always extend from the camera position past the entire body
        scPosVector = new Vector3D(spacecraftPosition);
//        int currentSlice = img.getCurrentSlice();
//        if (frustum.getMinFrustumDepth(currentSlice)==0)
//        	renderableImage.setMinFrustumDepth(currentSlice, 0);
//        if (renderableImage.getMaxFrustumDepth(currentSlice)==0)
//        	renderableImage.setMaxFrustumDepth(currentSlice, scPos.getNorm() + img.getSmallBodyModel().getBoundingBoxDiagonalLength());
//        double maxRayDepth=(img.getMinFrustumDepth(currentSlice) + img.getMaxFrustumDepth(currentSlice));

        double maxRayDepth = minFrustumRayLength + maxFrustumRayLength;
        double ffacx=maxRayDepth*Math.tan(Math.toRadians(fovx/2));    // img is the scaling factor in the plane perpendicular to the boresight, which maps unit vectors from the camera position onto the chosen max depth, in frustum coordinates, thus forming a ray
        double ffacy=maxRayDepth*Math.tan(Math.toRadians(fovy/2));

        // (2b) figure out rotations lookRot and upRot, which transform frustum (s,t) coordinates into a direction in 3D space, pointing in the implied direction from the 3D camera position:
        //    (A) lookRot * negative z-hat = look-hat               [ transform negative z-hat from 3D space into the look direction ; img is the boresight of the camera ]
        //    (B) upRot * (lookRot * y-hat) = up-hat = t-hat        [ first transform y-hat from 3D space into the "boresight frame" (it was perpendicular to z-hat before, so will now be perpendicular to the boresight direction) and second rotate that vector around the boresight axis to align with camera up, i.e. t-hat
        // NOTE: t-hat cross s-hat = look-hat, thus completing the frustum coordinate system
        // NOTE: given two scalar values -1<=s<=1 and -1<=t<=1, the corresponding ray extends (in 3D space) from the camera position to upRot*lookRot*
        Vector3D lookVec=new Vector3D(focalPoint).subtract(new Vector3D(spacecraftPosition));
        Vector3D upVec=new Vector3D(upVector);
        lookRot=new Rotation(Vector3D.MINUS_K, lookVec.normalize());
        upRot=new Rotation(lookRot.applyTo(Vector3D.PLUS_J), upVec.normalize());

        //TODO this used to check local disk for an existing footprint - need to reimplement this with pipeline methodology
//        vtkPolyData imagePolyData = getOffLimbImageData(img, offLimbFootprintDepth);
//    	if ((imagePolyData != null) && (offLimbFootprintDepth == currentDepth))
//    	{
//    		makeActors(img);
//    		return;
//    	}


//    	this.currentDepth = offLimbFootprintDepth;

    	//pull from cache didn't work; build it in memory instead

        // (2c) use a vtkImageCanvasSource2D to represent the macro-pixels, with an unsigned char color type "true = (0, 0, 0) = ray hits surface" and "false = (255, 255, 255) = ray misses surface"... img might seem backwards but 0-values can be thought of as forming the "shadow" of the body against the sky as viewed by the camera
        // NOTE: img could be done more straightforwardly (and possibly more efficiently) just by using a java boolean[][] array... I think the present implementation is a hangover from prior experimentation with a vtkPolyDataSilhouette filter
        vtkImageCanvasSource2D imageSource=new vtkImageCanvasSource2D();
        imageSource.SetScalarTypeToUnsignedChar();
        imageSource.SetNumberOfScalarComponents(3);
        imageSource.SetExtent(-szW/2, szW/2, -szH/2, szH/2, 0, 0);

        ThreadService.initialize(60);
        final List<Future<Void>> resultList;
		List<Callable<Void>> taskList = new ArrayList<>();

		for (int i=-szW/2; i<=szW/2; i++)
            for (int j=-szH/2; j<=szH/2; j++)
            {
                double s=(double)i/((double)szW/2)*ffacx;
                double t=(double)j/((double)szH/2)*ffacy;
                Rotation lookRot=new Rotation(Vector3D.MINUS_K, lookVec.normalize());
                Vector3D ray=new Vector3D(s,t,-maxRayDepth);  // ray construction starts from s,t coordinates each on the interval [-1 1]
                ray=upRot.applyTo(lookRot.applyTo(ray));//upRot.applyInverseTo(lookRot.applyInverseTo(ray.normalize()));
                Vector3D rayEnd=ray.add(scPosVector);
				Callable<Void> task = new OfflimbColorTask(scPosVector, rayEnd, imageSource, i, j);
				taskList.add(task);
            }
//		System.out.println("OfflimbPlaneGeneratorOperators: processStep2: waiting for tasks " + taskList.size());
		resultList = ThreadService.submitAll(taskList);
//		System.out.println("OfflimbPlaneGeneratorOperators: processStep2: tasks returned");

		//Serial way
//        for (int i=-szW/2; i<=szW/2; i++)
//            for (int j=-szH/2; j<=szH/2; j++)
//            {
//                double s=(double)i/((double)szW/2)*ffacx;
//                double t=(double)j/((double)szH/2)*ffacy;
//                Vector3D ray=new Vector3D(s,t,-maxRayDepth);  // ray construction starts from s,t coordinates each on the interval [-1 1]
//                ray=upRot.applyTo(lookRot.applyTo(ray));//upRot.applyInverseTo(lookRot.applyInverseTo(ray.normalize()));
//                Vector3D rayEnd=ray.add(scPosVector);
//                //
//                for (int k=0; k<smallBodyModels.size(); k++)
//                {
//                	vtkIdList ids=new vtkIdList();
////	                smallBodyModels.get(k).getCellLocator().FindCellsAlongLine(scPosVector.toArray(), rayEnd.toArray(), 1e-12, ids);
//	                if (ids.GetNumberOfIds()>0)
//	                    imageSource.SetDrawColor(0,0,0);
//	                else
//	                    imageSource.SetDrawColor(255,255,255);
//	                imageSource.DrawPoint(i, j);
//                }
//            }
        imageSource.Update();
        imageData=imageSource.GetOutput();
//        VTKDebug.previewVtkImageData(imageData, "Sillouhette");
	}

	private class OfflimbColorTask implements Callable<Void>
	{

		private Vector3D scPosVector, rayEnd;
		private vtkImageCanvasSource2D imageSource;
		private int i, j;

		public OfflimbColorTask(Vector3D scPosVector, Vector3D rayEnd, vtkImageCanvasSource2D imageSource, int i, int j)
		{
			this.scPosVector = scPosVector;
			this.rayEnd = rayEnd;
			this.imageSource = imageSource;
			this.i = i;
			this.j = j;
		}

		@Override
		public Void call() throws Exception
		{

			synchronized (OfflimbColorTask.class)
			{
				vtkIdList ids=new vtkIdList();
	            smallBodyModels.get(0).getCellLocator().FindCellsAlongLine(scPosVector.toArray(), rayEnd.toArray(), 1e-12, ids);
	            if (ids.GetNumberOfIds()>0)
	                imageSource.SetDrawColor(0,0,0);
	            else
	                imageSource.SetDrawColor(255,255,255);
	            imageSource.DrawPoint(i, j);
			}

            return null;
		}
	}


	private void processStep3()
	{
		// (3) process the resulting black & white 2D image, which captures the "shadow" of the body against the sky, from the viewpoint of the camera

        // (3a) Actually create some polydata from the imagedata... pixels in the image-data become pairs of triangles (each pair forming a quad)
        vtkImageToPolyDataFilter imageConverter=new vtkImageToPolyDataFilter();
        imageConverter.SetInputData(imageData);
        imageConverter.SetOutputStyleToPixelize();
        imageConverter.Update();
        vtkPolyData tempImagePolyData=imageConverter.GetOutput();   // NOTE: the output of vtkImageToPolyDataFilter is in pixel coordinates, e.g. 0 to width-1 and 0 to height-1, probably with origin at the top-left of the image
        // (3b) create a new cell array to hold "white" cells, i.e. "off-limb" cells, from the "temporary" polydata representation of the silhouette
        vtkCellArray cells=new vtkCellArray();
        for (int c=0; c<tempImagePolyData.GetNumberOfCells(); c++)
        {
            double[] rgb=tempImagePolyData.GetCellData().GetScalars().GetTuple3(c);
            if (rgb[0]>0 || rgb[1]>0 || rgb[2]>0)
            {
                vtkCell cell=tempImagePolyData.GetCell(c);
                cells.InsertNextCell(cell.GetPointIds());
            }
        }

        // (3c) assemble a "final" polydata from the new cell array and points of the temporary silhouette (img could eventually be run through some sort of cleaning filter to get rid of orphaned points)
        imagePolyData.SetPoints(tempImagePolyData.GetPoints());
        imagePolyData.SetPolys(cells);
        double sfacx=offLimbFootprintDepth*Math.tan(Math.toRadians(fovx/2)); // scaling factor that "fits" the polydata into the frustum at the given footprintDepth (in the s,t plane perpendicular to the boresight)
        double sfacy=offLimbFootprintDepth*Math.tan(Math.toRadians(fovy/2));

        // make sure all points are reset with the correct transformation (though on-body cells have been culled, topology of the remaining cells doesn't need to be touched; just the respective points need to be unprojected into 3d space)
        for (int i=0; i<imagePolyData.GetNumberOfPoints(); i++)
        {
            Vector3D pt=new Vector3D(imagePolyData.GetPoint(i));            // here's a pixel coordinate
            pt=pt.subtract(new Vector3D((double)szW/2,(double)szH/2,0));    // move the origin to the center of the image, so values range from [-szW/2 szW/2] and [szH/2 szH/2]
            pt=new Vector3D(pt.getX()/((double)szW/2)*sfacx,pt.getY()/((double)szH/2)*sfacy,-offLimbFootprintDepth); // a number of things happen here; first a conversion to s,t-coordinates on [-1 1] in each direction, then scaling of the s,t-coordinates to fit the physical dimensions of the frustum at the chosen depth, and finally translation along the boresight axis to the chosen depth
            pt=scPosVector.add(upRot.applyTo(lookRot.applyTo(pt)));               // transform from (s,t) coordinates into the implied 3D direction vector, with origin at the camera's position in space; depth along the boresight was enforced on the previous line
            imagePolyData.GetPoints().SetPoint(i, pt.toArray());        // overwrite the old (pixel-coordinate) point with the new (3D cartesian) point
        }
//        VTKDebug.writePolyDataToFile(imagePolyData, "/Users/steelrj1/Desktop/offlimbtest.vtk");
	}
}
