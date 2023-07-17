package edu.jhuapl.sbmt.image.model;

public enum ImageOrigin {
	SERVER("Server"),
	LOCAL("Local"),
	COMPOSITE("Composite");

	private String name;

	private ImageOrigin(String name)
	{
		this.name = name;
	}

	public String getFullName()
	{
		return name;
	}

	public static ImageOrigin valueFor(String description)
    {
        for (ImageOrigin source : values())
        {
            if (source.getFullName().equals(description))
            {
                return source;
            }
        }
        return null;
    }
}