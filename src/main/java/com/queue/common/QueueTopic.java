package com.queue.common;

import java.util.ArrayList;
import java.util.List;

public class QueueTopic<T extends Record> {

    private String queueName;

    private Integer partitionCount;

    private List<QueuePartition<T>> queuePartitions;

    public QueueTopic(String queueName){
        this(queueName,8);
    }

    public QueueTopic(String queueName,Integer partitionCount){
        this.queueName=queueName;
        this.partitionCount=partitionCount;
        this.queuePartitions=new ArrayList<QueuePartition<T>>(partitionCount);
        for (int i = 0; i < partitionCount; i++) {
            this.queuePartitions.add(new QueuePartition<T>());
        }
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Integer getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(Integer partitionCount) {
        this.partitionCount = partitionCount;
    }

    public List<QueuePartition<T>> getQueuePartitions() {
        return queuePartitions;
    }

    public void setQueuePartitions(List<QueuePartition<T>> queuePartitions) {
        this.queuePartitions = queuePartitions;
    }
}
