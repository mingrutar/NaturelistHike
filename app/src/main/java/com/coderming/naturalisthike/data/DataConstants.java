package com.coderming.naturalisthike.data;

public interface DataConstants  {
    static final String UriSS = "%s/%s";
    static final String UriSAny = "%s/*";
    static final String UriSSAny = "%s/%s/*";
    static final String UriSId = "%s/#";
    static final String UriSSId = "%s/%s/#";

    static final String QuerySQ = "%s=?";
    static final String QueryGE = ">=?";
    static final String QueryEQ = "=";
    static final String QueryAsc = " ASC";
    static final String QueryPS = "=?";
    static final String QueryQs = "'%s'";
    static final String QuerySqS = "%s='%s'";
    static final String QuerySd = "%s=%d";
    static final String QuerySdAndSd = "%s=%d AND %s=%d";
    static final String QuerySdAndS1 = "%s=%d AND %s=1";
    static final String QuerySdSqs = "%s=%d AND %s='%s'";
    static final String QuerySdSqSSd = "%s=%d AND %s='%s' AND %s=%d";

    static final String QueryEQTrue = "=1";

    static final String DelimCommon = ",";
    static final String DelimPeriod = ".";
    static final String DelimSpace = " ";
    static final String DelimSemi = ";";

    static final String CommentStart = "/*";
    static final String ForDelete = "1";

}
