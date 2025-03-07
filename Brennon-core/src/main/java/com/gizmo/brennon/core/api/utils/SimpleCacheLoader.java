package com.gizmo.brennon.core.api.utils;

public interface SimpleCacheLoader<K, V>
{

    V load( K k ) throws Exception;

}
