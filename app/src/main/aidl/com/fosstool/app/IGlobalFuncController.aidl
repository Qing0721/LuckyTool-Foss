package com.fosstool.app;

interface IGlobalFuncController {
    String getProp(String key,String def);
    String getFileText(String dir);
    String getOtaVersion();
    String getMarketName();
    String getLcdInfo();
    String getFlashInfo();
    String getPcbInfo();
    String getSnInfo();
}
