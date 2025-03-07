package com.gizmo.brennon.core.api.job.management;

import lombok.Value;

import java.lang.reflect.Method;

@Value
public class JobHandlerInfo
{

    Class<?> jobClass;
    Method handler;

}
