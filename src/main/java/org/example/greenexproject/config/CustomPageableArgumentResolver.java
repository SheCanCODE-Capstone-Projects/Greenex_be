package org.example.greenexproject.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

public class CustomPageableArgumentResolver extends PageableHandlerMethodArgumentResolver {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
                                    ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest,
                                    WebDataBinderFactory binderFactory) {

        String sortParam = webRequest.getParameter("sort");


        if (sortParam != null && sortParam.startsWith("[")) {
            try {
                List<String> sortFields = objectMapper.readValue(sortParam, new TypeReference<List<String>>() {});

                String pageParam = webRequest.getParameter("page");
                String sizeParam = webRequest.getParameter("size");

                int page = pageParam != null ? Integer.parseInt(pageParam) : 0;
                int size = sizeParam != null ? Integer.parseInt(sizeParam) : 20;

                if (!sortFields.isEmpty()) {
                    List<Sort.Order> orders = new ArrayList<>();
                    for (String field : sortFields) {
                        orders.add(Sort.Order.asc(field));
                    }
                    return PageRequest.of(page, size, Sort.by(orders));
                } else {
                    return PageRequest.of(page, size);
                }
            } catch (Exception e) {

            }
        }


        return super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
    }
}
