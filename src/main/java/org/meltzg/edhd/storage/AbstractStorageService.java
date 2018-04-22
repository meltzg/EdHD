package org.meltzg.edhd.storage;

import org.meltzg.edhd.db.DBServiceBase;
import org.meltzg.genmapred.conf.GenJobConfiguration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Abstract database service for storing files
 */
public abstract class AbstractStorageService extends DBServiceBase {
    /**
     * Add a MultipartFile to the storage service
     * This method will save the MultipartFile to EdHD's storage directory
     *
     * @param file - file to add
     * @return UUID of the file in the storage service
     * @throws IllegalStateException
     * @throws IOException
     */
    public abstract UUID putFile(MultipartFile file) throws IllegalStateException, IOException;

    /**
     * Add a GenJobConfiguration to the storage service
     * This method will save the GenJobConfiguration to a file in
     * EdHD's storage directory
     *
     * @param file - GenJobConfiguration to add
     * @return UUID of the GenJobConfiguration file in the storage service
     * @throws IllegalStateException
     * @throws IOException
     */
    public abstract UUID putFile(GenJobConfiguration file) throws IOException;

    /**
     * Add a file to the storage service
     * register the file at it's current location with the storage service
     *
     * @param file - file to add
     * @return UUID of the file in the storage service
     */
    public abstract UUID putFile(File file);

    /**
     * Retrieve a file from the storage service
     * @param id - UUID of the file to retrieve
     * @return
     */
    public abstract File getFile(UUID id);

    /**
     * Delete a file from the storage service
     * @param id - UUID of the file to delete
     * @return
     */
    public abstract boolean deleteFile(UUID id) throws ClassNotFoundException, SQLException;

    /**
     * @return the storage service's storage directory
     */
    public abstract String getStorageDir();
}
