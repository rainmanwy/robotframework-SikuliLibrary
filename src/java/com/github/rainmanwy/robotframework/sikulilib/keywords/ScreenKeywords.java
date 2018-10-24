package com.github.rainmanwy.robotframework.sikulilib.keywords;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import java.util.HashMap;
import java.util.List;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;
import org.robotframework.javalib.annotation.RobotKeywordOverload;

import com.github.rainmanwy.robotframework.sikulilib.exceptions.TimeoutException;
import com.github.rainmanwy.robotframework.sikulilib.exceptions.ScreenOperationException;
import com.github.rainmanwy.robotframework.sikulilib.utils.CaptureFolder;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

/**
 * Created by Wang Yang on 2015/8/19.
 */

@RobotKeywords
public class ScreenKeywords {

    private static double DEFAULT_TIMEOUT = 3.0;
    private static Screen screen = new Screen();
    private static Region region = new Region(screen);
    private double timeout;
    private Boolean isCaptureMatchedImage = true;
    private Map<String, Match> highlightMap = new HashMap<String, Match>();

    public ScreenKeywords() {
        timeout = DEFAULT_TIMEOUT;
    }

    public static Screen getScreen() {
        return screen;
    }

    public static Region getRegion() {
        return region;
    }

    private Pattern get_pattern(String locator) {
        /**
         * Parse locator string. It can be either of the following:
         * - Image.png
         * - Text
         * - Image.png = 0.9
         * This will return pattern and similarity by parsing above.
         */
        Pattern pattern = null;
        if (locator.contains(".png")) {
            if (locator.contains("=")) {
                locator = locator.replace(" ", "");
                pattern = new Pattern(locator.substring(0, locator.indexOf("="))).similar(Float.parseFloat(locator.substring(locator.indexOf("=") + 1)));
            } else {
                pattern = new Pattern(locator).similar((float)Settings.MinSimilarity);
            }
        } else {
            pattern = new Pattern(locator);
        }



        return pattern;
    }

    @RobotKeyword("Set timeout"
                + "\n\nSet Sikuli timeout(seconds)"
                + "\nExamples:"
                + "\n| Set timeout | 10 |")
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

    @RobotKeyword("Set captured folder"
            + "\n\nSet folder for captured images"
            + "\nExamples:"
            + "\n| Set captured folder | PATH |")
    @ArgumentNames({"path"})
    public void setCaptureFolder(String path) {
        CaptureFolder.getInstance().setCaptureFolder(path);
    }

    @RobotKeyword("Set capture matched image"
            + "\n\nSet capture matched images, the default value is true"
            + "\nExamples:"
            + "\n| Set Capture Matched Image | false |")
    @ArgumentNames({"value"})
    public void setCaptureMatchedImage(boolean value) {
        isCaptureMatchedImage = value;
    }

    @RobotKeyword("Click"
            + "\n\nClick on an image with similarity and offset."
            + "\nExamples:"
            + "\n| Set Capture Matched Image | false |")
    @ArgumentNames({"image", "xOffset=0", "yOffset=0"})
    public int[] click(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            region.click(get_pattern(image));
            Match match = region.getLastMatch();
            int[] reg = new int[4];
            reg[0] = match.getX();
            reg[1] = match.getY();
            reg[2] = match.getW();
            reg[3] = match.getH();
            return reg;
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
    }

    @RobotKeywordOverload
    public int[] click(String image, int xOffset, int yOffset) throws Exception{
        Match match = wait(image, Double.toString(this.timeout));
        Location center = match.getCenter();
        try {
            int newX = center.getX() + xOffset;
            int newY = center.getY() + yOffset;
            Location newLocation = new Location(newX, newY);
            region.click(newLocation);
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }

        int[] reg = new int[4];
        reg[0] = match.getX();
        reg[1] = match.getY();
        reg[2] = match.getW();
        reg[3] = match.getH();
        return reg;
    }

