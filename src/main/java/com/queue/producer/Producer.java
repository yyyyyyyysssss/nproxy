package com.queue.producer;

import com.queue.ProxyRecord;
import com.queue.common.QueuePartition;
import com.queue.common.QueueTopic;
import com.queue.common.Record;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.List;
import java.util.UUID;

public class Producer<T extends Record> {

    private QueueTopic<T> queueTopic;

    public Producer(QueueTopic<T> queueTopic){
        this.queueTopic=queueTopic;
    }


    public static void main(String[] args) throws InterruptedException {
        QueueTopic<ProxyRecord> queueTopic=new QueueTopic<ProxyRecord>("proxy_name");
        Producer<ProxyRecord> producer=new Producer<ProxyRecord>(queueTopic);

        for (int i = 0; i <100 ; i++) {
            ProxyRecord record=new ProxyRecord();
            record.setChannel(new NioSocketChannel());
            record.setMsg(new Object());
            record.setId(UUID.randomUUID().toString());
            producer.produce(record);
        }
        for (int i = 0; i <queueTopic.getQueuePartitions().size() ; i++) {
            System.out.println("第"+i+"分区:");
            System.out.println(queueTopic.getQueuePartitions().get(i));
        }

    }

    public boolean produce(T record){
        String key = record.key();
        Integer partitionCount = queueTopic.getPartitionCount();
        List<QueuePartition<T>> queuePartitions = queueTopic.getQueuePartitions();
        int partition=Math.abs(key.hashCode())% partitionCount;
        QueuePartition<T> queuePartition = queuePartitions.get(partition);
        return queuePartition.addRecord(record);
    }

}
