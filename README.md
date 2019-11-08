# PSIAP-FirstNet-Video-Streaming-Test-Architecture
Software architecture used for testing at FirstNet Test and Innovation Lab

This architecture contains a set of tools that were used for testing the performance of streaming video un
der various loads at the FistNet Test and Innovation Lab. The architecture consists of three primary compo
nents:
1.  FirstNet Testing App (Android)
2.  OBS Studio (desktop)
3.  ANT Media server (server)

The FirstNet Testing App is an Android mobile application written in Java. It is able to stream a local media file from the mobile device to a remote server running the ANT Media server using the RTMP protocol. It is also able to subscribe to an RTMP stream that is broadcast from a machine running OBS Studio (desktop) and save the stream as a recording.

The software was developed to facilitate evaluating the performance of the FirstNet wireless network as part of the NIST PSIAP initiative. The software implements basic functions for streaming video data to/from
mobile devices via commonly used protocols.

________________________________________________
DISTRIBUTION STATEMENT A. Approved for public release. Distribution is unlimited. (C) 2019 Massachusetts Institute of Technology.

This work was performed under the following financial assistance award 70NANB17Hl69 from U.S. Department of Commerce, National Institute of Standards and Technology.

The software/firmware is provided to you on an As-Is basis

Delivered to the U.S. Government with Unlimited Rights, as defined in DFARS Part 252.227-7013 or 7014 (Feb 2014). Notwithstanding any copyright notice, U.S. Government rights in this work are defined by DFARS 252.227-7013 or DFARS 252.227-7014 as detailed above. Use of this work other than as specifically authorized by the U.S. Government may violate any copyrights that exist in this work.
