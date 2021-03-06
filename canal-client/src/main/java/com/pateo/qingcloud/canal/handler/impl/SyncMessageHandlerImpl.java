package com.pateo.qingcloud.canal.handler.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.pateo.qingcloud.canal.handler.AbstractMessageHandler;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.handler.RowDataHandler;

import java.util.List;

/**
 * @author yang peng
 * @date 2019/3/2710:52
 */
public class SyncMessageHandlerImpl extends AbstractMessageHandler {


    public SyncMessageHandlerImpl(List<? extends EntryHandler> entryHandlers, RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        super(entryHandlers, rowDataHandler);
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
    }


}
