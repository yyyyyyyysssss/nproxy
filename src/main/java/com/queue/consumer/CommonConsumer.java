package com.queue.consumer;

import com.queue.common.QueuePartition;
import com.queue.common.Record;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public abstract class CommonConsumer<T extends Record> implements Runnable {

    protected volatile List<QueuePartition<T>> currentConsumePartition;

    public void run() {
        while (true){
            for (QueuePartition<T> tQueuePartition : currentConsumePartition) {
                BlockingQueue<T> recordQueue = tQueuePartition.getRecordQueue();
                while (!recordQueue.isEmpty()) {
                    T record = recordQueue.poll();
                    consume(record);
                }
            }
        }
    }

    public abstract void consume(T record);

}
