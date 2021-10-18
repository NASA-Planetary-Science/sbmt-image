package edu.jhuapl.sbmt.image.modules.io.builtIn;

import java.io.IOException;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipeline.publisher.BasePipelinePublisher;

import nom.tam.fits.FitsException;

public class BuiltInOBJReader extends BasePipelinePublisher<SmallBodyModel>
{
	public static void main(String[] args) throws FitsException, IOException
	{
		BuiltInOBJReader reader = new BuiltInOBJReader(new String[]{"/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT"}, "Eros");
	}

	public BuiltInOBJReader(String[] filenames, String... bodyNames)
	{
		try
		{
			this.outputs = Lists.newArrayList();
			int i=0;
			for (String filename : filenames)
			{
				SmallBodyModel smallBodyModel = new SmallBodyModel(bodyNames[i++], PolyDataUtil.loadOBJShapeModel(filename));
				smallBodyModel.getProps();
				outputs.add(smallBodyModel);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
