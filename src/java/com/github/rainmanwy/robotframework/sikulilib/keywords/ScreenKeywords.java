package com.github.rainmanwy.robotframework.sikulilib.keywords;

import java.io.File;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import com.github.rainmanwy.robotframework.sikulilib.exceptions.TimeoutException;
import com.github.rainmanwy.robotframework.sikulilib.exceptions.ScreenOperationException;
import com.github.rainmanwy.robotframework.sikulilib.utils.CaptureFolder;

import org.sikuli.script.*;

/**
 * Created by Wang Yang on 2015/8/19.
 */

@RobotKeywords
public class ScreenKeywords {

    private static double DEFAULT_TIMEOUT = 3.0;
    private final Screen screen = new Screen();
    private double timeout;

    public ScreenKeywords() {
        timeout = DEFAULT_TIMEOUT;
    }

    @RobotKeyword("Set Sikuli timeout(seconds)")
    @ArgumentNames({"timeout"})
    public String setTimeout(String timeout) {
        double oldTimeout = this.timeout;
        this.timeout = Double.parseDouble(timeout);
        return Double.toString(oldTimeout);
    }

    @RobotKeyword("Add image path")
    @ArgumentNames({"path"})
    public boolean addImagePath(String path) {
        return ImagePath.add(path);
    }

    @RobotKeyword("Set folder for captured images")
    @ArgumentNames({"path"})
    public void setCaptureFolder(String path) {
        CaptureFolder.getInstance().setCaptureFolder(path);
    }

    @RobotKeyword("Click image")
    @ArgumentNames({"image"})
    public void click(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            screen.click(image);
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
    }

    @RobotKeyword("Double click image")
    @ArgumentNames({"image"})
    public void doubleClick(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            screen.doubleClick(image);
        }
        catch (FindFailed e) {
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
    }

    @RobotKeyword("Right click image")
    @ArgumentNames({"image"})
    public void rightClick(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            screen.rightClick(image);
        }
        catch (FindFailed e) {
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
    }

//    @RobotKeyword("Wait image shown")
//    @ArgumentNames({"image", "timeout"})
    private Match wait(String image, String timeout) throws TimeoutException {
        try {
            Match match = screen.wait(image, Double.parseDouble(timeout));
            capture(match);
            return match;
        }
        catch(FindFailed e) {
            capture();
            throw new TimeoutException("Timeout happend, could not find "+image, e);
        }
    }

    @RobotKeyword("Wait until image shown in screen")
    @ArgumentNames({"image", "timeout"})
    public void waitUntilScreenContain(String image, String timeout) throws TimeoutException {
        wait(image, timeout);
    }

    @RobotKeyword("Input text")
    @ArgumentNames({"image", "text"})
    public void inputText(String image, String text) throws Exception {
        System.out.println("Input Text:");
        System.out.println(text);
        this.click(image);
        int result = screen.type(text);
        if (result == 0) {
            throw new ScreenOperationException("Input text failed");
        }
        Key
    }

    @RobotKeyword("Click in. \nClick target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void clickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        match.click(targetImage);
        capture(match.find(targetImage));
    }

    @RobotKeyword("Double click in. \nDouble click target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void doubleClickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        match.doubleClick(targetImage);
        capture(match.find(targetImage));
    }

    @RobotKeyword("Right click in. \nRight click target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void rightClickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        match.rightClick(targetImage);
        capture(match.find(targetImage));
    }

    private void capture() {
        ScreenImage image = screen.capture();
        String imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder());
        System.out.println("*DEBUG* Saved path: "+imagePath);
        File file = new File(imagePath);
        String fileName = file.getName();
        System.out.println("*HTML* <img src='" + CaptureFolder.getInstance().getSubFolder() + "/" + fileName + "'/>");
    }

    private void capture(Region region) {
        ScreenImage image = screen.capture(region);
        String imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder());
        System.out.println("*DEBUG* Saved path: "+imagePath);
        File file = new File(imagePath);
        String fileName = file.getName();
        System.out.println("*HTML* <img src='" + CaptureFolder.getInstance().getSubFolder() + "/" + fileName + "'/>");
    }

    @RobotKeyword("Capture whole screen")
    @ArgumentNames({})
    public void captureScreen(){
        capture();
    }

}
