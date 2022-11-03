# ROS Sensor Streamer

  ROS Sensor Streaming is an Android based application to stream several sensors 
  available on smartphones (GPS, Accelerometer, Gyroscope...) to a ROS bridge servers by opening a Web Socket connection between the smartphone and a ROS bridge server.

## Quickstart Guide

You can (in a near future) download the application from Google's **Play Store**, or by scanning the following QR Code:

`TODO: Insert QR code in the future`

To quickly getting started follow the next simple steps:
1. After opening the application, navigate to the **Settings**
2. Change the fields according to your needs 
3. Click on **Apply Changes** button. 
4. **After starting a ROS Bridge node** click on the **Connect** button to establish a comunication with the ROS Bridge server and advertise the topics to publish. A message will appear stating if the connection was succefull
5. Travel to the **Runs** page
6. Click on the **Plus** button and 
7. Start your test run, messages should shortly be publish on the the ROS channel. 

![](images/settings_fragment_tutorial.png)
![](images/runs_fragment_tutorial.png)
![](images/run_fragment_tutorial.png)

Check the complete [documentation]{https://mjpc13.github.io/SensorStreamer} for more information.