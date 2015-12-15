Sikuli Robot Framework Library
==============================

## Introduction
Sikuli Robot Framework Library provide keywords to test UI through [Sikuli](http://www.sikuli.org/).

## Overview
![](https://github.com/rainmanwy/robotframework-SikuliLibrary/blob/master/docs/img/architecture.png "architecture")
* This library is implemented with [Robot Framework Remote Library](https://code.google.com/p/robotframework/wiki/RemoteLibrary)
* Sikuli api is encapsulated as Robot keywords, and explored to clients with [jrobotremoteserver](https://github.com/ombre42/jrobotremoteserver)
* Client is implemented with python and use Robot remote library to communicate with server side
* Users could implement their own clients with different languages which support xml rpc

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


## Installation
### Pip installation
*If target OS is Windows, could use pip to install directly
```
pip install robotframework-SikuliLibrary
```
*If target OS is Linux, please download linux version from [pypi](https://pypi.python.org/pypi/robotframework-SikuliLibrary)
### Build With Maven
* Clone this project, and execute maven package command
* One zip file will be created in "target" folder, could unzip this file and add to PYTHONPATH

### Build With Ant
* Install Sikuli API for your target Operating System, replace sikulixapi.jar in lib folder(api for Windows is include in source, if target OS is Windows, COULD ignore this step)
* Clone this project, and execute ant command to build the project
* If no error, "dist" folder will be created. Add "dist" folder to PYTHONPATH environment variable
* Enter "demo" folder, and executing robot case "testsuite_sikuli_demo.txt" should be passed

###Note
* For Linux, there are some dependencies need be installed, please check [sikuli quick start](http://www.sikulix.com/quickstart.html) to get more details.

## Example
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
