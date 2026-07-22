package com.fosstool.app;

interface IRefreshRateController {
    boolean getRefreshRateDisplay();
    void setRefreshRateDisplay(boolean status);
    List getSupportModes();
    void setRefreshRateMode(int modeId);
    void resetRefreshRateMode();
}
