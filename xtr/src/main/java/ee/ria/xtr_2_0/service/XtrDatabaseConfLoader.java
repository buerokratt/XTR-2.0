package ee.ria.xtr_2_0.service;

import ee.ria.xtr_2_0.model.XtrDatabase;

import java.util.Set;

/**
 * Interface defines a method that returns Set of XtrDatabases.
 * XtrDatabases contain configurations about requests to X-Road services
 * @see XtrDatabase
 */
public interface XtrDatabaseConfLoader {

    /**
     * @return set of configurations that map request parameters to services and their methods
     */
    Set<XtrDatabase> loadConf();

}
