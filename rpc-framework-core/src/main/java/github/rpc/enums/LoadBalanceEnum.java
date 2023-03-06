package github.rpc.enums;

public enum  LoadBalanceEnum {
    CONSISTENTHASH("consistentHash"),RANDOM("random");
    private final String name;

    LoadBalanceEnum(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
