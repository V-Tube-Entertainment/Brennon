package com.gizmo.brennon.core.migration;

public interface MigrationManager
{

    void initialize();

    void migrate() throws Exception;

}
