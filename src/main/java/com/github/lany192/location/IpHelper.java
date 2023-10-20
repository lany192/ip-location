package com.github.lany192.location;

import org.lionsoul.ip2region.xdb.Searcher;

public class IpHelper {
    private Searcher searcher;
    private volatile static IpHelper instance;

    public static IpHelper getInstance() {
        if (instance == null) {
            synchronized (IpHelper.class) {
                if (instance == null) {
                    instance = new IpHelper();
                }
            }
        }
        return instance;
    }

    private IpHelper() {
    }

    private Searcher getSearcher() {
        if (searcher == null) {
            try {
                //db
                String dbPath = IpHelper.class.getResource("ip2region.xdb").getPath();
                // 1、从 dbPath 中预先加载 VectorIndex 缓存，并且把这个得到的数据作为全局变量，后续反复使用。
                byte[] vIndex = Searcher.loadVectorIndexFromFile(dbPath);
                // 2、使用全局的 vIndex 创建带 VectorIndex 缓存的查询对象。
                searcher = Searcher.newWithVectorIndex(dbPath, vIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return searcher;
    }

    public IpInfo getIpInfo(String ip) {
        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(ip);
        // 3、查询
        try {
            //国家|区域|省份|城市|ISP_   示例：中国|0|福建省|厦门市
            String region = getSearcher().search(ip);
            if (region != null && !region.isEmpty()) {
                ipInfo.setInfo(region);
                //国家|区域|省份|城市|ISP
                String[] split = region.split("\\|");
                if (split.length > 0) {
                    ipInfo.setCountry(split[0]);
                }
                if (split.length > 1) {
                    ipInfo.setRegion(split[1]);
                }
                if (split.length > 2) {
                    ipInfo.setProvince(split[2]);
                }
                if (split.length > 3) {
                    ipInfo.setCity(split[3]);
                }
                if (split.length > 4) {
                    ipInfo.setIsp(split[4]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 备注：每个线程需要单独创建一个独立的 Searcher 对象，但是都共享全局的制度 vIndex 缓存。
        return ipInfo;
    }
}
