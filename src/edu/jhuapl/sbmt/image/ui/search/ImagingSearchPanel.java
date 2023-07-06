package edu.jhuapl.sbmt.image.ui.search;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.jhuapl.saavtk.model.Controller;

public class ImagingSearchPanel extends JPanel implements Controller.View
{
    JScrollPane scrollPane;
    JPanel containerPanel;

    public ImagingSearchPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane();
        add(scrollPane);
        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        scrollPane.setViewportView(containerPanel);
    }

    public void addSubPanel(JPanel panel)
    {
        containerPanel.add(panel);
    }

    @Override
    public JPanel getComponent()
    {
        return this;
    }
}
