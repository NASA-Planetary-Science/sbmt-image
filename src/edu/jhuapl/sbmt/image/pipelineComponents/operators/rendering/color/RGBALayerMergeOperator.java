package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color;

import java.io.IOException;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelVector;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class RGBALayerMergeOperator extends BasePipelineOperator<Layer, Layer>
{
	PixelVector slicePixel;

	public RGBALayerMergeOperator(PixelVector slicePixel)
	{
		this.slicePixel = slicePixel;
	}

	@Override
	public void processData() throws IOException, Exception
	{
//		Preconditions.checkNotNull(slicePixel);
////		Preconditions.checkArgument(slicePixel.size() > index);
//		Layer inputLayer = inputs.get(0);
//		DoubleGetter2d getter = new DoubleGetter2d() {
//
//			@Override
//			public double get(int i, int j)
//			{
//				PixelVector pixel = new PixelVectorDoubleFactory().of(inputLayer.dataSizes().size(), Double.NaN);
//				inputLayer.get(i, j, pixel);
////				PixelDouble vecPixel = (PixelDouble)pixel.get(k);
//				double red = ((PixelDouble)pixel.get(0)).get();
//				double green = ((PixelDouble)pixel.get(1)).get();
//				double blue = ((PixelDouble)pixel.get(2)).get();
//				double alpha = 1.0;
//				if (inputLayer.dataSizes().size() == 4) alpha = ((PixelDouble)pixel.get(3)).get();
//
//			}
//
//		};
//		LayerDoubleBuilder builder = new LayerDoubleBuilder();
//		builder = builder.doubleGetter(getter, (int)(inputs.get(0).dataSizes().get(0)), (int)(inputs.get(0).dataSizes().get(1)));
//		Layer combinedLayer = builder.build();
//		outputs.add(combinedLayer);

//		Function<Layer, Layer> combinedLayer = layer ->
//		{
//			Preconditions.checkNotNull(layer);
//
//			List<Integer> dataSizes = layer.dataSizes();
//			Preconditions.checkNotNull(dataSizes);
//
//			Integer size;
//			if (dataSizes.isEmpty())
//			{
//				// Slicing a scalar layer is OK, though that will force index to
//				// be 0 below.
//				size = Integer.valueOf(1);
//			} else
//			{
//				// Slicing a vector layer is OK.
//				Preconditions.checkArgument(dataSizes.size() == 1);
//				size = dataSizes.get(0);
//			}
//
//			// Confirm the layer has at least *some* data.
//			Preconditions.checkNotNull(size);
////			Preconditions.checkArgument(size > index);
//
//			return new BasicLayer(layer.iSize(), layer.jSize())
//			{
//
//				@Override
//				public List<Integer> dataSizes()
//				{
//					return ImmutableList.of(Integer.valueOf(1));
//				}
//
//				@Override
//				protected void getElement(int i, int j, int k, Pixel p)
//				{
//					layer.get(i, j, slicePixel);
//					double red = slicePixel.get(0);
//					double green = slicePixel.get(1);
//					double blue = slicePixel.get(2);
//					double alpha = 1.0;
//					if (size == 4) alpha = slicePixel.get(3);
//					p.assignFrom(slicePixel.get(0));
//				}
//
//				@Override
//				protected void getVector(int i, int j, PixelVector pv)
//				{
//					layer.get(i, j, slicePixel);
//
//					pv.get(0).assignFrom(slicePixel.get(index));
//
//					for (int k = 1; k < pv.size(); ++k)
//					{
//						Pixel p = pv.get(k);
//						p.setInBounds(false);
//					}
//				}
//
//				@Override
//				public boolean isGetAccepts(Class<?> pixelType)
//				{
//					return layer.isGetAccepts(pixelType);
//				}
//			};
//
//		};
//		Layer compositeLayer = combinedLayer.apply(inputs.get(0));
//		outputs.add(compositeLayer);
	}
}
