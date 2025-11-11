package com.usi.m9000.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/* ImageFilter.java is used by FileChooserDemo2.java. */
public class M9kComtradeFileFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        int i = file.getName().lastIndexOf(".");
        String fileExtension;
        if (i > 0 && i < (file.getName().length() - 1))
        {
        	fileExtension = file.getName().substring(i+1);
        	if (fileExtension.equalsIgnoreCase("dat"))
        	{
        		return true;
        	}
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return "*.dat";
    }
}