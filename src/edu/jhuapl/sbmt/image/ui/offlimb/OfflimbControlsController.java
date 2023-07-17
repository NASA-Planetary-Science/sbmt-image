package edu.jhuapl.sbmt.image.ui.offlimb;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class OfflimbControlsController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	OfflimbImageControlPanel controlsPanel;
	PerspectiveImageCollection<G1> collection;
	ImageContrastController contrastController;
	G1 image;
	List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
	List<IRenderableImage> renderableImages;
	private DecimalFormat formatter = new DecimalFormat("##.##%");

	public OfflimbControlsController(PerspectiveImageCollection<G1> collection, G1 image) throws Exception
	{
		this.image = image;

		PerspectiveImageToRenderableImagePipeline pipeline1 = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		renderableImages = pipeline1.getRenderableImages();
		Just.of(renderableImages.get(0).getLayer())
			.operate(new VtkImageRendererOperator())
			.subscribe(Sink.of(displayedImages))
			.run();
		this.collection = collection;
		collection.setContrastSynced(image, true);
		this.contrastController = new ImageContrastController(displayedImages.get(0), new IntensityRange(0, 255), new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
					collection.setOffLimbContrastRange(image, contrastController.getIntensityRange());
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});

		controlsPanel = new OfflimbImageControlPanel();
		controlsPanel.getShowOfflimbButton().setSelected(collection.getImageOfflimbShowing(image));
		controlsPanel.getShowBoundaryButton().setSelected(collection.getOffLimbBoundaryShowing(image));

		init();
	}

	private void init()
	{
		ChangeListener changeListener = new ChangeListener()
		{

			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (e.getSource() == controlsPanel.getShowOfflimbButton())
				{
					collection.setImageOfflimbShowing(image, controlsPanel.getShowOfflimbButton().isSelected());
					controlsPanel.getShowBoundaryButton().setSelected(collection.getOffLimbBoundaryShowing(image));
				}
				else if (e.getSource() == controlsPanel.getFootprintDepthSlider() && !controlsPanel.getFootprintDepthSlider().getValueIsAdjusting())
				{
					DepthSlider<G1> depthSlider = controlsPanel.getFootprintDepthSlider();
					RenderablePointedImage renderableImage = (RenderablePointedImage)renderableImages.get(0);
					double depthValue = depthSlider.getDepthValue(renderableImage.getMinFrustumLength(), renderableImage.getMaxFrustumLength());
					collection.setOffLimbDepth(image, depthValue);
					controlsPanel.getFootprintDepthValue().setText(" " + depthValue);
				}
				else if (e.getSource() == controlsPanel.getFootprintTransparencySlider() && !controlsPanel.getFootprintTransparencySlider().getValueIsAdjusting())
				{
					AlphaSlider<G1> alphaSlider = controlsPanel.getFootprintTransparencySlider();
					double alphaValue = alphaSlider.getAlphaValue();
					collection.setOfflimbOpacity(image, alphaValue);

					controlsPanel.getFootprintTransparencyValue().setText(" " + formatter.format(alphaValue));
				}
				else if (e.getSource() == controlsPanel.getShowBoundaryButton())
				{
					ShowBoundaryButton<G1> showBoundaryButton = controlsPanel.getShowBoundaryButton();
					collection.setOffLimbBoundaryShowing(image, showBoundaryButton.isSelected());
				}
				else if (e.getSource() == controlsPanel.getSyncContrastButton())
				{
					// let everyone know that we're syncing or unsyncing
					SyncContrastSlidersButton<G1> syncButton = controlsPanel.getSyncContrastButton();
					collection.setContrastSynced(image, syncButton.isSelected());
					if (controlsPanel.getSyncContrastButton().isSelected()) {
						// if we're syncing, set the slider values to that of the img slider
						collection.setOffLimbContrastRange(image, new IntensityRange(contrastController.getLowValue(), contrastController.getHighValue()));
						collection.setImageContrastRange(image, new IntensityRange(contrastController.getLowValue(), contrastController.getHighValue()));
					}
				}
				else if (e.getSource() == controlsPanel.getResetButton())
				{
					// let everyone know that we're syncing or unsyncing
					controlsPanel.getSyncContrastButton().setSelected(false);
					controlsPanel.getShowBoundaryButton().setSelected(true);
					controlsPanel.getFootprintDepthSlider().setValue(0);
					controlsPanel.getFootprintTransparencySlider().setValue(50);
				}
			}
		};

		controlsPanel.getShowOfflimbButton().addChangeListener(changeListener);
		controlsPanel.getFootprintDepthSlider().addChangeListener(changeListener);
		controlsPanel.getFootprintTransparencySlider().addChangeListener(changeListener);
		controlsPanel.getShowBoundaryButton().addChangeListener(changeListener);
		controlsPanel.getSyncContrastButton().addChangeListener(changeListener);
		controlsPanel.getResetButton().addChangeListener(changeListener);

		controlsPanel.getBoundaryColorBtn().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Color currColor =  collection.getOffLimbBoundaryColor(image);
				Color color = ColorChooser.showColorChooser(null, new int[] {0,0});
				collection.setOffLimbBoundaryColor(image, color);
			}
		});
	}

	public JPanel getControlsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(contrastController.getView());
		panel.add(controlsPanel);
		return panel;
	}
}
