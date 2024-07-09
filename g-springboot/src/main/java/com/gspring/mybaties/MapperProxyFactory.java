package com.gspring.mybaties;

import com.gspring.mybaties.annotation.Param;
import com.gspring.mybaties.annotation.Select;
import com.gspring.mybaties.annotation.Update;
import com.gspring.mybaties.annotation.parameterhandle.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/**
 * @author xiangGang
 * @date 2023-01-19 13:49
 * @Description Mapper代理工厂
 */
public class MapperProxyFactory {

    /**
     * 类型处理器映射
     */
    private static final Map<Class<?>, TypeHandler> TYPE_HANDLER_MAP = new HashMap<>();

    /**
     * 私有构造
     */
    private MapperProxyFactory() {
    }

    static {
        TYPE_HANDLER_MAP.put(Integer.class, new IntegerTypeHandler());
        TYPE_HANDLER_MAP.put(String.class, new StringTypeHandler());
    }

    /**
     * 获取Mapper代理对象
     *
     * @param mapper mapper接口
     * @return {@link Object} 代理对象
     */
    public static Object getMapper(Class<?> mapper) {
        return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{mapper}, MapperProxyFactory::invoke);
    }

    /**
     * 执行方法
     *
     * @param proxy  代理对象
     * @param method 方法
     * @param args   参数
     * @return {@link Object} 结果
     */
    private static Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        Connection connection = JdbcUtils.getConnection();
        try (connection) {

            connection.setAutoCommit(false);
            if (method.isAnnotationPresent(Select.class)) {

                Select selectAnnotation = method.getAnnotation(Select.class);
                //处理并填充sql语句
                PreparedStatement preparedStatement = handlerSetPreparedStatement(method, args, connection, selectAnnotation.value());
                //执行sql语句
                preparedStatement.execute();

                ResultSet resultSet = preparedStatement.getResultSet();
                //获取当前方法的所有setter方法
                Map<String, Method> setterMethodMapping = getCurrentSetterMethod(method);

                //获取查询到的列名
                List<String> columnNameList = getColumNamesByResultSet(resultSet);

                //处理结果集
                List<Object> resultList = handleResultSet(method, resultSet, setterMethodMapping, columnNameList);

                connection.commit();
                if (method.getReturnType().isAssignableFrom(List.class)) {
                    return resultList;
                } else {
                    return resultList.get(0);
                }

            } else {

                Update updateAnnotation = method.getAnnotation(Update.class);
                //处理并填充sql语句
                PreparedStatement preparedStatement = handlerSetPreparedStatement(method, args, connection, updateAnnotation.value());

                //执行sql语句
                preparedStatement.execute();
                Integer updateCount = preparedStatement.getUpdateCount();

                if (!method.getReturnType().isAssignableFrom(Integer.class)) {
                    throw new RuntimeException("return type error");
                }

                connection.commit();
                return updateCount;

            }
        } catch (Exception e) {

            connection.rollback();
            throw new RuntimeException("execute sql error", e);

        }
    }

    /**
     * 处理结果集
     *
     * @param method              方法
     * @param resultSet           结果集
     * @param setterMethodMapping setter方法映射
     * @param columnNameList      列名集合
     * @return {@link List}                 处理后结果集
     * @throws SQLException              sql异常
     * @throws InstantiationException    实例化异常
     * @throws IllegalAccessException    非法访问异常
     * @throws InvocationTargetException 调用目标异常
     * @throws NoSuchMethodException     无此方法异常
     */
    @NotNull
    private static List<Object> handleResultSet(Method method, ResultSet
            resultSet, Map<String, Method> setterMethodMapping, List<String> columnNameList) throws
            SQLException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Type genericReturnType = method.getGenericReturnType();
        Class<?> returnType;
        if (genericReturnType instanceof Class) {
            //不是泛型
            returnType = (Class<?>) genericReturnType;
        } else if (genericReturnType instanceof ParameterizedType) {
            //是泛型
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            returnType = (Class<?>) actualTypeArguments[0];
        } else {
            returnType = method.getReturnType();
        }
        List<Object> resultList = new ArrayList<>(resultSet.getFetchSize());
        while (resultSet.next()) {
            Object instance = returnType.getDeclaredConstructor().newInstance();
            for (String columnName : columnNameList) {
                Method setMethod = setterMethodMapping.get(columnName);
                Class<?> parameterType = setMethod.getParameterTypes()[0];
                TypeHandler typeHandler = TYPE_HANDLER_MAP.get(parameterType);
                setMethod.invoke(instance, typeHandler.getResult(resultSet, columnName));
            }
            resultList.add(instance);
        }
        return resultList;
    }

    /**
     * 获取当前方法的所有setter方法
     *
     * @param method 方法
     * @return {@link Map} setter方法映射
     */
    @NotNull
    private static Map<String, Method> getCurrentSetterMethod(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        Class<?> returnType;
        if (genericReturnType instanceof Class) {
            //不是泛型
            returnType = (Class<?>) genericReturnType;
        } else if (genericReturnType instanceof ParameterizedType) {
            //是泛型
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            returnType = (Class<?>) actualTypeArguments[0];
        } else {
            returnType = method.getReturnType();
        }
        Map<String, Method> setterMethodMapping = new HashMap<>(method.getReturnType().getDeclaredMethods().length);
        for (Method declaredMethod : returnType.getDeclaredMethods()) {
            if (declaredMethod.getName().startsWith("set")) {
                String propertyName = declaredMethod.getName().substring(3);
                propertyName = propertyName.substring(0, 1).toLowerCase(Locale.ROOT) + propertyName.substring(1);
                setterMethodMapping.put(propertyName, declaredMethod);
            }
        }
        return setterMethodMapping;
    }

    /**
     * 获取查询到的列名
     *
     * @param resultSet 结果集
     * @return {@link List} 列名集合
     * @throws SQLException sql异常
     */
    @NotNull
    private static List<String> getColumNamesByResultSet(ResultSet resultSet) throws SQLException {

        ResultSetMetaData metaData = resultSet.getMetaData();
        List<String> columnNameList = new ArrayList<>(metaData.getColumnCount());

        for (int i = 0; i < metaData.getColumnCount(); i++) {
            
            if (metaData.getColumnName(i + 1).contains("_")) {

                String[] split = metaData.getColumnName(i + 1).split("_");
                StringBuilder stringBuilder = new StringBuilder();
                for (int j = 0; j < split.length; j++) {
                    if (j == 0) {
                        stringBuilder.append(split[j]);
                    } else {
                        stringBuilder.append(split[j].substring(0, 1).toUpperCase(Locale.ROOT)).append(split[j].substring(1));
                    }
                }
                columnNameList.add(stringBuilder.toString());

            } else {

                columnNameList.add(metaData.getColumnName(i + 1));

            }
        }

        return columnNameList;
    }

    /**
     * 填充PreparedStatement的参数
     *
     * @param method     方法
     * @param args       参数
     * @param connection 连接
     * @return {@link PreparedStatement} PreparedStatement
     * @throws SQLException sql异常
     */
    private static PreparedStatement handlerSetPreparedStatement(Method method, Object[] args, Connection
            connection, String oldSql) throws SQLException {
        //属性名和属性值关系映射  name : fxg  age : 24
        Map<String, Object> methodParamValueMapping = new HashMap<>(method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (method.getParameters()[i].isAnnotationPresent(Param.class)) {
                Param paramAnnotation = method.getParameters()[i].getAnnotation(Param.class);
                methodParamValueMapping.put(paramAnnotation.value(), args[i]);
            }
            methodParamValueMapping.put(method.getParameters()[i].getName(), args[i]);
        }
        //处理参数
        ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler();
        //sql解析器
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", tokenHandler);
        String newSql = genericTokenParser.parse(oldSql);
        List<ParameterMapping> sqlParameterMappingList = tokenHandler.getSqlParameterMappings();
        PreparedStatement preparedStatement = connection.prepareStatement(newSql);
        //填充参数
        for (int i = 0; i < sqlParameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = sqlParameterMappingList.get(i);
            Class<?> type = methodParamValueMapping.get(parameterMapping.getProperty()).getClass();
            TYPE_HANDLER_MAP.get(type).setParameter(preparedStatement, i + 1, methodParamValueMapping.get(parameterMapping.getProperty()));
        }

        return preparedStatement;
    }
}
