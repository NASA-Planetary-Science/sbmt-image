package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.PerspectiveImageOffsetUpdatePipeline;
import glum.gui.action.PopAction;
import net.miginfocom.swing.MigLayout;

public class ChangeNormalOffsetAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	private PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ChangeNormalOffsetAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

//		for (PerspectiveImage aItem : aItemL)
//		{
//			//TODO I think this can be done differently than down below
//		    PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)this.imagePopupMenu.modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
            ImageNormalOffsetChangerDialog changeOffsetDialog = new ImageNormalOffsetChangerDialog(aManager, aItemL.get(0));
            changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(null));
            changeOffsetDialog.setVisible(true);
//            int[] temp = boundaries.getBoundary(imageKey).getBoundaryColor();
//            boundaries.getBoundary(imageKey).setOffset(image.getOffset());
//            Color color = new Color(temp[0],temp[1],temp[2]);
//            boundaries.getBoundary(imageKey).setBoundaryColor(color);
//		}
	}

	class ImageNormalOffsetChangerDialog extends JDialog implements ActionListener
	{
//	    private Model model;
	    private JButton applyButton;
	    private JButton resetButton;
	    private JButton okayButton;
	    private JButton cancelButton;
	    private JFormattedTextField offsetField;
	    private String lastGood = "";
	    private PerspectiveImageCollection<G1> collection;
	    private G1 image;

	    public ImageNormalOffsetChangerDialog(PerspectiveImageCollection<G1> collection, G1 image)
	    {
	    	this.collection = collection;
	    	this.image = image;
//	        this.model = model;

	        JPanel panel = new JPanel();
	        panel.setLayout(new MigLayout());

	        NumberFormat nf = NumberFormat.getNumberInstance();
	        nf.setGroupingUsed(false);
	        nf.setMaximumFractionDigits(6);

	        JLabel offsetLabel = new JLabel("Normal Offset");
	        offsetField = new JFormattedTextField(nf);
	        offsetField.setPreferredSize(new Dimension(125, 23));
	        offsetField.setInputVerifier(new DoubleVerifier());
	        JLabel kmLabel = new JLabel("meters");


	        JPanel buttonPanel = new JPanel(new MigLayout());
	        applyButton = new JButton("Apply");
	        applyButton.addActionListener(this);
	        resetButton = new JButton("Reset");
	        resetButton.addActionListener(this);
	        okayButton = new JButton("OK");
	        okayButton.addActionListener(this);
	        cancelButton = new JButton("Cancel");
	        cancelButton.addActionListener(this);
	        buttonPanel.add(applyButton);
	        buttonPanel.add(resetButton);
	        buttonPanel.add(okayButton);
	        buttonPanel.add(cancelButton);


	        String tooltipText =
	                "<html>Objects displayed on a shape model need to be shifted slightly away from <br>" +
	                "the shape model in the direction normal to the plates as otherwise they will <br>" +
	                "interfere with the shape model itself and may not be visible. This dialog allows <br>" +
	                "you to explicitely set the offset amount in meters. In general, the smallest positive <br>" +
	                "value should be chosen such that the objects are visible. To revert the offset <br>" +
	                "to the default value, press the Reset button.</html>";
	        applyButton.setToolTipText(tooltipText);
	        resetButton.setToolTipText(tooltipText);
	        okayButton.setToolTipText(tooltipText);
	        cancelButton.setToolTipText(tooltipText);
	        offsetLabel.setToolTipText(tooltipText);
	        offsetField.setToolTipText(tooltipText);
	        kmLabel.setToolTipText(tooltipText);


	        panel.add(offsetLabel);
	        panel.add(offsetField);
	        panel.add(kmLabel, "wrap");

	        panel.add(buttonPanel, "span, align right");

	        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

	        add(panel, BorderLayout.CENTER);
	        pack();
	    }

	    public void actionPerformed(ActionEvent e)
	    {
	        if (e.getSource() == applyButton || e.getSource() == okayButton)
	        {
	        	double newOffset = Double.parseDouble(offsetField.getText());
	            newOffset /= 1000.0;
	        	PerspectiveImageOffsetUpdatePipeline.of(collection, image, newOffset);
	        	double offset = image.getOffset();
                offsetField.setValue(1000.0 * offset);
//	            try
//	            {
//	                double newOffset = Double.parseDouble(offsetField.getText());
//	                newOffset /= 1000.0;
//
//	                model.setOffset(newOffset);
//
//	                // Reset the text field in case the requested offset change was not
//	                // fully fulfilled (e.g. was negative)
//	                double offset = model.getOffset();
//	                offsetField.setValue(1000.0 * offset);
//	            }
//	            catch (NumberFormatException ex)
//	            {
//	                return;
//	            }
	        }
	        else if (e.getSource() == resetButton)
	        {
	            double defaultOffset = image.getDefaultOffset();

	            image.setOffset(defaultOffset);

	            // Reset the text field in case the requested offset change was not
	            // fully fulfilled.
	            double offset = image.getOffset();
	            offsetField.setValue(1000.0 * offset);
	            PerspectiveImageOffsetUpdatePipeline.of(collection, image, defaultOffset);
	        }

	        if (e.getSource() == okayButton || e.getSource() == cancelButton)
	        {
	            super.setVisible(false);
	        }
	    }

	    public void setVisible(boolean b)
	    {
	        setTitle("Change Normal Offset");

	        offsetField.setValue(1000.0 * image.getOffset());
	        lastGood = offsetField.getText();

	        super.setVisible(b);
	    }

	    private class DoubleVerifier extends InputVerifier
	    {
	        public boolean verify(JComponent input)
	        {
	            JTextField text = (JTextField)input;
	            String value = text.getText().trim();
	            try
	            {
	                double v = Double.parseDouble(value);
	                if (v < 0.0)
	                    throw new NumberFormatException();
	                lastGood = value;
	            }
	            catch (NumberFormatException e)
	            {
	                text.setText(lastGood);
	                return false;
	            }
	            return true;
	        }
	    }
	}


//	public void actionPerformed(ActionEvent e)
//    {
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//        if (image != null)
//        {
//            PerspectiveImageBoundaryCollection boundaries = (PerspectiveImageBoundaryCollection)this.imagePopupMenu.modelManager.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES);
//            NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
//            changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker));
//            changeOffsetDialog.setVisible(true);
//            int[] temp = boundaries.getBoundary(imageKey).getBoundaryColor();
//            boundaries.getBoundary(imageKey).setOffset(image.getOffset());
//            Color color = new Color(temp[0],temp[1],temp[2]);
//            boundaries.getBoundary(imageKey).setBoundaryColor(color);
//        }
//    }
}