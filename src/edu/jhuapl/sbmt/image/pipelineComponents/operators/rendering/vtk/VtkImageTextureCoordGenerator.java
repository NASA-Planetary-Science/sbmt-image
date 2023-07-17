package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk;
//package edu.jhuapl.sbmt.image.modules.rendering;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import vtk.vtkFloatArray;
//import vtk.vtkPointData;
//import vtk.vtkPolyData;
//
//import edu.jhuapl.saavtk.util.PolyDataUtil;
//import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;
//
//public class VtkImageTextureCoordGenerator extends BasePipelineOperator<vtkPolyData, vtkPolyData>
//{
//	private vtkFloatArray textureCoords;
//
//	public VtkImageTextureCoordGenerator()
//	{
//		textureCoords = new vtkFloatArray();
//	}
//
//	@Override
//	public void processData() throws IOException, Exception
//	{
//		outputs = new ArrayList<vtkPolyData>();
//		vtkPolyData footprint = inputs.get(0);
//		vtkPointData pointData = footprint.GetPointData();
//        pointData.SetTCoords(textureCoords);
//        PolyDataUtil.generateTextureCoordinates(getFrustum(),
//        										image.getImageWidth(),
//        										image.getImageHeight(),
//        										footprint);
//        pointData.Delete();
//
//		outputs.add(footprint);
//	}
//}
