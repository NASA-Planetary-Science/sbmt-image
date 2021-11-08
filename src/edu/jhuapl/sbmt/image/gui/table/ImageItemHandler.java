package edu.jhuapl.sbmt.image.gui.table;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image.core.Image;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;
import edu.jhuapl.sbmt.util.TimeUtil;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class ImageItemHandler<S extends Image> extends BasicItemHandler<S, ImageColumnLookup>
{
	private final ImageCollection imageCollection;
	private final PerspectiveImageBoundaryCollection boundaryCollection;
	private final ImageSearchModel imageSearchModel;

	public ImageItemHandler(ImageCollection aManager, ImageSearchModel searchModel, PerspectiveImageBoundaryCollection boundaryCollection, QueryComposer<ImageColumnLookup> aComposer)
	{
		super(aComposer);

		imageCollection = aManager;
		imageSearchModel = searchModel;
		this.boundaryCollection = boundaryCollection;
	}

	@Override
	public Object getColumnValue(S image, ImageColumnLookup aEnum)
	{
		//TODO: Switch to using an index so the get all items doesn't take so long to look up
		switch (aEnum)
		{
			case Map:
				return imageCollection.isImageMapped(image);
			case Show:
				return imageCollection.getVisibility(image);
			case Offlimb:
				return imageCollection.getOfflimbVisibility(image);
			case Frus:
				return imageCollection.getFrustumVisibility(image);
			case Bndr:
				return boundaryCollection.containsBoundary(image);
			case Id:
				return 0; //imageCollection.getImages().;
			case Filename:
				return image.getImageName();
			case Date:
				return TimeUtil.et2str(image.getTime());
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(S image, ImageColumnLookup aEnum, Object aValue)
	{
		String namePrefix = FileUtil.removeExtension(image.getImageName());
		if (aEnum == ImageColumnLookup.Map)
		{
			if (!imageCollection.isImageMapped(image))
			{
				imageSearchModel.loadImage(namePrefix);
			}
			else
			{
				imageSearchModel.unloadImage(namePrefix);
//				boundaryCollection.removeBoundary(image);
//				imageCollection.removeSpectrum(image);
			}
		}
		else if (aEnum == ImageColumnLookup.Show)
		{
			if (!imageCollection.isImageMapped(image)) return;
			imageSearchModel.setImageVisibility(namePrefix, (Boolean)aValue);
		}
		else if (aEnum == ImageColumnLookup.Offlimb)
		{
			if (!imageCollection.isImageMapped(image)) return;
		}
		else if (aEnum == ImageColumnLookup.Frus)
		{
			if (!imageCollection.isImageMapped(image)) return;
			image.setShowFrustum(!image.isFrustumShowing());
		}
		else if (aEnum == ImageColumnLookup.Bndr)
		{
//			if (!imageCollection.isImageMapped(image)) return;
//			if (!boundaryCollection.containsBoundary(image))
//                boundaryCollection.addBoundary(key);
//            else
//                boundaryCollection.removeBoundary(key);
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}