package com.pateo.qingcloud.canal.handler.impl;

import com.alibaba.otter.canal.protocol.FlatMessage;
import com.pateo.qingcloud.canal.handler.AbstractFlatMessageHandler;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.handler.RowDataHandler;

import java.util.List;
import java.util.Map;

public class SyncFlatMessageHandlerImpl extends AbstractFlatMessageHandler {



    public SyncFlatMessageHandlerImpl(List<? extends EntryHandler> entryHandlers, RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        super(entryHandlers, rowDataHandler);
    }

    @Override
    public void handleMessage(FlatMessage flatMessage) {
        super.handleMessage(flatMessage);
    }
}
