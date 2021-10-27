package edu.jhuapl.sbmt.image.modules.preview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlaybackPanelController
{
	PlaybackPanelView view;
	Timer timer;
	JSlider slider;
	Function<Double, Void> completionBlock;
	private double startTime;
	private double stopTime;

	public PlaybackPanelController(double startTime, double stopTime, Function<Double, Void> completionBlock)
	{
		this.startTime = startTime;
		this.stopTime = stopTime;
		this.completionBlock = completionBlock;
		view = new PlaybackPanelView();
		slider = view.getSlider();
		Timer timer = new Timer(1, new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
//            	System.out.println(
//						"PlaybackPanelView.PlaybackPanelView().new ActionListener() {...}: actionPerformed: firing");
            	slider.setValue(view.getSliderCurrent());
            	view.setSliderCurrent(view.getSliderCurrent()+1);
            	updateSlider();
            }
        });
//        timer.setDelay(timerInterval);

		view.setTimer(timer);
		slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
            	if(slider.getValueIsAdjusting()){
            		updateSlider();
            	}
            }
        });
	}

	private void updateSlider()
	{
    	int val = slider.getValue();
        int max = slider.getMaximum();
        double delta = stopTime - startTime;
        double fraction = (double)val/(double)max;
        double newTime = startTime + delta*fraction;
//        System.out.println(
//				"PlaybackPanelController.PlaybackPanelController(...).new ChangeListener() {...}: stateChanged: time is now " + TimeUtil.et2str(newTime));
        completionBlock.apply(newTime);
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
    private int sliderMax = 180;

    /**
     *	Minor tick value for slider
     */
    private int sliderMinorTick = 30;

    /**
     * Major tick value for slider
     */
    private int sliderMajorTick = 150;

    private int sliderCurrent = 0;

    private JButton playButton;


	public PlaybackPanelView()
	{
		playButton = new JButton("Play");

		add(playButton);
		slider = new JSlider();
        add(slider);
        slider.setMinimum(sliderMin);
        slider.setMaximum(sliderMax);
        slider.setMinorTickSpacing(sliderMinorTick);
        slider.setMajorTickSpacing(sliderMajorTick);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setValue(sliderCurrent);
	}

	public int getSliderCurrent()
	{
		return sliderCurrent;
	}

	public void setSliderCurrent(int current)
	{
		this.sliderCurrent = current;
	}

	public void setTimer(Timer timer)
	{
		playButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				timer.start();
			}
		});
	}


	/**
	 * @return the slider
	 */
	public JSlider getSlider()
	{
		return slider;
	}
}
