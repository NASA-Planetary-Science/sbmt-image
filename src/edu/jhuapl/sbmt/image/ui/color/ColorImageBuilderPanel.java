package edu.jhuapl.sbmt.image.ui.color;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkRendererPreview;

public class ColorImageBuilderPanel<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JPanel
{
	@SuppressWarnings("unused")
	private List<SmallBodyModel> smallBodyModels;
	private JPanel previewPanel;
	private SingleImagePreviewPanel<G1> redPreview;
	private SingleImagePreviewPanel<G1> greenPreview;
	private SingleImagePreviewPanel<G1> bluePreview;
	private JButton saveAndCloseButton;
	private JButton previewButton;
	private JButton cancelButton;
	private boolean redComplete = false, greenComplete = false, blueComplete = false;

	public ColorImageBuilderPanel(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
		redPreview = new SingleImagePreviewPanel<G1>("Red Image", s ->  { this.redComplete = redPreview.getPerspectiveImage() != null ? true : false; checkForButtonStatus(); });
		greenPreview = new SingleImagePreviewPanel<G1>("Green Image", s -> { this.greenComplete = greenPreview.getPerspectiveImage() != null ? true : false; checkForButtonStatus(); } );
		bluePreview = new SingleImagePreviewPanel<G1>("Blue Image", s ->   { this.blueComplete = bluePreview.getPerspectiveImage() != null ? true : false; checkForButtonStatus(); } );
		initGUI();
	}

	private void initGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(makeImagePanel());
		add(Box.createVerticalStrut(5));
		add(makeButtonPanel());
		add(Box.createVerticalStrut(5));
		add(makePreviewPanel());

		setSize(750, 1200);
	}

	private JPanel makeImagePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(redPreview);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(greenPreview);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(bluePreview);
		return panel;
	}

	private JPanel makePreviewPanel()
	{
		previewPanel = new JPanel();
		previewPanel.setSize(750, 650);
		previewPanel.add(new JLabel("Select 3 images, then click the preview button above."));
		previewPanel.add(new JLabel("When you are satisfied, click 'Save and Close'; the image will then appear under the 'Custom' subtab."));
		previewPanel.setPreferredSize(new Dimension(750, 650));
		previewPanel.setMaximumSize(new Dimension(750, 650));

		return previewPanel;
	}

	private JPanel makeButtonPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		previewButton = new JButton("Preview");
		cancelButton = new JButton("Close");
		saveAndCloseButton = new JButton("Save and Close");
		panel.add(previewButton);
		panel.add(cancelButton);
		panel.add(saveAndCloseButton);

		return panel;

	}

	public List<IPerspectiveImage> getImages()
	{
		return List.of(redPreview.getPerspectiveImage(), greenPreview.getPerspectiveImage(), bluePreview.getPerspectiveImage());
	}

	public void setImages(List<G1> images)
	{
		redPreview.setPerspectiveImage(images.get(0));
		greenPreview.setPerspectiveImage(images.get(1));
		bluePreview.setPerspectiveImage(images.get(2));
	}
	
	private void checkForButtonStatus()
	{
		previewButton.setEnabled(redComplete && greenComplete && blueComplete);
		saveAndCloseButton.setEnabled(redComplete && greenComplete && blueComplete);
	}

	/**
	 * @return the saveAndCloseButton
	 */
	public JButton getSaveAndCloseButton()
	{
		return saveAndCloseButton;
	}

	/**
	 * @return the previewButton
	 */
	public JButton getPreviewButton()
	{
		return previewButton;
	}

	public JButton getCancelButton()
	{
		return cancelButton;
	}

	/**
	 * @return the previewPanel
	 */
	public JPanel getPreviewPanel()
	{
		return previewPanel;
	}

	public void updatePreviewPanel(VtkRendererPreview preview)
	{
		previewPanel.removeAll();
		JPanel renderPanel = (JPanel)preview.getPanel();
		renderPanel.setMinimumSize(new Dimension(750, 650));
		renderPanel.setPreferredSize(new Dimension(750, 650));
		renderPanel.setMaximumSize(new Dimension(750, 650));
		previewPanel.add(renderPanel);
		previewPanel.repaint();
		previewPanel.validate();
		add(previewPanel);
	}
}