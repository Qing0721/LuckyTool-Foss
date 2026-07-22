package com.fosstool.app;

interface IFiveGController {
    boolean checkCompatibility(int subId);
    boolean getFiveGStatus(int subId);
    void setFiveGStatus(int subId,boolean enabled);
}
