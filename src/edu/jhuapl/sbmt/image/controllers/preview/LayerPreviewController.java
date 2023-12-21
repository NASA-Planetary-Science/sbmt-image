package edu.jhuapl.sbmt.image.controllers.preview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkImageData;
import vtk.vtkInteractorStyleImage;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.LayerPreviewModel;
import edu.jhuapl.sbmt.image.ui.LayerPreviewPanel;
import edu.jhuapl.sbmt.layer.api.Layer;

public class LayerPreviewController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	LayerPreviewModel<G1> model;
	LayerPreviewPanel<G1> panel;
	Runnable completionBlock;

	ImageMaskController maskController;
	ImageContrastController contrastController;
	ImageFillValuesController fillValuesController;

	public LayerPreviewController(LayerPreviewPanel<G1> panel, LayerPreviewModel<G1> model, Runnable completionBlock)
	{
		this.model = model;
		this.panel = panel;
		this.completionBlock = completionBlock;
		initializeSubViews();
		initializeListeners();
		maskController.setMaskValues(model.getCurrentMaskValues());
		try
		{
			setupRenderer();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initializeSubViews()
	{
		buildLayerComboBox(0);
		buildContrastController(1);
		buildTrimController(2);
		buildFillValuesController(3);
	}



	private void buildLayerComboBox(int ylevel)
	{
		if (model.getLayers().size() > 1)
		{
			String[] layerNames = new String[model.getLayers().size()];
			if (model.getMetadatas().size() != 0 && model.getMetadatas().get(0).get(0).size() > 0 && layerNames.length != 4)
			{
				List<HashMap<String, String>> list = model.getMetadatas().get(0);
				HashMap<String, String> values = list.get(0);
				values
					 .keySet()
					 .stream()
					 .filter(item -> item.contains("PLANE"))
					 .map(key  -> key + " - " + model.getMetadatas().get(0).get(0).get(key))
					 .sorted()
					 .toList()
					 .toArray(layerNames);
				int i=0;
				for (String name : layerNames)
				{
					String[] parts = name.split("PLANE");
					String[] parts2 = parts[1].split(" ");
					String paddedIndex = StringUtils.leftPad(""+(parts2[0]), 2);
					String desc = "";
					int j=0;
					for (String str : parts2)
					{
						if (j==0)
						{
							j++;
							continue;
						}
						desc += " " + str;
					}
					layerNames[i++] = "PLANE" + paddedIndex + desc;

 				}
				Arrays.sort(layerNames);

			}
			else
				for (int i=0; i<layerNames.length; i++)
				{
					String paddedIndex = StringUtils.leftPad(""+(i+1), 2);
					layerNames[i] = "PLANE" + paddedIndex;
				}
			panel.setLayerComboBox(new JComboBox<String>(layerNames));
			int indexToSelect = 0;
			Optional<String> matchedPlane = Arrays.stream(layerNames).filter(Objects::nonNull).filter(name -> name.contains("PLANE" + StringUtils.leftPad(""+(model.getDisplayedLayerIndex()), 2))).findFirst();
			if (matchedPlane.isPresent())
				indexToSelect = Arrays.stream(layerNames).toList().indexOf(matchedPlane.get()) + 1;
			panel.getLayerComboBox().setSelectedIndex(indexToSelect);

			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = ylevel;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 0.0;
			panel.getControlsPanel().add(panel.getLayerComboBox(), gridBagConstraints);

			gridBagConstraints.gridx = 1;
//			panel.getControlsPanel().add(panel.getApplyToBodyButton(), gridBagConstraints);

			panel.getLayerComboBox().addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String title = (String)panel.getLayerComboBox().getSelectedItem();
					int index = getSelectedIndex(title);
					try
					{
						model.setDisplayedLayerIndex(index);
						model.setLayer(model.getLayers().get(index));
						maskController.setLayer(model.getLayer());
//						generateVtkImageData(model.getLayer());
//						setIntensity(null);
						panel.getRenWin().Render();
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void buildContrastController(int ylevel)
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.gridwidth = panel.getControlsPanel().getWidth();
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		contrastController = new ImageContrastController(model.getDisplayedImage(), model.getIntensityRange(), new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
//					renderLayer();
					model.setDisplayedImage(t);
					model.setIntensityRange(contrastController.getIntensityRange());
//					setIntensity(null);
					panel.getRenWin().Render();
					if (completionBlock != null && panel.getSyncCheckBox().isSelected()) completionBlock.run();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});

		panel.getControlsPanel().add(contrastController.getView(), gridBagConstraints);
	}

	private void buildTrimController(int ylevel)
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 0, 3, 0);
		maskController = new ImageMaskController(model.getLayer(), model.getCurrentMaskValues(), new Function<Pair<Layer, int[]>, Void>()
		{

			@Override
			public Void apply(Pair<Layer, int[]> items)
			{
				try
				{
//					int[] masks = items.getRight();
//					renderLayer();
					model.setCurrentMaskValues(items.getRight());
//					model.getImage().setMaskValues(items.getRight());
//					renderLayer();
//					generateVtkImageData(model.getLayers().get(model.getDisplayedLayerIndex()));
//					updateImage(model.getDisplayedImage());
//					setIntensity(contrastController.getIntensityRange());
					if (panel.getRenWin() == null) return null;
					panel.getRenWin().Render();
					model.setLayer(items.getLeft());
					if (completionBlock != null && panel.getSyncCheckBox().isSelected()) completionBlock.run();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});
		panel.getControlsPanel().add(maskController.getView(), gridBagConstraints);
	}

	private void buildFillValuesController(int ylevel)
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 0, 3, 0);
		fillValuesController = new ImageFillValuesController(new Function<double[], Void>()
		{
			@Override
			public Void apply(double[] items)
			{
				if (completionBlock != null && panel.getSyncCheckBox().isSelected()) completionBlock.run();
				return null;
			}
		});

		for (double val : model.getCurrentFillValues())
		{
			fillValuesController.getFillValuesTextField().setText(fillValuesController.getFillValuesTextField().getText() + val + ",");
			fillValuesController.setFillValues(model.getCurrentFillValues());
		}



		panel.getControlsPanel().add(fillValuesController.getView(), gridBagConstraints);
	}
	
	private int getSelectedIndex(String title)
	{
		String[] titlePartsAroundDash = title.split("-")[0].split(" ");
		int index = 0;
		if (titlePartsAroundDash.length > 1)
			index = Integer.parseInt(titlePartsAroundDash[1]) - 1;
		else
		{
			String[] paddedIndex = titlePartsAroundDash[0].split("PLANE");
			index = Integer.parseInt(paddedIndex[1]) - 1;
		}
		return index;
	}

	private void initializeListeners()
	{
		panel.getSyncCheckBox().addActionListener(evt -> {
			panel.getApplyAllButton().setEnabled(!panel.getSyncCheckBox().isSelected());
			if (panel.getSyncCheckBox().isSelected())
			{
				panel.getApplyToBodyButton().doClick();
				completionBlock.run();
			}
		});



		panel.getApplyToBodyButton().addActionListener(evt -> {
			if (panel.getLayerComboBox() == null) return;
			String title = (String)panel.getLayerComboBox().getSelectedItem();
			model.setDisplayedLayerIndex(getSelectedIndex(title));
			if (completionBlock != null && panel.getSyncCheckBox().isSelected()) completionBlock.run();
		});

		fillValuesController.getFillValuesButton().addActionListener(e -> {
			String[] valueStrings = fillValuesController.getFillValuesTextField().getText().split(",");
			double[] doubleArray = new double[valueStrings.length];
			if (valueStrings.length == 0 || valueStrings[0].isBlank())
			{
				try
				{
					fillValuesController.setFillValues(new double[] {});
					model.setCurrentFillValues(new double[] {});
					if (panel.getRenWin() == null) return;
					SwingUtilities.invokeLater(() -> { panel.getRenWin().Render(); });

					return;
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			int i=0;
			for (String val : valueStrings)
			{
				doubleArray[i++] = Double.parseDouble(val);
			}
			fillValuesController.setFillValues(doubleArray);
			try
			{
				model.setCurrentFillValues(doubleArray);
				SwingUtilities.invokeLater(() -> { panel.getRenWin().Render(); });
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		panel.getApplyAllButton().addActionListener(evt -> {
			try
			{
				panel.getApplyToBodyButton().doClick();
				completionBlock.run();
//				model.setCurrentMaskValues(maskController.getMaskValues());
//				fillValuesController.getFillValuesButton().doClick();
//				maskController.getApplyButton().doClick();
//				model.setCurrentFillValues(fillValuesController.getFillValues());
			}
			catch (Exception e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});
	}

	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}


	private void setupRenderer()
	{
		panel.setRenWin(new vtkJoglPanelComponent());
		panel.getRenWin().getComponent().setPreferredSize(new Dimension(550, 550));

		vtkInteractorStyleImage style = new vtkInteractorStyleImage();
		panel.getRenWin().setInteractorStyle(style);

//		renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent", this,
//				"levelsChanged");

		panel.getRenWin().getRenderer().AddActor(model.getActor());

		panel.getRenWin().setSize(550, 550);
		panel.getRenWin().getRenderer().SetBackground(new double[] {0.5f, 0.5f, 0.5f});

//		panel.getRenWin().getComponent().addMouseListener(panel);
//		panel.getRenWin().getComponent().addMouseMotionListener(panel);
		panel.getRenWin().getRenderer().GetActiveCamera().Dolly(0.2);
		// renWin.addKeyListener(this);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		panel.getLayerPanel().add(panel.getRenWin().getComponent(), gridBagConstraints);
	}
}
