package edu.jhuapl.sbmt.image.pipeline;

import java.util.function.Function;

public interface IPipeline<Input, Output>
{
	public void runPipeline(Function<Input, Output> function);
}
