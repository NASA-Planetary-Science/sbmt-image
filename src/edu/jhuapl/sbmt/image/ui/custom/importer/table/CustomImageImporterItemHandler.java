package edu.jhuapl.sbmt.image.ui.custom.importer.table;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.BaseItemManager;

public class CustomImageImporterItemHandler<G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable > extends BasicItemHandler<G1, CustomImageImporterColumnLookup>
{
	@SuppressWarnings("unused")
	private final BaseItemManager<G1> imageCollection;

	public CustomImageImporterItemHandler(BaseItemManager<G1> aManager, QueryComposer<CustomImageImporterColumnLookup> aComposer)
	{
		super(aComposer);

		imageCollection = aManager;
	}

	@Override
	public Object getColumnValue(G1 image, CustomImageImporterColumnLookup aEnum)
	{
		switch (aEnum)
		{
			case IMAGE_PATH:
				return image.getFilename();
			case IMAGE_NAME:
				return image.getName();
			case POINTING_FILE:
				return image.getPointingSource();
			case IMAGE_ROTATION:
				return image.getRotation();
			case IMAGE_FLIP:
				return image.getFlip();
			case IMAGE_FLIP_ABOUT_X:
				return image.getFlip().equals("X");
			case LATITUDE_MIN:
				return image.getBounds().minLatitude();
			case LATITUDE_MAX:
				return image.getBounds().maxLatitude();
			case LONGITUDE_MIN:
				return image.getBounds().minLongitude();
			case LONGITUDE_MAX:
				return image.getBounds().maxLongitude();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(IPerspectiveImage image, CustomImageImporterColumnLookup aEnum, Object aValue)
	{
		if (aEnum == CustomImageImporterColumnLookup.IMAGE_FLIP_ABOUT_X)
		{
			image.setFlip((boolean)aValue ? "X" : "None");
		}
//		if (aEnum == CustomImageImporterColumnLookup.Map)
//		{
//			imageCollection.setImageMapped(image, (Boolean)aValue);
//		}
//		else if (aEnum == CustomImageImporterColumnLookup.Offlimb)
//		{
//			imageCollection.setImageOfflimbShowing(image, (Boolean)aValue);
//		}
//		else if (aEnum == CustomImageImporterColumnLookup.Frustum)
//		{
//			imageCollection.setImageFrustumVisible(image, (Boolean)aValue);
//		}
//		else if (aEnum == CustomImageImporterColumnLookup.Boundary)
//		{
//			imageCollection.setImageBoundaryShowing(image, (Boolean)aValue);
//		}
		else
			throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}
