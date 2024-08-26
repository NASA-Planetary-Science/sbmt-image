package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import glum.gui.action.PopAction;
import net.miginfocom.swing.MigLayout;

public class ChangeOpacityAction<G1 extends IPerspectiveImage> extends PopAction<G1>
{
    private PerspectiveImageCollection aManager;
    private final Renderer renderer;

	/**
	 * @param imagePopupMenu
	 */
	public ChangeOpacityAction(PerspectiveImageCollection aManager, Renderer renderer)
	{
		this.aManager = aManager;
		this.renderer = renderer;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		ImageOpacityChanger opacityChanger = new ImageOpacityChanger(aManager);
        opacityChanger.setLocationRelativeTo(renderer);
        opacityChanger.setVisible(true);
	}
}

class ImageOpacityChanger extends JDialog implements ChangeListener
{
    private JLabel opacityLabel;
    private JSpinner opacitySpinner;
    private JButton btnNewButton;
    PerspectiveImageCollection aManager;

    public ImageOpacityChanger(PerspectiveImageCollection aManager)
    {
    	this.aManager = aManager;

        opacityLabel = new JLabel("Opacity");
        opacitySpinner = new JSpinner(new SpinnerNumberModel(aManager.getOpacity(), 0.0, 1.0, 0.1));
        opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "0.00"));
        opacitySpinner.setPreferredSize(new Dimension(80, 21));
        opacitySpinner.addChangeListener(this);


        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("", "[]", "[][][]"));

        panel.add(opacityLabel, "cell 0 0");
        panel.add(opacitySpinner, "cell 0 0");

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        getContentPane().add(panel, BorderLayout.CENTER);

        btnNewButton = new JButton("Close");
        btnNewButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });
        panel.add(btnNewButton, "cell 0 1,alignx center");
        pack();
    }

    public void stateChanged(ChangeEvent e)
    {
        double val = (Double)opacitySpinner.getValue();
        aManager.setOpacity(val);

    }

}