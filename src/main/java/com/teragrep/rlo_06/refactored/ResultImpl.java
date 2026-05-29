package com.teragrep.rlo_06.refactored;

public final class ResultImpl<T> implements Result<T> {

    private final String name;
    private final T value;

    public ResultImpl(final String name, final T value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
