package com.image;

import com.company.Constants;

import javax.swing.*;

public class ImageFactory {
    public static ImageIcon createImage(Image image) {
        ImageIcon imageIcon;

        switch (image) {
            case ICON:
                imageIcon = new ImageIcon(Constants.ICON_IMAGE_URL);
                break;
            case BACKGROUND:
                imageIcon = new ImageIcon(Constants.BACKGROUND_IMAGE_URL);
                break;
            case POINT:
                imageIcon = new ImageIcon(Constants.POINT_IMAGE_URL);
                break;
            default:
                return null;
        }

        return imageIcon;
    }
}
