package com.gspring.mvc;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.gspring.AnnotationApplicationContext;
import com.gspring.gannotation.springmvc.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiangGang
 * @date 2022-07-09 1:06
 * @Description 前端控制器
 */
public class MyDispatcherServlet extends HttpServlet {
    /**
     * 日志
     */
    private static final Log LOG = LogFactory.getLog(MyDispatcherServlet.class);
    /**
     * 上下文
     */
    private final AnnotationApplicationContext context;

    /**
     * iOC容器
     */
    private Map<String, Object> iocContainerMap;

    /**
     * 请求路径映射集合
     */
    private final Map<String, HandlerMapping> handlerMappingMap = new HashMap<>();

    public MyDispatcherServlet(AnnotationApplicationContext context) {
        this.context = context;
    }

    /**
     * doGet方法
     *
     * @param request  请求体
     * @param response 响应体
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        this.doPost(request, response);
    }

    /**
     * doPost方法
     *
     * @param request  请求体
     * @param response 响应体
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            //运行执行器方法
            this.executeDispatch(request, response);
        } catch (IOException | InvocationTargetException | IllegalAccessException |
                 ServletException e) {
            e.printStackTrace();
        }
    }

    private void executeDispatch(@NotNull HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException, ServletException {
        //设置统一字符集
        request.setCharacterEncoding("UTF-8");
        //获取解析过的请求路径
        String requestUrl = formatUrl(request.getRequestURI());

        //根据请求路径查找对应的controller处理
        HandlerMapping handlerMapping = handlerMappingMap.get(requestUrl);
        //请求地址没有对应的控制器处理
        if (null == handlerMapping) {
            String str = handlerMappingMap.values().stream().map(HandlerMapping::getRequestUrl).filter(url -> {
                if (url.contains("{") && url.contains("}")) {
                    return requestUrl.startsWith(url.substring(0, url.indexOf("{")));
                }
                return false;
            }).findFirst().orElse(null);
            handlerMapping = handlerMappingMap.get(str);

            if (null == handlerMapping) {
                response.getWriter().write("404 Not  Found");
                return;
            }

        }
        //处理请求参数
        Object[] paramArr = handleParameter(request.getSession(), request, response, handlerMapping);

        //反射调用controller方法  执行方法的对象  方法的参数
        Object obj = handlerMapping.getMethod().invoke(handlerMapping.getTarget(), paramArr);

        //返回值处理
        handleReturnParameter(request, response, obj);
    }

    /**
     * 处理方法执行结果
     *
     * @param request  请求体
     * @param response 响应体
     * @param obj      方法执行结果
     * @throws IOException      io异常
     * @throws ServletException servlet异常
     */
    private void handleReturnParameter(HttpServletRequest request, HttpServletResponse response, Object obj) throws IOException, ServletException {
        if (obj != null) {
            //判断返回值是不是String
            if (String.class.getName().equals(obj.getClass().getTypeName())) {
                String methodReturnStr = (String) obj;
                //是否重定向
                if (methodReturnStr.startsWith("redirect")) {
                    String redirectStr = methodReturnStr.substring("redirect".length() + 1);
                    response.sendRedirect(redirectStr);
                    //是否转发
                } else if (methodReturnStr.startsWith("forward")) {
                    String forwardStr = methodReturnStr.substring("forward".length() + 1);
                    request.getRequestDispatcher(forwardStr).forward(request, response);
                } else {
                    responseBody(response, obj);
                }
            } else if (ArrayList.class.getName().equals(obj.getClass().getTypeName()) ||
                    HashMap.class.getName().equals(obj.getClass().getTypeName())) {
                responseBody(response, obj);
            } else {
                responseBody(response, obj);
            }
        }
    }

    /**
     * 处理请求参数
     *
     * @param request        请求体
     * @param response       响应体
     * @param handlerMapping 请求路径映射
     * @return {@link Object} 方法参数
     */
    private Object[] handleParameter(HttpSession session, HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping) {
        //获取方法中的参数类型
        Class<?>[] paramTypeArr = handlerMapping.getMethod().getParameterTypes();
        Object[] paramArr = new Object[paramTypeArr.length];

        for (int i = 0; i < paramTypeArr.length; i++) {
            Class<?> clazz = paramTypeArr[i];
            //参数只考虑三种类型，其他不考虑
            if (clazz == HttpServletRequest.class) {
                paramArr[i] = request;
            } else if (clazz == HttpServletResponse.class) {
                paramArr[i] = response;
            } else if (clazz == String.class) {
                Map<Integer, String> methodParam = handlerMapping.getMethodParams();
                paramArr[i] = request.getParameter(methodParam.get(i));
            } else if (clazz == Integer.class) {
                Map<Integer, String> methodParams = handlerMapping.getMethodParams();
                paramArr[i] = Integer.valueOf(request.getParameter(methodParams.get(i)));
            } else if (clazz == HttpSession.class) {
                paramArr[i] = session;
            } else {
                LOG.info("暂不支持的参数类型");
            }
        }
        return paramArr;
    }

