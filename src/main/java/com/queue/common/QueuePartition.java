package com.queue.common;

import com.queue.common.Record;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuePartition<T extends Record> {


    private BlockingQueue<T> recordQueue;

    public QueuePartition() {
        this.recordQueue = new LinkedBlockingQueue<T>();
    }

    public boolean addRecord(T record){

        return this.recordQueue.offer(record);
    }

    public BlockingQueue<T> getRecordQueue() {
        return recordQueue;
    }

    @Override
    public String toString() {

        return this.recordQueue.toString();
    }
}
