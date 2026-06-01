package com.teragrep.rlo_06.refactored;

public final class ResultImpl<T> implements Result<T> {

    private final ResultName name;
    private final T value;

    public ResultImpl(final ResultName name, final T value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public ResultName name() {
        return name;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
