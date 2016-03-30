package com.github.rainmanwy.robotframework.sikulilib.utils;

import java.io.File;

/**
 * Created by Wang Yang on 2015/10/8.
 */

public class CaptureFolder {

    private String captureFolder = ".";
    private static String SUB_FOLDER = "sikuli_captured";
    private static CaptureFolder MYSELF = null;
    private boolean setted = false;

    private CaptureFolder() {}

    public static CaptureFolder getInstance() {
        if (MYSELF == null) {
            MYSELF = new CaptureFolder();
        }
        return MYSELF;
    }

    public void setCaptureFolder(String captureFolder) {
        this.captureFolder = captureFolder + "/" + CaptureFolder.SUB_FOLDER;
        setted = true;
        File file = new File(this.captureFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public String getCaptureFolder() {
        return captureFolder;
    }

    public String getSubFolder() {
        if (setted) {
            return CaptureFolder.SUB_FOLDER;
        } else {
            return ".";
        }
    }

}
