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

public class BoundedObjectHyperTreeNode
{

    BoundedObjectHyperTreeNode parent;
    BoundedObjectHyperTreeNode[] children;
    Path path;
    HyperBox bbox;
    int maxPointsPerNode;
    DataOutputStreamPool pool;
    boolean isLeaf = true;
    private int numObjs = 0;

    public BoundedObjectHyperTreeNode(BoundedObjectHyperTreeNode parent, Path path,
            HyperBox bbox, int maxPoints, DataOutputStreamPool pool)
    {
        this.parent=parent;
        this.path=path;
        this.bbox=bbox;
        this.pool=pool;
        this.maxPointsPerNode=maxPoints;
        children=new BoundedObjectHyperTreeNode[(int)Math.pow(2, bbox.getDimension())];
        for (int i=0; i<children.length; i++)
            children[i]=null;
        path.toFile().mkdir();
    }

    protected BoundedObjectHyperTreeNode createNewChild(int i)
    {
        try
        {
            return new BoundedObjectHyperTreeNode(this, getChildPath(i), getChildBounds(i), maxPointsPerNode, pool);
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

    protected HyperBoundedObject createNewBoundedObject(DataInputStream stream)
    {
        return new HyperBoundedObject(stream);
    }

    // TODO  implement adding of images
    public boolean add(HyperBoundedObject obj) throws HyperException, IOException
    {
        if (!isLeaf) {
            for (int i=0; i<getNumberOfChildren(); i++)
                if (children[i].add(obj))
                    return true;
        } else {
            if (isInside(obj)) {
                obj.write(pool.getStream(getDataFilePath()));
                numObjs ++;
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

    public boolean isInside(HyperBoundedObject image) throws HyperException
    {
        return bbox.intersects(image.getBbox());
    }

    public int getNumberOfObjects()
    {
        return numObjs;
    }

    public void split() throws HyperException, IOException
    {
        for (int i=0; i<getNumberOfChildren(); i++)
            children[i]=createNewChild(i); // this creates a bounding box for where it is in comparison to the root
        DataInputStream instream=new DataInputStream(new BufferedInputStream(new FileInputStream(getDataFilePath().toFile())));
        while (instream.available()>0) // for every object in the node
        {
            HyperBoundedObject obj = createNewBoundedObject(instream);
            for (int i=0; i<getNumberOfChildren() ; i++)
                if (children[i].getBoundingBox().contains(obj))
                {
                    children[i].add(obj);
                    break;
                }
        }
        instream.close();
        isLeaf=false;
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

    public BoundedObjectHyperTreeNode getChild(int i)
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
