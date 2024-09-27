package edu.jhuapl.sbmt.image.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.image.model.PerspectiveImage;

public class MultispectralSpectrumInfoPanel extends ModelInfoWindow implements PropertyChangeListener
{
    private ModelManager modelManager;
    private PerspectiveImage perspectiveImage;
    private XYSeriesCollection xyDataset;
    private int nsegments;
    @SuppressWarnings("unused")
	private PopupMenu spectralImagesPopupMenu;

    public MultispectralSpectrumInfoPanel(PerspectiveImage perspectiveImage, ModelManager modelManager, PopupMenu spectralImagesPopupMenu)
    {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.modelManager = modelManager;
        this.perspectiveImage = perspectiveImage;
        this.spectralImagesPopupMenu = spectralImagesPopupMenu;

        JPanel panel = new JPanel(new BorderLayout());

        nsegments = perspectiveImage.getNumberOfSpectralSegments();

        xyDataset = new XYSeriesCollection();

        for (int spectrum = 0; spectrum<nsegments; spectrum++)
        {
            double[] wavelengths = this.perspectiveImage.getSpectrumWavelengths(spectrum);
            double[] values = this.perspectiveImage.getSpectrumValues(spectrum);

            // add the jfreechart graph
            XYSeries series = new XYSeries("Segment " + spectrum);

            for (int i=0; i<wavelengths.length; ++i)
                series.add(wavelengths[i], values[i]);

            xyDataset.addSeries(series);
        }

        String wavelengthUnits = perspectiveImage.getSpectrumWavelengthUnits();
        String valueUnits = perspectiveImage.getSpectrumValueUnits();
        JFreeChart chart = ChartFactory.createXYLineChart("LEISA Spectrum", "Wavelength (" + wavelengthUnits + ")", "Flux (" + valueUnits + ")", xyDataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShapesFilled(0, true);
            renderer.setDrawSeriesLineAsPath(true);
        }

        panel.add(chartPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel,
                BoxLayout.PAGE_AXIS));

        // Add a text box for showing information about the image
        String[] columnNames = {"Property", "Value"};

        HashMap<String, String> properties = null;
        Object[][] data = {    {"", ""} };

        try
        {

            properties = this.perspectiveImage.getProperties();
            int size = properties.size();
            data = new Object[size][2];

            int i=0;
            for (String key : properties.keySet())
            {
                data[i][0] = key;
                data[i][1] = properties.get(key);

                ++i;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }



        JTable table = new JTable(data, columnNames)
        {
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        table.setBorder(BorderFactory.createTitledBorder(""));
        table.setPreferredScrollableViewportSize(new Dimension(500, 130));

        JScrollPane scrollPane = new JScrollPane(table);

        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(scrollPane);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);

        createMenus();

        // Finally make the frame visible
        setTitle("Spectrum Properties");

        pack();
        setVisible(true);
    }


    public Model getModel()
    {
        return perspectiveImage;
    }

    public Model getCollectionModel()
    {
        return modelManager.getModel(ModelNames.IMAGES);
    }


    /**
     * The following function is a bit of a hack. We want to reuse the MSIPopupMenu
     * class, but instead of having a right-click popup menu, we want instead to use
     * it as an actual menu in a menu bar. Therefore we simply grab the menu items
     * from that class and put these in our new JMenu.
     */
    private void createMenus()
    {
    	//TODO: FIX THIS.  This is centered around spectral iamges, not spectra, so it really needs it's own popup menu
//        SpectrumPopupMenu msiImagesPopupMenu =
//        new SpectrumPopupMenu(modelManager, null, null);

//        msiImagesPopupMenu.setCurrentSpectrum(perspectiveImage.getServerPath());
//        msiImagesPopupMenu.setCurrentSpectrum("<file path>");

        JMenuBar menuBar = new JMenuBar();

//        JMenu menu = new JMenu("Options");
//        menu.setMnemonic('O');
//
//        Component[] components = msiImagesPopupMenu.getComponents();
//        for (Component item : components)
//        {
//            if (item instanceof JMenuItem)
//                menu.add(item);
//        }
//
//        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    public void propertyChange(PropertyChangeEvent arg0)
    {
        xyDataset.removeAllSeries();

        for (int segment = 0; segment<nsegments; segment++)
        {
            double[] wavelengths = this.perspectiveImage.getSpectrumWavelengths(segment);
            double[] values = this.perspectiveImage.getSpectrumValues(segment);

            // add the jfreechart graph
            XYSeries series = new XYSeries("Segment " + segment);

            for (int i=0; i<wavelengths.length; ++i)
                series.add(wavelengths[i], values[i]);

            xyDataset.addSeries(series);
        }
    }
}
