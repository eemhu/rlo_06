package com.teragrep.rlo_06.refactored;

public final class ResultStub<T> implements Result<T> {
    @Override
    public T value() {
        throw new UnsupportedOperationException("Stub result");
    }

    @Override
    public ResultName name() {
        throw new UnsupportedOperationException("Stub result");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
