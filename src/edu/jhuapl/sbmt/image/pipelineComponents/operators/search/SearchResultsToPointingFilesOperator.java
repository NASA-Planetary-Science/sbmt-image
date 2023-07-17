package edu.jhuapl.sbmt.image.pipelineComponents.operators.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.core.config.ISmallBodyViewConfig;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.util.ImageFileUtil;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class SearchResultsToPointingFilesOperator
		extends BasePipelineOperator<Pair<List<List<String>>, ImagingInstrument>, Triple<List<List<String>>, ImagingInstrument, List<String>>>
{
	private List<List<String>> results;
	private ISmallBodyViewConfig viewConfig;
	private static final Map<String, ImmutableMap<String, String>> SUM_FILE_MAP = new HashMap<>();
	private PointingSource imageSource;

	public SearchResultsToPointingFilesOperator(ISmallBodyViewConfig viewConfig)
	{
		this.viewConfig = viewConfig;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		results = inputs.get(0).getLeft();
		ImagingInstrument instrument = inputs.get(0).getRight();
		outputs = Lists.newArrayList();
		List<String> infoBaseNames = Lists.newArrayList();
		for (List<String> imageInfo : results)
		{
			imageSource = PointingSource.valueFor(imageInfo.get(2).replace("_", " "));
			String pointingSource = new ImageFileUtil().getPointingServerPath(imageInfo.get(0), instrument, imageSource);
			File pointingFile = FileCache.getFileFromServer(pointingSource);
			if (pointingFile.exists())
				infoBaseNames.add(pointingSource);
			else
				infoBaseNames.add(null);

//			System.out.println("SearchResultsToPointingFilesOperator: processData: image info 0 " + imageInfo.get(0));
//			String extension = ".INFO";
//			String pointingDir = "infofiles";
//		    imageSource = ImageSource.valueFor(imageInfo.get(2).replace("_", " "));
//			if (imageSource == ImageSource.GASKELL || imageSource == ImageSource.GASKELL_UPDATED)
//			{
//				extension = ".SUM";
//				pointingDir = "sumfiles";
//				if (viewConfig.getUniqueName().contains("Eros"))
//					pointingDir = "sumfiles_to_be_delivered";
//			}
//			if (imageSource == ImageSource.LABEL)
//			{
//				extension = ".LBL";
//				pointingDir = "labels";
//			}
//
//			String imagePath = "images";
//
//			if (viewConfig.getUniqueName().contains("Bennu")
//					&& Arrays.asList(viewConfig.getPresentInMissions()).contains(Mission.PUBLIC_RELEASE))
//				imagePath = "images/public";
//			String infoBaseName = FilenameUtils.removeExtension(imageInfo.get(0)).replace(imagePath, pointingDir);
//			if (viewConfig.getUniqueName().contains("Eros"))
//			{
//				if (extension == ".SUM")
//				{
//					String filename = FilenameUtils
//							.getBaseName(imageInfo.get(0).substring(imageInfo.get(0).lastIndexOf("/")));
//					String filenamePrefix = filename.substring(0, filename.indexOf("_"));
//					infoBaseName = infoBaseName.replace(filename,
//							filenamePrefix.substring(0, filenamePrefix.length() - 2));
//				}
//			}
//			else
//			{
//				if (extension == ".SUM")
//					try
//					{
//						infoBaseName = infoBaseName.substring(0, infoBaseName.lastIndexOf("/")) + File.separator
//								+ getSumFileName(instrument.getSearchQuery().getRootPath(), imageInfo.get(0));
//					} catch (IOException | ParseException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			}
//			System.out.println("SearchResultsToPointingFilesOperator: processData: adding " + (infoBaseName + extension));
//			infoBaseNames.add(infoBaseName + extension);
		}
		outputs.add(Triple.of(results, instrument, infoBaseNames));
	}

	private String getSumFileName(String imagerDirectory, String imageFilename) throws IOException, ParseException
    {
        if (!SUM_FILE_MAP.containsKey(imagerDirectory))
        {
        	String sumfileName = "make_sumfiles.in";
        	if (imageSource == PointingSource.CORRECTED) sumfileName = "make_sumfiles_corrected.in";
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            File mapFile = FileCache.getFileFromServer(SafeURLPaths.instance().getString(imagerDirectory, sumfileName));
            try (BufferedReader reader = new BufferedReader(new FileReader(mapFile)))
            {
                while (reader.ready())
                {
                    String wholeLine = reader.readLine();
                    String[] line = wholeLine.split("\\s*,\\s*");
                    if (line[0].equals(wholeLine))
                    {
                        line = wholeLine.split("\\s\\s*");
                    }
                    if (line.length < 2) throw new ParseException("Cannot parse line " + String.join(" ", line) + " to get sum file/image file names", line.length > 0 ? line[0].length() : 0);
                    String sumFile = line[0];
                    String imageFile = getImageFileName(line[line.length - 1]);

                    builder.put(imageFile, sumFile);
                }
            }
            SUM_FILE_MAP.put(imagerDirectory, builder.build());
        }

        File imageFile = new File(imageFilename);
        ImmutableMap<String, String> imagerSumFileMap = SUM_FILE_MAP.get(imagerDirectory);
        if (imagerSumFileMap.containsKey(imageFile.getName()))
        {
            return SUM_FILE_MAP.get(imagerDirectory).get(imageFile.getName());
        }
        return null;
    }

	private String getImageFileName(String imageName)
    {
        // If the proposed name does not include the extension, add .fits.
        if (!imageName.matches("^.*\\.[^\\\\.]*$"))
        {
            imageName += ".fits";
        }

        return imageName;
    }
}
