Sikuli Robot Framework Library
==============================

Introduction
------------------------------
Sikuli Robot Framework Library provide keywords to test UI through [Sikuli](http://www.sikuli.org/).

Overview
------------------------------
![](https://github.com/rainmanwy/robotframework-SikuliLibrary/blob/master/docs/img/architecture.png "architecture")
* This library is implemented with [Robot Framework Remote Library](https://code.google.com/p/robotframework/wiki/RemoteLibrary)
* Sikuli api is encapsulated as Robot keywords, and explored to clients with [jrobotremoteserver](https://github.com/ombre42/jrobotremoteserver)
* Client is implemented with python and use Robot remote library to communicate with server side
* Users could implement their own clients with different languages which support xml rpc

Difference With Other Similiar Sikuli Libraries
------------------------------
* Do not plan to expose sikuli api to Robot Framework directly. All sikuli apis are encapsulated. One benefit is easy to use
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
  * Keyword to handel similiar images problem, could check "click_in" test suite in demo folder to get details
```java
     public void clickIn(String areaImage, String targetImage) throws Exception {
         wait(areaImage, Double.toString(this.timeout));
         Match match = screen.find(areaImage);
         System.out.println(areaImage + " is found!");
         match.click(targetImage);
         capture(match.find(targetImage));
     }
```

Installation
------------------------------
If want to build yourself, Please follow below steps
* Install ant tool
* Install Sikuli API for your target Operating System, replace sikulixapi.jar in lib folder(api for Windows is include in source, if target OS is Windows, COULD ignore this step)
* Clone this project, and execute ant command to build the project
* If no error, "dist" folder will be created. Add "dist" folder to PYTHONPATH environment variable
* Enter "demo" folder, and executing robot case "testsuite_sikuli_demo.txt" should be passed

Example
------------------------------


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
