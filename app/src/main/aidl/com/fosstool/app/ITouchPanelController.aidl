package com.fosstool.app;

interface ITouchPanelController {
    boolean checkTouchMode();
    boolean getTouchMode();
    void setTouchMode(boolean status);

    boolean checkSamplingRateLevel();
    int getSamplingRateLevel();
    void setSamplingRateLevel(int level);
    void resetSamplingRateLevel();
}
