package edu.jhuapl.sbmt.image.hyperoctree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.lidar.DataOutputStreamPool;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperBox;
import edu.jhuapl.sbmt.lidar.hyperoctree.HyperException;

public class BoundedObjectHyperTreeGenerator
{
    final Path outputDirectory;
    final int maxNumberObjectsPerLeaf;
    final HyperBox bbox;
    final int maxNumberOfOpenOutputFiles;
    final DataOutputStreamPool pool;
    BoundedObjectHyperTreeNode root;
    long totalObjectsWritten = 0;


    BiMap<Path, Integer> fileMap=HashBiMap.create();

    public BoundedObjectHyperTreeGenerator(Path outputDirectory, int maxObjectsPerLeaf, HyperBox bbox, int maxNumberOfOpenOutputFiles, DataOutputStreamPool pool)
    {
        this.outputDirectory = outputDirectory;
        this.maxNumberObjectsPerLeaf = maxObjectsPerLeaf;
        this.maxNumberOfOpenOutputFiles = maxNumberOfOpenOutputFiles;
        this.bbox = bbox; // bounding box of body
        this.pool = pool;
        root = new BoundedObjectHyperTreeNode(null, outputDirectory, bbox, maxObjectsPerLeaf,pool);
    }

    private void addAllObjectsFromFile(String inputFile) throws HyperException, IOException
    {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] toks = line.split(",");
                String objName = toks[0];
                String objId = toks[1];
                HyperBox objBBox = new HyperBox(new double[]{Double.parseDouble(toks[2]), Double.parseDouble(toks[3])},
                        new double[]{Double.parseDouble(toks[4]), Double.parseDouble(toks[5])});

                HyperBoundedObject obj = new HyperBoundedObject(objName, Integer.parseInt(objId), objBBox);
                root.add(obj);
                totalObjectsWritten++;
            }
        }

    }


    public void expand() throws HyperException, IOException
    {
        expandNode(root);
    }

    public void expandNode(BoundedObjectHyperTreeNode node) throws HyperException, IOException
    {
        if (node.getNumberOfObjects() > maxNumberObjectsPerLeaf)
        {
            node.split();
            for (int i=0; i<node.getNumberOfChildren(); i++)
                if (node.childExists(i))
                {
                    System.out.println(node.getChild(i).getPath());
                    expandNode((BoundedObjectHyperTreeNode)node.getChild(i));
                }
        }
    }

    public void commit() throws IOException
    {
        pool.closeAllStreams();// close any files that are still open
        finalCommit(root);
    }

    void finalCommit(BoundedObjectHyperTreeNode node) throws IOException
    {
        File dataFile = node.getDataFilePath().toFile();  // clean up any data files with zero points
        if (!node.isLeaf())
        {
            if (dataFile.exists())
                dataFile.delete();
            for (int i=0; i<8; i++)
                finalCommit((BoundedObjectHyperTreeNode)node.getChild(i));
        }
        else {
            if (!dataFile.exists() || dataFile.length()==0l)
            {
//                node.getBoundsFilePath().toFile().delete();
                node.getPath().toFile().delete();
            }
        }
    }

//    public double convertBytesToMB(long bytes)
//    {
//        return (double)bytes/(double)(1024*1024);
//    }
//
//    public long countBytes()
//    {
//        List<FSHyperTreeNode> nodeList=getAllNonEmptyLeafNodes();
//        long total=0;
//        for (FSHyperTreeNode node : nodeList)
//            total+=node.getDataFilePath().toFile().length();
//        return total;
//    }

    public List<BoundedObjectHyperTreeNode> getAllNonEmptyLeafNodes()
    {
        List<BoundedObjectHyperTreeNode> nodeList=Lists.newArrayList();
        getAllNonEmptyLeafNodes(root, nodeList);
        return nodeList;
    }

    void getAllNonEmptyLeafNodes(BoundedObjectHyperTreeNode node, List<BoundedObjectHyperTreeNode> nodeList)
    {
        if (!node.isLeaf())
            for (int i=0; i<node.getNumberOfChildren(); i++)
                getAllNonEmptyLeafNodes((BoundedObjectHyperTreeNode)node.getChild(i), nodeList);
        else if (node.getDataFilePath().toFile().exists())
            nodeList.add(node);
    }

    private static void printUsage()
    {
        System.out.println("Arguments:");
        System.out.println("  (1) ");
        System.out.println("  (2) output directory to build the search tree in");
        System.out.println("  (3) ");
        System.out.println("  (4) max number of open output files");
        System.out.println("  (5) ");
        System.out.println("  (6) instrument name (options are )");
    }


    public static void main(String[] args) throws IOException, HyperException
    {
        if (args.length!=4)
        {
            printUsage();
            return;
        }

        //String inputDirectoryString=args[0];    // "/Volumes/dumbledore/sbmt/OLA"
        String inputFile = args[0];
        String outputDirectoryString=args[1];   // "/Volumes/dumbledore/sbmt/ola_hypertree"
        double dataFileMBLimit=Double.valueOf(args[2]); // 1
        int maxNumOpenOutputFiles=Integer.valueOf(args[3]);   // 32

        System.out.println("Input file = "+ inputFile);
        System.out.println("Output tree location = "+outputDirectoryString);
        System.out.println("Data file MB limit = "+dataFileMBLimit);
        System.out.println("Max # open output files = "+maxNumOpenOutputFiles);

        NativeLibraryLoader.loadVtkLibrariesHeadless();
        Path inputDirectoryListFile=Paths.get(inputFile);
        Path outputDirectory=Paths.get(outputDirectoryString);

        int dataFileByteLimit=(int)(dataFileMBLimit*1024*1024);
        int maxObjectsPerLeaf = 2;
        DataOutputStreamPool pool=new DataOutputStreamPool(maxNumOpenOutputFiles);


        if (!outputDirectory.toFile().exists())
        {
            System.out.println("Error: Output directory \""+outputDirectory.toString()+"\" does not exist");
            return;
        }
        else
        {
            System.out.println();
            System.out.println("++++++++++++++++++");
            System.out.println("Warning: output directory \""+outputDirectoryString.toString()+"\" already exists; if it is not empty this will cause big problems later. ");
            System.out.println("++++++++++++++++++");
            System.out.println();
        }

        // TODO min and max dimensions for hyperbox around body
        double[] min = {};
        double[] max = {};
        HyperBox hbox = new HyperBox(min, max);
        BoundedObjectHyperTreeGenerator generator = new BoundedObjectHyperTreeGenerator(outputDirectory, maxObjectsPerLeaf, hbox, maxNumOpenOutputFiles, pool);

        generator.addAllObjectsFromFile(inputFile);

        System.out.println("Expanding tree.");
        System.out.println("Max # pts per leaf="+maxObjectsPerLeaf);
        generator.expand();
        System.out.println();
        generator.commit(); // clean up any empty or open data files


        Path fileMapPath = outputDirectory.resolve("fileMap.txt");
        System.out.print("Writing file map to "+fileMapPath+"... ");
        FileWriter writer = new FileWriter(fileMapPath.toFile());
        for (int i : generator.fileMap.inverse().keySet())
            writer.write(i+" "+generator.fileMap.inverse().get(i)+"\n");
        writer.close();
        System.out.println("Done.");
    }


}
