package edu.jhuapl.sbmt.image.hyperoctree;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException.HyperDimensionMismatchException;

public class ImageFSHyperTreeNode
{

    ImageFSHyperTreeNode parent;
    ImageFSHyperTreeNode[] children;
    Path path;
    HyperBox bbox;
    int maxPointsPerNode;
    DataOutputStreamPool pool;
    boolean isLeaf = true;
    private int numPoints = 0;

    public ImageFSHyperTreeNode(ImageFSHyperTreeNode parent, Path path,
            HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        this.parent=parent;
        this.path=path;
        this.bbox=bbox;
        this.pool=pool;
        this.maxPointsPerNode=maxPoints;
        children=new ImageFSHyperTreeNode[(int)Math.pow(2, bbox.getDimension())];
        for (int i=0; i<children.length; i++)
            children[i]=null;
        path.toFile().mkdir();
    }

    protected ImageFSHyperTreeNode createNewChild(int i)
    {
        try
        {
            return new ImageFSHyperTreeNode(this, getChildPath(i), getChildBounds(i), maxPointsPerNode, pool);
        }
        catch (HyperDimensionMismatchException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    protected HyperBox getChildBounds(int i) throws HyperDimensionMismatchException
    {
        double[] min=new double[getDimension()];
        double[] max=new double[getDimension()];
        for (int j=0; j<getDimension(); j++)
        {
            int whichBit=(int)Math.pow(2,j);
            if ((i&whichBit)>>j==0)
            {
                min[j]=bbox.getMin()[j];
                max[j]=bbox.getMid()[j];
            }
            else
            {
                min[j]=bbox.getMid()[j];
                max[j]=bbox.getMax()[j];
            }
        }
        return new HyperBox(min, max);
    }

    public int getDimension()
    {
        return bbox.getDimension();
    }

    public Path getChildPath(int i)
    {
        return path.resolve(String.valueOf(i));
    }

    protected FSHyperImage createNewImage(DataInputStream stream)
    {
        return new FSHyperImage(stream);
    }

    // TODO  implement adding of images
    public boolean add(FSHyperImage image) throws HyperException, IOException
    {
        if (!isLeaf) {
            for (int i=0; i<getNumberOfChildren(); i++)
                if (children[i].add(image))
                    return true;
        } else {
            if (isInside(image)) {
                image.write(pool.getStream(getDataFilePath()));
                numPoints ++;
                return true;
            }
        }
        return false;

    }

    public Path getDataFilePath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNumberOfChildren()
    {
        return children.length;
    }

    public boolean isInside(FSHyperImage image) throws HyperException
    {
        return bbox.contains(image);
    }

    public int getNumberOfPoints()
    {
        return numPoints;
    }

    public void split() throws HyperException, IOException
    {
        pool.closeStream(getDataFilePath());
        for (int i=0; i<getNumberOfChildren(); i++)
            children[i]=createNewChild(i); // this creates a bounding box for where it is in comparison to the root
        DataInputStream instream=new DataInputStream(new BufferedInputStream(new FileInputStream(getDataFilePath().toFile())));
        while (instream.available()>0)
        {
            FSHyperImage im = createNewImage(instream);
            for (int i=0; i<getNumberOfChildren() ; i++)
                if (children[i].getBoundingBox().contains(im))
                {
                    children[i].add(im);
                    break;
                }
        }
        instream.close();
        isLeaf=false;
        deleteDataFile();
    }

    void deleteDataFile() {
        getDataFilePath().toFile().delete();
    }

    private HyperBox getBoundingBox()
    {
        return bbox;
    }

    public boolean childExists(int i)
    {
        return children[i]!=null;
    }

    public ImageFSHyperTreeNode getChild(int i)
    {
        return children[i];
    }

    public Path getPath()
    {
        return path;
    }

    public boolean isLeaf()
    {
        return isLeaf;
    }


}
