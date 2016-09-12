package com.github.rainmanwy.robotframework.sikulilib.keywords;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.sikuli.basics.Settings;

/**
 * Created by Wang Yang on 2016/5/5.
 */

@RobotKeywords
public class SettingsKeywords {

    @RobotKeyword("Set Sikuli minSimilarity(0-1)")
    @ArgumentNames({"minSimilarity"})
    public String setMinSimilarity(String minSimilarity) {
        double prevMinSimilarity = Settings.MinSimilarity;
        Settings.MinSimilarity = Double.parseDouble(minSimilarity);
        return Double.toString(prevMinSimilarity);
    }

    @RobotKeyword("Set Sikuli OCR text read(true/false)")
    @ArgumentNames({"ocrTextRead"})
    public void setOcrTextRead(boolean ocrTextRead) {
        Settings.OcrTextRead = ocrTextRead;
    }

}