    /**
     * 根据请求体解析地址
     *
     * @param requestUrl 请求地址
     * @return @{@link String}  解析到的地址
     */
    private String formatUrl(String requestUrl) {
        requestUrl = requestUrl.replaceAll("/+", "/");
        if (requestUrl.lastIndexOf("/") == requestUrl.length() - 1) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }
        return requestUrl;
    }

    /**
     * 初始化方法
     *
     * @param config Servlet的配置对象
     */
    @Override
    public void init(ServletConfig config) {
        iocContainerMap = context.getiocContainerMap();
        doDependencyInjection();
    }

    /**
     * 封装请求
     */
    private void doDependencyInjection() {
        if (iocContainerMap.isEmpty()) {
            return;
        }
        //循环IOC容器中的类
        iocContainerMap.entrySet().forEach((entry -> {
            Class<?> clazz = entry.getValue().getClass();
            //初始化HandlerMapping
            StringBuilder requestUrl = new StringBuilder();
            //获取Controller类上的请求路径
            if (clazz.isAnnotationPresent(Controller.class)) {
                requestUrl.append(clazz.getAnnotation(Controller.class).value());
                //循环类中的方法，获取方法上的路径
                Method[] methods = new Method[0];
                try {
                    methods = clazz.getMethods();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                for (Method method : methods) {
                    handleGetMappingAnnotation(entry, requestUrl, method);
                    handlePostMappingAnnotation(entry, requestUrl, method);
                    requestUrl.delete(clazz.getAnnotation(Controller.class).value().length(), requestUrl.length());
                }
            }
        }));
    }

    private void handlePostMappingAnnotation(Map.Entry<String, Object> entry, StringBuilder requestUrl, Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) {

        }

    }

    private void handleGetMappingAnnotation(Map.Entry<String, Object> entry, StringBuilder requestUrl, Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping wolfGetMapping = method.getDeclaredAnnotation(GetMapping.class);
            //拼成完成的请求路径
            requestUrl.append("/").append(wolfGetMapping.value());
            //不考虑正则匹配路径/xx/* 的情况，只考虑完全匹配的情况
            List<String> paramList = new ArrayList<>();
            if (requestUrl.toString().contains("/{")) {
                String str = requestUrl.toString();
                int lastIndex = str.indexOf("}");
                while (lastIndex != -1) {
                    String substring = str.substring(str.indexOf("{") + 1, lastIndex);
                    paramList.add(substring);
                    str = str.substring(lastIndex + 1);
                    lastIndex = str.indexOf("}");
                }
            }
            if (handlerMappingMap.containsKey(requestUrl.toString())) {
                LOG.info("重复路径");
                return;
            }
            //获取方法中参数的注解
            Annotation[][] annotationArr = method.getParameterAnnotations();
            //存储参数的顺序和参数名
            Map<Integer, String> methodParam = new HashMap<>(annotationArr.length);
            for (int i = 0; i < annotationArr.length; i++) {
                for (Annotation annotation : annotationArr[i]) {
                    if (annotation instanceof RequestParam) {
                        RequestParam wolfRequestParam = (RequestParam) annotation;
                        //存储参数的位置和注解中定义的参数名
                        methodParam.put(i, wolfRequestParam.value());
                        break;
                    } else if (annotation instanceof PathVariable) {
                        PathVariable pathVariable = (PathVariable) annotation;
                        methodParam.put(i, pathVariable.value());
                        break;
                    }
                }
            }
            //主要是防止路径多了/导致路径匹配不上
            requestUrl = new StringBuilder(this.formatUrl(requestUrl.toString()));
            HandlerMapping handlerMapping = new HandlerMapping();
            //请求路径
            handlerMapping.setRequestUrl(requestUrl.toString());
            //请求方法
            handlerMapping.setMethod(method);
            //请求方法所在controller对象
            handlerMapping.setTarget(entry.getValue());
            //请求方法的参数信息
            handlerMapping.setMethodParams(methodParam);
            handlerMapping.setUrlParams(paramList);
            //存入hashmap
            handlerMappingMap.put(requestUrl.toString(), handlerMapping);
        }
    }

    /**
     * 统一响应
     *
     * @param response 响应体
     * @param obj      响应数据
     * @throws IOException IO异常
     */
    private void responseBody(HttpServletResponse response, Object obj) throws IOException {
        //这表示往响应体中写文本内容，用utf-8
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "text/html;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(obj));
    }
}
