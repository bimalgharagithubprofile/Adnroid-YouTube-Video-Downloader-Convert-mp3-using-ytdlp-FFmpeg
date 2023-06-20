# Adnroid-YouTube-Video-Downloader-Convert-mp3-using-ytdlp-FFmpeg
plug-in python library ytdlp into android and use it to download video from YouTube and also convert that video into a audio file using FFmpeg library.

# YouTube Video Download (YT-DLP)
    Paste a YouTuve video link and select a folder where you want to save the final file (MP3).
    Initially video file downloaded inside internal stoage (data/data/com.bimalghara.mp3downloader/no-backup/downloader/video)

# Convert Video into HD Audio (FFmpeg)
    Video file is executed in FFmpeg with high bitrate to maintaing the HE audio.
    converted mp3 is kept inside  internal stoage (data/data/com.bimalghara.mp3downloader/no-backup/downloader/audio)
    Video file deleted as no longer needed
    Audio file moved in selected folder by the user

# Design
    Design is used combination of Material and Appcompat.
    Both Day & Night Themes are mainaitant even though the values are same which is day - this is something that i just choosed.
    All colors are maintained in material attributes therefoew, if shioft to multi-module we will have direct access of the base theme.
    All size (width, height) are from a single resource that is dimen.xml, nothing hardcoded in the xml file directly, so, if we need to modify size - its just one place that you need to go and make the changes.

# Architecture
    Clean architecture used with MVVM.
    Clean architecture allowes isolated features and modules. allows perfect sacalability. easy and deep testing (both: JVM & UI). easy versioning. less conflicts. more speed of development when time to modify/add modueles or features.
    MVVM is been used becase, this jetpack is perfect for state management also it support reactive programing like FlowState/LiveData etc..
    Usecases are been used becase, as official recomandation of single responsibility classes are one of the core fundamental.
    load/refresh network data in advance - before user reach to home, app will get updated data from cloud. - this bring good experience to user to launch the app and start using/enjoying without seesing loading bar

# Guidelines 
    for FE/UI - Material3 guidelines used.
    for BE/Class - SOLID principles used.
    Corutines - therad execution and management
    Corutines Scope - proper scoping of therad(s) execution
    Flow - Executable task in dedicated thread.
    Resource Wrapper - data transaction between layers are wrapped in sealed class, this is for keep the data impact with all possible state and generic.
    Csutom Exception - any exception is mapped in Error module - we can easily change the error message or add new message from only one resource and map it.
    Use cases - all tasks are hidden and privdede a use case class to the upper layer to call/execture and get some data(Resource Wrappered)  
    Mappers - as remote data class/structure and local data class/structure shouldn't be same, DTO and Entity used. they are converting using mapper class
    Concern of separations - whereever possible to separate the blocks or function or extensions, are separated.
    Koltin Extension function - all possible blockes are maintained in Extension function (Arch Compon, View, Theme) -  this allows common code block executtion and remove deuplication code, also everywhere behaviour will be same. 
    Generic class for builder - whereever possible to instanciate a object at runtime they are not hardcodede or duplicated, instead uses a generic builder. 
    Source of Truth - data in the UI is taken from single srouce and not mixed which is Local DB in this case. (app supports offline access)
    Splash screen - this helps prepare and execture initial stuff(s). 
    - proper folder strcuture
    - name of class/resource is clear
    - name of function is clear
    - proper memrory usage (exmaple - no unnecessary loading icon/png or string or color in layout xml like if it will be load after a netwoerk call, or falls under any condiotion, etc..)

# Tools & Jetpack
    Hilt for DI
    ViewBinding for views
    sdp for size

# Library
    YT-DLP
    FFmpeg
    Retrfit

# Additional Features
    Network monitor - As app having network call, we are observing the network availibility. and when the RestAPI call  
    Csutom Exception - this is to remove dependency of hard-codeded error message everywhere and dificulty while modifiying in future
    Base classes -  this is bring a common skelton of classes/fragments/doaligs together. this will simpilify/clean the code for all over the project.
    Dependency Injection (jetpack) - this is to manage instances of classes
    Reactive programming - LiveData and Flow used to update the UI time-time when any changes in data also maintain the UI state when statel losses & retored
    navGraph - nav grpath is used to naviagte between screens throughout the app, this make app lightweight and faster navigation and less memory consumption and easy activity/frgament lifecycle management.