    @RobotKeyword("Click region"
            + "\n\n Click on defined region cooridinates."
            + "\n Optionally Wait for specified time to ensure region has changed."
            + "\n Also, optionally set highlight"
            + "\n\n Examples:"
            + "\n | Click on region | [x,y,w,h] | image.png |"
            + "\n | Click on region | [x,y,w,h] | image.png | 0 |"
            + "\n | Click on region | [x,y,w,h] | image.png | 0 | 2 |")
    @ArgumentNames({"cooridnates", "waitChange=0", "timeout=0"})
    public void clickRegion(ArrayList<Object> cooridnates, double waitChange, int highlight_timeout) {
        int x = Integer.parseInt(cooridnates.get(0).toString());
        int y = Integer.parseInt(cooridnates.get(1).toString());
        int w = Integer.parseInt(cooridnates.get(2).toString());
        int h = Integer.parseInt(cooridnates.get(3).toString());
        Region region = new Region(x, y, w, h);
        // After clicking on plugin, make sure it has changed, before returning from this step.
        String img = capture(region);
        region.click();
        if (waitChange > 0) {
            region.waitVanish(img, waitChange);
            capture(region);
        }
        if (highlight_timeout > 0) {
            region.highlight(highlight_timeout);
        }
    }

    @RobotKeywordOverload
    public void clickRegion(ArrayList<Object> cooridnates, double waitChange) {
        clickRegion(cooridnates, waitChange, 0);
    }

    @RobotKeywordOverload
    public void clickRegion(ArrayList<Object> cooridnates) {
        clickRegion(cooridnates, this.timeout, 0);
    }

    @RobotKeyword("Click nth"
            + "\n\n Click on specific image."
            + "\n Optionally pass similarity and sort by column or row."
            + "\n\n Examples:"
            + "\n | Click on nth image in region | image.png | 1 | 0.9 |"
            + "\n | Click on nth image in region | image.png | 1 | 0.9 | ${FALSE} |")
    @ArgumentNames({"image", "index", "similarity", "sortByColumn=true"})
    public int[] clickNth(String image, int index, Boolean sortByColumn) throws Exception {
        List<Match> matches = null;
        if (sortByColumn) {
            matches = region.findAllByColumn(get_pattern(image));
        } else{
            matches = region.findAllByRow(get_pattern(image));
        }
        Match match = matches.get(index);
        capture(match);
        matches.get(index).click();
        int[] reg = new int[4];
        reg[0] = match.getX();
        reg[1] = match.getY();
        reg[2] = match.getW();
        reg[3] = match.getH();
        return reg;
    }

    @RobotKeywordOverload
    public int[] clickNth(String image, int index) throws Exception {
        return clickNth(image, index, false);
    }

    @RobotKeyword("Double click")
    @ArgumentNames({"image", "xOffset=0", "yOffset=0"})
    public int[] doubleClick(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            region.doubleClick(get_pattern(image));
        }
        catch (FindFailed e) {
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
        Match match = region.getLastMatch();
        int[] reg = new int[4];
        reg[0] = match.getX();
        reg[1] = match.getY();
        reg[2] = match.getW();
        reg[3] = match.getH();
        return reg;
    }

    //added by auyong
    @RobotKeywordOverload
    public int[] doubleClick(String image, int xOffset, int yOffset) throws Exception{
        Match match = wait(image, Double.toString(this.timeout));
        try {
            int newX = match.getX() + xOffset;
            int newY = match.getY() + yOffset;
            Location newLocation = new Location(newX, newY);
            region.doubleClick(newLocation);
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Double Click "+image+" failed"+e.getMessage(), e);
        }
        int[] reg = new int[4];
        reg[0] = match.getX();
        reg[1] = match.getY();
        reg[2] = match.getW();
        reg[3] = match.getH();
        return reg;
    }

