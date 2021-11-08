package edu.jhuapl.sbmt.image.controllers.custom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import edu.jhuapl.sbmt.image.gui.custom.CustomImageImporterDialog;
import edu.jhuapl.sbmt.image.gui.custom.CustomImagesControlPanel;
import edu.jhuapl.sbmt.image.types.customImage.CustomImageKeyInterface;
import edu.jhuapl.sbmt.image.types.customImage.CustomImagesModel;

public class CustomImagesControlController
{
    CustomImagesControlPanel panel;
    CustomImagesModel model;
    List<CustomImageKeyInterface> customImages;

    public CustomImagesControlController(CustomImagesModel model)
    {
        panel = new CustomImagesControlPanel();
        this.model = model;
        this.customImages = model.getCustomImages();
        init();
    }

    public CustomImagesControlPanel getPanel()
    {
        return panel;
    }

    private void init()
    {
        panel.getNewButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                newButtonActionPerformed(e);
            }
        });

        panel.getEditButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                editButtonActionPerformed(e);
            }
        });

        panel.getDeleteButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteButtonActionPerformed(e);
            }
        });
    }

    private void newButtonActionPerformed(ActionEvent evt)
    {
        CustomImageImporterDialog dialog = new CustomImageImporterDialog(null, false, model.getInstrument());
        dialog.setCurrentImageNames(model.getCustomImageNames());
        dialog.setImageInfo(null, model.getModelManager().getPolyhedralModel().isEllipsoid());
        dialog.setLocationRelativeTo(getPanel());
        dialog.setVisible(true);

        // If user clicks okay add to list
        if (dialog.getOkayPressed())
        {
            CustomImageKeyInterface imageInfo = dialog.getImageInfo();
            try
            {
                saveImage(model.getImageResults().size(), null, imageInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        model.editButtonActionPerformed(evt);
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        model.deleteButtonActionPerformed(evt);
    }

    private void saveImage(int index, CustomImageKeyInterface oldImageInfo, CustomImageKeyInterface newImageInfo) throws IOException
    {
        model.saveImage(index, oldImageInfo, newImageInfo);
    }
}
