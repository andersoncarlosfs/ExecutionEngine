package com.andersoncarlosfs.execution.constants;

public class Settings {

    private static final String root = "./";
    public static final String cache = root + "cache/";
    public static final String definitions = root + "definitions/";

    public static final String getDirForCallResults(String ws) {
        return cache + ws + "/call_results/";
    }

    public static final String getDirForTransformationResults(String ws) {
        return cache + ws + "/transf_results/";
    }

}
