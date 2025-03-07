package com.gizmo.brennon.core.migration;

public interface Migration
{

    boolean shouldRun() throws Exception;

    void migrate() throws Exception;

}
