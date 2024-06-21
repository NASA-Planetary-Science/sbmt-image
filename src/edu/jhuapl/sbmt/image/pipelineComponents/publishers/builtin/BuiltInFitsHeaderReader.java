package edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;

public class BuiltInFitsHeaderReader extends BasePipelinePublisher<HashMap<String, String>>
{
	public BuiltInFitsHeaderReader(String filename)
	{
		outputs = new ArrayList<HashMap<String, String>>();
		try
		{
			outputs.add(loadHeaders(filename));
		}
		catch (IOException | FitsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private HashMap<String, String> loadHeaders(String filename) throws IOException, FitsException
	{
		HashMap<String, String> properties = new HashMap<String, String>();
		try (Fits f = new Fits(filename))
        {
			BasicHDU hdu = f.getHDU(0);
			Header header = hdu.getHeader();
	        HeaderCard headerCard;
	        while((headerCard = header.nextCard()) != null)
	        {
	            String headerKey = headerCard.getKey();
	            String headerValue = headerCard.getValue();
	            properties.put(headerKey, headerValue);
	        }
        }
		return properties;
	}
}
