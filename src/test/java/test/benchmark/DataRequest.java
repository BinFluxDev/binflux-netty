package test.benchmark;

import java.io.Serializable;

public class DataRequest implements Serializable {

    private final String string;
    private final long tLong;
    private final int tInt;

    public DataRequest(String string, long tLong, int tInt) {
        this.string = string;
        this.tLong = tLong;
        this.tInt = tInt;
    }

    public String getString() {
        return string;
    }

    public long gettLong() {
        return tLong;
    }

    public int gettInt() {
        return tInt;
    }
}
