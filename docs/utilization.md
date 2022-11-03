---
layout: default
title: Utilization Guide
nav_order: 2
---

# Utilization Guide

In order to use this application to broadcast sensor messages to a running ROS environment it is necesary to initiate a websocket server in ROS. The easiest way to open a websocket in ROS1 is with the [ros bridge package](https://github.com/RobotWebTools/rosbridge_suite). With the application running in your Android device, navigate to the settings page, here you can select the desired configuration to run, a brief explanation of every option available is now provided.

### Settings

The following settings are available to the user:
- **Web Socket**: The web socket URL for the node running the ros bridge server (default: ws://127.0.0.1:9090)
- **ROS Topic Prefix**: The prefix for the rostopics of the several topics to be created. This means the messages will be publish under /\[prefix_topic\]/gps/assisted (default: android).
- **Frame Id**: The frame ID inserted in the **Header** object of the ros message (default: smartphone_frame).
- **GPS Rate (Hz)**: The update frequency of the GPS localization in hertz (default: 0.2Hz).

 The **Timestamp** of messages will be sended empty to the server, which will be automatically filled by the ros bridge server when publishing the message to the ROS channel. This help synchronization between messages.

### Topics published

 The following topics are broadcast through the web socket:
 - **/\[prefix_topic\]/gps/assisted** - This topic publish a `sensor_msgs/NavSatFix/` message, where the latitude, longitude and altitude (if available) are acquired by the [fusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) object. This object fuses the GNSS information with network and celular tower informations. The accuracy given by the [location object](https://developer.android.com/reference/android/location/Location) is a radius, therefor the diagonal of covariance matrix is filled as (radius^2)/2, with the covariance of altitude equal to 0 since that information is not provided by the [location object](https://developer.android.com/reference/android/location/Location).


## Using Docker

You can use `docker` and `docker compose` to quickly setup a rosbridge server in you computer. Take the following docker compose as an example:

```yml
version: "3"
services:
  ros-bridge:
    image: mjpc13/ros:noetic-base
    depends_on:
      - ros-master
    network_mode: "host"
    command: roslaunch --wait rosbridge_server rosbridge_websocket.launch
```
The web socket connection value to insert in the application will be **ws://[ip_host_computer]:9090**, since the **9090** is the default port of ros bridge. 

If you desire to create a **bridge** network in docker and gives a explicit IP address to every service available, take the following docker compose as an example:

```yml
version: "3"
networks:
  ros:
    driver: bridge
    ipam:
      config:
        - subnet: 172.26.1.0/16
          gateway: 172.26.0.1
services:
  ros-master:
    image: ros:noetic-core
    networks:
      ros:
        ipv4_address: 172.26.0.2
    command: roscore
    
  ros-bridge:
    image: mjpc13/ros:noetic-base
    depends_on:
      - ros-master
    networks:
      ros:
        ipv4_address: 172.26.0.3
    ports:
      - "9091:9090" 
    environment:
        - "ROS_MASTER_URI=http://ros-master:11311"
        - "ROS_IP=172.26.0.3"
    command: roslaunch --wait rosbridge_server rosbridge_websocket.launch
```
Since we want other devices to be able to comunicate to the rosbridge, the **9090** port of the **ros-bridge** service is forwarded to the port **9091** of the host computer. This means that the web socket connection value to insert in the application will be **ws://[ip_host_computer]:9091**.