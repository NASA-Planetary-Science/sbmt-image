package edu.jhuapl.sbmt.image.pipelineComponents.operators.backplane;

import edu.jhuapl.sbmt.util.BackplanesFile;
import edu.jhuapl.sbmt.util.ImgBackplanesFile;

public enum BackplanesFileFormat
{
    FITS(new FitsBackplanesFile(), ".fit"), IMG(new ImgBackplanesFile(), ".img");

    private BackplanesFile file;
    private String extension;

    private BackplanesFileFormat(BackplanesFile file, String extension)
    {
        this.file = file;
        this.extension = extension;
    }

    public BackplanesFile getFile()
    {
        return file;
    }

    public String getExtension()
    {
        return extension;
    }
}