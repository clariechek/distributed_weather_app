
public class Crash {
    public static void main() {
        Object[] o = null;

        System.out.println("Crashing server...");
        while (true) {
            o = new Object[] {o};
        }
    }
}
