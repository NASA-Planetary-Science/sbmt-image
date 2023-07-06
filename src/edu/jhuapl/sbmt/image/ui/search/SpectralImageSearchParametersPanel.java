package edu.jhuapl.sbmt.image.ui.search;

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.swing.CheckBoxTree;

public class SpectralImageSearchParametersPanel extends ImageSearchParametersPanel
{
    private JTable filterTable;
    private JTable userParamTable;
    private JPanel aux;

    public SpectralImageSearchParametersPanel()
    {
        super();

        //TODO: Override and setup in subclass
        hierarchicalSearchScrollPane = new javax.swing.JScrollPane();


        aux = getAuxPanel();
        aux.setLayout(new BoxLayout(aux, BoxLayout.X_AXIS));

        filterTable = new JTable();

        String[] columnNames = new String[]{
                "Select", "Filter Name"
        };

        filterTable.setModel(new FilterTableModel(new Object[0][2], columnNames));

        filterTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);



        filterTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        filterTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        filterTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        filterTable.getColumnModel().getColumn(0).setResizable(true);


        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        aux.add(scrollPane);
        scrollPane.setViewportView(filterTable);

        userParamTable = new JTable();

        String[] columnNames2 = new String[]{
                "Select", "User Defined Search Name"
        };

        userParamTable.setModel(new FilterTableModel(new Object[0][2], columnNames2));

        userParamTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);



        userParamTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        userParamTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        userParamTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        userParamTable.getColumnModel().getColumn(0).setResizable(true);


        JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setPreferredSize(new java.awt.Dimension(300, 100));
        aux.add(scrollPane2);
        scrollPane2.setViewportView(userParamTable);

        //maybe these need to overridden later?
//        redComboBox.setVisible(false);
//        greenComboBox.setVisible(false);
//        blueComboBox.setVisible(false);
//
//        ComboBoxModel redModel = getRedComboBoxModel();
//        ComboBoxModel greenModel = getGreenComboBoxModel();
//        ComboBoxModel blueModel = getBlueComboBoxModel();
//        if (redModel != null && greenModel != null && blueModel != null)
//        {
//            redComboBox.setModel(redModel);
//            greenComboBox.setModel(greenModel);
//            blueComboBox.setModel(blueModel);
//
//            redComboBox.setVisible(true);
//            greenComboBox.setVisible(true);
//            blueComboBox.setVisible(true);
//
//            redButton.setVisible(false);
//            greenButton.setVisible(false);
//            blueButton.setVisible(false);
//        }


    }

    public SpectralImageSearchParametersPanel(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public SpectralImageSearchParametersPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public SpectralImageSearchParametersPanel(LayoutManager layout,
            boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    protected List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }

    public CheckBoxTree getCheckBoxTree()
    {
        return checkBoxTree;
    }

    public void setCheckBoxTree(CheckBoxTree checkBoxTree)
    {
        this.checkBoxTree = checkBoxTree;
    }

    public JTable getFilterTable()
    {
        return filterTable;
    }


    public JTable getUserParamTable()
    {
        return userParamTable;
    }


    public class FilterTableModel extends DefaultTableModel
    {
        public FilterTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            if (column == 0) return true;
            return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex == 0)
                return Boolean.class;
            else
                return String.class;
        }
    }

}
