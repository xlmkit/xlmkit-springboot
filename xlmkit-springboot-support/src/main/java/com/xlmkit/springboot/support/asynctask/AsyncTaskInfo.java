package com.xlmkit.springboot.support.asynctask;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
class AsyncTaskInfo {
    private Object bean;
    private Method method;
}
