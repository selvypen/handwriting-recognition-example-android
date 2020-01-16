/*!
 *  @date 2020/01/02
 *  @file WritingRecognizer.java
 *  @author SELVAS AI
 *
 *  Copyright 2020. SELVAS AI Inc. All Rights Reserved.
 */

package com.selvasai.handwriting;

import android.content.Context;

import com.diotek.dhwr.DHWR;

import java.util.Locale;

public class WritingRecognizer {

    private final static int MAX_CANDIDATES = 5;

    private Context mContext;
    private DHWR.Ink mInk;
    private DHWR.Setting mSetting;
    private DHWR.Result mResult;

    public WritingRecognizer(Context context) {
        mContext = context;
        initialize();
    }

    private int initialize() {
        final String filesPath = mContext.getFilesDir().getAbsolutePath();
        int status = DHWR.Create(filesPath + "/" + "license.key");
        DHWR.SetExternalResourcePath(filesPath.toCharArray());
        DHWR.SetExternalLibraryPath(mContext.getApplicationInfo().nativeLibraryDir.toCharArray());

        mInk = new DHWR.Ink();
        mSetting = new DHWR.Setting();
        mResult = new DHWR.Result();

        DHWR.SetRecognitionMode(mSetting.GetHandle(), DHWR.MULTICHAR);
        DHWR.SetCandidateSize(mSetting.GetHandle(), MAX_CANDIDATES);
        DHWR.ClearLanguage(mSetting.GetHandle());
        DHWR.AddLanguage(mSetting.GetHandle(), DHWR.DLANG_KOREAN,
                DHWR.DTYPE_KOREAN | DHWR.DTYPE_UPPERCASE | DHWR.DTYPE_LOWERCASE | DHWR.DTYPE_SIGN | DHWR.DTYPE_NUMERIC);
        DHWR.SetAttribute(mSetting.GetHandle());

        return status;
    }

    public int destroy() {
        return DHWR.Close();
    }

    public void clearInk() {
        mInk.Clear();
    }

    public void addPoint(int x, int y) {
        mInk.AddPoint(x, y);
    }

    public void endStroke() {
        mInk.EndStroke();
    }

    public String recognize() {
        int status = DHWR.Recognize(mInk, mResult);
        String candidates = "";
        if (status == DHWR.ERR_SUCCESS) {
            candidates = getCandidates(mResult);
        }
        if (candidates.isEmpty()) {
            candidates = "No result";
        }
        return candidates;
    }

    public void setLanguage(int language, int option) {
        DHWR.ClearLanguage(mSetting.GetHandle());
        DHWR.AddLanguage(mSetting.GetHandle(), language, option);
        DHWR.SetAttribute(mSetting.GetHandle());
    }

    private String getCandidates(DHWR.Result result) {
        StringBuilder candidates = new StringBuilder();
        boolean exit = false;
        int resultSize = result.size();
        if (resultSize < 1) {
            return "";
        }

        for (int i = 0; i < MAX_CANDIDATES; i++) {
            for (int j = 0; j < resultSize; j++) {
                DHWR.Line line = result.get(j);
                for (int k = 0; k < line.size(); k++) {
                    DHWR.Block block = line.get(k);
                    if (block.candidates.size() <= i) {
                        exit = true;
                        break;
                    }
                    candidates.append(String.format(Locale.US, " [%d] ", i + 1));
                    candidates.append(block.candidates.get(i));
                    if (k + 1 < line.size()) {
                        candidates.append(" ");
                    }
                }
                if (exit) {
                    break;
                }
                if (j + 1 < result.size()) {
                    candidates.append("\n");
                }
            }
            if (exit) {
                break;
            }
            candidates.append("\n");
        }
        return candidates.toString();
    }

    public String getVersion() {
        final int MAX_VERSION_LENGTH = 64;
        char[] version = new char[MAX_VERSION_LENGTH];
        DHWR.GetRevision(version);
        return String.valueOf(version).trim();
    }
}
