package com.pateo.qingcloud.canal.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import java.util.List;
import java.util.concurrent.CountDownLatch;


@Slf4j
public class ZooKeeperUtils {

    private ZooKeeper zooKeeper = null;

    public ZooKeeperUtils(String zkServers) {
        zkConnectionInit(zkServers);
    }


    private void zkConnectionInit(String zkServers) {
        if (zooKeeper == null) {
            synchronized (ZooKeeperUtils.class) {
                if (zooKeeper == null) {
                    try {
                        final CountDownLatch latch = new CountDownLatch(1);
                        zooKeeper = new ZooKeeper(zkServers, 30000, new Watcher() {
                            public void process(WatchedEvent event) {
                                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                                    log.info("连接创建成功");
                                    latch.countDown();
                                }
                            }
                        });
                        //主线程阻塞等待连接对象的创建成功
                        latch.await();
                    } catch (Exception e) {
                        log.error("创建zk连接发生异常" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 创建zk节点
     *
     * @param path
     * @param data
     * @param acl
     * @param createMode
     * @return
     */
    public String create(final String path, String data, List<ACL> acl, CreateMode createMode) {
        String zkPath = null;
        try {
            Stat stat = zooKeeper.exists(path, true);
            if (stat == null) {
                zkPath = zooKeeper.create(path, data.getBytes(), acl, createMode);
                log.info("{}\t节点创建成功", path);
            } else {
                log.info("{}\t节点已经存在了", path);
            }
        } catch (Exception e) {
            log.error("创建zk节点发生异常" + e.getMessage());
        }

        return zkPath;
    }

    /**
     * 删除zk节点
     *
     * @param path
     */
    public void delete(String path) {
        try {
            zooKeeper.delete(path, -1);
            log.info("{}\t删除成功", path);
        } catch (Exception e) {
            log.error("删除zk节点发生异常" + e.getMessage());
        }
    }

    /**
     * 获取某个节点下面有多少个子节点
     *
     * @param path
     * @return
     */
    public int getChildrenCount(String path) {

        int count = 0;
        try {
            List<String> childrenList = zooKeeper.getChildren(path, true);
            return childrenList.size();
        } catch (Exception e) {
            log.error("获取某个节点下面有多少个子节点发生异常" + e.getMessage());
        }

        return count;

    }

}
