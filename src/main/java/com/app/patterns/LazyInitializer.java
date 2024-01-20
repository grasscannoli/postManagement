package com.app.patterns;

public class LazyInitializer <Type, Param>{
    public interface Creator<T, P> {

        T create(P param);
    }
    private final Creator<Type, Param> creator;
    private volatile Type instance;

    public LazyInitializer(Creator<Type, Param> creator) {
        this.creator = creator;
    }

    //https://en.wikipedia.org/wiki/Double-checked_locking
    public Type getOrCreate(Param param) {
        Type localVar = this.instance;
        if (localVar == null) {
            synchronized (this) {
                localVar = this.instance;
                if (localVar == null) {
                    localVar = this.instance = creator.create(param);
                }
            }
        }
        return localVar;
    }
}
