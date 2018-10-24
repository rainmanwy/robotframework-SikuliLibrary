package com.github.rainmanwy.robotframework.sikulilib.keywords;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import org.sikuli.script.App;

/**
 * Created by Rajesh Taneja on 2018/10/23.
 */

@RobotKeywords
public class ApplicationKeywords {

    @RobotKeyword("Open application"
            + "\n To open app with parameters, refer:"
            + "\n https://sikulix-2014.readthedocs.io/en/latest/appclass.html#App.App")
    @ArgumentNames({"path"})
    public App openApplication(String path) {
        return App.open(path);
    }

    @RobotKeyword("Close application")
    @ArgumentNames({"name"})
    public App closeApplication(String name) {
        return App.close(name);
    }
}
