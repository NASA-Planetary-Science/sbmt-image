package edu.jhuapl.sbmt.image.controllers.preview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk.VtkImageMaskPipeline;
import edu.jhuapl.sbmt.layer.api.Layer;

public class ImageMaskController
{
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel8;
	private JPanel jPanel1;
	private JSpinner leftSpinner;
	private JSpinner rightSpinner;
	private JSpinner topSpinner;
	private JSpinner bottomSpinner;
	private Layer layer;
	private JPanel panel;
	private VtkImageMaskPipeline maskPipeline;
	private Function<Pair<Layer, int[]>, Void> completionBlock;
	private int[] currentMaskValues;
	private int leftMask = 0, rightMask = 0, topMask = 0, bottomMask = 0;
	private JButton applyButton;
	private boolean showApplyButton = false;
	private boolean applyImmediately = true;

	public ImageMaskController(Layer layer, int[] currentMaskValues, Function<Pair<Layer, int[]>, Void> completionBlock)
	{
		this(layer, currentMaskValues, false, completionBlock);
	}

	public ImageMaskController(Layer layer, int[] currentMaskValues, boolean showApplyButton, Function<Pair<Layer, int[]>, Void> completionBlock)
	{
		this.showApplyButton = showApplyButton;
		this.layer = layer;
		this.maskPipeline = new VtkImageMaskPipeline();
		this.panel = new JPanel();
		this.completionBlock = completionBlock;
		this.currentMaskValues = currentMaskValues;
		panel.setLayout(new GridBagLayout());
		initGUI();
	}

	public JPanel getView()
	{
		return panel;
	}

	public void setMaskValues(int[] maskValues)  //was 3, 2, 0, 1
	{
		applyImmediately = false;
		bottomSpinner.setValue((int)maskValues[0]); //2
		rightSpinner.setValue((int)maskValues[3]);	//1
		topSpinner.setValue((int)maskValues[1]);	//3
		leftSpinner.setValue((int)maskValues[2]);	//0

		applyImmediately = true;
		croppingChanged();
	}

	public void setLayer(Layer layer)
	{
		this.layer = layer;
	}

	private void initGUI()
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		jPanel1 = new JPanel();
		leftSpinner = new JSpinner();
		bottomSpinner = new JSpinner();
		jLabel3 = new JLabel();
		rightSpinner = new JSpinner();
		topSpinner = new JSpinner();
		jLabel6 = new JLabel();
		jLabel4 = new JLabel();
		jLabel5 = new JLabel();
		jLabel8 = new JLabel();

		jPanel1.setAlignmentX(0.0F);
		jPanel1.setLayout(new GridBagLayout());

		leftSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		leftSpinner.setPreferredSize(new Dimension(60, 28));
		leftSpinner.addChangeListener(evt ->
		{
			if (applyImmediately) applyButton.doClick();
//			System.out.println("ImageMaskController: initGUI: left");
//			croppingChanged();
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 7;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 10);
		jPanel1.add(leftSpinner, gridBagConstraints);

		bottomSpinner
				.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		bottomSpinner.setPreferredSize(new Dimension(60, 28));
		bottomSpinner.addChangeListener(evt ->
		{
			if (applyImmediately) applyButton.doClick();
//			if (bottomSpinner.getValue() != )
//			croppingChanged();
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 10);
		jPanel1.add(bottomSpinner, gridBagConstraints);

		jLabel3.setText("Bottom");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel3, gridBagConstraints);

		rightSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		rightSpinner.setPreferredSize(new Dimension(60, 28));
		rightSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent evt)
			{
				if (applyImmediately) applyButton.doClick();
//				System.out.println("ImageMaskController.initGUI().new ChangeListener() {...}: stateChanged: right");
//				croppingChanged();
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 10);
		jPanel1.add(rightSpinner, gridBagConstraints);

		topSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		topSpinner.setPreferredSize(new Dimension(60, 28));
		topSpinner.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent evt)
			{
				if (applyImmediately) applyButton.doClick();
//				System.out.println("ImageMaskController.initGUI().new ChangeListener() {...}: stateChanged: top");
//				croppingChanged();
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 10);
		jPanel1.add(topSpinner, gridBagConstraints);

		jLabel6.setHorizontalAlignment(SwingConstants.TRAILING);
		jLabel6.setText("Left");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel6, gridBagConstraints);

		jLabel4.setText("Right");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel4, gridBagConstraints);

		jLabel5.setText("Top");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel5, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 0);
		panel.add(jPanel1, gridBagConstraints);

		jLabel8.setText("Mask:");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(3, 30, 3, 0);
		panel.add(jLabel8, gridBagConstraints);


		applyButton = new JButton("Apply");
		applyButton.setVisible(showApplyButton);
		applyButton.addActionListener(evt -> {
			croppingChanged();
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		panel.add(applyButton, gridBagConstraints);

	}

	public int[] getMaskValues()
	{
		return new int[] {(int)bottomSpinner.getValue(), (int)topSpinner.getValue(), (int)leftSpinner.getValue(), (int)rightSpinner.getValue()};
	}

	public JButton getApplyButton()
	{
		return applyButton;
	}

	private void croppingChanged()
	{
		if (layer == null) return;
		try
		{
			maskPipeline.run(layer,
					(int)bottomSpinner.getValue(), (int)topSpinner.getValue(),
					(int)leftSpinner.getValue(), (int)rightSpinner.getValue());

			completionBlock.apply(Pair.of(maskPipeline.getUpdatedData().get(0), getMaskValues()));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
