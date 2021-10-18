package edu.jhuapl.sbmt.image.modules.io.builtIn;

import java.io.IOException;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipeline.publisher.BasePipelinePublisher;

import nom.tam.fits.FitsException;

public class BuiltInVTKReader extends BasePipelinePublisher<SmallBodyModel>
{
	public static void main(String[] args) throws FitsException, IOException
	{
		BuiltInVTKReader reader = new BuiltInVTKReader("/Users/steelrj1/.sbmt/cache/2/EROS/ver64q.vtk");
	}

	public BuiltInVTKReader(String... filenames)
	{
		try
		{
			this.outputs = Lists.newArrayList();
			for (String filename : filenames)
			{
				SmallBodyModel smallBodyModel = new SmallBodyModel("Eros", PolyDataUtil.loadVTKShapeModel(filename));
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
