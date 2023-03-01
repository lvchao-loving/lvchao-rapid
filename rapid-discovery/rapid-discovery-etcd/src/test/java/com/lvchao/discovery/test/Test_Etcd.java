package com.lvchao.discovery.test;

import com.lvchao.rapid.etcd.api.EtcdClient;
import com.lvchao.rapid.etcd.core.EtcdClientImpl;

/**
 * <p>
 * etcd test
 * </p>
 *
 * @author lvchao
 * @since 2023/2/20 20:09
 */
public class Test_Etcd {
    public static void main(String[] args) throws Exception {

        String registryAddress = "http://192.168.58.100:2379,http://192.168.58.101:2379,http://192.168.58.102:2379";

        EtcdClient etcdClient = new EtcdClientImpl(registryAddress, true);

        etcdClient.putKeyWithExpireTime("/lvchao","1",10);
        System.err.println("etcdClient: " + etcdClient);

        etcdClient.putKeyWithLeaseId("/test/lvchao","2",etcdClient.getHeartBeatLeaseId());
    }
}
