package top.tslj.dsb2s;

public class IServiceImpl implements IService{
    private DemoProperties demoProperties;

    public IServiceImpl(DemoProperties demoProperties) {
        this.demoProperties = demoProperties;
    }

    @Override
    public String showTime() {
        return "{" + demoProperties.getName() + ":" + demoProperties.getValue() + "} - [" + demoProperties.getHost() + ":" + demoProperties.getPort() + "]"; // {name:value} - [host:port]
    }
}
