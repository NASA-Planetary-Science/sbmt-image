package edu.jhuapl.sbmt.image.gui.controllers.images;

import com.jidesoft.swing.RangeSlider;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;

public class ContrastSlider extends RangeSlider
{

	Image image;
	boolean offlimb;

	public ContrastSlider(Image image, boolean offlimb)
	{
		this.image = image;
		this.offlimb = offlimb;
		setMinimum(0);
		setMaximum(255);
		int lowValue = 0;
		int hiValue = 255;
		// get existing contrast and set slider appropriately
		if (image instanceof PerspectiveImage)
		{
			if (!offlimb) { // get image contrast
				lowValue = ((PerspectiveImage)image).getDisplayedRange().min;
				hiValue  = ((PerspectiveImage)image).getDisplayedRange().max;
			}
			else { // get offlimb contrast
				lowValue = ((PerspectiveImage)image).getOffLimbDisplayedRange().min;
				hiValue  = ((PerspectiveImage)image).getOffLimbDisplayedRange().max;
			}
		}
		this.setHighValue(hiValue);
		this.setLowValue(lowValue);
		setPaintTicks(true);
		setMajorTickSpacing(10);

	}

	public void applyContrastToImage()
	{
		if (image != null && image instanceof PerspectiveImage) {
			PerspectiveImage pimage = (PerspectiveImage)image;
			IntensityRange range = new IntensityRange(getLowValue(), getHighValue());
			if (pimage.isContrastSynced()) {
				adjustOfflimb(pimage, range);
				adjustImage(pimage, range);
			}
			else { // just do the appropriate one if not synced
				if (!offlimb) {
					adjustImage(pimage, range);
				}
				else {
					adjustOfflimb(pimage, range);
				}
			}
		}
	}



	private void adjustImage(PerspectiveImage image, IntensityRange range) {
		image.setDisplayedImageRange(
				new IntensityRange(getLowValue(), getHighValue()));
	}

	private void adjustOfflimb(PerspectiveImage image, IntensityRange range) {
		image.setOfflimbImageRange(
				new IntensityRange(getLowValue(), getHighValue()));
	}


	public void sliderStateChanged(javax.swing.event.ChangeEvent evt)
	{
		if (getValueIsAdjusting())
			return;

		applyContrastToImage();
	}

}