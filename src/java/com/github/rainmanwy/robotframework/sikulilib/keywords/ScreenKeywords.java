package com.github.rainmanwy.robotframework.sikulilib.keywords;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.annotation.RobotKeywordOverload;

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
    private Map<String, Match> highlightMap = new HashMap<String, Match>();

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

    @RobotKeyword("Remove image path")
    @ArgumentNames({"path"})
    public boolean removeImagePath(String path) {
        return ImagePath.remove(path);
    }

    @RobotKeyword("Set folder for captured images")
    @ArgumentNames({"path"})
    public void setCaptureFolder(String path) {
        CaptureFolder.getInstance().setCaptureFolder(path);
    }

    @RobotKeyword("Click image")
    @ArgumentNames({"image", "xOffset=0", "yOffset=0"})
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

    @RobotKeywordOverload
    public void click(String image, int xOffset, int yOffset) throws Exception{
        Match match = wait(image, Double.toString(this.timeout));
        try {
            int newX = match.getX() + xOffset;
            int newY = match.getY() + yOffset;
            Location newLocation = new Location(newX, newY);
            screen.click(newLocation);
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
    }

    @RobotKeyword("Double click image")
    @ArgumentNames({"image", "xOffset=0", "yOffset=0"})
    public void doubleClick(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            screen.doubleClick(image);
        }
        catch (FindFailed e) {
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
    }

//added by auyong    
    @RobotKeywordOverload
    public void doubleClick(String image, int xOffset, int yOffset) throws Exception{
        Match match = wait(image, Double.toString(this.timeout));
        try {
            int newX = match.getX() + xOffset;
            int newY = match.getY() + yOffset;
            Location newLocation = new Location(newX, newY);
            screen.doubleClick(newLocation);
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Double Click "+image+" failed"+e.getMessage(), e);
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

    private Match find(String image) {
        try {
            Match match = screen.find(image);
            capture(match);
            return match;
        } catch (FindFailed e) {
            System.out.println("Could not find " + image);
            return null;
        }
    }

    @RobotKeyword("Wait until image shown in screen")
    @ArgumentNames({"image", "timeout"})
    public void waitUntilScreenContain(String image, String timeout) throws TimeoutException {
        wait(image, timeout);
    }

    @RobotKeyword("Wait until image not in screen")
    @ArgumentNames({"image", "timeout"})
    public void waitUntilScreenNotContain(String image, String timeout) throws TimeoutException {
        boolean result = screen.waitVanish(image, Double.parseDouble(timeout));
        capture();
        if (result==false) {
            throw new TimeoutException(image+" is still in screen");
        }
    }

    @RobotKeyword("Screen should contain image")
    @ArgumentNames({"image"})
    public void screenShouldContain(String image) throws ScreenOperationException {
        Match match = find(image);
        if (match == null) {
            capture();
            throw new ScreenOperationException("Screen should contain "+image);
        }
    }

    @RobotKeyword("Screen should not contain image")
    @ArgumentNames({"image"})
    public void screenShouldNotContain(String image) throws ScreenOperationException {
        Match match = find(image);
        if (match != null) {
            capture();
            throw new ScreenOperationException("Screen should not contain "+image);
        }
    }

    @RobotKeyword("Input text. Image could be empty")
    @ArgumentNames({"image", "text"})
    public void inputText(String image, String text) throws Exception {
        System.out.println("Input Text:");
        System.out.println(text);
        if ( !"".equals(image) ) {
            this.click(image);
        }
        int result = screen.type(text);
        if (result == 0) {
            throw new ScreenOperationException("Input text failed");
        }
    }

    @RobotKeyword("Type with modifiers" +
            "\n Example:" +
            "\n |Type With Modifiers| A| CTRL |")
    @ArgumentNames({"text", "*modifiers"})
    public void typeWithModifiers(String text, String[] modifiers) throws Exception {
        System.out.println("Input Text:");
        String keys = "";
        for (String modifier : modifiers) {
            keys = modifier + "+";
        }
        keys = keys + text;
        System.out.println(keys);

        int sum = 0;
        for (String modifer : modifiers) {
            try {
                Object key = KeyModifier.class.getField(modifer).get(null);
                sum = sum + (Integer) key;
            } catch(ReflectiveOperationException e){
                throw new ScreenOperationException("No " +modifer.toString() + " in class org.sikuli.script.Key ");
            }
        }

        screen.type(text, sum);

    }

    @RobotKeyword("Paste text. Image could be empty")
    @ArgumentNames({"image", "text"})
    public void pasteText(String image, String text) throws Exception {
        System.out.println("Paste Text:");
        System.out.println(text);
        if ( !"".equals(image) ) {
            this.click(image);
        }
        int result = screen.paste(text);
        if (result == 0) {
            throw new ScreenOperationException("Paste text failed");
        }
    }

    @RobotKeyword("Click in. \nClick target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void clickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        capture(match.find(targetImage));
        match.click(targetImage);
    }

    @RobotKeyword("Double click in. \nDouble click target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void doubleClickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        capture(match.find(targetImage));
        match.doubleClick(targetImage);
    }

    @RobotKeyword("Right click in. \nRight click target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void rightClickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        capture(match.find(targetImage));
        match.rightClick(targetImage);
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

    @RobotKeyword("Highlight matched image")
    @ArgumentNames({"image"})
    public void highlight(String image) throws Exception{
        Match match = null;
        if (highlightMap.containsKey(image)==false) {
            match = screen.find(image);
            highlightMap.put(image, match);
            match.highlight();
            capture();
        } else {
            System.out.println("*WARN* "+image+" was already highlighted");
        }
    }

    @RobotKeyword("Clear highlight from screen")
    @ArgumentNames({"image"})
    public void clearHighlight(String image) {
        if (highlightMap.containsKey(image)) {
            Match match = highlightMap.get(image);
            match.highlight();
            highlightMap.remove(image);
        } else {
            System.out.println("*WARN* " + image + " was not highlighted before");
        }
    }

    @RobotKeyword("Clear all highlights from screen")
    @ArgumentNames({})
    public void clearAllHighlights() {
        for(Match match : highlightMap.values()) {
            match.highlight();
        }
        highlightMap.clear();
    }

    @RobotKeyword("Drag the source image to target image.\nIf source image is empty, drag the last match and drop at given target")
    @ArgumentNames({"srcImage", "targetImage"})
    public void dragAndDrop(String srcImage, String targetImage) throws Exception {
        int result = 0;
        if ( "".equals(srcImage) ) {
            result = screen.dragDrop(targetImage);
            wait(targetImage, Double.toString(this.timeout));
        } else {
            Match srcMatch = wait(srcImage, Double.toString(this.timeout));
            Match targetMatch = wait(targetImage, Double.toString(this.timeout));
            result = screen.dragDrop(srcMatch, targetMatch);
        }
        if (result==0) {
            capture();
            throw new ScreenOperationException("Failed to drag "+srcImage+" to " +targetImage);
        }
    }

    @RobotKeyword("Drag the source image to target by offset.\nIf source image is empty, drag the last match and drop at given target")
    @ArgumentNames({"srcImage", "xOffset", "yOffset"})
    public void dragAndDropByOffset(String srcImage, int xOffset, int yOffset) throws Exception {
        int result = 0;
        Match srcMatch;
        if ( "".equals(srcImage) ) {
            srcMatch = screen.getLastMatch();
            if(srcMatch == null) {
                throw new ScreenOperationException("Please input srcImage");
            }
        } else {
            srcMatch = wait(srcImage, Double.toString(this.timeout));
        }
        int newX = srcMatch.getX() + xOffset;
        int newY = srcMatch.getY() + yOffset;
        Location newLocation = new Location(newX, newY);
        result = screen.dragDrop(newLocation);
        if (result==0) {
            capture();
            throw new ScreenOperationException("Failed to drag "+srcImage+" to " +newLocation);
        }
    }



    @RobotKeyword( "Tries to find the image on the screen, returns accuracy score (0-1)" 
                + "\n Example Usage:"
                + "\n | ${score} = | Get Match Score |  somethingThatMayExist.png |"
                + "\n | Run Keyword if | ${score} > 0.95 | keyword1 | ELSE | keyword2 |")
    @ArgumentNames({"image"})
    public Double getMatchScore(String image) throws ScreenOperationException {
        Match match = find(image);
        if (match == null) {
            return 0.0;
        }else{
            return match.getScore();
        }
    }

    @RobotKeyword( "Presses a special keyboard key." 
                + "\n\n For a list of possible Keys view docs for org.sikuli.script.Key ."
                + "\n\n Example Usage:"
                + "\n | Double Click | textFieldWithDefaultText.png | "
                + "\n | Press Special Key | DELETE | ")
    @ArgumentNames({"keyConstant"})
    public void pressSpecialKey(String specialCharName) throws ScreenOperationException{
        try{
            Object key =  Key.class.getField(specialCharName).get(null);
            screen.type(key.toString());
        }
        catch(ReflectiveOperationException e){
            throw new ScreenOperationException("No " +specialCharName.toString() + " in class org.sikuli.script.Key ");
        }
    }

    @RobotKeyword("Move the mouse pointer to the target"
                + "\n\n @image: if image is empty, will move mouse to the last matched."
                + "\n\n Example Usage:"
                + "\n | Mouse Move              | test.png | "
                + "\n | Screen Should Contain   | test.png | "
                + "\n | Mouse Move |")
    @ArgumentNames({"image="})
    public void mouseMove(String image) throws Exception{
        Match match = wait(image, Double.toString(this.timeout));
        int result = match.mouseMove(image);
        if (result == 0) {
            throw new ScreenOperationException("Failed to move mouse to "+image);
        }
    }

    @RobotKeywordOverload
    public void mouseMove() throws Exception{
        int result = screen.mouseMove();
        if (result==0) {
            throw new ScreenOperationException("Failed to move mouse to last matched image");
        }
    }

    @RobotKeyword("Press and hold the specified buttons"
            + "\n\n @mouseButtons: Could be LEFT, MIDDLE, RIGHT"
            + "\n\n Example Usage:"
            + "\n | Mouse Move   | test.png | "
            + "\n | Mouse Down   | LEFT     | RIGHT |"
            + "\n | Mouse Up     |")
    @ArgumentNames({"*mouseButtons"})
    public void mouseDown(String[] mouseButtons) throws Exception{
        String currentButton = "";
        try{
            int sum = 0;
            for (String button : mouseButtons) {
                currentButton = button;
                int buttonValue =  (Integer) Button.class.getField(button).get(null);
                sum = sum + buttonValue;
            }
            screen.mouseDown(sum);
        }
        catch(ReflectiveOperationException e){
            throw new ScreenOperationException("No " +currentButton + " in class org.sikuli.script.Button ");
        }
    }

    @RobotKeyword("release the specified mouse buttons"
            + "\n\n @mouseButtons: Could be LEFT, MIDDLE, RIGHT. If empty, all currently held buttons are released"
            + "\n\n Example Usage:"
            + "\n | Mouse Move   | test.png | "
            + "\n | Mouse Down   | LEFT     | RIGHT |"
            + "\n | Mouse Up     | LEFT     | RIGHT |")
    @ArgumentNames({"*mouseButtons"})
    public void mouseUp(String[] mouseButtons) throws Exception{
        String currentButton = "";
        try{
            int sum = 0;
            for (String button : mouseButtons) {
                currentButton = button;
                int buttonValue =  (Integer) Button.class.getField(button).get(null);
                sum = sum + buttonValue;
            }
            screen.mouseUp(sum);
        }
        catch(ReflectiveOperationException e){
            throw new ScreenOperationException("No " +currentButton + " in class org.sikuli.script.Button ");
        }
    }

    @RobotKeywordOverload
    public void mouseUp() throws Exception{
        screen.mouseUp();
    }

    @RobotKeyword("Move mouse to the target, and wheel up with give steps"
            + "\n\n Example Usage:"
            + "\n | Wheel Up     | 5   | "
            + "\n | Wheel Up     | 5   |  test.png   |")
    @ArgumentNames({"steps", "image="})
    public void wheelUp(int steps, String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        screen.wheel(image, Button.WHEEL_UP, steps);
    }

    @RobotKeywordOverload
    public void wheelUp(int steps) throws Exception{
        screen.wheel(Button.WHEEL_UP, steps);
    }

    @RobotKeyword("Move mouse to the target, and wheel down with give steps"
            + "\n\n Example Usage:"
            + "\n | Wheel Down     | 5   | "
            + "\n | Wheel Down     | 5   |  test.png   |")
    @ArgumentNames({"steps", "image="})
    public void wheelDown(int steps, String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        screen.wheel(image, Button.WHEEL_DOWN, steps);
    }

    @RobotKeywordOverload
    public void wheelDown(int steps) throws Exception{
        screen.wheel(Button.WHEEL_DOWN, steps);
    }

    @RobotKeyword("Get text"
            + "\n\n If image is not given, keyword will get text from whole Screen"
            + "\n If image is given, keyword will get text from matched region"
            + "\n Call keyword setOcrTextRead to set OcrTextRead as true, before using text recognition keywords"
            + "\n\n Example Usage:"
            + "\n | Set Ocr Text Read  | true       |"
            + "\n | Get Text           |"
            + "\n | Get Text           | test.png   |")
    @ArgumentNames({"image="})
    public String getText() throws Exception {
        return screen.text();

    }

    @RobotKeywordOverload
    public String getText(String image) throws Exception {
        Match match = find(image);
        if (match == null) {
            throw new ScreenOperationException("Could not find " + image);
        }
        Image matchImage = match.getImage();
        return matchImage.text();
    }

    @RobotKeyword("Wait For Image"
            + "\n\n Check wantedImage exist. If notWantedImage appear or timeout happened, throw exception"
            + "\n @wantedImage: expected image in screen"
            + "\n @notWantedImage: unexpected image in screen"
            + "\n @timeout: wait seconds"
            + "\n\n Example Usage:"
            + "\n | Wait For Image  | wanted.png | notWanted.png | 5 |")
    @ArgumentNames({"image="})
    public void waitForImage(String wantedImage, String notWantedImage, int timeout) throws Exception {
        Date begineTime = new Date();
        while (System.currentTimeMillis() - begineTime.getTime() < timeout*1000) {
            Match wantedMatch = screen.exists(wantedImage, 0);
            Match notWantedMatch = screen.exists(notWantedImage, 0);
            if (wantedMatch != null) {
                return;
            } else if ( notWantedMatch != null ) {
                throw new ScreenOperationException(notWantedImage + " is founded! " + notWantedMatch);
            } else {
                Thread.sleep(500);
            }
        }
        throw new TimeoutException("Could not find " + wantedImage);

    }

    @RobotKeyword("Exists"
            + "\n\n Check whether image exists in screen"
            + "\n @image: expected image in screen"
            + "\n @timeout: wait seconds"
            + "\n\n Example Usage:"
            + "\n | ${is_exist}  | Exists | image.png | 0 |")
    @ArgumentNames({"image", "timeout="})
    public Boolean exists(String image, int timeout) {
        Match match = screen.exists(image, timeout);
        if (match != null) {
            return true;
        }
        return false;
    }

    @RobotKeywordOverload
    public Boolean exists(String image) throws Exception{
        return exists(image, 0);
    }

}
