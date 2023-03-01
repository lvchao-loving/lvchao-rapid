package com.lvchao.rapid.client.test;


import com.lvchao.rapid.common.util.ServiceLoader;
import com.lvchao.rapid.discovery.api.RegistryService;

import java.util.Iterator;

/**
 * <p>
 * spi test
 * </p>
 *
 * @author lvchao
 * @since 2023/2/20 15:45
 */
public class Test_SPI {
    public static void main(String[] args) {
        ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);

        Iterator<RegistryService> iterator = serviceLoader.iterator();
        while (iterator.hasNext()){
            RegistryService next = iterator.next();
            System.out.println(next);
        }
    }
}
