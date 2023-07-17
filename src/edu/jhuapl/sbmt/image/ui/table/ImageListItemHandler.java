package edu.jhuapl.sbmt.image.ui.table;

import java.text.DecimalFormat;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;

public class ImageListItemHandler<G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable > extends BasicItemHandler<G1, ImageColumnLookup>
{
	private final PerspectiveImageCollection<G1> imageCollection;

	public ImageListItemHandler(PerspectiveImageCollection<G1> aManager, QueryComposer<ImageColumnLookup> aComposer)
	{
		super(aComposer);

		imageCollection = aManager;
	}

	@Override
	public Object getColumnValue(G1 image, ImageColumnLookup aEnum)
	{
		DecimalFormat formatter = new DecimalFormat("##.####");
		switch (aEnum)
		{
			case Map:
				return imageCollection.getImageMapped(image);
			case Status:
				return imageCollection.getImageStatus(image);
			case Offlimb:
				return imageCollection.getImageOfflimbShowing(image);
			case Frustum:
				return imageCollection.getFrustumShowing(image);
			case Boundary:
				return imageCollection.getImageBoundaryShowing(image);
			case Id:
				return image.getIndex();
			case Filename:
				return FilenameUtils.getName(image.getName());
			case Date:	//this is till LONG coming from the server, so Date is right here
				return image.getDate().toInstant().toString();
//			case Source:
//				return imageCollection.getImageOrigin(image);
			case Dimension:
				return imageCollection.getImageNumberOfLayers(image);
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(G1 image, ImageColumnLookup aEnum, Object aValue)
	{
		if (aEnum == ImageColumnLookup.Map)
		{
			imageCollection.setImageMapped(image, (Boolean)aValue);
		}
		else if (aEnum == ImageColumnLookup.Offlimb)
		{
			if (image.getNumberOfLayers() == 1)
				imageCollection.setImageOfflimbShowing(image, (Boolean)aValue);
		}
		else if (aEnum == ImageColumnLookup.Frustum)
		{
			if (image.getNumberOfLayers() != 3)
				imageCollection.setImageFrustumVisible(image, (Boolean)aValue);
		}
		else if (aEnum == ImageColumnLookup.Boundary)
		{
			imageCollection.setImageBoundaryShowing(image, (Boolean)aValue);
		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}
