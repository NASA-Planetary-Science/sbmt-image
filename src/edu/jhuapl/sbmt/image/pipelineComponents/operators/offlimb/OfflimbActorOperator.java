package edu.jhuapl.sbmt.image.pipelineComponents.operators.offlimb;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkCleanPolyData;
import vtk.vtkFeatureEdges;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkTexture;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class OfflimbActorOperator extends BasePipelineOperator<Pair<RenderablePointedImage, vtkPolyData>, vtkActor>
{
	private vtkPolyData offLimbPlane=null;
	private vtkActor offLimbActor;
    private vtkTexture offLimbTexture;
    private vtkPolyData offLimbBoundary=null;
    private vtkActor offLimbBoundaryActor;
    List<vtkImageData> imageData = Lists.newArrayList();

    public OfflimbActorOperator(List<vtkImageData> imageData)
    {
    	this.imageData = imageData;
    }

	@Override
	public void processData() throws IOException, Exception
	{
		RenderablePointedImage renderableImage = inputs.get(0).getLeft();
		vtkPolyData imagePolyData = inputs.get(0).getRight();
		PointingFileReader infoReader = renderableImage.getPointing();
		double[] scPos = infoReader.getSpacecraftPosition();
    	double[] frustum1Adjusted = infoReader.getFrustum1();
    	double[] frustum2Adjusted = infoReader.getFrustum2();
    	double[] frustum3Adjusted = infoReader.getFrustum3();
    	double[] frustum4Adjusted = infoReader.getFrustum4();
    	Frustum frustum = new Frustum(scPos,
						    			frustum1Adjusted,
						    			frustum3Adjusted,
						    			frustum4Adjusted,
						    			frustum2Adjusted);
		if (imageData.isEmpty())
		{
	    	 VtkImageRendererOperator imageRenderer = new VtkImageRendererOperator();
	         Just.of(renderableImage.getLayer())
	         	.operate(imageRenderer)
	         	.operate(new VtkImageContrastOperator(renderableImage.getOfflimbIntensityRange()))
	         	.operate(new VtkImageVtkMaskingOperator(renderableImage.getMasking().getMask()))
	         	.subscribe(Sink.of(imageData)).run();
		}

//		//clean up the polydata to merge overlapping sub pixels
    	vtkCleanPolyData cleanFilter = new vtkCleanPolyData();
        cleanFilter.SetInputData(imagePolyData);
        cleanFilter.Update();
        imagePolyData = cleanFilter.GetOutput();

    	// keep a reference to a copy of the polydata
        offLimbPlane=new vtkPolyData();
        offLimbPlane.DeepCopy(imagePolyData);

        PolyDataUtil.generateTextureCoordinates(frustum, renderableImage.getImageWidth(), renderableImage.getImageHeight(), offLimbPlane); // generate (u,v) coords; individual components lie on the interval [0 1]; https://en.wikipedia.org/wiki/UV_mapping

        // now, if there is an "active" image, cf. PerspectiveImage class", then map it to the off-limb polydata
        if (imageData.get(0) != null)
        {
            if (offLimbTexture==null)
            {
                // create the texture first
            	offLimbTexture = new vtkTexture();
                offLimbTexture.SetInputData(imageData.get(0));
                offLimbTexture.Modified();
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
            offLimbActor.SetForceOpaque(true);
            offLimbActor.Modified();

            outputs.add(offLimbActor);

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
//            Color color = img.getOfflimbBoundaryColor();
            //TODO: This needs to be updatable
            Color color = Color.red;
            offLimbBoundaryActor.GetProperty().SetColor(new double[] {color.getRed()/255, color.getGreen()/255, color.getBlue()/255});
            offLimbBoundaryActor.GetProperty().SetLineWidth(1);
            offLimbBoundaryActor.Modified();

            outputs.add(offLimbBoundaryActor);
        }
	}
}
