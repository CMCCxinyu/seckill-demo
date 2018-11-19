package info.neilqin.resolver;

import com.alibaba.fastjson.JSONObject;
import info.neilqin.anno.JsonParam;
import info.neilqin.exceptions.ValidatorException;
import info.neilqin.utils.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义参数解析器
 * 对@JsonParam参数进行解析
 * @author Neil
 * @date 2018/11/14 15:41
 */
@Component
public class JsonParamResolver implements HandlerMethodArgumentResolver {


    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JsonParam.class) || parameter.getMethod().getAnnotation(JsonParam.class)!=null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 获取请求体
        String requestBody = ServletUtils.getRequestBody(webRequest);
        JSONObject jsonObject = JSONObject.parseObject(requestBody);
        Object val;
        // 参数名称
        String parameterName = parameter.getParameterName();
        // 参数上注解
        if (parameter.hasParameterAnnotation(JsonParam.class)){
            JsonParam jsonParamAnno = parameter.getParameterAnnotation(JsonParam.class);
            // 获取请求体 value 属性
            if (StringUtils.isNotEmpty(jsonParamAnno.value())){
                val = jsonObject.get(jsonParamAnno.value());
            } else {
                val = jsonObject.get(parameterName);
            }
            if (null == val && StringUtils.isNotEmpty(jsonParamAnno.defaultValue())){
                val =  jsonParamAnno.defaultValue();
            }
            if (jsonParamAnno.required() && null == val && StringUtils.isEmpty(jsonParamAnno.defaultValue())){
                throw ValidatorException.VALIDATOR_ERR.format(parameter+"不能为空");
            }
        // 方法上注解
        } else {
            val = jsonObject.get(parameterName);
        }
        // 强制类型转换
        if(null != binderFactory && null != val){
            WebDataBinder binder = binderFactory.createBinder(webRequest, parameter, parameterName);
            val = binder.convertIfNecessary(val,parameter.getParameterType(),parameter);
        }
        return val;
    }

}
