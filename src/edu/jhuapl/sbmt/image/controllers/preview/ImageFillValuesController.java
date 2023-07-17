package edu.jhuapl.sbmt.image.controllers.preview;

import java.awt.Dimension;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ImageFillValuesController
{
	private JTextField fillValuesTextField;
	private JButton fillValuesButton;
	private JPanel panel;
	private double[] fillValues;
	private Function<double[], Void> completionBlock;
	private boolean showApplyButton = false;

	public ImageFillValuesController(Function<double[], Void> completionBlock)
	{
		this(false, completionBlock);
	}

	public ImageFillValuesController(boolean showApplyButton, Function<double[], Void> completionBlock)
	{
		this.showApplyButton = showApplyButton;
		initGUI();
		this.completionBlock = completionBlock;
	}

	private void initGUI()
	{
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.add(Box.createHorizontalStrut(30));
		labelPanel.add(new JLabel("Fill Value(s):"));
		labelPanel.add(Box.createHorizontalGlue());
		panel.add(labelPanel);

		JPanel textFieldPanel = new JPanel();
		textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.X_AXIS));
		fillValuesButton = new JButton("Apply");
		fillValuesButton.setVisible(showApplyButton);
		fillValuesTextField = new JTextField();
		fillValuesTextField.addActionListener(evt -> { fillValuesButton.doClick(); });
		fillValuesTextField.setMinimumSize(new Dimension(350, 30));
		fillValuesTextField.setPreferredSize(new Dimension(400, 30));
		fillValuesTextField.setMaximumSize(new Dimension(550, 30));
		textFieldPanel.add(Box.createHorizontalStrut(50));
		textFieldPanel.add(fillValuesTextField);
		textFieldPanel.add(fillValuesButton);


		panel.add(textFieldPanel);
	}


	public JPanel getView()
	{
		return panel;
	}

	/**
	 * @return the fillValuesTextField
	 */
	public JTextField getFillValuesTextField()
	{
		return fillValuesTextField;
	}

	/**
	 * @return the fillValuesButton
	 */
	public JButton getFillValuesButton()
	{
		return fillValuesButton;
	}

	public double[] getFillValues()
	{
		return fillValues;
	}

	public void setFillValues(double[] vals)
	{
		this.fillValues = vals;
		if (completionBlock != null) completionBlock.apply(vals);

	}
}
