package edu.jhuapl.sbmt.image.ui;
//package edu.jhuapl.sbmt.image2.ui;
//
//import com.jidesoft.swing.RangeSlider;
//
//import edu.jhuapl.saavtk.util.IntensityRange;
//import edu.jhuapl.sbmt.image2.interfaces.IPerspectiveImage;
//
//
//public class ContrastSlider extends RangeSlider
//{
//
//	IPerspectiveImage image;
//	boolean offlimb;
//
//	public ContrastSlider(IPerspectiveImage image, boolean offlimb)
//	{
//		this.image = image;
//		this.offlimb = offlimb;
//		setMinimum(0);
//		setMaximum(255);
//		int lowValue = 0;
//		int hiValue = 255;
//		// get existing contrast and set slider appropriately
//		if (image instanceof IPerspectiveImage)
//		{
//			if (!offlimb) { // get image contrast
//				lowValue = ((IPerspectiveImage)image).getDisplayedRange().min;
//				hiValue  = ((IPerspectiveImage)image).getDisplayedRange().max;
//			}
//			else { // get offlimb contrast
//				lowValue = ((IPerspectiveImage)image).getOffLimbDisplayedRange().min;
//				hiValue  = ((IPerspectiveImage)image).getOffLimbDisplayedRange().max;
//			}
//		}
//		this.setHighValue(hiValue);
//		this.setLowValue(lowValue);
//		setPaintTicks(true);
//		setMajorTickSpacing(10);
//
//	}
//
//	public void applyContrastToImage()
//	{
//		if (image != null && image instanceof IPerspectiveImage) {
//			IPerspectiveImage pimage = (IPerspectiveImage)image;
//			IntensityRange range = new IntensityRange(getLowValue(), getHighValue());
//			if (pimage.isContrastSynced()) {
//				adjustOfflimb(pimage, range);
//				adjustImage(pimage, range);
//			}
//			else { // just do the appropriate one if not synced
//				if (!offlimb) {
//					adjustImage(pimage, range);
//				}
//				else {
//					adjustOfflimb(pimage, range);
//				}
//			}
//		}
//	}
//
//
//
//	private void adjustImage(IPerspectiveImage image, IntensityRange range) {
//		image.setDisplayedImageRange(
//				new IntensityRange(getLowValue(), getHighValue()));
//	}
//
//	private void adjustOfflimb(IPerspectiveImage image, IntensityRange range) {
//		image.setOfflimbImageRange(
//				new IntensityRange(getLowValue(), getHighValue()));
//	}
//
//
//	public void sliderStateChanged(javax.swing.event.ChangeEvent evt)
//	{
//		if (getValueIsAdjusting())
//			return;
//
//		applyContrastToImage();
//	}
//
//}