package edu.jhuapl.sbmt.image.modules.preview;

import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import com.jidesoft.swing.RangeSlider;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.modules.rendering.VtkImageContrastPipeline;

public class ImageContrastController
{
	ImageContrastSlider slider;


	public ImageContrastController(vtkImageData imageData, IntensityRange intensityRange, Function<vtkImageData, Void> completionBlock)
	{
		slider = new ImageContrastSlider(imageData, intensityRange, completionBlock);
	}

	public JPanel getView()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("Contrast"));
		panel.add(slider);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		return panel;
	}

	public void setImageData(vtkImageData imageData)
	{
		slider.setImageData(imageData);
	}
}

class ImageContrastSlider extends RangeSlider
{
	private VtkImageContrastPipeline pipeline;
	private IntensityRange intensityRange;
	private Function<vtkImageData, Void> completionBlock;
	private vtkImageData imageData;

	public ImageContrastSlider(vtkImageData imageData, IntensityRange intensityRange, Function<vtkImageData, Void> completionBlock)
	{
		this.completionBlock = completionBlock;
		this.imageData = imageData;
		setMinimum(0);
		setMaximum(255);
		int lowValue = 0;
		int hiValue = 255;
		lowValue = intensityRange.min;
		hiValue  = intensityRange.max;
		this.setHighValue(hiValue);
		this.setLowValue(lowValue);
		setPaintTicks(true);
		setMajorTickSpacing(10);
		addChangeListener(evt -> {
           sliderStateChanged(evt);
        });
	}

	public void setImageData(vtkImageData imageData)
	{
		this.imageData = imageData;
	}

	public void applyContrastToImage()
	{
		try
		{
			intensityRange = new IntensityRange(getLowValue(), getHighValue());
			pipeline = new VtkImageContrastPipeline(imageData, intensityRange);
			completionBlock.apply(pipeline.getUpdatedData().get(0));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		vtkImageData imageData = pipeline.apply(intensityRange);
//		if (image != null && image instanceof PerspectiveImage) {
//			PerspectiveImage pimage = (PerspectiveImage)image;
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
	}



//	private void adjustImage(PerspectiveImage image, IntensityRange range) {
//		image.setDisplayedImageRange(
//				new IntensityRange(getLowValue(), getHighValue()));
//	}
//
//	private void adjustOfflimb(PerspectiveImage image, IntensityRange range) {
//		image.setOfflimbImageRange(
//				new IntensityRange(getLowValue(), getHighValue()));
//	}


	public void sliderStateChanged(ChangeEvent evt)
	{
		if (getValueIsAdjusting())
			return;

		applyContrastToImage();
	}

}