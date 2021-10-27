package edu.jhuapl.sbmt.image.pipeline;

import java.util.List;

public interface IPipeline<T>
{
	public void run() throws Exception;

	public List<T> getOutput();
}
