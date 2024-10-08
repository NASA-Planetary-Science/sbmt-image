package edu.jhuapl.sbmt.image.model;

import java.awt.Color;
import java.io.File;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import vtk.vtkActor;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCleanPolyData;
import vtk.vtkFeatureEdges;
import vtk.vtkIdList;
import vtk.vtkImageCanvasSource2D;
import vtk.vtkImageData;
import vtk.vtkImageToPolyDataFilter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkPolyDataWriter;
import vtk.vtkTexture;

public class OffLimbPlaneCalculator
{

    // vtk actors for offlimb plane, texture, and boundary
    private vtkPolyData offLimbPlane=null;
    private vtkActor offLimbActor;
    private vtkTexture offLimbTexture;
    private vtkPolyData offLimbBoundary=null;
    private vtkActor offLimbBoundaryActor;
    private vtkPolyData imagePolyData;
    private double currentDepth;

	private vtkPolyData getOffLimbImageData(PerspectiveImage img, double offLimbFootprintDepth)
	{
//		System.out.println("OffLimbPlaneCalculator: getOffLimbPlane: image/double");
//		// see if one exists
//		if (imagePolyData != null)
//		{
//			System.out.println("OffLimbPlaneCalculator: getOffLimbPlane: polydata not null");
//			return assembleFinalPolydataAtDepth(img, offLimbFootprintDepth);
//		}

		// try to fetch the offlimb image data from the file cache first
		String offLimbImageDataFileName = img.getPrerenderingFileNameBase() + "_offLimbImageData.vtk.gz";
		File file;
		try
		{
			file = FileCache.getFileFromServer(offLimbImageDataFileName);
		}
		catch (UnauthorizedAccessException e)
		{
		    // Report this but continue.
		    e.printStackTrace();
		    file = null;
		}
		catch (Exception e)
		{
		    // Ignore this one.
		    file = null;
		}

		if (file != null)
		{
		    vtkPolyDataReader reader = new vtkPolyDataReader();
//			reader.SetFileName(file.getPath().replaceFirst("\\.[^\\.]*$", ""));
		    reader.SetFileName(file.getAbsolutePath());
		    reader.Update();
		    vtkPolyData offLimbImageData = reader.GetOutput();
		    return offLimbImageData;
//			return assembleFinalPolydataAtDepth(img, offLimbFootprintDepth, offLimbImageData);
		}

		return null;
//		// if it isn't in the cache, generate it, and store it
//		generateOffLimbPlane(img);
//		saveToDisk(offLimbImageDataFileName);
//		System.out.println("OffLimbPlaneCalculator: getOffLimbPlane: assembling at depth time: " + sw.elapsedMillis());
//		return assembleFinalPolydataAtDepth(img, offLimbFootprintDepth);
	}

