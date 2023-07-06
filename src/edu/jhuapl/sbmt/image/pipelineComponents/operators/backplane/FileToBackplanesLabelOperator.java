package edu.jhuapl.sbmt.image.pipelineComponents.operators.backplane;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class FileToBackplanesLabelOperator extends BasePipelineOperator<Triple<RenderablePointedImage, String, String>, File>
{
	@Override
	public void processData() throws IOException, Exception
	{
		String lblFileName = inputs.get(0).getRight();
		String modelName = inputs.get(0).getMiddle();
		RenderablePointedImage image = inputs.get(0).getLeft();
		StringBuffer strbuf = new StringBuffer("");

        int numBands = 16;

        appendWithPadding(strbuf, "PDS_VERSION_ID               = PDS3");
        appendWithPadding(strbuf, "");

        appendWithPadding(strbuf, "PRODUCT_TYPE                 = DDR");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String dateStr = sdf.format(date).replace(' ', 'T');
        appendWithPadding(strbuf, "PRODUCT_CREATION_TIME        = " + dateStr);
        appendWithPadding(strbuf, "PRODUCER_INSTITUTION_NAME    = \"APPLIED PHYSICS LABORATORY\"");
        appendWithPadding(strbuf, "SOFTWARE_NAME                = \"Small Body Mapping Tool\"");
        appendWithPadding(strbuf, "SHAPE_MODEL                  = \"" + modelName + "\"");

        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "/* This DDR label describes one data file:                               */");
        appendWithPadding(strbuf, "/* 1. A multiple-band backplane image file with wavelength-independent,  */");
        appendWithPadding(strbuf, "/* spatial pixel-dependent geometric and timing information.             */");
        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "OBJECT                       = FILE");

        appendWithPadding(strbuf, "  ^IMAGE                     = \"" + image.getFilename() + "\"");

        appendWithPadding(strbuf, "  RECORD_TYPE                = FIXED_LENGTH");
        appendWithPadding(strbuf, "  RECORD_BYTES               = " + (image.getImageHeight() * 4));
        appendWithPadding(strbuf, "  FILE_RECORDS               = " + (image.getImageWidth() * numBands));
        appendWithPadding(strbuf, "");

        appendWithPadding(strbuf, "  OBJECT                     = IMAGE");
        appendWithPadding(strbuf, "    LINES                    = " + image.getImageHeight());
        appendWithPadding(strbuf, "    LINE_SAMPLES             = " + image.getImageWidth());
        appendWithPadding(strbuf, "    SAMPLE_TYPE              = IEEE_REAL");
        appendWithPadding(strbuf, "    SAMPLE_BITS              = 32");
        appendWithPadding(strbuf, "    CORE_NULL                = 16#F49DC5AE#"); // bit pattern of -1.0e32 in hex

        appendWithPadding(strbuf, "    BANDS                    = " + numBands);
        appendWithPadding(strbuf, "    BAND_STORAGE_TYPE        = BAND_SEQUENTIAL");
        appendWithPadding(strbuf, "    BAND_NAME                = (\"Pixel value\",");
        appendWithPadding(strbuf, "                                \"x coordinate of center of pixel, km\",");
        appendWithPadding(strbuf, "                                \"y coordinate of center of pixel, km\",");
        appendWithPadding(strbuf, "                                \"z coordinate of center of pixel, km\",");
        appendWithPadding(strbuf, "                                \"Latitude, deg\",");
        appendWithPadding(strbuf, "                                \"Longitude, deg\",");
        appendWithPadding(strbuf, "                                \"Distance from center of body, km\",");
        appendWithPadding(strbuf, "                                \"Incidence angle, deg\",");
        appendWithPadding(strbuf, "                                \"Emission angle, deg\",");
        appendWithPadding(strbuf, "                                \"Phase angle, deg\",");
        appendWithPadding(strbuf, "                                \"Horizontal pixel scale, km per pixel\",");
        appendWithPadding(strbuf, "                                \"Vertical pixel scale, km per pixel\",");
        appendWithPadding(strbuf, "                                \"Slope, deg\",");
        appendWithPadding(strbuf, "                                \"Elevation, m\",");
        appendWithPadding(strbuf, "                                \"Gravitational acceleration, m/s^2\",");
        appendWithPadding(strbuf, "                                \"Gravitational potential, J/kg\")");
        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "  END_OBJECT                 = IMAGE");
        appendWithPadding(strbuf, "END_OBJECT                   = FILE");

        appendWithPadding(strbuf, "");
        appendWithPadding(strbuf, "END");

        // return strbuf.toString();
        byte[] bytes = strbuf.toString().getBytes();
        File file = new File(lblFileName + ".lbl");
        OutputStream out = new FileOutputStream(file);
        out.write(bytes, 0, bytes.length);
        out.close();
        outputs.add(file);
	}

	void appendWithPadding(StringBuffer strbuf, String str)
    {
        strbuf.append(str);

        int length = str.length();
        while (length < 78)
        {
            strbuf.append(' ');
            ++length;
        }

        strbuf.append("\r\n");
    }
}
