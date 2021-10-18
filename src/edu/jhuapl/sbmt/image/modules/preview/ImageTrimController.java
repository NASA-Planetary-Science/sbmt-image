package edu.jhuapl.sbmt.image.modules.preview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.modules.rendering.VtkImageTrimPipeline;

public class ImageTrimController
{
	private JLabel jLabel1;
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
	private VtkImageTrimPipeline trimPipeline;
	private Function<Layer, Void> completionBlock;

	public ImageTrimController(Layer layer, Function<Layer, Void> completionBlock)
	{
		this.layer = layer;
		this.trimPipeline = new VtkImageTrimPipeline();
		this.panel = new JPanel();
		this.completionBlock = completionBlock;
		panel.setLayout(new GridBagLayout());
		initGUI();
	}

	public JPanel getView()
	{
		return panel;
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
			leftSpinnerStateChanged(evt);
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
			bottomSpinnerStateChanged(evt);
		});

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 0, 0, 10);
		jPanel1.add(bottomSpinner, gridBagConstraints);

		jLabel3.setText("Left");
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
				rightSpinnerStateChanged(evt);
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
				topSpinnerStateChanged(evt);
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
		jLabel6.setText("Bottom");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel6, gridBagConstraints);

		jLabel4.setText("Top");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel4, gridBagConstraints);

		jLabel5.setText("Right");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_END;
		gridBagConstraints.insets = new Insets(0, 0, 0, 2);
		jPanel1.add(jLabel5, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 8, 0, 0);
		panel.add(jPanel1, gridBagConstraints);

		jLabel8.setText("Crop:");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(3, 6, 3, 0);
		panel.add(jLabel8, gridBagConstraints);
	}

	private void leftSpinnerStateChanged(ChangeEvent evt)
	{// GEN-FIRST:event_leftSpinnerStateChanged
//		VtkImageMaskingPipeline pipeline = new VtkImageMaskingPipeline();
		croppingChanged();
	}// GEN-LAST:event_leftSpinnerStateChanged

	private void topSpinnerStateChanged(ChangeEvent evt)
	{// GEN-FIRST:event_topSpinnerStateChanged
		croppingChanged();
	}// GEN-LAST:event_topSpinnerStateChanged

	private void rightSpinnerStateChanged(ChangeEvent evt)
	{// GEN-FIRST:event_rightSpinnerStateChanged
		croppingChanged();
	}// GEN-LAST:event_rightSpinnerStateChanged

	private void bottomSpinnerStateChanged(ChangeEvent evt)
	{// GEN-FIRST:event_bottomSpinnerStateChanged
		croppingChanged();
	}// GEN-LAST:event_bottomSpinnerStateChanged

	private void croppingChanged()
	{
		try
		{
			trimPipeline.run(layer,
					(int)leftSpinner.getValue(), (int)rightSpinner.getValue(),
					(int)bottomSpinner.getValue(), (int)topSpinner.getValue());
//			layer = trimPipeline.getUpdatedData().get(0);
			completionBlock.apply(trimPipeline.getUpdatedData().get(0));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		try
//		{

//
//
//			generateVtkImageData(layer);
//			updateImage(displayedImage);
//			setIntensity(null);
//			renWin.Render();
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (!initialized)
//			return;
//
//		Integer top = (Integer) leftSpinner.getValue();
//		Integer right = (Integer) topSpinner.getValue();
//		Integer bottom = (Integer) rightSpinner.getValue();
//		Integer left = (Integer) bottomSpinner.getValue();
//
//		int[] masking =
//		{ top, right, bottom, left };
//
//		image.setCurrentMask(masking);
	}

}
