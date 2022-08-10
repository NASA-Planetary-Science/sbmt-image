package edu.jhuapl.sbmt.image.gui.ui.color;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.dialog.OpacityChanger;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.util.ColorUtil;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.core.image.NoOverlapException;
import edu.jhuapl.sbmt.image.model.ColorImage;
import edu.jhuapl.sbmt.image.model.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.image.model.ColorImageCollection;

import nom.tam.fits.FitsException;


public class ColorImagePopupMenu extends PopupMenu
{
    private Component invoker;
    private ColorImageCollection imageCollection;
    private ColorImageKey imageKey;
    //private ModelManager modelManager;
    private JMenuItem showRemoveImageIn3DMenuItem;
    private JMenuItem mapBoundaryMenuItem;
    private JMenuItem showImageInfoMenuItem;
    private JMenuItem changeNormalOffsetMenuItem;
    private JMenuItem changeOpacityMenuItem;
    private JMenuItem hideImageMenuItem;
    private SbmtInfoWindowManager infoPanelManager;
    private List<ColorImageKey> imageKeys = new ArrayList<ColorImageKey>();
    private JMenuItem centerImageMenuItem;
    private JMenuItem showFrustumMenuItem;
    private JMenu colorMenu;
    private List<JCheckBoxMenuItem> colorMenuItems = new ArrayList<JCheckBoxMenuItem>();
    private JMenuItem customColorMenuItem;
    private Renderer renderer;

    /**
     *
     * @param modelManager
     * @param type the type of popup. 0 for right clicks on items in the search list,
     * 1 for right clicks on boundaries mapped on the small body, 2 for right clicks on images
     * mapped to the small body.
     */
    public ColorImagePopupMenu(
            ColorImageCollection imageCollection,
            SbmtInfoWindowManager infoPanelManager,
            ModelManager modelManager,
            Renderer renderer,
            Component invoker)
    {
        this.imageCollection = imageCollection;
        this.infoPanelManager = infoPanelManager;
        this.renderer = renderer;
        //this.modelManager = modelManager;
        this.invoker = invoker;

        showRemoveImageIn3DMenuItem = new JCheckBoxMenuItem(new ShowRemoveIn3DAction());
        showRemoveImageIn3DMenuItem.setText("Map Color Image");
        this.add(showRemoveImageIn3DMenuItem);

        mapBoundaryMenuItem = new JCheckBoxMenuItem(new MapBoundaryAction());
        mapBoundaryMenuItem.setText("Map Image Boundary");
//        this.add(mapBoundaryMenuItem);

        if (this.infoPanelManager != null)
        {
            showImageInfoMenuItem = new JMenuItem(new ShowInfoAction());
            showImageInfoMenuItem.setText("Properties...");
            this.add(showImageInfoMenuItem);
        }

//        if (renderer != null)
//        {
//            centerImageMenuItem = new JMenuItem(new CenterImageAction());
//            centerImageMenuItem.setText("Center in Window");
//            this.add(centerImageMenuItem);
//        }

//        showFrustumMenuItem = new JCheckBoxMenuItem(new ShowFrustumAction());
//        showFrustumMenuItem.setText("Show Frustum");
//        this.add(showFrustumMenuItem);

        changeNormalOffsetMenuItem = new JMenuItem(new ChangeNormalOffsetAction());
        changeNormalOffsetMenuItem.setText("Change Normal Offset...");
        this.add(changeNormalOffsetMenuItem);

        changeOpacityMenuItem = new JMenuItem(new ChangeOpacityAction());
        changeOpacityMenuItem.setText("Change Opacity...");
        this.add(changeOpacityMenuItem);

        hideImageMenuItem = new JCheckBoxMenuItem(new HideImageAction());
        hideImageMenuItem.setText("Hide Image");
        this.add(hideImageMenuItem);

        colorMenu = new JMenu("Boundary Color");
//        this.add(colorMenu);
        for (ColorUtil.DefaultColor color : ColorUtil.DefaultColor.values())
        {
            JCheckBoxMenuItem colorMenuItem = new JCheckBoxMenuItem(new BoundaryColorAction(color.color()));
            colorMenuItems.add(colorMenuItem);
            colorMenuItem.setText(color.toString().toLowerCase().replace('_', ' '));
            colorMenu.add(colorMenuItem);
        }
        colorMenu.addSeparator();
        customColorMenuItem = new JMenuItem(new CustomBoundaryColorAction());
        customColorMenuItem.setText("Custom...");
        colorMenu.add(customColorMenuItem);
    }

    public void setCurrentImage(ColorImageKey key)
    {
        imageKey = key;
        imageKeys.add(key);
        updateMenuItems();
    }

