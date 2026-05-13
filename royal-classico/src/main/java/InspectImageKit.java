import java.lang.reflect.*;

public class InspectImageKit {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: InspectImageKit <fully.qualified.ClassName> [jars...]");
            System.exit(2);
        }
        String className = args[0];
        StringBuilder cp = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) cp.append(System.getProperty("path.separator"));
            cp.append(args[i]);
        }
        if (cp.length() > 0) {
            System.setProperty("java.class.path", cp.toString());
        }
        Class<?> cls = Class.forName(className);
        System.out.println("Class: " + cls.getName());
        System.out.println("Constructors:");
        for (Constructor<?> c : cls.getConstructors()) {
            System.out.println("  " + c);
        }
        System.out.println("Methods:");
        for (Method m : cls.getMethods()) {
            System.out.println("  " + m);
        }
    }
}

