package com.pateo.qingcloud.canal.factory;


import com.pateo.qingcloud.canal.enums.TableNameEnum;
import com.pateo.qingcloud.canal.handler.EntryHandler;
import com.pateo.qingcloud.canal.util.GenericUtil;
import com.pateo.qingcloud.canal.util.HandlerUtil;

public abstract class AbstractModelFactory<T> implements IModelFactory<T> {


    @Override
    public <R> R newInstance(EntryHandler entryHandler, T t) throws Exception {
        String canalTableName = HandlerUtil.getCanalTableName(entryHandler);
        if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableName)) {
            return (R) t;
        }
        Class<R> tableClass = GenericUtil.getTableClass(entryHandler);
        if (tableClass != null) {
            return newInstance(tableClass, t);
        }
        return null;
    }


    abstract <R> R newInstance(Class<R> c, T t) throws Exception;
}