    private void updateMenuItems()
    {
        boolean containsImage = imageCollection.containsImage(imageKey);

        showRemoveImageIn3DMenuItem.setSelected(containsImage);

        if (showImageInfoMenuItem != null)
            showImageInfoMenuItem.setEnabled(containsImage);

        changeNormalOffsetMenuItem.setEnabled(containsImage);
        changeOpacityMenuItem.setEnabled(containsImage);
        hideImageMenuItem.setEnabled(containsImage);

        ColorImage image = imageCollection.getImage(imageKey);
        if (image != null)
        {
            boolean selectHideImage = !image.isVisible();
            hideImageMenuItem.setSelected(selectHideImage);
        }
    }


    private class ShowRemoveIn3DAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (showRemoveImageIn3DMenuItem.isSelected())
                    imageCollection.addImage(imageKey);
                else
                    imageCollection.removeImage(imageKey);

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            catch (NoOverlapException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class ShowInfoAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                imageCollection.addImage(imageKey);
                infoPanelManager.addData(imageCollection.getImage(imageKey));

                updateMenuItems();
            }
            catch (FitsException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private class MapBoundaryAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            for (ColorImageKey imageKey : imageKeys)
            {
              	imageCollection.getImage(imageKey).setBoundaryVisibility(((JMenuItem)e.getSource()).isSelected());
            }

            updateMenuItems();
        }
    }

//    private class CenterImageAction extends AbstractAction
//    {
//        public void actionPerformed(ActionEvent e)
//        {
//            if (imageKeys.size() != 1)
//                return;
//            ColorImageKey imageKey = imageKeys.get(0);
//
//            double[] spacecraftPosition = new double[3];
//            double[] focalPoint = new double[3];
//            double[] upVector = new double[3];
//            double viewAngle = 0.0;
//
//            if (imageBoundaryCollection != null && imageBoundaryCollection.containsBoundary(imageKey))
//            {
//                PerspectiveImageBoundary boundary = imageBoundaryCollection.getBoundary(imageKey);
//                boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//
//                viewAngle = boundary.getImage().getMaxFovAngle();
//            }
//            else if (imageCollection.containsImage(imageKey))
//            {
//                ColorImage image = (ColorImage)imageCollection.getImage(imageKey);
//                image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//
//                viewAngle = image.getMaxFovAngle();
//            }
//            else
//            {
//                return;
//            }
//
//            renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
//        }
//    }

//    private class ShowFrustumAction extends AbstractAction
//    {
//        public void actionPerformed(ActionEvent e)
//        {
//            for (ColorImageKey imageKey : imageKeys)
//            {
//                try
//                {
//                    imageCollection.addImage(imageKey);
//                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(imageKey);
//                    image.setShowFrustum(showFrustumMenuItem.isSelected());
//                }
//                catch (Exception ex)
//                {
//                    ex.printStackTrace();
//                }
//            }
//
//            updateMenuItems();
//        }
//    }

    private class ChangeNormalOffsetAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            ColorImage image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                NormalOffsetChangerDialog changeOffsetDialog = new NormalOffsetChangerDialog(image);
                changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                changeOffsetDialog.setVisible(true);
            }
        }
    }

    private class ChangeOpacityAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            ColorImage image = imageCollection.getImage(imageKey);
            if (image != null)
            {
                OpacityChanger opacityChanger = new OpacityChanger(image);
                opacityChanger.setLocationRelativeTo(JOptionPane.getFrameForComponent(invoker));
                opacityChanger.setVisible(true);
            }
        }
    }

    private class HideImageAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                imageCollection.addImage(imageKey);
                ColorImage image = imageCollection.getImage(imageKey);
                image.setVisible(!hideImageMenuItem.isSelected());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            updateMenuItems();
        }
    }

    public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId,
            double[] pickedPosition)
    {
        if (pickedProp instanceof vtkActor)
        {
            if (imageCollection.getImage((vtkActor)pickedProp) != null)
            {
                ColorImage image = imageCollection.getImage((vtkActor)pickedProp);
                setCurrentImage(image.getColorKey());
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class BoundaryColorAction extends AbstractAction
    {
        private Color color;

        public BoundaryColorAction(Color color)
        {
            this.color = color;
        }

        public void actionPerformed(ActionEvent e)
        {
            for (ColorImageKey imageKey : imageKeys)
            {
            	imageCollection.getImage(imageKey).setBoundaryColor(color);
            }

            updateMenuItems();
        }
    }

    private class CustomBoundaryColorAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            Color currentColor = imageCollection.getImage(imageKey).getBoundaryColor();
            Color newColor = ColorChooser.showColorChooser(invoker, currentColor);
            if (newColor != null)
            {
                for (ColorImageKey imageKey : imageKeys)
                {
                	imageCollection.getImage(imageKey).setBoundaryColor(newColor);
                }
            }
        }
    }

}
