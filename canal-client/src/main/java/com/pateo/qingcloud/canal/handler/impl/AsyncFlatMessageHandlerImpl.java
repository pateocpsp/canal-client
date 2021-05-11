package com.pateo.qingcloud.canal.handler.impl;

import com.alibaba.otter.canal.protocol.FlatMessage;
import com.pateo.qingcloud.canal.handler.AbstractFlatMessageHandler;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.handler.RowDataHandler;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AsyncFlatMessageHandlerImpl extends AbstractFlatMessageHandler {


    private ExecutorService executor;


    public AsyncFlatMessageHandlerImpl(List<? extends EntryHandler> entryHandlers, RowDataHandler<List<Map<String, String>>> rowDataHandler, ExecutorService executor) {
        super(entryHandlers, rowDataHandler);
        this.executor = executor;
    }

    @Override
    public void handleMessage(FlatMessage flatMessage) {
        executor.execute(() -> super.handleMessage(flatMessage));
    }
}
