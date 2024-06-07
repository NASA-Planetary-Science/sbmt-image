package edu.jhuapl.sbmt.image.ui.color;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.Image;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.VtkLayerRenderer;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import glum.gui.GuiUtil;


public class SingleImagePreviewPanel extends JPanel
{
	JTextField imageTextField;
	JButton deleteImageButton;
	String title;
	IPerspectiveImage perspectiveImage;
	JPanel previewPanel;
	Consumer<Void> closure;

	public SingleImagePreviewPanel(String title, Consumer<Void> closure)
	{
		this.title = title;
		imageTextField = new JTextField();
        imageTextField.setPreferredSize(new Dimension(250, 20));
        imageTextField.setMaximumSize(new Dimension(250, 20));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder(title));
        setSize(300, 300);
		setPreferredSize(new Dimension(250, 270));
		setMaximumSize(new Dimension(250, 270));
		this.closure = closure;
        makeImagePanel();
	}

	public IPerspectiveImage getPerspectiveImage()
	{
		return perspectiveImage;
	}

	public void setPerspectiveImage(IPerspectiveImage image)
	{
		this.perspectiveImage = image;
		imageTextField.setText(image.getFilename());
		runPreviewPipeline();
	}

	private void makeImagePanel()
	{
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.add(imageTextField);
		imageTextField.setTransferHandler(new PerspectiveImageTransferHandler());
		deleteImageButton = GuiUtil.formButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				imageTextField.setText("");
				deleteImageButton.setEnabled(false);
				clearRGBPreviewPanel();
			}
		}, IconUtil.getItemDel());
		deleteImageButton.setToolTipText(ToolTipUtil.getItemDel());
		deleteImageButton.setEnabled(false);
		labelPanel.add(deleteImageButton);
		add(labelPanel);

		makeRGBPreviewPanel();
		add(previewPanel);
	}
	
	private void clearRGBPreviewPanel()
	{
		previewPanel.removeAll();
		JTextArea label = new JTextArea("Drag and drop a row from the table\n to the text field above for the " + title);
		label.setMinimumSize(new Dimension(250, 100));
		label.setColumns(20);
		label.setLineWrap(true);
		label.setWrapStyleWord(true);
		previewPanel.add(label);
		previewPanel.setSize(250, 250);
		previewPanel.setPreferredSize(new Dimension(250, 250));
		previewPanel.setMaximumSize(new Dimension(250, 250));
		previewPanel.repaint();
		previewPanel.validate();
		perspectiveImage = null;
		closure.accept(null);
	}

	private void makeRGBPreviewPanel()
	{
		if (imageTextField.getText().isEmpty())
		{
			previewPanel = new JPanel();
			JTextArea label = new JTextArea("Drag and drop a row from the table\n to the text field above for the " + title);
			label.setMinimumSize(new Dimension(250, 100));
			label.setColumns(20);
			label.setLineWrap(true);
			label.setWrapStyleWord(true);
			previewPanel.add(label);
			previewPanel.setSize(250, 250);
			previewPanel.setPreferredSize(new Dimension(250, 250));
			previewPanel.setMaximumSize(new Dimension(250, 250));
		}
		else runPreviewPipeline();
	}

	private void runPreviewPipeline()
	{
		previewPanel.removeAll();
		VtkLayerRenderer preview = new VtkLayerRenderer(false);

		try {
			PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(perspectiveImage));
			List<IRenderableImage> renderableImages = pipeline.getRenderableImages();
			Just.of(renderableImages.get(0).getLayer())
				.subscribe(preview)
				.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		JPanel renderPanel = (JPanel)preview.getPanel();
		renderPanel.setSize(250, 250);
		renderPanel.setPreferredSize(new Dimension(250, 250));
		renderPanel.setMaximumSize(new Dimension(250, 250));
		previewPanel.add(renderPanel);

		previewPanel.repaint();
		previewPanel.validate();
	}

	public class PerspectiveImageTransferHandler<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends TransferHandler {

        @Override
        public boolean canImport(TransferSupport support) {
            return (support.getComponent() instanceof JTextField) && support.isDataFlavorSupported(PerspectiveImageTransferable.PERSPECTIVE_IMAGE_DATA_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport support) {
            boolean accept = false;
            if (canImport(support)) {
                try {
                    Transferable t = support.getTransferable();
                    G1 value = (G1)t.getTransferData(PerspectiveImageTransferable.PERSPECTIVE_IMAGE_DATA_FLAVOR);
                    if (value instanceof G1) {
                        Component component = support.getComponent();
                        if (component instanceof JTextField) {
                        	G1 image = (G1)value;
                        	JTextField field = ((JTextField)component);
                            field.setText(image.getFilename());
                            perspectiveImage = image;
                            runPreviewPipeline();
                            accept = true;
                            deleteImageButton.setEnabled(true);
                            closure.accept(null);
                        }
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
            return accept;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return DnDConstants.ACTION_COPY_OR_MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            Transferable t = null;
            if (c instanceof JList) {
                @SuppressWarnings("unchecked")
                JList<PerspectiveImageMetadata> list = (JList<PerspectiveImageMetadata>) c;
                Object value = list.getSelectedValue();
                if (value instanceof PerspectiveImageMetadata) {
                	IPerspectiveImage li = (IPerspectiveImage) value;
                    t = new PerspectiveImageTransferable(li);
                }
            }
            return t;
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            System.out.println("ExportDone");
            // Here you need to decide how to handle the completion of the transfer,
            // should you remove the item from the list or not...
        }
    }

    public static class PerspectiveImageTransferable implements Transferable {

        public static final DataFlavor PERSPECTIVE_IMAGE_DATA_FLAVOR = new DataFlavor(PerspectiveImageMetadata.class, "sbmt/PerspectiveImage");
        private IPerspectiveImage image;

        public PerspectiveImageTransferable(IPerspectiveImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{PERSPECTIVE_IMAGE_DATA_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(PERSPECTIVE_IMAGE_DATA_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

            return image;

        }
    }

    public static class CustomImageTransferable implements Transferable {

        public static final DataFlavor CUSTOM_IMAGE_DATA_FLAVOR = new DataFlavor(PerspectiveImageMetadata.class, "sbmt/CustomImage");
        private Image image;

        public CustomImageTransferable(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{CUSTOM_IMAGE_DATA_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(CUSTOM_IMAGE_DATA_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

            return image;

        }
    }

}
