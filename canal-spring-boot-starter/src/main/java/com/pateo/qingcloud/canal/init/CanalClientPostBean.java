package com.pateo.qingcloud.canal.init;

import com.pateo.qingcloud.canal.KafkaCanalClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

/**
 * @author gujiachun
 */
//@Component
public class CanalClientPostBean implements ApplicationRunner, DisposableBean {

    @Autowired
    public List<KafkaCanalClient> kafkaCanalClientList;

    @Override
    public void destroy() throws Exception {
        System.out.println("=========destroy begin========");
        if(null != kafkaCanalClientList && kafkaCanalClientList.size() > 0){
            for (KafkaCanalClient client:kafkaCanalClientList) {
                System.out.println("=========destroy========");
                client.stop();
            }
        }
        System.out.println("=========destroy end========");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(null != kafkaCanalClientList && kafkaCanalClientList.size() > 0){
            for (KafkaCanalClient client:kafkaCanalClientList) {
                client.start();
            }
        }
    }
}
