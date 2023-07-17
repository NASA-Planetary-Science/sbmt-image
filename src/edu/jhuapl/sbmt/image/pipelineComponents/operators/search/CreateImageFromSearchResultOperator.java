package edu.jhuapl.sbmt.image.pipelineComponents.operators.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image.model.ImageOrigin;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.image.util.ImageFileUtil;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class CreateImageFromSearchResultOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Triple<List<List<String>>, IImagingInstrument, PointingSource>, G1>
{
	private List<List<String>> results;
	private PointingSource imageSource;
	private IImagingInstrument instrument;

	public CreateImageFromSearchResultOperator()
	{
	    super();
	}

	@Override
	public void processData() throws IOException, Exception
	{
		results = inputs.get(0).getLeft();
		imageSource = inputs.get(0).getRight();
		instrument = inputs.get(0).getMiddle();
		outputs = new ArrayList<G1>();
		int i=1;
        for (List<String> imageInfo : results)
        {
        	String pointingSource = new ImageFileUtil().getPointingServerPath(imageInfo.get(0), instrument, imageSource);

//        	String imagePath = "images";
//        	if (viewConfig.getUniqueName().contains("Bennu")) imagePath = "images/public";
//
////        	String infoBaseName = FilenameUtils.removeExtension(imageInfo.get(0)).replace(imagePath, pointingDir);
//        	String infoBaseName = rootDir  + File.separator + pointingDir + File.separator +  FilenameUtils.removeExtension(FilenameUtils.getBaseName(imageInfo.get(0)));
//        	if (extension == ".SUM")
//    		{
//	        	if (viewConfig.getUniqueName().contains("Eros"))
//	    		{
//	        		//MSI doesn't have a makesumfiles.in to query.
//	        		String filename = FilenameUtils.getBaseName(imageInfo.get(0).substring(imageInfo.get(0).lastIndexOf("/")));
//	            	String filenamePrefix = filename.substring(0, filename.indexOf("_"));
//	        		infoBaseName = infoBaseName.replace(filename, filenamePrefix.substring(0, filenamePrefix.length()-2));
//        		}
//	        	else //all other sumfiles based instruments should have makesumfiles.in
//	        	{
//					try
//					{
//						infoBaseName = infoBaseName.substring(0, infoBaseName.lastIndexOf("/")) + File.separator + getSumFileName(instrument.getSearchQuery().getRootPath(), imageInfo.get(0));
//					}
//        			catch (IOException | ParseException | NonexistentRemoteFile e)
//					{
//        				String filename = FilenameUtils.getBaseName(imageInfo.get(0).substring(imageInfo.get(0).lastIndexOf("/")));
//						infoBaseName = infoBaseName.substring(0, infoBaseName.lastIndexOf("/"))
//										+ File.separator
//										+ filename;
//					}
//	        	}
//    		}
        	PerspectiveImageMetadata image = new PerspectiveImageMetadata(imageInfo.get(0), instrument.getType(), imageSource, pointingSource, new double[] {});

        	Orientation orientation = instrument.getOrientation(imageSource);
        	image.setFlip(orientation.getFlip().flip());
        	image.setRotation(orientation.getRotation());
        	image.setImageOrigin(ImageOrigin.SERVER);

        	image.setLinearInterpolatorDims(instrument.getLinearInterpolationDims());
        	image.setMaskValues(instrument.getMaskValues());
        	image.setFillValues(instrument.getFillValues());
        	image.setImageBinPadding(instrument.getBinPadding());
        	image.setLongTime(Long.parseLong(imageInfo.get(1)));
        	CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
        	compImage.setIndex(i++);
        	outputs.add((G1)compImage);
        }
	}

}
