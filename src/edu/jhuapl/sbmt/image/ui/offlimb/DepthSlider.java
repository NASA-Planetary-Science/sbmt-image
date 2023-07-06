package edu.jhuapl.sbmt.image.ui.offlimb;

import javax.swing.JSlider;

public class DepthSlider<G1> extends JSlider
	{
		double depthMin, depthMax;

		public DepthSlider()
		{
			setMinimum(-50);
			setMaximum(50);
		}

		public void applyDepthToImage(int currentSlice)
		{
//			depthMin = image.getMinFrustumDepth();
//			depthMax = image.getMaxFrustumDepth(currentSlice);
////			image.setOffLimbPlaneDepth(getDepthValue());
//			collection.setOffLimbDepth(getDepthValue());
		}

		public double getDepthValue(double depthMin, double depthMax)
		{
			double value = getValue();
			double sliderRange = getMaximum() - getMinimum();
			double imgRange = depthMax - depthMin;
//			System.out.println("DepthSlider: getDepthValue: slide range " + sliderRange);
//			System.out.println("DepthSlider: getDepthValue: img range " + imgRange);
//			System.out.println("DepthSlider: getDepthValue: value is " + value + " min " + getMinimum());
//			System.out.println("DepthSlider: getDepthValue: returning " + ((int) ((value - getMinimum()) * (imgRange/sliderRange) + depthMin)));
			return (int) ((value - getMinimum()) * (imgRange/sliderRange) + depthMin);
		}

	}