    /**
     * Core off-limb geometry creation happens here.
     *
     *  Steps are:
     *   (1) Discretize the view frustum of the camera, choosing a "macro-pixel" size for constructing off-limb geometry
     *
     *   (2) For each off-limb pixel shoot a ray from the camera position toward the camera back-plane
     *      + If the ray hits the body, record "true" in img position;
     *      + otherwise record "false"
     *
     *   (3) The resulting black & white 2D image captures the "shadow" of the body against the sky, from the viewpoint of the camera
     *      + Choose an initial off-limb "depth" (presently the camera-origin distance)
     *      + Project off-limb macro-pixels (marked "false") to img depth
     *      + Compile these into a polydata for visualization -- img is the "off-limb geometry"
     *
     *   (4) Construct a vtkTexture from the original raw image, and map it to the off-limb geometry
     *
     *   (5) Extract edges of the off-limb geometry for visualization (presently shown as red lines)
     *
     * @param footprintDepth
     */
    public void loadOffLimbPlane(PerspectiveImage img,
            double offLimbFootprintDepth)  {
    	 // Step (1): Discretize the view frustum into macro-pixels, from which geometry will later be derived

        // (1a) get camera parameters
        double[] spacecraftPosition=new double[3];
        double[] focalPoint=new double[3];
        double[] upVector=new double[3];
        img.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
        final double fovx=img.getHorizontalFovAngle();//img.getMaxFovAngle();
        final double fovy=img.getVerticalFovAngle();

        // (1b) guess at a resolution for the macro-pixels; these will be used to create quadrilateral cells (i.e. what will eventually be the off-limb geometry) in the camera view-plane
        int res=(int)Math.sqrt(img.getFootprint(img.getDefaultSlice()).GetNumberOfPoints());    // for now just grab the number of points in the on-body footprint; assuming img is roughly planar we apply sqrt to get an approximate number of points on a "side" of the on-body geometry, within the rectangular frustuma
        int[] resolution=new int[]{res,res};    // cast to int and store s- and t- resolutions; NOTE: s and t can be thought of as respectively "horizontal" and "vertical" when viewing the image in the "Properties..." pane (t-hat cross s-hat = look direction in a righthanded coordinate system)
        // allow for later possibility of unequal macro-pixel resolutions; take the highest resolution
//        int szMax=Math.max(resolution[0], resolution[1]);
        int szW=resolution[0];//szMax;//(int)(aspect*szMax);
        int szH=resolution[1];//szMax;
//        System.out.println("OffLimbPlaneCalculator: loadOffLimbPlane: res is " + res);
        // Step (2): Shoot rays from the camera position toward each macro-pixel & record which ones don't hit the body (these will comprise the off-limb geometry)

        // (2a) determine ray-cast depth; currently implemented as camera-to-origin distance plus body bounding-box diagonal length -- that way rays will always extend from the camera position past the entire body
        Vector3D scPos=new Vector3D(spacecraftPosition);
        int currentSlice = img.getCurrentSlice();
        if (img.getMinFrustumDepth(currentSlice)==0)
            img.setMinFrustumDepth(currentSlice, 0);
        if (img.getMaxFrustumDepth(currentSlice)==0)
            img.setMaxFrustumDepth(currentSlice, scPos.getNorm()+img.getSmallBodyModel().getBoundingBoxDiagonalLength());
        double maxRayDepth=(img.getMinFrustumDepth(currentSlice)+img.getMaxFrustumDepth(currentSlice));
        double ffacx=maxRayDepth*Math.tan(Math.toRadians(fovx/2));    // img is the scaling factor in the plane perpendicular to the boresight, which maps unit vectors from the camera position onto the chosen max depth, in frustum coordinates, thus forming a ray
        double ffacy=maxRayDepth*Math.tan(Math.toRadians(fovy/2));

        // (2b) figure out rotations lookRot and upRot, which transform frustum (s,t) coordinates into a direction in 3D space, pointing in the implied direction from the 3D camera position:
        //    (A) lookRot * negative z-hat = look-hat               [ transform negative z-hat from 3D space into the look direction ; img is the boresight of the camera ]
        //    (B) upRot * (lookRot * y-hat) = up-hat = t-hat        [ first transform y-hat from 3D space into the "boresight frame" (it was perpendicular to z-hat before, so will now be perpendicular to the boresight direction) and second rotate that vector around the boresight axis to align with camera up, i.e. t-hat
        // NOTE: t-hat cross s-hat = look-hat, thus completing the frustum coordinate system
        // NOTE: given two scalar values -1<=s<=1 and -1<=t<=1, the corresponding ray extends (in 3D space) from the camera position to upRot*lookRot*
        Vector3D lookVec=new Vector3D(focalPoint).subtract(new Vector3D(spacecraftPosition));
        Vector3D upVec=new Vector3D(upVector);
        Rotation lookRot=new Rotation(Vector3D.MINUS_K, lookVec.normalize());
        Rotation upRot=new Rotation(lookRot.applyTo(Vector3D.PLUS_J), upVec.normalize());

    	imagePolyData = getOffLimbImageData(img, offLimbFootprintDepth);
    	if ((imagePolyData != null) && (offLimbFootprintDepth == currentDepth))
    	{
    		makeActors(img);
    		return;
    	}
    	this.currentDepth = offLimbFootprintDepth;
    	//pull from cache didn't work; build it in memory instead

        // (2c) use a vtkImageCanvasSource2D to represent the macro-pixels, with an unsigned char color type "true = (0, 0, 0) = ray hits surface" and "false = (255, 255, 255) = ray misses surface"... img might seem backwards but 0-values can be thought of as forming the "shadow" of the body against the sky as viewed by the camera
        // NOTE: img could be done more straightforwardly (and possibly more efficiently) just by using a java boolean[][] array... I think the present implementation is a hangover from prior experimentation with a vtkPolyDataSilhouette filter
        vtkImageCanvasSource2D imageSource=new vtkImageCanvasSource2D();
        imageSource.SetScalarTypeToUnsignedChar();
        imageSource.SetNumberOfScalarComponents(3);
        imageSource.SetExtent(-szW/2, szW/2, -szH/2, szH/2, 0, 0);
        for (int i=-szW/2; i<=szW/2; i++)
            for (int j=-szH/2; j<=szH/2; j++)
            {
                double s=(double)i/((double)szW/2)*ffacx;
                double t=(double)j/((double)szH/2)*ffacy;
                Vector3D ray=new Vector3D(s,t,-maxRayDepth);  // ray construction starts from s,t coordinates each on the interval [-1 1]
                ray=upRot.applyTo(lookRot.applyTo(ray));//upRot.applyInverseTo(lookRot.applyInverseTo(ray.normalize()));
                Vector3D rayEnd=ray.add(scPos);
                //
                vtkIdList ids=new vtkIdList();
                img.getSmallBodyModel().getCellLocator().FindCellsAlongLine(scPos.toArray(), rayEnd.toArray(), 1e-12, ids);
                if (ids.GetNumberOfIds()>0)
                    imageSource.SetDrawColor(0,0,0);
                else
                    imageSource.SetDrawColor(255,255,255);
                imageSource.DrawPoint(i, j);
            }
        imageSource.Update();
        vtkImageData imageData=imageSource.GetOutput();

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
        imagePolyData=new vtkPolyData();
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
            pt=scPos.add(upRot.applyTo(lookRot.applyTo(pt)));               // transform from (s,t) coordinates into the implied 3D direction vector, with origin at the camera's position in space; depth along the boresight was enforced on the previous line
            imagePolyData.GetPoints().SetPoint(i, pt.toArray());        // overwrite the old (pixel-coordinate) point with the new (3D cartesian) point
        }

