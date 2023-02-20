import github.rpc.util.GenerateMessageID;

import java.util.UUID;

public class UUIDTest {
    public static void main(String[] args) {
        System.out.println(GenerateMessageID.getMessageId().length);
    }
}
