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

    @RobotKeyword("Set min similarity")
    @ArgumentNames({"minSimilarity"})
    public String setMinSimilarity(String minSimilarity) {
        double prevMinSimilarity = Settings.MinSimilarity;
        Settings.MinSimilarity = Double.parseDouble(minSimilarity);
        return Double.toString(prevMinSimilarity);
    }

    @RobotKeyword("OCR text read")
    @ArgumentNames({"ocrTextRead"})
    public void setOcrTextRead(boolean ocrTextRead) {
        Settings.OcrTextRead = ocrTextRead;
    }

    @RobotKeyword("Set show actions")
    @ArgumentNames({"showActions"})
    public void setShowActions(boolean showActions) {
        Settings.setShowActions(showActions);
    }

    @RobotKeyword("Set move mouse delay")
    @ArgumentNames({"delay"})
    public void setMoveMouseDelay(float delay) {
        Settings.MoveMouseDelay = delay;
    }

    @RobotKeyword("Set slow motion delay"
            + "\n Control the duration of the visual effect (seconds).")
    @ArgumentNames({"delay"})
    public void setSlowMotionDelay(float delay) {
        Settings.SlowMotionDelay = delay;
    }

    @RobotKeyword("Set wait scan rate"
            + "\n Specify the number of times actual search operations are performed per second while waiting for a pattern to appear or vanish.")
    @ArgumentNames({"delay"})
    public void setWaitScanRate(float scanRate) {
        Settings.WaitScanRate = scanRate;
    }
}
