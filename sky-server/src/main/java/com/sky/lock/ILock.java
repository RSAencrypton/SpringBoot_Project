package com.sky.lock;

public interface ILock {

    boolean TryLock(Long TTL);
    void unLock();
}
