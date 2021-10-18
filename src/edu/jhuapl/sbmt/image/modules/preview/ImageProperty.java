package edu.jhuapl.sbmt.image.modules.preview;

public class ImageProperty
{
	private String property;
	private String value;

	public ImageProperty(String property, String value)
	{
		this.property = property;
		this.value = value;
	}

	public String getProperty()
	{
		return property;
	}

	public String getValue()
	{
		return value;
	}

}
