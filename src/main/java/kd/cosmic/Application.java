package kd.cosmic;

import kd.cosmic.server.Launcher;

/**
 * 启动本地应用程序(微服务节点)
 */
public class Application {

    public static void main(String[] args) {
        Launcher cosmic = new Launcher();

        cosmic.setClusterNumber("ierp");
        cosmic.setTenantNumber("uptdsprd");
        cosmic.setServerIP("172.18.27.133");

        cosmic.setAppName("cosmic-admin-VI44FHsS");
        cosmic.setWebPath("C:/Users/admin/kingdee/cosmic133-server/webapp");
        cosmic.setConfigUrl("172.18.27.133:2181","zookeeper","Cosmic@9800");

        cosmic.setStartWithQing(false);

        cosmic.start();
    }
}