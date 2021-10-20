package edu.jhuapl.sbmt.image.modules.preview;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlaybackPanelController
{
	PlaybackPanelView view;

	public PlaybackPanelController(double startTime, double stopTime)
	{
		view = new PlaybackPanelView();
		JSlider slider = view.getSlider();
		slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                if(slider.getValueIsAdjusting()){
                	int val = slider.getValue();
                    int max = slider.getMaximum();
                    double delta = stopTime - startTime;
                    double fraction = (double)val/(double)max;
                    double newTime = startTime + delta*fraction;
                }
            }
        });
	}

	public PlaybackPanelView getView()
	{
		return view;
	}

}

class PlaybackPanelView extends JPanel
{
	private JSlider slider;
	 /**
     * Minimum slider value
     */
    private int sliderMin = 0;

    /**
     * Maximum slider value
     */
    private int sliderMax = 900;

    /**
     *	Minor tick value for slider
     */
    private int sliderMinorTick = 30;

    /**
     * Major tick value for slider
     */
    private int sliderMajorTick = 150;


	public PlaybackPanelView()
	{
		slider = new JSlider();
        add(slider);
        slider.setMinimum(sliderMin);
        slider.setMaximum(sliderMax);
        slider.setMinorTickSpacing(sliderMinorTick);
        slider.setMajorTickSpacing(sliderMajorTick);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setValue(sliderMin);
	}


	/**
	 * @return the slider
	 */
	public JSlider getSlider()
	{
		return slider;
	}
}
