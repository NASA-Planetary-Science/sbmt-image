package edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;

import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.gdal.LayerLoaderBuilder;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker2d;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;

public class GDALReader extends BasePipelinePublisher<Layer>
{
	private String filename;
	private boolean isVectorFile;

	public GDALReader(String filename, boolean isVectorFile, ValidityChecker checker, double oobValue) throws InvalidGDALFileTypeException
	{
		this.filename = filename;
		this.isVectorFile = isVectorFile;
		if (FilenameUtils.getExtension(filename).equals("pgm")) throw new InvalidGDALFileTypeException("SBMT does not currently support PGM files.");
		outputs = new ArrayList<Layer>();
		loadData(checker, oobValue);
	}

	private void loadData(ValidityChecker checker, double oobValue) throws InvalidGDALFileTypeException
	{
		synchronized (GDALReader.class)
		{
			if (!isVectorFile)
			{
				Dataset dataset = gdal.Open(filename, 0);
				if (dataset == null)
				{
					//TODO this seems to helo with a threading race; need to investigate more
					System.out.println("GDALReader: loadData: dataset null");
					throw new InvalidGDALFileTypeException("SBMT cannot read the file with the built in GDAL library; please ensure the file format is correct");
//					return;
//					System.exit(1);
				}
				Layer layer = new LayerLoaderBuilder()
						.dataSet(dataset)
						.checker(checker)
						.build()
						.load();
				int numLayers = layer.dataSizes().get(0);
				for (int i=0; i < numLayers; i++)
				{
					 Function<Layer, Layer> transform = new LayerDoubleTransformFactory().slice(i, Double.NaN);
					 Layer singleLayer = transform.apply(layer);
					 outputs.add(singleLayer);
				}

				//insert a composite RGB(A) layer as the 0th layer
				if (dataset.GetDriver().getShortName().equals("PNG") || dataset.GetDriver().getShortName().equals("JPEG") || dataset.GetDriver().getShortName().equals("JPG"))
				{
					outputs.add(0, layer);
				}
				SwingUtilities.invokeLater(() -> {
					dataset.delete();
				});

			}
			else
			{
			    // TODO handle data sources as well?
//				DataSource datasource = ogr.Open(filename);
			}
		}
	}

	public static void main(String[] args) throws InvalidGDALFileTypeException
	{
		NativeLibraryLoader.loadAllVtkLibraries();
        gdal.AllRegister();
        // This is a DART/LICIA/LUKE test image, which is a 3-band UNSIGNED byte
        // image that has both Didymos and Dimorphos visible and fairly large.
        // For a given (i, j), all 3 k-bands have the same pixel value.
        String sampleFile = Paths.get(System.getProperty("user.home"), //
                "Downloads", //
                "liciacube_luke_l0_717506291_294_01.fits").toString();
        String sampleFile2 = Paths.get(System.getProperty("user.home"), //
                "Desktop/SBMT Example Data files/", //
                "Global_20181213_20181201_Shape14_NatureEd.png").toString();
        ValidityChecker2d vc = (i, j, value) -> {
            return !(i == 2047 && j == 0);
        };
        GDALReader reader = new GDALReader(sampleFile2, false, vc, Double.NaN);

	}
}
