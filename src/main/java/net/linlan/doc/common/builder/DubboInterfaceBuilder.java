package net.linlan.doc.common.builder;

import net.linlan.doc.common.util.StringUtil;

import java.util.List;

/**
 * @author sunyu
 */
public class DubboInterfaceBuilder {

    /**
     * Generate dubbo provider service registration list
     *
     * @param classes list of class
     * @return dubbo provider interfaces
     */
    public static String generateDubboProviderInterface(List<Class> classes) {
        StringBuilder builder = new StringBuilder();
        for (Class clazz : classes) {
            builder.append("<dubbo:service interface=\"").append(clazz.getName());
            builder.append("\"").append(" ref=\"").append(StringUtil.firstToLowerCase(clazz.getSimpleName())).append("\"/>\n");
        }
        return builder.toString();
    }

    /**
     * 生成dubbo消费方服务注册列表
     *
     * @param classes list of class
     * @return dubbo consumer references
     */
    public static String generateDubboConsumerInterface(List<Class> classes) {
        StringBuilder builder = new StringBuilder();
        for (Class clazz : classes) {
            builder.append("<dubbo:reference interface=\"").append(clazz.getName());
            builder.append("\"").append(" id=\"").append(StringUtil.firstToLowerCase(clazz.getSimpleName())).append("\"/>\n");
        }
        return builder.toString();
    }
}
