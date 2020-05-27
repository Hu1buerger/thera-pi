package specs;

public final class Contracts {
    public static void require(boolean precondition, String message) {
        if (!precondition) {
            throw new ContractException(message);
        }
    }

}
