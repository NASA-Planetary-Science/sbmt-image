package edu.jhuapl.sbmt.image.interfaces;

import edu.jhuapl.saavtk.model.FileType;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImageType;

import crucible.crust.metadata.api.Metadata;

public interface ImageKeyInterface
{

	String toString();

	String getName();

	String getImageFilename();

	ImageType getImageType();

	public Metadata store();

	public PointingSource getSource();

	int getSlice();

	IImagingInstrument getInstrument();

	FileType getFileType();

	String getBand();

	String getPointingFile();

	String getOriginalName();

	String getFlip();

	public double getRotation();

}