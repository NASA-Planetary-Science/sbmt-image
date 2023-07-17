package edu.jhuapl.sbmt.image.model;

public class NoOverlapException extends Exception
{
    public NoOverlapException()
    {
        super("No overlap in 3 images");
    }
}