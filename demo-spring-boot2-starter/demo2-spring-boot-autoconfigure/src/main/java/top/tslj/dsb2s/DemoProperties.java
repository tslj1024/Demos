package top.tslj.dsb2s;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo")
public class DemoProperties {
    
    /**
     * demo 是否开启
     */
    private boolean enabled;
    
    /**
     * demo name 名字
     */
    private String name;

    /**
     * demo value 值
     */
    private String value;

    /**
     * demo host 地址
     */
    private String host;

    /**
     * demo port 端口
     */
    private int port;
    
    public boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
