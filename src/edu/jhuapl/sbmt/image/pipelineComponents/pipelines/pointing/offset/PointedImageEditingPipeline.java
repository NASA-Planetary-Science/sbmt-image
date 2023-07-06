package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointing.offset;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset.PointedImageRotateAboutTargetOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset.PointedImageRotateTargetPixelDirToLocalOriginOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset.PointedImageTranslateInImagePlaneOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.offset.PointedImageZoomOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;

public class PointedImageEditingPipeline
{
	Pair<SpacecraftPointingState, SpacecraftPointingDelta>[] finalAdjustment = new Pair[1];

	public PointedImageEditingPipeline(SpacecraftPointingState pointingState, SpacecraftPointingDelta pointingDelta) throws Exception
	{
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> inputs =
				Pair.of(pointingState, pointingDelta);

		Just.of(inputs)
			.operate(new PointedImageRotateTargetPixelDirToLocalOriginOperator())
			.operate(new PointedImageTranslateInImagePlaneOperator())
			.operate(new PointedImageRotateAboutTargetOperator())
			.operate(new PointedImageZoomOperator())
			.subscribe(PairSink.of(finalAdjustment))
			.run();
	}

	public Pair<SpacecraftPointingState, SpacecraftPointingDelta> getFinalState()
	{
		return finalAdjustment[0];
	}
}
