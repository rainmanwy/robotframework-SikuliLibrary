*** Settings ***
Library    SikuliLibrary
Suite Teardown     Stop Remote Server
*** Test Cases ***
My Test
    Add Image Path    D:\\workspace\\github\\robotframework-SikuliLibrary
    set Always Resize    0
    #Double Click    1.png    xOffset=0   yOffset=-100
    #Right Click    1.png
    Click    1.png
    #${coor}   Create List  0  0  300  300
    #Click Region      ${coor}
    #${text}   Get Text
    #Log   ${text}

