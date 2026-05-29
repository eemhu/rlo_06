package com.teragrep.rlo_06.refactored;

import com.teragrep.stb_01.Stubable;

public interface Result<T> extends Stubable {
    public abstract T value();
    public abstract String name();
}
