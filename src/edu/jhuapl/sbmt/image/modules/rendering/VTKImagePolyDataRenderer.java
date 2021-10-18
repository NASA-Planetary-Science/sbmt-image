//package edu.jhuapl.sbmt.image.modules.rendering;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import vtk.vtkFloatArray;
//import vtk.vtkPointData;
//import vtk.vtkPolyData;
//
//import edu.jhuapl.saavtk.util.Frustum;
//import edu.jhuapl.saavtk.util.PolyDataUtil;
//import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;
//import edu.jhuapl.sbmt.model.image.InfoFileReader;
//
//public class VTKImagePolyDataRenderer extends BasePipelineOperator<RenderableImage, vtkPolyData>
//{
//	private vtkFloatArray textureCoords;
//	private RenderableImage renderableImage;
//
//	public VTKImagePolyDataRenderer()
//	{
//		textureCoords = new vtkFloatArray();
//		renderableImage = inputs.get(0);
//	}
//
//	@Override
//	public void processData() throws IOException, Exception
//	{
//		outputs = new ArrayList<vtkPolyData>();
//		vtkPolyData footprint = new vtkPolyData();
//		InfoFileReader infoReader = renderableImage.getPointing();
//
//		double[] spacecraftPositionAdjusted = infoReader.getSpacecraftPosition();
//    	double[] frustum1Adjusted = infoReader.getFrustum1();
//    	double[] frustum2Adjusted = infoReader.getFrustum2();
//    	double[] frustum3Adjusted = infoReader.getFrustum3();
//    	double[] frustum4Adjusted = infoReader.getFrustum4();
//    	Frustum frustum = new Frustum(spacecraftPositionAdjusted,
//						    			frustum1Adjusted,
//						    			frustum2Adjusted,
//						    			frustum3Adjusted,
//						    			frustum4Adjusted);
//
//    	vtkPolyData tmp = null;
//
//        tmp = image.getSmallBodyModel().computeFrustumIntersection(spacecraftPositionAdjusted,
//        															frustum1Adjusted,
//        															frustum3Adjusted,
//        															frustum4Adjusted,
//        															frustum2Adjusted);
//        if (tmp == null)
//            return;
//
//        // Need to clear out scalar data since if coloring data is being shown,
//        // then the color might mix-in with the image.
//        tmp.GetCellData().SetScalars(null);
//        tmp.GetPointData().SetScalars(null);
//
//
//        footprint.DeepCopy(tmp);
//
//        vtkPointData pointData = footprint.GetPointData();
//        pointData.SetTCoords(textureCoords);
//        PolyDataUtil.generateTextureCoordinates(frustum, renderableImage.getImageWidth(), renderableImage.getImageHeight(), footprint);
//        pointData.Delete();
//
//		outputs.add(footprint);
//	}
//
//}
