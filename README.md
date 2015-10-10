Sikuli Robot Framework Library
==============================

Introduction
------------------------------
Sikuli Robot Framework Library provide keywords to test UI through [Sikuli](http://www.sikuli.org/).

Overview
------------------------------
![](https://raw.github.com/rainmanwy/temp/master/doc/img/architecture.png "architecture")

Installation
------------------------------
If want to build yourself, Please follow below steps
* Install ant tool
* Clone this project, and execute ant command to build the project
* Install Sikuli API for your target Operating System, replace sikulixapi.jar in lib folder
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