    @RobotKeyword("Right click")
    @ArgumentNames({"image"})
    public int[] rightClick(String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        try {
            region.rightClick(get_pattern(image));
        }
        catch (FindFailed e) {
            throw new ScreenOperationException("Click "+image+" failed"+e.getMessage(), e);
        }
        Match match = region.getLastMatch();
        int[] reg = new int[4];
        reg[0] = match.getX();
        reg[1] = match.getY();
        reg[2] = match.getW();
        reg[3] = match.getH();
        return reg;
    }

    private Match wait(String image, String timeout) throws TimeoutException {
        try {
            Match match = region.wait(get_pattern(image), Double.parseDouble(timeout));
            capture(match);
            return match;
        }
        catch(FindFailed e) {
            capture(region);
            throw new TimeoutException("Timeout happend, could not find "+get_pattern(image).toString(), e);
        }
    }

    private Match find(String image) {
        try {
            Match match = region.find(get_pattern(image));
            capture(match);
            return match;
        } catch (FindFailed e) {
            System.out.println("Could not find " + get_pattern(image).toString());
            return null;
        }
    }

    @RobotKeyword("Wait until screen contain"
            + "\n Wait until image shown in screen")
    @ArgumentNames({"image", "timeout"})
    public void waitUntilScreenContain(String image, String timeout) throws TimeoutException {
        wait(image, timeout);
    }

    @RobotKeyword("Wait until screen not contain"
            + "\n Wait until image not in screen")
    @ArgumentNames({"image", "timeout"})
    public void waitUntilScreenNotContain(String image, String timeout) throws TimeoutException {
        boolean result = region.waitVanish(get_pattern(image), Double.parseDouble(timeout));
        capture(region);
        if (!result) {
            throw new TimeoutException(image+" is still in screen");
        }
    }

    @RobotKeyword("Screen should contain")
    @ArgumentNames({"image"})
    public void screenShouldContain(String image) throws ScreenOperationException {
        Match match = find(image);
        if (match == null) {
            capture();
            throw new ScreenOperationException("Screen should contain "+image);
        }
    }

    @RobotKeyword("Screen should not contain"
            + "\n Screen should not contain image"
            + "\n\n Examples:"
            + "\n | Screen should not contain | image.png |")
    @ArgumentNames({"image"})
    public void screenShouldNotContain(String image) throws ScreenOperationException {
        Match match = find(image);
        if (match != null) {
            capture();
            throw new ScreenOperationException("Screen should not contain "+image);
        }
    }

    @RobotKeyword("Input text"
            + "\n Image could be empty"
            + "\n\n Examples:"
            + "\n | Input text | Sikuli |")
    @ArgumentNames({"image", "text"})
    public void inputText(String image, String text) throws Exception {
        System.out.println("Input Text:");
        System.out.println(text);
        if ( !"".equals(image) ) {
            this.click(image);
        }
        int result = region.type(text);
        if (result == 0) {
            throw new ScreenOperationException("Input text failed");
        }
    }

