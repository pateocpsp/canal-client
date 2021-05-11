package com.pateo.qingcloud.canal.handler.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.pateo.qingcloud.canal.handler.AbstractMessageHandler;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.handler.RowDataHandler;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author yang peng
 * @date 2019/3/2921:40
 */
public class AsyncMessageHandlerImpl extends AbstractMessageHandler {


    private ExecutorService executor;


    public AsyncMessageHandlerImpl(List<? extends EntryHandler> entryHandlers, RowDataHandler<CanalEntry.RowData> rowDataHandler, ExecutorService executor) {
        super(entryHandlers, rowDataHandler);
        this.executor = executor;
    }

    @Override
    public void handleMessage(Message message) {
        executor.execute(() -> super.handleMessage(message));
    }
}
