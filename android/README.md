# PSIAP-FirstNet-Architecture
## PSIAP Analytic Tester 
Android application to integrate with DeviceHive and Ant Media Server.  It has the capability of broadcasting a video saved on the Android device, stream from the camera, or run analytics based on the camera's frames.  

### Bugs
Do not use special characters (', ", _, -, etc) in the stream name.  There is a bug with the Ant media server android package that fails to create filenames with these special characters and the app will contine to crash.  
