package edu.jhuapl.sbmt.image.controllers.search;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.image.gui.search.SpectralImageSearchParametersPanel;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;

public class SpectralImageSearchParametersController
        extends ImageSearchParametersController
{
    SpectralImageSearchParametersPanel specPanel = new SpectralImageSearchParametersPanel();

    public SpectralImageSearchParametersController(ImageSearchModel model,
            PickManager pickManager)
    {
        super(model, pickManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setupSearchParametersPanel()
    {
        // TODO Auto-generated method stub
        setPanel(specPanel);
        super.setupSearchParametersPanel();

        String[] filterNames = smallBodyConfig.imageSearchFilterNames;
        JTable filterTable = specPanel.getFilterTable();
        ((DefaultTableModel)filterTable.getModel()).setRowCount(filterNames.length);
        int i = 0;
        for (String name : filterNames)
        {
            model.getFiltersSelected().add(i);
            filterTable.setValueAt(true, i, 0);
            filterTable.setValueAt(name, i++, 1);
        }

        String[] userSearchNames = smallBodyConfig.imageSearchUserDefinedCheckBoxesNames;
        JTable userTable = specPanel.getUserParamTable();
        ((DefaultTableModel)userTable.getModel()).setRowCount(userSearchNames.length);
        i = 0;
        for (String name : userSearchNames)
        {
            model.getCamerasSelected().add(i);
            userTable.setValueAt(true, i, 0);
            userTable.setValueAt(name, i++, 1);
        }

        specPanel.getFilterTable().getModel().addTableModelListener(new TableModelListener()
        {

            @Override
            public void tableChanged(TableModelEvent e)
            {
                model.getFiltersSelected().clear();
                for (int i=0; i < specPanel.getFilterTable().getModel().getRowCount(); i++)
                {
                    if ((Boolean)specPanel.getFilterTable().getValueAt(i, 0))
                    {
                        model.getFiltersSelected().add(i);
                    }
                }
            }
        });


        specPanel.getUserParamTable().getModel().addTableModelListener(new TableModelListener()
        {

            @Override
            public void tableChanged(TableModelEvent e)
            {
                model.getCamerasSelected().clear();
                for (int i=0; i < specPanel.getUserParamTable().getModel().getRowCount(); i++)
                {
                    if ((Boolean)specPanel.getUserParamTable().getValueAt(i, 0))
                    {
                        model.getCamerasSelected().add(i);
                    }
                }
            }
        });
    }

    @Override
    protected void pullFromModel()
    {
        super.pullFromModel();

        for (Integer i : model.getFiltersSelected())
        {
            specPanel.getFilterTable().getValueAt(i, 0);
        }

        for (Integer i : model.getCamerasSelected())
        {
            specPanel.getUserParamTable().getValueAt(i, 0);
        }

    }

    @Override
    protected void pushInputToModel()
    {
        super.pushInputToModel();

        model.getFiltersSelected().clear();
        for (int i=0; i < specPanel.getFilterTable().getModel().getRowCount(); i++)
        {
            if ((Boolean)specPanel.getFilterTable().getValueAt(i, 0))
            {
                model.getFiltersSelected().add(i);
            }
        }

        model.getCamerasSelected().clear();
        for (int i=0; i < specPanel.getUserParamTable().getModel().getRowCount(); i++)
        {
            if ((Boolean)specPanel.getUserParamTable().getValueAt(i, 0))
            {
                model.getCamerasSelected().add(i);
            }
        }
    }

}
