Sikuli Robot Framework Library
==============================

## Introduction
Sikuli Robot Framework Library provides keywords to be used within [Robot Framework's](https://robotframework.org/) environment to test UI through [Sikulix](http://sikulix.com/), a automation tool that uses image recognition to identify and interact with UI components.  

This library supports python 2.x, and python 3.x

## Mapping With Sikulix API Version
As this library is depended with sikulixapi, below table describe the mapping between SikuliLibrary version and sikulixapi version.
Before using this library, please check [doc of sikulix](https://sikulix-2014.readthedocs.io/en/latest/index.html), and make sure the environment satisfy the requirement of sikulix.

|  SikuliLibrary          |  sikulixapi   |
|  ---                    |  ---          |
|  master                 |  2.0.1        |
|  [v1.1.2](https://github.com/rainmanwy/robotframework-SikuliLibrary/tree/v1.1.2)   |   1.1.2       |



## Overview
![](https://github.com/rainmanwy/robotframework-SikuliLibrary/blob/master/docs/img/architecture.png "architecture")
* This library is implemented with [Robot Framework Remote Library](https://code.google.com/p/robotframework/wiki/RemoteLibrary)
* Sikuli api is encapsulated as Robot keywords, and explored to clients with [jrobotremoteserver](https://github.com/ombre42/jrobotremoteserver)
* Client is implemented with python and use Robot remote library to communicate with server side
* Users could implement their own clients with different languages which support xml rpc


## Keyword Documentation

Here is a list of the available [Keywords](http://rainmanwy.github.io/robotframework-SikuliLibrary/doc/SikuliLibrary.html)

# Getting Started 

This guide will take you through setting up Robot Framework with Sikuli Library, on a Windows machine.


## Step 1: Install the basic components

Make sure you have at least java 8 installed, python 2.x or 3.x and [pip](https://pypi.org/project/pip/)  

Run the command line below to check the currently version that you have installed:

To check java version:  ```java -version```

To check python version: ``` python --version ```

To check pip version:  ``` pip --version ```

## Step 2: Install Robot Framework and Sikuli Library

Using pip, you can install Robot Framework

```
pip install robotframework
```
And then install the library 
```
pip install robotframework-SikuliLibrary
```

* If target OS is Linux, please download linux version from [pypi](https://pypi.python.org/pypi/robotframework-SikuliLibrary)
* Note: pypi version may not latest version, if you want to use latest version, please check "Build With Maven"


## Other options for Linux users

### Build With Maven
* Clone this project, and execute maven package command
* One zip file will be created in "target" folder, could unzip this file and add to PYTHONPATH
* If want to installed to python, please execute
```
python setup.py install
```

### Note
* For Linux, there are some dependencies need be installed, please check [sikuli quick start](http://www.sikulix.com/specials/files/linux-setup-prerequisites.html) to get more details.
* Python should be installed as maven will execute python command
* OS should allow java process access Internet 

# Writing your first test

### Hello World Example
```
*** Settings ***
Documentation     Sikuli Library Demo
Test Setup        Add Needed Image Path
Test Teardown     Stop Remote Server
Library           SikuliLibrary

*** Variables ***
${IMAGE_DIR}      ${CURDIR}\\img

*** Test Cases ***
Windows Notpad Hellow World
    Open Windows Start Menu
    Open Notepad
    Input In Notepad
    Quit Without Save

*** Keywords ***
Add Needed Image Path
    Add Image Path    ${IMAGE_DIR}

Open Windows Start Menu
    Click    windows_start_menu.png

Open Notepad
    Input Text    search_input.png    notepad
    Click    notepad.png
    Double Click    notepad_title.png

Input In Notepad
    Input Text    notepad_workspace.png    Hello World
    Text Should Exist    Hello World

Quit Without Save
    Click    close.png
    Click    dont_save.png
```
### Click In Example
```
*** Settings ***
Documentation     Sikuli Library Click In Example
Test Setup        Add Needed Image Path
Test Teardown     Stop Remote Server
Library           SikuliLibrary
Library           OperatingSystem

*** Variables ***
${IMAGE_DIR}      ${CURDIR}\\img

*** Test Cases ***
Click In Example
    Open Example UI
    Click Right OK Button
    Click Left OK Button

*** Keywords ***
Add Needed Image Path
    Add Image Path    ${IMAGE_DIR}

Open Example UI
    Run         chrome ${CURDIR}\\html\\click_in_demo.html

Click Right OK Button
    Click In        right_area.png      target.png

Click Left OK Button
    Click In        left_area.png      target.png

```
### Ruby Client Example
```ruby
require "xmlrpc/client"
require "pathname"

client = XMLRPC::Client.new("127.0.0.1", "/", 10000)
client.call("get_keyword_names")
client.call("run_keyword", "addImagePath", [Pathname.new(File.dirname(__FILE__)).realpath.to_s+"/img"])
client.call("run_keyword", "click", ["windows_start_menu.png"])
client.call("run_keyword", "waitUntilScreenContain", ["search_input.png", "5"])
client.call("run_keyword", "input_text", ["search_input.png", "notepad"])
client.call("run_keyword", "click", ["notepad.png"])
client.call("run_keyword", "doubleClick", ["notepad_title.png"])
client.call("run_keyword", "click", ["close.png"])
```



# Advance Options

## Start Server Manually
SikuliLibrary contains a standalone jar file which could be started in command line. Sometimes user want to do test on different OS. The steps are:
* Find SikuliLibrary.jar in "SikuliLibrary/lib" folder and upload to target OS.
* Start jar with command
```
java -jar SikuliLibrary.jar  <port>  <captured_imagine_folder>
```
* User could use different clients to connect to server and call keywords. For example [Remote Library](https://github.com/robotframework/RemoteInterface) in robot framework.
```
Library        Remote        http://<ip>:<port>/
```

## "NEW" mode
* By default, SikuliLibrary will start sikuli java process implicitly when library is initializing by Robot Framework. This behavior bring some problems.
* Now with **"NEW"** mode, user could use keyword [Start Sikuli Process](http://rainmanwy.github.io/robotframework-SikuliLibrary/doc/SikuliLibrary.html#Start%20Sikuli%20Process) to start the process explicitly.
You may check the detail in [issue 16](https://github.com/rainmanwy/robotframework-SikuliLibrary/issues/16)
* Example:
```
*** Settings ***
Library            SikuliLibrary     mode=NEW
Suite Setup        Start Sikuli Process
Suite Teardown     Stop Remote Server

*** Test Cases ***
New Mode
    Add Image Path    E:/config
    Click             click.png
```


### Start Server
```
java -jar SikuliLibrary.jar 10000 .

0 [main] INFO org.robotframework.remoteserver.RemoteServer  - Mapped path / to library com.github.rainmanwy.robotframework.sikulilib.SikuliLibrary.
1 [main] INFO org.robotframework.remoteserver.RemoteServer  - Robot Framework remote server starting
1 [main] INFO org.eclipse.jetty.server.Server  - jetty-7.x.y-SNAPSHOT
28 [main] INFO org.eclipse.jetty.server.handler.ContextHandler  - started o.e.j.s.ServletContextHandler{/,null}
129 [main] INFO org.eclipse.jetty.server.AbstractConnector  - Started SelectChannelConnector@0.0.0.0:10000
129 [main] INFO org.robotframework.remoteserver.RemoteServer  - Robot Framework remote server started on port 10000.
```

## Disable Java Process Log File
Could configure environment variable *DISABLE_SIKULI_LOG* to disable create log files
```
Linux: export DISABLE_SIKULI_LOG=yes
```

## Microsoft Management Console (MMC)
In Windows environment, some applications are created using MMC. SikuliX is only able to interact with MMC if you launch as Administrator the Sikuli IDE or the test script using SikuliX library.

If you start seeing errors like the below, you are running your tests against an MMC application as a non-admin:
```
[log] CLICK on L(1061,118)@S(0)[0,0 1920x1080]
[error] RobotDesktop: checkMousePosition: should be L(1061,118)@S(0)[0,0 1920x1080]
but after move is L(137,215)@S(0)[0,0 1920x1080]
Possible cause in case you did not touch the mouse while script was running:
 Mouse actions are blocked generally or by the frontmost application.
You might try to run the SikuliX stuff as admin.
```
Another symptom is that your mouse will not move, and if it moves (there are random instances when the mouse moves), it will not click, so your test will fail. 

Setting UAC to the lowest level (not to notify the user) will reduce the instances of MMC dialogs. This does not mean that UAC is turned off, just that it does not have any unnecessary popup when your tests are being run (or you will have to take care of them in your test scripts).


## Differences With Other Similiar Sikuli Libraries
* Robot Remote Library technology is used, different client part program languages are supported
* Do not plan to expose sikuli api to Robot Framework directly. All sikuli api are encapsulated as Keywords.
* Wait functionality is added for each operations
  ```java
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
  ```
* Keyword to handel similiar images issue, could check "click_in" test suite in demo folder to get details
  ```java
     public void clickIn(String areaImage, String targetImage) throws Exception {
         wait(areaImage, Double.toString(this.timeout));
         Match match = screen.find(areaImage);
         System.out.println(areaImage + " is found!");
         match.click(targetImage);
         capture(match.find(targetImage));
     }
  ```
* Operating images could be shown in robot logs, easy to troubleshooting
![](https://github.com/rainmanwy/robotframework-SikuliLibrary/blob/master/docs/img/log.png "log")

