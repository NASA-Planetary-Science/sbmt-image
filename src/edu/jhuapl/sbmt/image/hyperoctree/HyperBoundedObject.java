package edu.jhuapl.sbmt.image.hyperoctree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;

public class HyperBoundedObject
{

//    protected double[] data = new double[8]; // TODO how are the position/dimensions of pictures defined?
    // data = {minx, maxx, miny, maxy, minz, maxz, mint, maxt};
    protected String name;
    protected int fileNum;
    protected HyperBox bbox;

    public HyperBoundedObject(DataInputStream stream)
    {
        // TODO create a bounded object from an input stream
    }

    public HyperBoundedObject(String objName, int objId, HyperBox objBBox)
    {
        name = objName;
        fileNum = objId;
        bbox = objBBox;
    }


    // TODO fix the read/write functions based on file format

    public void read(DataInputStream inputStream) throws IOException, HyperDimensionMismatchException
    {
        double[] data = new double[8];
        for (int i=0; i<8; i++)
            data[i]=inputStream.readDouble();
        fileNum=inputStream.readInt();

        // TODO fix these coordinates.  make a method BoundingBoxFromData(double[] data) that finds the appropriate boudns
        bbox = new HyperBox(new double[]{data[0], data[2], data[4], data[6]}, new double[]{data[1], data[3], data[5], data[7]});
    }

    public void write(DataOutputStream outputStream) throws IOException
    {
        double[] data = getData();
        for (int i=0; i<8; i++)
            outputStream.writeDouble(data[i]);
        outputStream.writeInt(fileNum);
    }

    private double[] getData()
    {
        return bbox.getBounds();
    }

    public int getSizeInBytes()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getCoordinate(int i)
    {
//        return data[i];
        return 0;
    }

    public HyperBox getBbox()
    {
        return bbox;
    }

}
