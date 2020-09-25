package com.elderresearch.commons.jri;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import lombok.extern.log4j.Log4j2;

/**
 * Callback for the R engine. This will forward any output from the R console to the Java log.
 * 
 * @author <a href="dimeo@elderresearch.com">John Dimeo</a>
 * @since Sep 24, 2020
 */
@Log4j2
public class RCallback implements RMainLoopCallbacks {
    public static final RCallback INSTANCE = new RCallback();

    @Override
    public void rWriteConsole(Rengine re, String text, int oType) {
        if (oType == 0) {
            log.info(text.trim());
        } else {
            log.warn(text.trim());
        }
    }

    @Override
    public void rBusy(Rengine re, int which) {
        if (which > 0) { log.info("R is working..."); }
    }

    @Override
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        System.out.print(prompt);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String s = br.readLine();
            return StringUtils.isEmpty(s) ? null : s + System.lineSeparator();
        } catch (Exception e) {
            log.warn("Error reading from console", e);
            return null;
        }
    }

    @Override
    public void rShowMessage(Rengine re, String message) {
    	log.info(message);
    }

    @Override
    public String rChooseFile(Rengine re, int newFile) {
        return rReadConsole(re, String.format("Please enter a file name to %s:", newFile == 0 ? "load" : "save"), 0);
    }

    @Override
    public void rFlushConsole(Rengine re) {
    	System.out.flush();
    }

    @Override
    public void rLoadHistory(Rengine re, String filename) {
        // No-op
    }

    @Override
    public void rSaveHistory(Rengine re, String filename) {
        // No-op
    }
}