    @RobotKeyword("Type with modifiers" +
            "\n\n Examples:" +
            "\n |Type With Modifiers| A | CTRL |")
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
        region.type(text, sum);
    }

    @RobotKeyword("Paste text. Image could be empty")
    @ArgumentNames({"image", "text"})
    public void pasteText(String image, String text) throws Exception {
        System.out.println("Paste Text:");
        System.out.println(text);
        if ( !"".equals(image) ) {
            this.click(image);
        }
        int result = region.paste(text);
        if (result == 0) {
            throw new ScreenOperationException("Paste text failed");
        }
    }

    @RobotKeyword("Click in. \nClick target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void clickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        capture(match.find(get_pattern(targetImage)));
        match.click(get_pattern(targetImage));
    }

    @RobotKeyword("Double click in. \nDouble click target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void doubleClickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        capture(match.find(get_pattern(targetImage)));
        match.doubleClick(get_pattern(targetImage));
    }

    @RobotKeyword("Right click in. \nRight click target image in area image.")
    @ArgumentNames({"areaImage", "targetImage"})
    public void rightClickIn(String areaImage, String targetImage) throws Exception {
        Match match = wait(areaImage, Double.toString(this.timeout));
        System.out.println(areaImage + " is found!");
        capture(match.find(get_pattern(targetImage)));
        match.rightClick(get_pattern(targetImage));
    }

    private String capture() {
        ScreenImage image = screen.capture(region);
        String imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder());
        System.out.println("*DEBUG* Saved path: "+imagePath);
        File file = new File(imagePath);
        String fileName = file.getName();
        System.out.println("*HTML* <img src='" + CaptureFolder.getInstance().getSubFolder() + "/" + fileName + "'/>");
        return imagePath;
    }

    private String capture(Region region) {
        if (isCaptureMatchedImage) {
            ScreenImage image = screen.capture(region);
            String imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder());
            System.out.println("*DEBUG* Saved path: "+imagePath);
            File file = new File(imagePath);
            String fileName = file.getName();
            System.out.println("*HTML* <img src='" + CaptureFolder.getInstance().getSubFolder() + "/" + fileName + "'/>");
            return imagePath;
        }
        return null;
    }

    @RobotKeyword("Capture region\n"
            + "\n\nCapture region passed"
            + "\nExamples:"
            + "\n| ${screenshotname}= | Capture region | [x, y, w, h] |")
    @ArgumentNames({"cooridnates"})
    public static String captureRegion(ArrayList<Object> cooridnates) {
        int x = Integer.parseInt(cooridnates.get(0).toString());
        int y = Integer.parseInt(cooridnates.get(1).toString());
        int w = Integer.parseInt(cooridnates.get(2).toString());
        int h = Integer.parseInt(cooridnates.get(3).toString());
        Region region = new Region(x, y, w, h);
        ScreenImage image = ScreenKeywords.getScreen().capture(region);
        String imagePath = image.save(CaptureFolder.getInstance().getCaptureFolder());
        System.out.println("*DEBUG* Saved path: "+imagePath);
        File file = new File(imagePath);
        String fileName = file.getName();
        System.out.println("*HTML* <img src='" + CaptureFolder.getInstance().getSubFolder() + "/" + fileName + "'/>");
        return imagePath;
    }

    @RobotKeyword("Capture Roi")
    @ArgumentNames({})
    public String captureRoi(){
        return capture(region);
    }

    @RobotKeyword("Capture whole screen, file name is returned")
    @ArgumentNames({})
    public String captureScreen(){
        return capture();
    }

    @RobotKeyword("Highlight matched image.\n If secs is set, highlight will vanish automatically after setted seconds")
    @ArgumentNames({"image", "secs="})
    public void highlight(String image, Integer secs) throws Exception{
        Match match = null;
        if (highlightMap.containsKey(image)==false) {
            match = region.find(get_pattern(image));
            if (secs != null) {
                match.highlight(secs);
            } else {
                highlightMap.put(image, match);
                match.highlight();
            }
            capture();
        } else {
            System.out.println("*WARN* "+image+" was already highlighted");
        }
    }

    @RobotKeywordOverload
    public void highlight(String image) throws Exception{
        this.highlight(image, null);
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

    @RobotKeyword("Highlight region")
    @ArgumentNames({"cooridnates", "timeout"})
    public void highlightRegion(ArrayList<Object> cooridnates, int timeout) {
        int x = Integer.parseInt(cooridnates.get(0).toString());
        int y = Integer.parseInt(cooridnates.get(1).toString());
        int w = Integer.parseInt(cooridnates.get(2).toString());
        int h = Integer.parseInt(cooridnates.get(3).toString());
        Region region = new Region(x, y, w, h);
        region.highlight(timeout);
        capture(region);
    }

    @RobotKeyword("Highlight ROI")
    @ArgumentNames({"timeout"})
    public void highlightRoi(int timeout) {
        region.highlight(timeout);
        capture(region);
    }

    @RobotKeyword("Drag the source image to target image.\nIf source image is empty, drag the last match and drop at given target")
    @ArgumentNames({"srcImage", "targetImage"})
    public void dragAndDrop(String srcImage, String targetImage) throws Exception {
        int result = 0;
        if ( "".equals(srcImage) ) {
            result = region.dragDrop(targetImage);
            wait(targetImage, Double.toString(this.timeout));
        } else {
            Match srcMatch = wait(srcImage, Double.toString(this.timeout));
            Match targetMatch = wait(targetImage, Double.toString(this.timeout));
            result = region.dragDrop(srcMatch, targetMatch);
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
            srcMatch = region.getLastMatch();
            if(srcMatch == null) {
                throw new ScreenOperationException("Please input srcImage");
            }
        } else {
            srcMatch = wait(srcImage, Double.toString(this.timeout));
        }
        int newX = srcMatch.getX() + xOffset;
        int newY = srcMatch.getY() + yOffset;
        Location newLocation = new Location(newX, newY);
        result = region.dragDrop(newLocation);
        if (result==0) {
            capture();
            throw new ScreenOperationException("Failed to drag "+srcImage+" to " +newLocation);
        }
    }

    @RobotKeyword("Get match score"
            + "Tries to find the image on the screen, returns accuracy score (0-1)"
            + "\n\n Examples:"
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

    @RobotKeyword("Press special key"
            + "\n Presses a special keyboard key."
            + "\n\n For a list of possible Keys view docs for org.sikuli.script.Key ."
            + "\n\n Examples:"
            + "\n | Double Click | textFieldWithDefaultText.png | "
            + "\n | Press Special Key | DELETE | ")
    @ArgumentNames({"keyConstant"})
    public void pressSpecialKey(String specialCharName) throws ScreenOperationException{
        try{
            Object key =  Key.class.getField(specialCharName).get(null);
            region.type(key.toString());
        }
        catch(ReflectiveOperationException e){
            throw new ScreenOperationException("No " +specialCharName.toString() + " in class org.sikuli.script.Key ");
        }
    }

    @RobotKeyword("Mouse move"
            + "Move the mouse pointer to the target"
            + "\n\n @image: if image is empty, will move mouse to the last matched."
            + "\n\n Examples:"
            + "\n | Mouse Move              | test.png | "
            + "\n | Screen Should Contain   | test.png | "
            + "\n | Mouse Move |")
    @ArgumentNames({"image="})
    public void mouseMove(String image) throws Exception{
        Match match = wait(image, Double.toString(this.timeout));
        int result = match.mouseMove(get_pattern(image));
        if (result == 0) {
            throw new ScreenOperationException("Failed to move mouse to "+image);
        }
    }

    @RobotKeywordOverload
    public void mouseMove() throws Exception{
        int result = region.mouseMove();
        if (result==0) {
            throw new ScreenOperationException("Failed to move mouse to last matched image");
        }
    }

    @RobotKeyword("Mouse down"
            + "\n Press and hold the specified buttons"
            + "\n\n @mouseButtons: Could be LEFT, MIDDLE, RIGHT"
            + "\n\n Examples:"
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
            region.mouseDown(sum);
        }
        catch(ReflectiveOperationException e){
            throw new ScreenOperationException("No " +currentButton + " in class org.sikuli.script.Button ");
        }
    }

    @RobotKeyword("Mouse up"
            + "\n Release the specified mouse buttons"
            + "\n\n @mouseButtons: Could be LEFT, MIDDLE, RIGHT. If empty, all currently held buttons are released"
            + "\n\n Examples:"
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
            region.mouseUp(sum);
        }
        catch(ReflectiveOperationException e){
            throw new ScreenOperationException("No " +currentButton + " in class org.sikuli.script.Button ");
        }
    }

    @RobotKeywordOverload
    public void mouseUp() throws Exception{
        region.mouseUp();
    }

    @RobotKeyword("Wheel up"
            + "\n Move mouse to the target, and wheel up with give steps"
            + "\n\n Examples:"
            + "\n | Wheel Up     | 5   | "
            + "\n | Wheel Up     | 5   |  test.png   |")
    @ArgumentNames({"steps", "image="})
    public void wheelUp(int steps, String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        region.wheel(get_pattern(image), Button.WHEEL_UP, steps);
    }

    @RobotKeywordOverload
    public void wheelUp(int steps) throws Exception{
        region.wheel(Button.WHEEL_UP, steps);
    }

    @RobotKeyword("Wheel down"
            + "\n Move mouse to the target, and wheel down with give steps"
            + "\n\n Examples:"
            + "\n | Wheel Down     | 5   | "
            + "\n | Wheel Down     | 5   |  test.png   |")
    @ArgumentNames({"steps", "image="})
    public void wheelDown(int steps, String image) throws Exception{
        wait(image, Double.toString(this.timeout));
        region.wheel(get_pattern(image), Button.WHEEL_DOWN, steps);
    }

    @RobotKeywordOverload
    public void wheelDown(int steps) throws Exception{
        region.wheel(Button.WHEEL_DOWN, steps);
    }

    @RobotKeyword("Get text"
            + "\n\n If image is not given, keyword will get text from whole Screen"
            + "\n If image is given, keyword will get text from matched region"
            + "\n Call keyword setOcrTextRead to set OcrTextRead as true, before using text recognition keywords"
            + "\n\n Examples:"
            + "\n | Set Ocr Text Read  | true       |"
            + "\n | Get Text           |"
            + "\n | Get Text           | test.png   |")
    @ArgumentNames({"image="})
    public String getText() throws Exception {
        return region.text();
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
            + "\n\n @wantedImage: expected image in screen"
            + "\n\n @notWantedImage: unexpected image in screen"
            + "\n\n @timeout: wait seconds"
            + "\n\n Examples:"
            + "\n | Wait For Image  | wanted.png | notWanted.png | 5 |")
    @ArgumentNames({"wantedImage", "notWantedImage", "timeout"})
    public void waitForImage(String wantedImage, String notWantedImage, int timeout) throws Exception {
        Date begineTime = new Date();
        while (System.currentTimeMillis() - begineTime.getTime() < timeout*1000) {
            Match wantedMatch = region.exists(get_pattern(wantedImage), 0);
            Match notWantedMatch = region.exists(get_pattern(notWantedImage), 0);
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

    @RobotKeyword("Wait For Multiple Images"
            + "\n\n Check if images exists in expectedImages or notExpectedImages list. "
            + "If image appears that is listed in notExpectedImages list or timeout happened, throw exception "
            + "If image appears that is listed in expectedImageslist return succesfully. "
            + "\n\n @timeout: wait seconds"
            + "\n\n @pollingInterval: time in seconds between screen checks"
            + "\n\n @expectedImages: list of expected images in screen"
            + "\n\n @notExpectedImages: list of not expected images in screen"
            + "\n\n Examples:"
            + "\n | @{wanted_images} =  | Create List | wanted_image1.png | wanted_image2.png |"
            + "\n | @{not_wanted_images} =  | Create List | not_wanted_image1.png | not_wanted_image2.png | not_wanted_image3.png |"
            + "\n | Wait For Multiple Images | 900 | 10 | ${wanted_images} | ${not_wanted_images} |")
    @ArgumentNames({"timeout", "pollingInterval", "expectedImages", "notExpectedImages"})
    public String waitForMultipleImages(int timeout, int pollingInterval,
                                        ArrayList<String> expectedImages, ArrayList<String> notExpectedImages) throws Exception {

        Date beginTime = new Date();

        while (System.currentTimeMillis() - beginTime.getTime() < timeout*1000) {

            for (String wantedImage : expectedImages)
            {
                Match wantedMatch = region.exists(get_pattern(wantedImage), 0);

                if (wantedMatch != null) {
                    return wantedImage;
                }
            }

            for (String notWantedImage : notExpectedImages) {
                Match notWantedMatch = region.exists(get_pattern(notWantedImage), 0);

                if (notWantedMatch != null) {
                    capture();
                    throw new ScreenOperationException(notWantedImage + " is found! " + notWantedMatch);
                }
            }

            Thread.sleep(pollingInterval * 1000);
        }

        capture();
        throw new TimeoutException("Could not find any images " + Arrays.toString(expectedImages.toArray()) +
                Arrays.toString(notExpectedImages.toArray()));

    }

    @RobotKeyword("Exists"
            + "\n\n Check whether image exists in screen"
            + "\n @image: expected image in screen"
            + "\n @timeout: wait seconds"
            + "\n\n Examples:"
            + "\n | ${is_exist}  | Exists | image.png | 0 |")
    @ArgumentNames({"image", "timeout="})
    public Boolean exists(String image, int timeout) {
        Match match = region.exists(get_pattern(image), timeout);
        if (match != null) {
            return true;
        }
        return false;
    }

    @RobotKeywordOverload
    public Boolean exists(String image) throws Exception{
        return exists(image, 0);
    }

    @RobotKeyword("Change screen id"
            + "\n For multi display, user could use this keyword to switch to the correct screen"
            + "\n\n Examples:"
            + "\n | Change screen id | 1 |")
    @ArgumentNames({"screenId"})
    public void changeScreenId(int screenId) {
        screen = new Screen(screenId);
        region = new Region(screen);
    }

    @RobotKeyword("Reset ROI"
            + "\n Set Region of interest to full screen"
            + "\n\n Examples:"
            + "\n | Reset roi |")
    @ArgumentNames({})
    public void resetRoi() {
        region = new Region(screen);
    }

    @RobotKeyword("Get current screen id")
    @ArgumentNames({})
    public int getCurrentScreenId() {
        return screen.getID();
    }

    @RobotKeyword("Get number of screens")
    @ArgumentNames({})
    public int getNumberOfScreens() {
        return Screen.getNumberScreens();
    }

    @RobotKeyword("Get screen coordinates"
            + "\n\nReturn screen coordinates for active screen"
            + "\n\nExamples:"
            + "\n| @{coordinates}=  | Get Screen Coordinates | 0 |")
    @ArgumentNames({})
    public int[] getScreenCoordinates() {
        int[] coordinates = new int[4];
        coordinates[0] = screen.getX();
        coordinates[1] = screen.getY();
        coordinates[2] = screen.getW();
        coordinates[3] = screen.getH();
        return coordinates;
    }

    @RobotKeyword("Get Image Coordinates"
            + "\n\n Return image coordinates, within region"
            + "\n Examples:"
            + "\n | ${imageCoordinates}= | Get Image Coordinates | image.png=0.75 |"
            + "\n | ${imageCoordinates}= | Get Image Coordinates | image.png=0.75 | [x, y, w, z] |")
    @ArgumentNames({"image", "coordinates=[]"})
    public int[] getImageCoordinates(String image,  ArrayList<Object> coordinates) throws Exception {
        Match match = null;
        if (coordinates.isEmpty()) {
            match = ScreenKeywords.getScreen().find(get_pattern(image));
        } else {
            int x = Integer.parseInt(coordinates.get(0).toString());
            int y = Integer.parseInt(coordinates.get(1).toString());
            int w = Integer.parseInt(coordinates.get(2).toString());
            int h = Integer.parseInt(coordinates.get(3).toString());
            Region region = new Region(x, y, w, h);
            match = region.find(get_pattern(image));
        }

        int [] image_coordinates = new int[4];
        image_coordinates[0] = match.getX();
        image_coordinates[1] = match.getY();
        image_coordinates[2] = match.getW();
        image_coordinates[3] = match.getH();
        return image_coordinates;
    }

    @RobotKeywordOverload
    public int[] getImageCoordinates(String image) throws Exception{
        return getImageCoordinates(image, new ArrayList<Object>());
    }

    @RobotKeyword("Get extended region from"
                + "\n Extended the given image creating a region above or below with the same width"
                + "\n The height can change using the multiplier @number_of_times_to_repeat, if 2 is given the new region will have twice the height of the orignalge ")
    @ArgumentNames({"image", "direction", "number_of_times_to_repeat"})
    public int[] getExtendedRegionFrom(String image, String direction, int number_of_times_to_repeat) throws Exception {

        Match match = null;
        try{
            match = screen.find(get_pattern(image));
            Region new_region = new Region(match);
            int height = new_region.h;
            int width = new_region.w;

            Region r = null;
            int [] result = new int[4];
            if (direction.equals("below")){
                r = new_region.below(height * number_of_times_to_repeat);
                r.highlight(1);
            }
            else if(direction.equals("above")){
                r = new_region.above(height * number_of_times_to_repeat);
                r.highlight(1);
            }
            else if(direction.equals("left")){
                r = new_region.left(height * number_of_times_to_repeat);
            }
            else if(direction.equals("right")){
                r = new_region.right(height * number_of_times_to_repeat);
            }
            else{
                r = new_region;
            }
            System.out.println(r);
            result[0] = r.x;
            result[1] = r.y;
            result[2] = r.w;
            result[3] = r.h;
            return result;
        }
        catch (FindFailed e) {
            capture();
            throw new ScreenOperationException("Extended image "+image+" failed" + e.getMessage(), e);
        }

    }

    @RobotKeyword("Read text from region")
    @ArgumentNames({"reg"})
    public String readTextFromRegion(ArrayList<Object> reg){
        System.out.println("reg variable: " + reg);

        int x = Integer.parseInt(reg.get(0).toString());
        int y = Integer.parseInt(reg.get(1).toString());
        int w = Integer.parseInt(reg.get(2).toString());
        int h = Integer.parseInt(reg.get(3).toString());

        Region region = new Region(x,y,w,h);
        if (region != null) {
            return region.text();
        } else {
            System.out.println("error region test: " + reg);
            return "ERROR";
        }
    }

    @RobotKeyword("Set ROI"
            + "\n\n Set region of interest on screen"
            + "\n Optionally pass highlight timeout."
            + "\n\n Examples:"
            + "\n | Set ROI | [x, y, w, h] |"
            + "\n | Set ROI | [x, y, w, h] | 2 |")
    @ArgumentNames({"cooridnates", "timeout=0"})
    public void setRoi(ArrayList<Object> cooridnates, int timeout) {
        int x = Integer.parseInt(cooridnates.get(0).toString());
        int y = Integer.parseInt(cooridnates.get(1).toString());
        int w = Integer.parseInt(cooridnates.get(2).toString());
        int h = Integer.parseInt(cooridnates.get(3).toString());
        region.setROI(x, y, w, h);
        if (timeout > 0) {
            this.highlightRoi(timeout);
        }
    }

    @RobotKeywordOverload
    public void setRoi(ArrayList<Object> cooridnates) {
        setRoi(cooridnates, 0);
    }

    @RobotKeyword("Select Region"
            + "\n\n Allow user to select a region and capture it."
            + "\n Return array of [capturedImagePath, x, y, w, h]"
            + "\n\n Examples:"
            + "\n | @{SelectedRegion}= | Select region |")
    @ArgumentNames({"message"})
    public String[] selectRegion(String message) {
        Region region = screen.selectRegion(message);
        String imagePath = capture(region);
        String[] retval = new String[5];
        retval[0] = imagePath;
        retval[1] = String.valueOf(region.getX());
        retval[2] = String.valueOf(region.getY());
        retval[3] = String.valueOf(region.getW());
        retval[4] = String.valueOf(region.getH());
        return retval;
    }
}
