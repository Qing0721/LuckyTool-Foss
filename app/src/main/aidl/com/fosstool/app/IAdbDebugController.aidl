package com.fosstool.app;

interface IAdbDebugController {
    int getAdbPort();
    void setAdbPort(int port);
    String getWifiIP();
    void restartAdb();
}
