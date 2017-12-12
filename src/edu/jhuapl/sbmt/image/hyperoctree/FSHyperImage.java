package edu.jhuapl.sbmt.image.hyperoctree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FSHyperImage
{

    protected double[] data=new double[8]; // TODO how are the position/dimensions of pictures defined?
    int fileNum;

    public FSHyperImage(DataInputStream stream)
    {
        // TODO Auto-generated constructor stub
    }

    public void read(DataInputStream inputStream) throws IOException    // the hyperpoint only has 4 coordinates but we need to write all 8
    {
        for (int i=0; i<8; i++)
            data[i]=inputStream.readDouble();
        fileNum=inputStream.readInt();
    }

    public void write(DataOutputStream outputStream) throws IOException
    {
        for (int i=0; i<8; i++)
            outputStream.writeDouble(data[i]);
        outputStream.writeInt(fileNum);
    }

    public int getSizeInBytes()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getCoordinate(int i)
    {
        return data[i];
    }

}
