# PSIAP-FirstNet-Architecture
## PSIAP Analytic Tester 
Android application to integrate with DeviceHive and Ant Media Server.  It has the capability of broadcasting a video saved on the Android device, stream from the camera, or run analytics based on the camera's frames.  

### Bugs
Do not use special characters (', ", _, -, etc) in the stream name.  There is a bug with the Ant media server android package that fails to create filenames with these special characters and the app will contine to crash.  

________________________________________________
DISTRIBUTION STATEMENT A. Approved for public release. Distribution is unlimited. (C) 2019 Massachusetts Institute of Technology.

This work was performed under the following financial assistance award 70NANB17Hl69 from U.S. Department of Commerce, National Institute of Standards and Technology.

The software/firmware is provided to you on an As-Is basis

Delivered to the U.S. Government with Unlimited Rights, as defined in DFARS Part 252.227-7013 or 7014 (Feb 2014). Notwithstanding any copyright notice, U.S. Government rights in this work are defined by DFARS 252.227-7013 or DFARS 252.227-7014 as detailed above. Use of this work other than as specifically authorized by the U.S. Government may violate any copyrights that exist in this work.