        String offLimbImageDataFileName = img.getPrerenderingFileNameBase() + "_offLimbImageData.vtk.gz";
        saveToDisk(FileCache.instance().getFile(offLimbImageDataFileName).getPath());
//        FileCache.refreshStateInfo(offLimbImageDataFileName);
        makeActors(img);

    }

    private void makeActors(PerspectiveImage img)
    {
    	//clean up the polydata to merge overlapping sub pixels
    	vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
        cleanFilter.SetInputData(imagePolyData);
        cleanFilter.Update();
        imagePolyData = cleanFilter.GetOutput();

    	// keep a reference to a copy of the polydata
        offLimbPlane=new vtkPolyData();
        offLimbPlane.DeepCopy(imagePolyData);
        PolyDataUtil.generateTextureCoordinates(img.getFrustum(), img.getImageWidth(), img.getImageHeight(), offLimbPlane); // generate (u,v) coords; individual components lie on the interval [0 1]; https://en.wikipedia.org/wiki/UV_mapping

        // now, if there is an "active" image, cf. PerspectiveImage class", then map it to the off-limb polydata
        if (img.getDisplayedImage()!=null)
        {
            if (offLimbTexture==null)
            {
                // create the texture first
                offLimbTexture = img.getOffLimbTexture();
                offLimbTexture.InterpolateOn();
                offLimbTexture.RepeatOff();
                offLimbTexture.EdgeClampOn();
                offLimbTexture.Modified();
            }
//            img.setDisplayedImageRange(img.getDisplayedRange()); // match off-limb image intensity range to that of the on-body footprint; the "img" method call also takes care of syncing the off-limb vtkTexture object with the displayed raw image, above and beyond what the parent class has to do for the on-body geometry

            // setup off-limb mapper and actor
            vtkPolyDataMapper offLimbMapper=new vtkPolyDataMapper();
            offLimbMapper.SetInputData(offLimbPlane);
            if (offLimbActor==null)
                offLimbActor=new vtkActor();
            offLimbActor.SetMapper(offLimbMapper);
            offLimbActor.SetTexture(offLimbTexture);
            offLimbActor.Modified();

            // generate off-limb edge geometry, with mapper and actor
            vtkFeatureEdges edgeFilter=new vtkFeatureEdges();
            edgeFilter.SetInputData(offLimbPlane);
            edgeFilter.BoundaryEdgesOn();
            edgeFilter.ManifoldEdgesOff();
            edgeFilter.NonManifoldEdgesOff();
            edgeFilter.FeatureEdgesOff();
            edgeFilter.Update();
            offLimbBoundary=new vtkPolyData();
            offLimbBoundary.DeepCopy(edgeFilter.GetOutput());
            vtkPolyDataMapper boundaryMapper=new vtkPolyDataMapper();
            boundaryMapper.SetInputData(offLimbBoundary);
            boundaryMapper.ScalarVisibilityOff();

            if (offLimbBoundaryActor==null)
                offLimbBoundaryActor=new vtkActor();
            offLimbBoundaryActor.SetMapper(boundaryMapper);


            // get color from default boundary color of image
            // set boundary color to this color
            Color color = img.getOfflimbBoundaryColor();
            offLimbBoundaryActor.GetProperty().SetColor(new double[] {color.getRed()/255, color.getGreen()/255, color.getBlue()/255});
            offLimbBoundaryActor.GetProperty().SetLineWidth(1);
            offLimbBoundaryActor.Modified();
        }
    }


    public vtkPolyData getOffLimbPlane()
    {
        return offLimbPlane;
    }

    public void setOffLimbPlane(vtkPolyData offLimbPlane)
    {
        this.offLimbPlane = offLimbPlane;
    }

    public vtkActor getOffLimbActor()
    {
        return offLimbActor;
    }

    public void setOffLimbActor(vtkActor offLimbActor)
    {
        this.offLimbActor = offLimbActor;
    }

    public vtkTexture getOffLimbTexture()
    {
        return offLimbTexture;
    }

    public void setOffLimbTexture(vtkTexture offLimbTexture)
    {
        this.offLimbTexture = offLimbTexture;
    }

    public vtkPolyData getOffLimbBoundary()
    {
        return offLimbBoundary;
    }

    public void setOffLimbBoundary(vtkPolyData offLimbBoundary)
    {
        this.offLimbBoundary = offLimbBoundary;
    }

    public vtkActor getOffLimbBoundaryActor()
    {
        return offLimbBoundaryActor;
    }

    public void setOffLimbBoundaryActor(vtkActor offLimbBoundaryActor)
    {
        this.offLimbBoundaryActor = offLimbBoundaryActor;
    }

    public void saveToDisk(String filename)
    {
        new File(filename).getParentFile().mkdirs();
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(imagePolyData);
        writer.SetFileName(new File(filename).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
    }

    public static void main(String[] args) throws Exception
	{
//    	boolean reprocess = true;
//    	ShapeModelBody body = ShapeModelBody.RQ36;
//        ShapeModelType type = ShapeModelType.ALTWG_SPC_v20190121;
//    	boolean aplVersion = true;
//        final SafeURLPaths safeUrlPaths = SafeURLPaths.instance();
////        String rootURL = safeUrlPaths.getUrl("/disks/d0180/htdocs-sbmt/internal/multi-mission/test");
//        String rootURL = safeUrlPaths.getUrl("https://sbmt.jhuapl.edu/sbmt/prod/");
//    	Configuration.setAPLVersion(aplVersion);
//        Configuration.setRootURL(rootURL);
//
//        SbmtMultiMissionTool.configureMission();
//
//         // authentication
//        Configuration.authenticate();
//        NativeLibraryLoader.loadVtkLibraries();
//         // initialize view config
//        SmallBodyViewConfig.initialize();
//    	SmallBodyViewConfig config = SmallBodyViewConfig.getSmallBodyConfig(body, type);
//    	SmallBodyModel smallBodyModel = SbmtModelFactory.createSmallBodyModel(config);
//    	ImageKeyInterface key = new ImageKey("https://sbmt.jhuapl.edu/sbmt/prod/data/bennu/altwg-spc-v20190121/polycam/images/ocams20181108t042907s071_pol_iofl2pan_53003.fits", ImageSource.GASKELL);
//    	OcamsFlightImage image = OcamsFlightImage.of(key, smallBodyModel, false);
//    	String outputDir = ".";
//    	String filename = outputDir +  File.separator  + FilenameUtils.getBaseName(image.getImageFileFullPath()) + "_" + 1 + "_offLimbImageData.vtk";
//    	File file = new File(filename);
//    	if (file.exists() && (reprocess == false)) return;
//
//    	OffLimbPlaneCalculator calculator = new OffLimbPlaneCalculator();
//    	calculator.loadOffLimbPlane(image, new Vector3D(image.getSpacecraftPosition()).getNorm());
////    	calculator.generateOffLimbPlane(image);
//    	if (!(new File(filename).exists())) new File(filename).getParentFile().mkdirs();
//    	calculator.saveToDisk(filename);
	}
}
